package net.bytebuddy.dynamic.scaffold;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.*;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.StubMethod;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.test.utility.JavaVersionRule;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import net.bytebuddy.utility.JavaConstant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TypeWriterDefaultTest {

    private static final String FOO = "foo", BAR = "bar";

    private static final String LEGACY_INTERFACE = "net.bytebuddy.test.precompiled.LegacyInterface";

    private static final String JAVA_8_INTERFACE = "net.bytebuddy.test.precompiled.SingleDefaultMethodInterface";

    @Rule
    public MethodRule javaVersionRule = new JavaVersionRule();

    @Test(expected = IllegalStateException.class)
    public void testConstructorOnInterfaceAssertion() throws Exception {
        new ByteBuddy()
                .makeInterface()
                .defineConstructor(Visibility.PUBLIC)
                .intercept(SuperMethodCall.INSTANCE)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testConstructorOnAnnotationAssertion() throws Exception {
        new ByteBuddy()
                .makeAnnotation()
                .defineConstructor(Visibility.PUBLIC)
                .intercept(SuperMethodCall.INSTANCE)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAbstractConstructorAssertion() throws Exception {
        new ByteBuddy()
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineConstructor(Visibility.PUBLIC)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testStaticAbstractMethodAssertion() throws Exception {
        new ByteBuddy()
                .subclass(Object.class)
                .defineMethod(FOO, void.class, Ownership.STATIC)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testPrivateAbstractMethodAssertion() throws Exception {
        new ByteBuddy()
                .subclass(Object.class)
                .defineMethod(FOO, void.class, Visibility.PRIVATE)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAbstractMethodOnNonAbstractClassAssertion() throws Exception {
        new ByteBuddy()
                .subclass(Object.class)
                .defineMethod(FOO, String.class)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonPublicFieldOnInterfaceAssertion() throws Exception {
        new ByteBuddy()
                .makeInterface()
                .defineField(FOO, String.class, Ownership.STATIC, FieldManifestation.FINAL)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonPublicFieldOnAnnotationAssertion() throws Exception {
        new ByteBuddy()
                .makeAnnotation()
                .defineField(FOO, String.class, Ownership.STATIC, FieldManifestation.FINAL)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonStaticFieldOnInterfaceAssertion() throws Exception {
        new ByteBuddy()
                .makeInterface()
                .defineField(FOO, String.class, Visibility.PUBLIC, FieldManifestation.FINAL)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonStaticFieldOnAnnotationAssertion() throws Exception {
        new ByteBuddy()
                .makeAnnotation()
                .defineField(FOO, String.class, Visibility.PUBLIC, FieldManifestation.FINAL)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonFinalFieldOnInterfaceAssertion() throws Exception {
        new ByteBuddy()
                .makeInterface()
                .defineField(FOO, String.class, Visibility.PUBLIC, Ownership.STATIC)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonFinalFieldOnAnnotationAssertion() throws Exception {
        new ByteBuddy()
                .makeAnnotation()
                .defineField(FOO, String.class, Visibility.PUBLIC, Ownership.STATIC)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testStaticFieldWithIncompatibleConstantValue() throws Exception {
        new ByteBuddy()
                .subclass(Object.class)
                .defineField(FOO, String.class, Ownership.STATIC)
                .value(0)
                .make();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStaticFieldWithNullConstantValue() throws Exception {
        new ByteBuddy()
                .subclass(Object.class)
                .defineField(FOO, String.class, Ownership.STATIC)
                .value(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testStaticNumericFieldWithIncompatibleConstantValue() throws Exception {
        new ByteBuddy()
                .subclass(Object.class)
                .defineField(FOO, boolean.class, Ownership.STATIC)
                .value(Integer.MAX_VALUE)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testStaticFieldWithNonDefinableConstantValue() throws Exception {
        new ByteBuddy()
                .subclass(Object.class)
                .defineField(FOO, Object.class, Ownership.STATIC)
                .value(FOO)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonPublicMethodOnInterfaceAssertion() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V6)
                .makeInterface()
                .defineMethod(FOO, void.class)
                .withoutCode()
                .make();
    }

    @Test
    public void testNonPublicMethodOnInterfaceAssertionJava8() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V8)
                .makeInterface()
                .defineMethod(FOO, void.class)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonPublicMethodOnAnnotationAssertion() throws Exception {
        new ByteBuddy()
                .makeAnnotation()
                .defineMethod(FOO, void.class)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testStaticMethodOnInterfaceAssertion() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V6)
                .makeInterface()
                .defineMethod(FOO, String.class, Visibility.PUBLIC, Ownership.STATIC)
                .withoutCode()
                .make();
    }

    @Test
    @JavaVersionRule.Enforce(8)
    public void testStaticMethodOnAnnotationAssertionJava8() throws Exception {
        new ByteBuddy()
                .makeInterface()
                .defineMethod(FOO, String.class, Visibility.PUBLIC, Ownership.STATIC)
                .intercept(StubMethod.INSTANCE)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testStaticMethodOnAnnotationAssertion() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V6)
                .makeAnnotation()
                .defineMethod(FOO, String.class, Visibility.PUBLIC, Ownership.STATIC)
                .intercept(StubMethod.INSTANCE)
                .make();
    }

    @Test
    @JavaVersionRule.Enforce(8)
    public void testStaticMethodOnInterfaceAssertionJava8() throws Exception {
        new ByteBuddy()
                .makeAnnotation()
                .defineMethod(FOO, String.class, Visibility.PUBLIC, Ownership.STATIC)
                .intercept(StubMethod.INSTANCE)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAnnotationDefaultValueOnClassAssertion() throws Exception {
        new ByteBuddy()
                .subclass(Object.class)
                .merge(TypeManifestation.ABSTRACT)
                .defineMethod(FOO, String.class)
                .defaultValue(BAR, String.class)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAnnotationDefaultValueOnInterfaceClassAssertion() throws Exception {
        new ByteBuddy()
                .makeInterface()
                .defineMethod(FOO, String.class)
                .defaultValue(BAR, String.class)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAnnotationPropertyWithVoidReturnAssertion() throws Exception {
        new ByteBuddy()
                .makeAnnotation()
                .defineMethod(FOO, void.class, Visibility.PUBLIC)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAnnotationPropertyWithParametersAssertion() throws Exception {
        new ByteBuddy()
                .makeAnnotation()
                .defineMethod(FOO, String.class, Visibility.PUBLIC).withParameters(Void.class)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testPackageDescriptionWithModifiers() throws Exception {
        new ByteBuddy()
                .makePackage(FOO)
                .modifiers(Visibility.PRIVATE)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testPackageDescriptionWithInterfaces() throws Exception {
        new ByteBuddy()
                .makePackage(FOO)
                .implement(Serializable.class)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testPackageDescriptionWithField() throws Exception {
        new ByteBuddy()
                .makePackage(FOO)
                .defineField(FOO, Void.class)
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testPackageDescriptionWithMethod() throws Exception {
        new ByteBuddy()
                .makePackage(FOO)
                .defineMethod(FOO, void.class)
                .withoutCode()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAnnotationPreJava5TypeAssertion() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V4)
                .makeAnnotation()
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAnnotationOnTypePreJava5TypeAssertion() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V4)
                .subclass(Object.class)
                .annotateType(AnnotationDescription.Builder.ofType(Foo.class).build())
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAnnotationOnFieldPreJava5TypeAssertion() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V4)
                .subclass(Object.class)
                .defineField(FOO, Void.class)
                .annotateField(AnnotationDescription.Builder.ofType(Foo.class).build())
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testAnnotationOnMethodPreJava5TypeAssertion() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V4)
                .subclass(Object.class)
                .defineMethod(FOO, void.class)
                .intercept(StubMethod.INSTANCE)
                .annotateMethod(AnnotationDescription.Builder.ofType(Foo.class).build())
                .make();
    }

    @Test
    public void testTypeInitializerOnInterface() throws Exception {
        assertThat(new ByteBuddy()
                .makeInterface()
                .invokable(isTypeInitializer())
                .intercept(StubMethod.INSTANCE)
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded(), notNullValue(Class.class));
    }

    @Test
    public void testTypeInitializerOnAnnotation() throws Exception {
        assertThat(new ByteBuddy()
                .makeAnnotation()
                .invokable(isTypeInitializer())
                .intercept(StubMethod.INSTANCE)
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded(), notNullValue(Class.class));
    }

    @Test
    @JavaVersionRule.Enforce(8)
    public void testTypeInitializerOnRebasedModernInterface() throws Exception {
        assertThat(new ByteBuddy()
                .rebase(Class.forName(JAVA_8_INTERFACE))
                .invokable(isTypeInitializer())
                .intercept(StubMethod.INSTANCE)
                .make(), notNullValue(DynamicType.class));
    }

    @Test
    public void testTypeInitializerOnRebasedLegacyInterface() throws Exception {
        assertThat(new ByteBuddy()
                .rebase(Class.forName(LEGACY_INTERFACE))
                .invokable(isTypeInitializer())
                .intercept(StubMethod.INSTANCE)
                .make(), notNullValue(DynamicType.class));
    }

    @Test
    public void testTypeInitializerOnRebasedInterfaceWithFrameComputation() throws Exception {
        assertThat(new ByteBuddy()
                .makeInterface()
                .visit(new AsmVisitorWrapper.ForDeclaredMethods().writerFlags(ClassWriter.COMPUTE_FRAMES))
                .invokable(isTypeInitializer())
                .intercept(StubMethod.INSTANCE)
                .make(), notNullValue(DynamicType.class));
    }

    @Test
    public void testTypeInitializerOnRebasedInterfaceWithFrameExpansion() throws Exception {
        assertThat(new ByteBuddy()
                .makeInterface()
                .visit(new AsmVisitorWrapper.ForDeclaredMethods().readerFlags(ClassReader.EXPAND_FRAMES))
                .invokable(isTypeInitializer())
                .intercept(StubMethod.INSTANCE)
                .make(), notNullValue(DynamicType.class));
    }

    @Test
    public void testTypeInitializerOnRebasedInterfaceWithInitializer() throws Exception {
        assertThat(new ByteBuddy()
                .makeInterface()
                .initializer(new ByteCodeAppender.Simple())
                .invokable(isTypeInitializer())
                .intercept(StubMethod.INSTANCE)
                .make(), notNullValue(DynamicType.class));
    }

    @Test
    public void testTypeInLegacyConstantPoolRemapped() throws Exception {
        Class<?> dynamicType = new ByteBuddy(ClassFileVersion.JAVA_V4)
                .with(TypeValidation.DISABLED)
                .subclass(Object.class)
                .defineMethod(FOO, Object.class, Visibility.PUBLIC)
                .intercept(FixedValue.value(Object.class))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(dynamicType.getDeclaredMethod(FOO).invoke(dynamicType.getDeclaredConstructor().newInstance()), is((Object) Object.class));
    }

    @Test
    public void testArrayTypeInLegacyConstantPoolRemapped() throws Exception {
        Class<?> dynamicType = new ByteBuddy(ClassFileVersion.JAVA_V4)
                .with(TypeValidation.DISABLED)
                .subclass(Object.class)
                .defineMethod(FOO, Object.class, Visibility.PUBLIC)
                .intercept(FixedValue.value(Object[].class))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(dynamicType.getDeclaredMethod(FOO).invoke(dynamicType.getDeclaredConstructor().newInstance()), is((Object) Object[].class));
    }

    @Test
    public void testPrimitiveTypeInLegacyConstantPoolRemapped() throws Exception {
        Class<?> dynamicType = new ByteBuddy(ClassFileVersion.JAVA_V4)
                .with(TypeValidation.DISABLED)
                .subclass(Object.class)
                .defineMethod(FOO, Object.class, Visibility.PUBLIC)
                .intercept(FixedValue.value(int.class))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(dynamicType.getDeclaredMethod(FOO).invoke(dynamicType.getDeclaredConstructor().newInstance()), is((Object) int.class));
    }

    @Test
    public void testLegacyTypeRedefinitionIsDiscovered() throws Exception {
        Class<?> dynamicType = new ByteBuddy()
                .with(TypeValidation.DISABLED)
                .redefine(Class.forName("net.bytebuddy.test.precompiled.TypeConstantSample"))
                .method(named(BAR))
                .intercept(FixedValue.value(int.class))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(dynamicType.getDeclaredMethod(BAR).invoke(null), is((Object) int.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testMethodTypeInLegacyConstantPool() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V4)
                .subclass(Object.class)
                .defineMethod(FOO, Object.class)
                .intercept(FixedValue.value(JavaConstant.MethodType.of(Object.class, Object.class)))
                .make();
    }

    @Test(expected = IllegalStateException.class)
    public void testMethodHandleInLegacyConstantPool() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V4)
                .subclass(Object.class)
                .defineMethod(FOO, Object.class)
                .intercept(FixedValue.value(JavaConstant.MethodHandle.of(new MethodDescription.ForLoadedMethod(Object.class.getDeclaredMethod("toString")))))
                .make();
    }

    @Test(expected = IllegalStateException.class)
    @JavaVersionRule.Enforce(8)
    public void testDefaultMethodCallFromLegacyType() throws Exception {
        new ByteBuddy(ClassFileVersion.JAVA_V7)
                .subclass(Class.forName("net.bytebuddy.test.precompiled.SingleDefaultMethodInterface"))
                .method(isDefaultMethod())
                .intercept(SuperMethodCall.INSTANCE)
                .make();
    }

    @Test
    public void testBridgeNonLegacyType() throws Exception {
        Class<?> base = new ByteBuddy(ClassFileVersion.JAVA_V5)
                .subclass(Object.class)
                .modifiers(Visibility.PACKAGE_PRIVATE)
                .defineMethod("foo", void.class, Visibility.PUBLIC).intercept(StubMethod.INSTANCE)
                .defineMethod("bar", Object.class).intercept(StubMethod.INSTANCE)
                .defineMethod("bar", String.class).intercept(StubMethod.INSTANCE)
                .make()
                .load(null, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        Class<?> subclass = new ByteBuddy(ClassFileVersion.JAVA_V5)
                .subclass(base)
                .modifiers(Visibility.PUBLIC)
                .method(named("bar")).intercept(StubMethod.INSTANCE)
                .make()
                .load(base.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
        assertThat(subclass.getDeclaredMethods().length, is(3));
        assertThat(subclass.getDeclaredMethod("foo").isBridge(), is(true));
        assertThat(subclass.getDeclaredMethod("bar").isBridge(), is(false));
        assertThat(subclass.getDeclaredMethod("bar").getReturnType(), is((Object) String.class));
    }

    @Test
    public void testNoBridgeLegacyType() throws Exception {
        Class<?> base = new ByteBuddy(ClassFileVersion.JAVA_V4)
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .modifiers(Visibility.PACKAGE_PRIVATE)
                .defineConstructor(Visibility.PUBLIC).intercept(SuperMethodCall.INSTANCE)
                .defineMethod(FOO, void.class, Visibility.PUBLIC).intercept(StubMethod.INSTANCE)
                .defineMethod(BAR, Object.class).intercept(StubMethod.INSTANCE)
                .defineMethod(BAR, String.class).intercept(StubMethod.INSTANCE)
                .make()
                .load(null, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        Class<?> subclass = new ByteBuddy(ClassFileVersion.JAVA_V4)
                .subclass(base)
                .modifiers(Visibility.PUBLIC)
                .method(named(BAR)).intercept(StubMethod.INSTANCE)
                .make()
                .load(base.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
        assertThat(subclass.getDeclaredMethods().length, is(1));
        assertThat(subclass.getDeclaredMethod(BAR).isBridge(), is(false));
        assertThat(subclass.getDeclaredMethod(BAR).getReturnType(), is((Object) String.class));
    }

    @Test
    public void testIncompatibleBridgeMethodIsFiltered() throws Exception {
        Class<?> base = new ByteBuddy()
                .subclass(Object.class)
                .defineMethod(FOO, Object.class, Visibility.PUBLIC).intercept(StubMethod.INSTANCE)
                .defineMethod(FOO, void.class, Visibility.PUBLIC, MethodManifestation.BRIDGE).intercept(StubMethod.INSTANCE)
                .make()
                .load(null, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        Class<?> subclass = new ByteBuddy()
                .subclass(base)
                .method(named(FOO)).intercept(StubMethod.INSTANCE)
                .make()
                .load(base.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
        assertThat(subclass.getDeclaredMethods().length, is(1));
        assertThat(subclass.getDeclaredMethod(FOO).isBridge(), is(false));
        assertThat(subclass.getDeclaredMethod(FOO).getReturnType(), is((Object) Object.class));

    }

    @Test
    public void testClassDump() throws Exception {
        TypeDescription instrumentedType = mock(TypeDescription.class);
        byte[] binaryRepresentation = new byte[]{1, 2, 3};
        File file = File.createTempFile(FOO, BAR);
        assertThat(file.delete(), is(true));
        file = new File(file.getParentFile(), "temp" + System.currentTimeMillis());
        assertThat(file.mkdir(), is(true));
        when(instrumentedType.getName()).thenReturn(FOO + "." + BAR);
        new TypeWriter.Default.ClassDumpAction(file.getAbsolutePath(), instrumentedType, binaryRepresentation).run();
        File[] child = file.listFiles();
        assertThat(child, notNullValue(File[].class));
        assertThat(child.length, is(1));
        assertThat(child[0].length(), is(3L));
        assertThat(child[0].delete(), is(true));
        assertThat(file.delete(), is(true));
    }

    @Test
    public void testMembersWithLatentTypeDescription() throws Exception {
        TypeDescription type = new TypeDescription.Latent("B", 0, TypeDescription.Generic.UNDEFINED);
        assertThat(new ByteBuddy()
                .subclass(Object.class)
                .name(FOO)
                .defineField(FOO, type)
                .defineMethod(FOO, type).withParameters(type)
                .intercept(FixedValue.nullValue())
                .make(), notNullValue(DynamicType.class));
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(TypeWriter.Default.UnresolvedType.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ClassDumpAction.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ForCreation.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ForInlining.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ForInlining.InitializationHandler.Creating.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ValidatingClassVisitor.Constraint.ForAnnotation.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ValidatingClassVisitor.Constraint.ForInterface.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ValidatingClassVisitor.Constraint.ForClass.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ValidatingClassVisitor.Constraint.ForPackageType.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ValidatingClassVisitor.Constraint.ForClassFileVersion.class).apply();
        ObjectPropertyAssertion.of(TypeWriter.Default.ValidatingClassVisitor.Constraint.Compound.class).create(new ObjectPropertyAssertion.Creator<List<?>>() {
            @Override
            public List<?> create() {
                return Collections.singletonList(mock(TypeWriter.Default.ValidatingClassVisitor.Constraint.class));
            }
        }).apply();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Foo {
        /* empty */
    }
}
