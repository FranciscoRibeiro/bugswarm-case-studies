package org.apache.commons.lang3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.Validate;

public class MethodUtils {

    public MethodUtils() {
        super();
    }

    public static Object invokeMethod(final Object object, final String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invokeMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invokeMethod(object, forceAccess, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeMethod(final Object object, final String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeMethod(object, methodName, args, parameterTypes);
    }

    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeMethod(object, forceAccess, methodName, args, parameterTypes);
    }

    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        args = ArrayUtils.nullToEmpty(args);
        final String messagePrefix;
        Method method = null;
        if (forceAccess) {
            messagePrefix = "No such method: ";
            method = getMatchingMethod(object.getClass(), methodName, parameterTypes);
            if (method != null && !method.isAccessible()) {
                method.setAccessible(true);
            }
        } else {
            messagePrefix = "No such accessible method: ";
            method = getMatchingAccessibleMethod(object.getClass(), methodName, parameterTypes);
        }
        if (method == null) {
            throw new NoSuchMethodException(messagePrefix + methodName + "() on object: " + object.getClass().getName());
        }
        args = toVarArgs(method, args);
        return method.invoke(object, args);
    }

    public static Object invokeMethod(final Object object, final String methodName, final Object[] args, final Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invokeMethod(object, false, methodName, args, parameterTypes);
    }

    public static Object invokeExactMethod(final Object object, final String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invokeExactMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeExactMethod(final Object object, final String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeExactMethod(object, methodName, args, parameterTypes);
    }

    public static Object invokeExactMethod(final Object object, final String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getAccessibleMethod(object.getClass(), methodName, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
        }
        return method.invoke(object, args);
    }

    public static Object invokeExactStaticMethod(final Class<?> cls, final String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getAccessibleMethod(cls, methodName, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + cls.getName());
        }
        return method.invoke(null, args);
    }

    public static Object invokeStaticMethod(final Class<?> cls, final String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeStaticMethod(cls, methodName, args, parameterTypes);
    }

