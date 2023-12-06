//
// Decompiled by Jadx - 540ms
//
package com.ma.mediascanner.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {
    public static Object callAnyObjectMethod(Class<?> cls, Object obj, String methodName, Class<?>[] parameterTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(obj, args);
    }

    public static Object callObjectMethod(Object obj, String methodName, Class<?> cls, Class<?>[] parameterTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(obj, args);
    }

    public static Object callObjectMethod2(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    public static Object callStaticObjectMethod(Class<?> cls, String methodName, Class<?>[] parameterTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(null, args);
    }

    public static Object callStaticObjectMethod(Class<?> cls, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method declaredMethod = cls.getDeclaredMethod(methodName);
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(null);
    }

    public static Object getObjectField(Object obj, Class<?> cls, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return declaredField.get(obj);
    }

    public static Object getObjectField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = obj.getClass().getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return declaredField.get(obj);
    }

    public static Object getStaticObjectField(Class<?> cls, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return declaredField.get(null);
    }

    public static void setObjectField(Object obj, Class<?> cls, String fieldName, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        declaredField.set(obj, newValue);
    }

    public static void setObjectField(Object obj, String fieldName, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = obj.getClass().getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        declaredField.set(obj, newValue);
    }
}
