package net.bytebuddy.description.method;

import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.ModifierReviewable;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.annotation.AnnotatedCodeElement;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

/**
 * Description of the parameter of a Java method or constructor.
 */
public interface ParameterDescription extends AnnotatedCodeElement,
        NamedElement.WithRuntimeName,
        ModifierReviewable,
        ByteCodeElement.TypeDependant<ParameterDescription.InDefinedShape, ParameterDescription.Token> {

    /**
     * The prefix for names of an unnamed parameter.
     */
    String NAME_PREFIX = "arg";

    /**
     * Returns the type of this parameter.
     *
     * @return The type of this parameter.
     */
    TypeDescription.Generic getType();

    /**
     * Returns the method that declares this parameter.
     *
     * @return The method that declares this parameter.
     */
    MethodDescription getDeclaringMethod();

    /**
     * Returns this parameter's index.
     *
     * @return The index of this parameter.
     */
    int getIndex();

    /**
     * Checks if this parameter has an explicit name. A parameter without an explicit name is named implicitly by
     * {@code argX} with {@code X} denoting the zero-based index.
     *
     * @return {@code true} if the parameter has an explicit name.
     */
    boolean isNamed();

    /**
     * Checks if this parameter has an explicit modifier. A parameter without a modifier is simply treated as
     * if it had a modifier of zero.
     *
     * @return {@code true} if this parameter defines explicit modifiers.
     */
    boolean hasModifiers();

    /**
     * Returns the offset to the parameter value within the local method variable.
     *
     * @return The offset of this parameter's value.
     */
    int getOffset();

    /**
     * Represents a parameter description in its generic shape, i.e. in the shape it is defined by a generic or raw type.
     */
    interface InGenericShape extends ParameterDescription {

        @Override
        MethodDescription.InGenericShape getDeclaringMethod();
    }

    /**
     * Represents a parameter in its defined shape, i.e. in the form it is defined by a class without its type variables being resolved.
     */
    interface InDefinedShape extends ParameterDescription {

        @Override
        MethodDescription.InDefinedShape getDeclaringMethod();

        /**
         * An abstract base implementation of a parameter description in its defined shape.
         */
        abstract class AbstractBase extends ParameterDescription.AbstractBase implements InDefinedShape {

            @Override
            public InDefinedShape asDefined() {
                return this;
            }
        }
    }

    /**
     * A base implementation of a method parameter description.
     */
    abstract class AbstractBase extends ModifierReviewable.AbstractBase implements ParameterDescription {

        @Override
        public String getName() {
            return NAME_PREFIX.concat(String.valueOf(getIndex()));
        }

        @Override
        public String getInternalName() {
            return getName();
        }

        @Override
        public String getSourceCodeName() {
            return isNamed()
                    ? getName()
                    : EMPTY_NAME;
        }

        @Override
        public int getModifiers() {
            return EMPTY_MASK;
        }

        @Override
        public int getOffset() {
            TypeList parameterType = getDeclaringMethod().getParameters().asTypeList().asErasures();
            int offset = getDeclaringMethod().isStatic()
                    ? StackSize.ZERO.getSize()
                    : StackSize.SINGLE.getSize();
            for (int i = 0; i < getIndex(); i++) {
                offset += parameterType.get(i).getStackSize().getSize();
            }
            return offset;
        }

        @Override
        public Token asToken(ElementMatcher<? super TypeDescription> matcher) {
            return new Token(getType().accept(new TypeDescription.Generic.Visitor.Substitutor.ForDetachment(matcher)),
                    getDeclaredAnnotations(),
                    isNamed()
                            ? getName()
                            : Token.NO_NAME,
                    hasModifiers()
                            ? (Integer) getModifiers()
                            : Token.NO_MODIFIERS);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            } else if (!(other instanceof ParameterDescription)) {
                return false;
            }
            ParameterDescription parameterDescription = (ParameterDescription) other;
            return getDeclaringMethod().equals(parameterDescription.getDeclaringMethod())
                    && getIndex() == parameterDescription.getIndex();
        }

        @Override
        public int hashCode() {
            return getDeclaringMethod().hashCode() ^ getIndex();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(Modifier.toString(getModifiers()));
            if (getModifiers() != EMPTY_MASK) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(isVarArgs()
                    ? getType().asErasure().getName().replaceFirst("\\[\\]$", "...")
                    : getType().asErasure().getName());
            return stringBuilder.append(' ').append(getName()).toString();
        }
    }

    /**
     * Description of a loaded parameter with support for the information exposed by {@code java.lang.reflect.Parameter}.
     *
     * @param <T> The type of the {@code java.lang.reflect.Executable} that this list represents.
     */
    abstract class ForLoadedParameter<T> extends InDefinedShape.AbstractBase {

        /**
         * A dispatcher for reading properties from {@code java.lang.reflect.Executable} instances.
         */
        private static final Dispatcher DISPATCHER;

        /*
         * Creates a dispatcher for a loaded parameter if the type is available for the running JVM.
         */
        static {
            Dispatcher dispatcher;
            try {
                Class<?> executableType = Class.forName("java.lang.reflect.Executable");
                Class<?> parameterType = Class.forName("java.lang.reflect.Parameter");
                dispatcher = new Dispatcher.ForModernVm(executableType.getDeclaredMethod("getParameters"),
                        parameterType.getDeclaredMethod("getName"),
                        parameterType.getDeclaredMethod("isNamePresent"),
                        parameterType.getDeclaredMethod("getModifiers"));
            } catch (RuntimeException exception) {
                throw exception;
            } catch (Exception ignored) {
                dispatcher = Dispatcher.ForLegacyVm.INSTANCE;
            }
            DISPATCHER = dispatcher;
        }

        /**
         * The {@code java.lang.reflect.Executable} for which the parameter types are described.
         */
        protected final T executable;

        /**
         * The parameter's index.
         */
        protected final int index;

        /**
         * Creates a new description for a loaded parameter.
         *
         * @param executable The {@code java.lang.reflect.Executable} for which the parameter types are described.
         * @param index      The parameter's index.
         */
        protected ForLoadedParameter(T executable, int index) {
            this.executable = executable;
            this.index = index;
        }

        @Override
        public String getName() {
            return DISPATCHER.getName(executable, index);
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public boolean isNamed() {
            return DISPATCHER.isNamePresent(executable, index);
        }

        @Override
        public int getModifiers() {
            return DISPATCHER.getModifiers(executable, index);
        }

        @Override
        public boolean hasModifiers() {
            // Rational: If a parameter is not named despite the information being attached,
            // it is synthetic, i.e. it has non-default modifiers.
            return isNamed() || getModifiers() != EMPTY_MASK;
        }

        /**
         * A dispatcher creating parameter descriptions based on the API that is available for the current JVM.
         */
        protected interface Dispatcher {

            /**
             * Returns the given parameter's modifiers.
             *
             * @param executable The executable to introspect.
             * @param index      The parameter's index.
             * @return The parameter's modifiers.
             */
            int getModifiers(Object executable, int index);

            /**
             * Returns {@code true} if the given parameter has an explicit name.
             *
             * @param executable The parameter to introspect.
             * @param index      The parameter's index.
             * @return {@code true} if the given parameter has an explicit name.
             */
            boolean isNamePresent(Object executable, int index);

            /**
             * Returns the given parameter's implicit or explicit name.
             *
             * @param executable The parameter to introspect.
             * @param index      The parameter's index.
             * @return The parameter's name.
             */
            String getName(Object executable, int index);

            /**
             * A dispatcher for VMs that support the {@code java.lang.reflect.Parameter} API for Java 8+.
             */
            class ForModernVm implements Dispatcher {

                /**
                 * A reference to {@code java.lang.reflect.Executable#getParameters}.
                 */
                private final Method getParameters;

                /**
                 * A reference to {@code java.lang.reflect.Parameter#getName}.
                 */
                private final Method getName;

                /**
                 * A reference to {@code java.lang.reflect.Parameter#isNamePresent}.
                 */
                private final Method isNamePresent;

                /**
                 * A reference to {@code java.lang.reflect.Parameter#getModifiers}.
                 */
                private final Method getModifiers;

                /**
                 * Creates a new dispatcher for a modern VM.
                 *
                 * @param getParameters A reference to {@code java.lang.reflect.Executable#getTypeArguments}.
                 * @param getName       A reference to {@code java.lang.reflect.Parameter#getName}.
                 * @param isNamePresent A reference to {@code java.lang.reflect.Parameter#isNamePresent}.
                 * @param getModifiers  A reference to {@code java.lang.reflect.Parameter#getModifiers}.
                 */
                protected ForModernVm(Method getParameters, Method getName, Method isNamePresent, Method getModifiers) {
                    this.getParameters = getParameters;
                    this.getName = getName;
                    this.isNamePresent = isNamePresent;
                    this.getModifiers = getModifiers;
                }

                @Override
                public int getModifiers(Object executable, int index) {
                    try {
                        return (Integer) getModifiers.invoke(getParameter(executable, index));
                    } catch (IllegalAccessException exception) {
                        throw new IllegalStateException("Cannot access java.lang.reflect.Parameter#getModifiers", exception);
                    } catch (InvocationTargetException exception) {
                        throw new IllegalStateException("Error invoking java.lang.reflect.Parameter#getModifiers", exception.getCause());
                    }
                }

                @Override
                public boolean isNamePresent(Object executable, int index) {
                    try {
                        return (Boolean) isNamePresent.invoke(getParameter(executable, index));
                    } catch (IllegalAccessException exception) {
                        throw new IllegalStateException("Cannot access java.lang.reflect.Parameter#isNamePresent", exception);
                    } catch (InvocationTargetException exception) {
                        throw new IllegalStateException("Error invoking java.lang.reflect.Parameter#isNamePresent", exception.getCause());
                    }
                }

                @Override
                public String getName(Object executable, int index) {
                    try {
                        return (String) getName.invoke(getParameter(executable, index));
                    } catch (IllegalAccessException exception) {
                        throw new IllegalStateException("Cannot access java.lang.reflect.Parameter#getName", exception);
                    } catch (InvocationTargetException exception) {
                        throw new IllegalStateException("Error invoking java.lang.reflect.Parameter#getName", exception.getCause());
                    }
                }

                /**
                 * Returns the {@code java.lang.reflect.Parameter} of an executable at a given index.
                 *
                 * @param executable The executable for which a parameter should be read.
                 * @param index      The index of the parameter.
                 * @return The parameter for the given index.
                 */
                private Object getParameter(Object executable, int index) {
                    try {
                        return Array.get(getParameters.invoke(executable), index);
                    } catch (IllegalAccessException exception) {
                        throw new IllegalStateException("Cannot access java.lang.reflect.Executable#getParameters", exception);
                    } catch (InvocationTargetException exception) {
                        throw new IllegalStateException("Error invoking java.lang.reflect.Executable#getParameters", exception.getCause());
                    }
                }

                @Override
                public boolean equals(Object other) {
                    if (this == other) return true;
                    if (other == null || getClass() != other.getClass()) return false;
                    ForModernVm legal = (ForModernVm) other;
                    return getParameters.equals(legal.getParameters)
                            && getName.equals(legal.getName)
                            && isNamePresent.equals(legal.isNamePresent)
                            && getModifiers.equals(legal.getModifiers);
                }

                @Override
                public int hashCode() {
                    int result = getParameters.hashCode();
                    result = 31 * result + getName.hashCode();
                    result = 31 * result + isNamePresent.hashCode();
                    result = 31 * result + getModifiers.hashCode();
                    return result;
                }

                @Override
                public String toString() {
                    return "ParameterDescription.ForLoadedParameter.Dispatcher.ForModernVm{" +
                            "getParameters=" + getParameters +
                            ", getName=" + getName +
                            ", isNamePresent=" + isNamePresent +
                            ", getModifiers=" + getModifiers +
                            '}';
                }
            }

            /**
             * A dispatcher for a legacy VM that does not know the {@code java.lang.reflect.Parameter} type that only throws
             * exceptions on any property extraction.
             */
            enum ForLegacyVm implements Dispatcher {

                /**
                 * The singleton instance.
                 */
                INSTANCE;

                @Override
                public int getModifiers(Object executable, int index) {
                    throw new IllegalStateException("Cannot dispatch method for java.lang.reflect.Parameter");
                }

                @Override
                public boolean isNamePresent(Object executable, int index) {
                    throw new IllegalStateException("Cannot dispatch method for java.lang.reflect.Parameter");
                }

                @Override
                public String getName(Object executable, int index) {
                    throw new IllegalStateException("Cannot dispatch method for java.lang.reflect.Parameter");
                }

                @Override
                public String toString() {
                    return "ParameterDescription.ForLoadedParameter.Dispatcher.ForLegacyVm." + name();
                }
            }
        }

        /**
         * A description of a loaded {@link Method} parameter for a modern VM.
         */
        protected static class OfMethod extends ForLoadedParameter<Method> {

            /**
             * Creates a new description for a loaded method.
             *
             * @param method The method for which a parameter is represented.
             * @param index  The index of the parameter.
             */
            protected OfMethod(Method method, int index) {
                super(method, index);
            }

            @Override
            public MethodDescription.InDefinedShape getDeclaringMethod() {
                return new MethodDescription.ForLoadedMethod(executable);
            }

            @Override
            public TypeDescription.Generic getType() {
                return new TypeDescription.Generic.LazyProjection.OfMethodParameter(executable, index, executable.getParameterTypes());
            }

            @Override
            public AnnotationList getDeclaredAnnotations() {
                return new AnnotationList.ForLoadedAnnotations(executable.getParameterAnnotations()[index]);
            }
        }

        /**
         * A description of a loaded {@link Constructor} parameter for a modern VM.
         */
        protected static class OfConstructor extends ForLoadedParameter<Constructor<?>> {

            /**
             * Creates a new description for a loaded constructor.
             *
             * @param constructor The constructor for which a parameter is represented.
             * @param index       The index of the parameter.
             */
            protected OfConstructor(Constructor<?> constructor, int index) {
                super(constructor, index);
            }

            @Override
            public MethodDescription.InDefinedShape getDeclaringMethod() {
                return new MethodDescription.ForLoadedConstructor(executable);
            }

            @Override
            public TypeDescription.Generic getType() {
                return new TypeDescription.Generic.LazyProjection.OfConstructorParameter(executable, index, executable.getParameterTypes());
            }

            @Override
            public AnnotationList getDeclaredAnnotations() {
                return new AnnotationList.ForLoadedAnnotations(executable.getParameterAnnotations()[index]);
            }
        }

        /**
         * Description of a loaded method's parameter on a virtual machine where {@code java.lang.reflect.Parameter}
         * is not available.
         */
        protected static class OfLegacyVmMethod extends InDefinedShape.AbstractBase {

            /**
             * The method that declares this parameter.
             */
            private final Method method;

            /**
             * The index of this parameter.
             */
            private final int index;

            /**
             * The type erasures of the represented method.
             */
            private final Class<?>[] parameterType;

            /**
             * The annotations of the represented method's parameters.
             */
            private final Annotation[][] parameterAnnotation;

            /**
             * Creates a legacy representation of a method's parameter.
             *
             * @param method              The method that declares this parameter.
             * @param index               The index of this parameter.
             * @param parameterType       The type erasures of the represented method.
             * @param parameterAnnotation The annotations of the represented method's parameters.
             */
            protected OfLegacyVmMethod(Method method, int index, Class<?>[] parameterType, Annotation[][] parameterAnnotation) {
                this.method = method;
                this.index = index;
                this.parameterType = parameterType;
                this.parameterAnnotation = parameterAnnotation;
            }

            @Override
            public TypeDescription.Generic getType() {
                return new TypeDescription.Generic.LazyProjection.OfMethodParameter(method, index, parameterType);
            }

            @Override
            public MethodDescription.InDefinedShape getDeclaringMethod() {
                return new MethodDescription.ForLoadedMethod(method);
            }

            @Override
            public int getIndex() {
                return index;
            }

            @Override
            public boolean isNamed() {
                return false;
            }

            @Override
            public boolean hasModifiers() {
                return false;
            }

            @Override
            public AnnotationList getDeclaredAnnotations() {
                return new AnnotationList.ForLoadedAnnotations(parameterAnnotation[index]);
            }
        }

        /**
         * Description of a loaded constructor's parameter on a virtual machine where {@code java.lang.reflect.Parameter}
         * is not available.
         */
        protected static class OfLegacyVmConstructor extends InDefinedShape.AbstractBase {

            /**
             * The method that declares this parameter.
             */
            private final Constructor<?> constructor;

            /**
             * The index of this parameter.
             */
            private final int index;

            /**
             * The type erasures of the represented method.
             */
            private final Class<?>[] parameterType;

            /**
             * The annotations of this parameter.
             */
            private final Annotation[][] parameterAnnotation;

            /**
             * Creates a legacy representation of a method's parameter.
             *
             * @param constructor         The constructor that declares this parameter.
             * @param index               The index of this parameter.
             * @param parameterType       The type erasures of the represented method.
             * @param parameterAnnotation An array of all parameter annotations of the represented method.
             */
            protected OfLegacyVmConstructor(Constructor<?> constructor, int index, Class<?>[] parameterType, Annotation[][] parameterAnnotation) {
                this.constructor = constructor;
                this.index = index;
                this.parameterType = parameterType;
                this.parameterAnnotation = parameterAnnotation;
            }

            @Override
            public TypeDescription.Generic getType() {
                return new TypeDescription.Generic.LazyProjection.OfConstructorParameter(constructor, index, parameterType);
            }

            @Override
            public MethodDescription.InDefinedShape getDeclaringMethod() {
                return new MethodDescription.ForLoadedConstructor(constructor);
            }

            @Override
            public int getIndex() {
                return index;
            }

            @Override
            public boolean isNamed() {
                return false;
            }

            @Override
            public boolean hasModifiers() {
                return false;
            }

            @Override
            public AnnotationList getDeclaredAnnotations() {
                return new AnnotationList.ForLoadedAnnotations(parameterAnnotation[index]);
            }
        }
    }

    /**
     * A latent description of a parameter that is not attached to a method or constructor.
     */
    class Latent extends InDefinedShape.AbstractBase {

        /**
         * The method that is declaring the parameter.
         */
        private final MethodDescription.InDefinedShape declaringMethod;

        /**
         * The type of the parameter.
         */
        private final TypeDescription.Generic parameterType;

        /**
         * The annotations of the parameter.
         */
        private final List<? extends AnnotationDescription> declaredAnnotations;

        /**
         * The name of the parameter or {@code null} if no name is explicitly defined.
         */
        private final String name;

        /**
         * The modifiers of the parameter or {@code null} if no modifiers are explicitly defined.
         */
        private final Integer modifiers;

        /**
         * The index of the parameter.
         */
        private final int index;

        /**
         * The parameter's offset in the local method variables array.
         */
        private final int offset;

        /**
         * Creates a latent parameter description. All provided types are attached to this instance before they are returned.
         *
         * @param declaringMethod The method that is declaring the parameter.
         * @param token           The token describing the shape of the parameter.
         * @param index           The index of the parameter.
         * @param offset          The parameter's offset in the local method variables array.
         */
        public Latent(MethodDescription.InDefinedShape declaringMethod, Token token, int index, int offset) {
            this(declaringMethod,
                    token.getType(),
                    token.getAnnotations(),
                    token.getName(),
                    token.getModifiers(),
                    index,
                    offset);
        }

        /**
         * Creates a new latent parameter descriptions for a parameter without explicit meta data or annotations.
         *
         * @param declaringMethod The method declaring this parameter.
         * @param parameterType   The type of the parameter.
         * @param index           The index of the parameter.
         * @param offset          The offset of the parameter.
         */
        public Latent(MethodDescription.InDefinedShape declaringMethod,
                      TypeDescription.Generic parameterType,
                      int index,
                      int offset) {
            this(declaringMethod,
                    parameterType,
                    Collections.<AnnotationDescription>emptyList(),
                    Token.NO_NAME,
                    Token.NO_MODIFIERS,
                    index,
                    offset);
        }

        /**
         * Creates a latent parameter description. All provided types are attached to this instance before they are returned.
         *
         * @param declaringMethod     The method that is declaring the parameter.
         * @param parameterType       The parameter's type.
         * @param declaredAnnotations The annotations of the parameter.
         * @param name                The name of the parameter or {@code null} if no name is explicitly defined.
         * @param modifiers           The modifiers of the parameter or {@code null} if no modifiers are explicitly defined.
         * @param index               The index of the parameter.
         * @param offset              The parameter's offset in the local method variables array.
         */
        public Latent(MethodDescription.InDefinedShape declaringMethod,
                      TypeDescription.Generic parameterType,
                      List<? extends AnnotationDescription> declaredAnnotations,
                      String name,
                      Integer modifiers,
                      int index,
                      int offset) {
            this.declaringMethod = declaringMethod;
            this.parameterType = parameterType;
            this.declaredAnnotations = declaredAnnotations;
            this.name = name;
            this.modifiers = modifiers;
            this.index = index;
            this.offset = offset;
        }

        @Override
        public TypeDescription.Generic getType() {
            return parameterType.accept(TypeDescription.Generic.Visitor.Substitutor.ForAttachment.of(this));
        }

        @Override
        public MethodDescription.InDefinedShape getDeclaringMethod() {
            return declaringMethod;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public boolean isNamed() {
            return name != null;
        }

        @Override
        public boolean hasModifiers() {
            return modifiers != null;
        }

        @Override
        public String getName() {
            return isNamed()
                    ? name
                    : super.getName();
        }

        @Override
        public int getModifiers() {
            return hasModifiers()
                    ? modifiers
                    : super.getModifiers();
        }

        @Override
        public AnnotationList getDeclaredAnnotations() {
            return new AnnotationList.Explicit(declaredAnnotations);
        }
    }

    /**
     * <p>
     * A parameter description that represents a given parameter but with a substituted parameter type.
     * </p>
     * <p>
     * <b>Note</b>: The supplied visitor must assure to not substitute
     * </p>
     */
    class TypeSubstituting extends AbstractBase implements InGenericShape {

        /**
         * The method that declares this type-substituted parameter.
         */
        private final MethodDescription.InGenericShape declaringMethod;

        /**
         * The represented parameter.
         */
        private final ParameterDescription parameterDescription;

        /**
         * A visitor that is applied to the parameter type.
         */
        private final TypeDescription.Generic.Visitor<? extends TypeDescription.Generic> visitor;

        /**
         * Creates a new type substituting parameter.
         *
         * @param declaringMethod      The method that declares this type-substituted parameter.
         * @param parameterDescription The represented parameter.
         * @param visitor              A visitor that is applied to the parameter type.
         */
        public TypeSubstituting(MethodDescription.InGenericShape declaringMethod,
                                ParameterDescription parameterDescription,
                                TypeDescription.Generic.Visitor<? extends TypeDescription.Generic> visitor) {
            this.declaringMethod = declaringMethod;
            this.parameterDescription = parameterDescription;
            this.visitor = visitor;
        }

        @Override
        public TypeDescription.Generic getType() {
            return parameterDescription.getType().accept(visitor);
        }

        @Override
        public MethodDescription.InGenericShape getDeclaringMethod() {
            return declaringMethod;
        }

        @Override
        public int getIndex() {
            return parameterDescription.getIndex();
        }

        @Override
        public boolean isNamed() {
            return parameterDescription.isNamed();
        }

        @Override
        public boolean hasModifiers() {
            return parameterDescription.hasModifiers();
        }

        @Override
        public int getOffset() {
            return parameterDescription.getOffset();
        }

        @Override
        public String getName() {
            return parameterDescription.getName();
        }

        @Override
        public int getModifiers() {
            return parameterDescription.getModifiers();
        }

        @Override
        public AnnotationList getDeclaredAnnotations() {
            return parameterDescription.getDeclaredAnnotations();
        }

        @Override
        public InDefinedShape asDefined() {
            return parameterDescription.asDefined();
        }
    }

    /**
     * A token representing a parameter's properties detached from a type.
     */
    class Token implements ByteCodeElement.Token<Token> {

        /**
         * Indicator for a method parameter without an explicit name.
         */
        public static final String NO_NAME = null;

        /**
         * Indicator for a method parameter without explicit modifiers.
         */
        public static final Integer NO_MODIFIERS = null;

        /**
         * The type of the represented parameter.
         */
        private final TypeDescription.Generic type;

        /**
         * A list of parameter annotations.
         */
        private final List<? extends AnnotationDescription> annotations;

        /**
         * The name of the parameter or {@code null} if no explicit name is defined.
         */
        private final String name;

        /**
         * The modifiers of the parameter or {@code null} if no explicit modifiers is defined.
         */
        private final Integer modifiers;

        /**
         * Creates a new parameter token without an explicit name, an explicit modifier or annotations.
         *
         * @param type The type of the represented parameter.
         */
        public Token(TypeDescription.Generic type) {
            this(type, Collections.<AnnotationDescription>emptyList());
        }

        /**
         * Creates a new parameter token without an explicit name or an explicit modifier.
         *
         * @param type        The type of the represented parameter.
         * @param annotations The annotations of the parameter.
         */
        public Token(TypeDescription.Generic type, List<? extends AnnotationDescription> annotations) {
            this(type, annotations, NO_NAME, NO_MODIFIERS);
        }

        /**
         * Creates a parameter token without annotations.
         *
         * @param type The type of the represented parameter.
         * @param name            The name of the parameter or {@code null} if no explicit name is defined.
         * @param modifiers       The modifiers of the parameter or {@code null} if no explicit modifiers is defined.
         */
        public Token(TypeDescription.Generic type, String name, Integer modifiers) {
            this(type, Collections.<AnnotationDescription>emptyList(), name, modifiers);
        }

        /**
         * Creates a new parameter token.
         *
         * @param type        The type of the represented parameter.
         * @param annotations The annotations of the parameter.
         * @param name                   The name of the parameter or {@code null} if no explicit name is defined.
         * @param modifiers              The modifiers of the parameter or {@code null} if no explicit modifiers is defined.
         */
        public Token(TypeDescription.Generic type,
                     List<? extends AnnotationDescription> annotations,
                     String name,
                     Integer modifiers) {
            this.type = type;
            this.annotations = annotations;
            this.name = name;
            this.modifiers = modifiers;
        }

        /**
         * Returns the type of the represented method parameter.
         *
         * @return The type of the represented method parameter.
         */
        public TypeDescription.Generic getType() {
            return type;
        }

        /**
         * Returns the annotations of the represented method parameter.
         *
         * @return The annotations of the represented method parameter.
         */
        public AnnotationList getAnnotations() {
            return new AnnotationList.Explicit(annotations);
        }

        /**
         * Returns the name of the represented method parameter.
         *
         * @return The name of the parameter or {@code null} if no explicit name is defined.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the modifiers of the represented method parameter.
         *
         * @return The modifiers of the parameter or {@code null} if no explicit modifiers is defined.
         */
        public Integer getModifiers() {
            return modifiers;
        }

        @Override
        public Token accept(TypeDescription.Generic.Visitor<? extends TypeDescription.Generic> visitor) {
            return new Token(type.accept(visitor),
                    annotations,
                    name,
                    modifiers);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            Token token = (Token) other;
            return type.equals(token.type)
                    && annotations.equals(token.annotations)
                    && (name != null ? name.equals(token.name) : token.name == null)
                    && (modifiers != null ? modifiers.equals(token.modifiers) : token.modifiers == null);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + annotations.hashCode();
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (modifiers != null ? modifiers.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ParameterDescription.Token{" +
                    "type=" + type +
                    ", annotations=" + annotations +
                    ", name=" + (name == null ? null : "'" + name + '\'') +
                    ", modifiers=" + modifiers +
                    '}';
        }

        /**
         * A list of types represented as a list of parameter tokens.
         */
        public static class TypeList extends AbstractList<Token> {

            /**
             * The list of types to represent as parameter tokens.
             */
            private final List<? extends TypeDefinition> typeDescriptions;

            /**
             * Creates a new list of types that represent parameters.
             *
             * @param typeDescriptions The types to represent.
             */
            public TypeList(List<? extends TypeDefinition> typeDescriptions) {
                this.typeDescriptions = typeDescriptions;
            }

            @Override
            public Token get(int index) {
                return new Token(typeDescriptions.get(index).asGenericType());
            }

            @Override
            public int size() {
                return typeDescriptions.size();
            }
        }
    }
}