    public static Object invokeStaticMethod(final Class<?> cls, final String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getMatchingAccessibleMethod(cls, methodName, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + cls.getName());
        }
        args = toVarArgs(method, args);
        return method.invoke(null, args);
    }

    private static Object[] toVarArgs(final Method method, Object[] args) {
        if (method.isVarArgs()) {
            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            args = getVarArgs(args, methodParameterTypes);
        }
        return args;
    }

    static Object[] getVarArgs(final Object[] args, final Class<?>[] methodParameterTypes) {
        if (args.length == methodParameterTypes.length && args[args.length - 1].getClass().equals(methodParameterTypes[methodParameterTypes.length - 1])) {
            return args;
        }
        // Construct a new array matching the method's declared parameter types.
final Object[] newArgs = new Object[methodParameterTypes.length];
        // Copy the normal (non-varargs) parameters
System.arraycopy(args, 0, newArgs, 0, methodParameterTypes.length - 1);
        // Construct a new array for the variadic parameters
final Class<?> varArgComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
        final int varArgLength = args.length - methodParameterTypes.length + 1;
        Object varArgsArray = Array.newInstance(ClassUtils.primitiveToWrapper(varArgComponentType), varArgLength);
        // Copy the variadic arguments into the varargs array.
System.arraycopy(args, methodParameterTypes.length - 1, varArgsArray, 0, varArgLength);
        if (varArgComponentType.isPrimitive()) {
            // unbox from wrapper type to primitive type
varArgsArray = ArrayUtils.toPrimitive(varArgsArray);
        }
        // Store the varargs array in the last position of the array to return
newArgs[methodParameterTypes.length - 1] = varArgsArray;
        return newArgs;
    }

    public static Object invokeExactStaticMethod(final Class<?> cls, final String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeExactStaticMethod(cls, methodName, args, parameterTypes);
    }

    public static Method getAccessibleMethod(final Class<?> cls, final String methodName, final Class<?>... parameterTypes) {
        try {
            return getAccessibleMethod(cls.getMethod(methodName, parameterTypes));
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    public static Method getAccessibleMethod(Method method) {
        if (!MemberUtils.isAccessible(method)) {
            return null;
        }
        // If the declaring class is public, we are done
final Class<?> cls = method.getDeclaringClass();
        if (Modifier.isPublic(cls.getModifiers())) {
            return method;
        }
        final String methodName = method.getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        // Check the implemented interfaces and subinterfaces
method = getAccessibleMethodFromInterfaceNest(cls, methodName, parameterTypes);
        if (method == null) {
            method = getAccessibleMethodFromSuperclass(cls, methodName, parameterTypes);
        }
        return method;
    }

    private static Method getAccessibleMethodFromSuperclass(final Class<?> cls, final String methodName, final Class<?>... parameterTypes) {
        Class<?> parentClass = cls.getSuperclass();
        while (parentClass != null) {
            if (Modifier.isPublic(parentClass.getModifiers())) {
                try {
                    return parentClass.getMethod(methodName, parameterTypes);
                } catch (final NoSuchMethodException e) {
                    return null;
                }
            }
            parentClass = parentClass.getSuperclass();
        }
        return null;
    }

    private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls, final String methodName, final Class<?>... parameterTypes) {
        for (; cls != null; cls = cls.getSuperclass()) {
            // Check the implemented interfaces of the parent class
final Class<?>[] interfaces = cls.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (!Modifier.isPublic(anInterface.getModifiers())) {
                    continue;
                }
                try {
                    return anInterface.getDeclaredMethod(methodName, parameterTypes);
                } catch (final NoSuchMethodException e) {
                }
                // Recursively check our parent interfaces
final Method method = getAccessibleMethodFromInterfaceNest(anInterface, methodName, parameterTypes);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Method getMatchingAccessibleMethod(final Class<?> cls, final String methodName, final Class<?>... parameterTypes) {
        try {
            final Method method = cls.getMethod(methodName, parameterTypes);
            MemberUtils.setAccessibleWorkaround(method);
            return method;
        } catch (final NoSuchMethodException e) {
        }
        // search through all methods
Method bestMatch = null;
        final Method[] methods = cls.getMethods();
        for (final Method method : methods) {
            if (method.getName().equals(methodName) && MemberUtils.isMatchingMethod(method, parameterTypes)) {
                // get accessible version of method
final Method accessibleMethod = getAccessibleMethod(method);
                if (accessibleMethod != null && (bestMatch == null || MemberUtils.compareMethodFit(accessibleMethod, bestMatch, parameterTypes) < 0)) {
                    bestMatch = accessibleMethod;
                }
            }
        }
        if (bestMatch != null) {
            MemberUtils.setAccessibleWorkaround(bestMatch);
        }
        if (bestMatch != null && bestMatch.isVarArgs() && bestMatch.getParameterTypes().length > 0 && parameterTypes.length > 0) {
            final Class<?>[] methodParameterTypes = bestMatch.getParameterTypes();
            final Class<?> methodParameterComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
            final String methodParameterComponentTypeName = ClassUtils.primitiveToWrapper(methodParameterComponentType).getName();
            final String parameterTypeName = parameterTypes[parameterTypes.length - 1].getName();
            final String parameterTypeSuperClassName = parameterTypes[parameterTypes.length - 1].getSuperclass().getName();
            if (!methodParameterComponentTypeName.equals(parameterTypeName) && !methodParameterComponentTypeName.equals(parameterTypeSuperClassName)) {
                return null;
            }
        }
        return bestMatch;
    }

    public static Method getMatchingMethod(final Class<?> cls, final String methodName, final Class<?>... parameterTypes) {
        Validate.notNull(cls, "Null class not allowed.");
        Validate.notEmpty(methodName, "Null or blank methodName not allowed.");
        // Address methods in superclasses
Method[] methodArray = cls.getDeclaredMethods();
        final List<Class<?>> superclassList = ClassUtils.getAllSuperclasses(cls);
        for (final Class<?> klass : superclassList) {
            methodArray = ArrayUtils.addAll(methodArray, klass.getDeclaredMethods());
        }
        Method inexactMatch = null;
        for (final Method method : methodArray) {
            if (methodName.equals(method.getName()) && Objects.deepEquals(parameterTypes, method.getParameterTypes())) {
                return method;
            } else if (methodName.equals(method.getName()) && ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)) {
                if (inexactMatch == null) {
                    inexactMatch = method;
                } else if (distance(parameterTypes, method.getParameterTypes()) < distance(parameterTypes, inexactMatch.getParameterTypes())) {
                    inexactMatch = method;
                }
            }
        }
        return inexactMatch;
    }

    private static int distance(final Class<?>[] classArray, final Class<?>[] toClassArray) {
        int answer = 0;
        if (!ClassUtils.isAssignable(classArray, toClassArray, true)) {
            return -1;
        }
        for (int offset = 0; offset < classArray.length; offset++) {
            if (classArray[offset].equals(toClassArray[offset])) {
                continue;
            } else if (ClassUtils.isAssignable(classArray[offset], toClassArray[offset], true) && !ClassUtils.isAssignable(classArray[offset], toClassArray[offset], false)) {
                answer++;
            } else {
                answer = answer + 2;
            }
        }
        return answer;
    }

    public static Set<Method> getOverrideHierarchy(final Method method, final Interfaces interfacesBehavior) {
        Validate.notNull(method);
        final Set<Method> result = new LinkedHashSet<>();
        result.add(method);
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Class<?> declaringClass = method.getDeclaringClass();
        final Iterator<Class<?>> hierarchy = ClassUtils.hierarchy(declaringClass, interfacesBehavior).iterator();
        //skip the declaring class :P
hierarchy.next();
        hierarchyTraversal: while (hierarchy.hasNext()) {
            final Class<?> c = hierarchy.next();
            final Method m = getMatchingAccessibleMethod(c, method.getName(), parameterTypes);
            if (m == null) {
                continue;
            }
            if (Arrays.equals(m.getParameterTypes(), parameterTypes)) {
                // matches without generics
result.add(m);
                continue;
            }
            // necessary to get arguments every time in the case that we are including interfaces
final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(declaringClass, m.getDeclaringClass());
            for (int i = 0; i < parameterTypes.length; i++) {
                final Type childType = TypeUtils.unrollVariables(typeArguments, method.getGenericParameterTypes()[i]);
                final Type parentType = TypeUtils.unrollVariables(typeArguments, m.getGenericParameterTypes()[i]);
                if (!TypeUtils.equals(childType, parentType)) {
                    continue hierarchyTraversal;
                }
            }
            result.add(m);
        }
        return result;
    }

    public static Method[] getMethodsWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls) {
        return getMethodsWithAnnotation(cls, annotationCls, false, false);
    }

    public static List<Method> getMethodsListWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls) {
        return getMethodsListWithAnnotation(cls, annotationCls, false, false);
    }

    public static Method[] getMethodsWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls, boolean searchSupers, boolean ignoreAccess) {
        final List<Method> annotatedMethodsList = getMethodsListWithAnnotation(cls, annotationCls, searchSupers, ignoreAccess);
        return annotatedMethodsList.toArray(new Method[annotatedMethodsList.size()]);
    }

    public static List<Method> getMethodsListWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls, boolean searchSupers, boolean ignoreAccess) {
        Validate.isTrue(cls != null, "The class must not be null");
        Validate.isTrue(annotationCls != null, "The annotation class must not be null");
        List<Class<?>> classes = (searchSupers ? getAllSuperclassesAndInterfaces(cls) : new ArrayList<Class<?>>());
        classes.add(cls);
        final List<Method> annotatedMethods = new ArrayList<>();
        for (Class<?> acls : classes) {
            final Method[] methods = (ignoreAccess ? acls.getDeclaredMethods() : acls.getMethods());
            for (final Method method : methods) {
                if (method.getAnnotation(annotationCls) != null) {
                    annotatedMethods.add(method);
                }
            }
        }
        return annotatedMethods;
    }

    public static <A extends Annotation> A getAnnotation(final Method method, final Class<A> annotationCls, boolean searchSupers, boolean ignoreAccess) {
        Validate.isTrue(method != null, "The method must not be null");
        Validate.isTrue(annotationCls != null, "The annotation class must not be null");
        if (!ignoreAccess && !MemberUtils.isAccessible(method)) {
            return null;
        }
        A annotation = method.getAnnotation(annotationCls);
        if (annotation == null && searchSupers) {
            Class<?> mcls = method.getDeclaringClass();
            List<Class<?>> classes = getAllSuperclassesAndInterfaces(mcls);
            for (Class<?> acls : classes) {
                Method equivalentMethod;
                try {
                    equivalentMethod = (ignoreAccess ? acls.getDeclaredMethod(method.getName(), method.getParameterTypes()) : acls.getMethod(method.getName(), method.getParameterTypes()));
                } catch (NoSuchMethodException e) {
                    continue;
                }
                annotation = equivalentMethod.getAnnotation(annotationCls);
                if (annotation != null) {
                    break;
                }
            }
        }
        return annotation;
    }

    private static List<Class<?>> getAllSuperclassesAndInterfaces(final Class<?> cls) {
        if (cls == null) {
            return null;
        }
        final List<Class<?>> allSuperClassesAndInterfaces = new ArrayList<>();
        List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(cls);
        int superClassIndex = 0;
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(cls);
        int interfaceIndex = 0;
        while (interfaceIndex < allInterfaces.size() || superClassIndex < allSuperclasses.size()) {
            Class<?> acls;
            if (interfaceIndex >= allInterfaces.size()) {
                acls = allSuperclasses.get(superClassIndex++);
            } else if (superClassIndex >= allSuperclasses.size()) {
                acls = allInterfaces.get(interfaceIndex++);
            } else if (interfaceIndex < superClassIndex) {
                acls = allSuperclasses.get(interfaceIndex++);
            } else if (superClassIndex < interfaceIndex) {
                acls = allSuperclasses.get(superClassIndex++);
            } else {
                acls = allInterfaces.get(interfaceIndex++);
            }
            allSuperClassesAndInterfaces.add(acls);
        }
        return allSuperClassesAndInterfaces;
    }
}
