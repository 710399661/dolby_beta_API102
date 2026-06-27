package com.raincat.dolby_beta;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * 临时适配层：将 Legacy Xposed 调用映射到标准反射。
 * 逐步迁移完成后可删除此类。
 */
public class XposedAdapter {

    public static Class<?> _findClass(String name, ClassLoader cl) {
        if (cl != null) try { return cl.loadClass(name); } catch (ClassNotFoundException ignored) {}
        try { return Class.forName(name); } catch (ClassNotFoundException ignored) {}
        return null;
    }

    public static Class<?> _findClassIfExists(String name, ClassLoader cl) {
        if (cl != null) try { return cl.loadClass(name); } catch (ClassNotFoundException ignored) {}
        try { return Class.forName(name); } catch (ClassNotFoundException ignored) {}
        return null;
    }

    public static Method[] _findMethods(Class<?> clazz, Class<?>... types) {
        java.util.ArrayList<Method> result = new java.util.ArrayList<>();
        for (Method m : clazz.getDeclaredMethods()) {
            if (java.util.Arrays.equals(m.getParameterTypes(), types)) result.add(m);
        }
        return result.toArray(new Method[0]);
    }

    public static Field _findField(Class<?> clazz, Class<?> type) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getType() == type) return f;
        }
        return null;
    }

    // ==================== 方法查找辅助（遍历继承链） ====================

    /** Walk class hierarchy to find a declared method matching name+params */
    private static Method findMethodInHierarchy(Class<?> clazz, String name, Class<?>... types) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try { return c.getDeclaredMethod(name, types); }
            catch (NoSuchMethodException ignored) {}
        }
        // Also check interfaces
        for (Class<?> iface : clazz.getInterfaces()) {
            try { return iface.getDeclaredMethod(name, types); }
            catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    // ==================== Hook方法 ====================

    /** findAndHookMethod(clazz, "name", hook) */
    public static void _hookMethod(Class<?> clazz, String methodName, XC_MethodHook hook) {
        if (clazz == null) { Log.w("XP", "_hookMethod: null class for " + methodName); return; }
        try {
            Method m = findMethodInHierarchy(clazz, methodName);
            if (m != null) { _hookMethod2(m, hook); return; }
        } catch (Exception e) { Log.w("XP", "_hookMethod: " + methodName, e); }
    }

    /** findAndHookMethod(clazz, "name", param1.class, hook) */
    public static void _hookMethod(Class<?> clazz, String methodName, Class<?> p1, XC_MethodHook hook) {
        if (clazz == null) { Log.w("XP", "_hookMethod: null class for " + methodName); return; }
        try {
            Method m = findMethodInHierarchy(clazz, methodName, p1);
            if (m != null) { _hookMethod2(m, hook); return; }
        } catch (Exception e) { Log.w("XP", "_hookMethod: " + methodName, e); }
    }

    /** findAndHookMethod(clazz, "name", param1.class, param2.class, hook) */
    public static void _hookMethod(Class<?> clazz, String methodName, Class<?> p1, Class<?> p2, XC_MethodHook hook) {
        if (clazz == null) { Log.w("XP", "_hookMethod: null class for " + methodName); return; }
        try {
            Method m = findMethodInHierarchy(clazz, methodName, p1, p2);
            if (m != null) { _hookMethod2(m, hook); return; }
        } catch (Exception e) { Log.w("XP", "_hookMethod: " + methodName, e); }
    }

    /** findAndHookMethod("classname", classLoader, "methodName", paramTypes..., hook) */
    public static void _hookMethod(String className, ClassLoader cl, String methodName, Object... params) {
        try {
            Class<?> clazz = cl.loadClass(className);
            int typeCount = params.length - 1;         // last is XC_MethodHook
            Class<?>[] types = new Class<?>[typeCount]; // Bug 1&2 fixed: correct array size, 0 is valid
            for (int i = 0; i < typeCount; i++) types[i] = (Class<?>) params[i];
            Method m = findMethodInHierarchy(clazz, methodName, types); // Bug 3 fixed: walk hierarchy
            XC_MethodHook hook = (XC_MethodHook) params[typeCount];
            if (m != null) _hookMethod2(m, hook);
            else Log.w("XP", "_hookMethod(S,C,S,...): method not found: " + methodName);
        } catch (Exception e) { Log.w("XP", "_hookMethod(S): " + methodName, e); }
    }

    public static void _hookMethod2(Method method, XC_MethodHook hook) {
        XposedBridge.hookMethod(method, hook);
    }

    public static void _hookMethod2(java.lang.reflect.Executable exec, XC_MethodHook hook) {
        XposedBridge.hookMethod(exec, hook);
    }

    public static void _hookAllMethods(Class<?> clazz, String name, XC_MethodHook hook) {
        if (clazz == null) { Log.w("XP", "_hookAllMethods: null class"); return; }
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name)) _hookMethod2(m, hook);
        }
    }

    public static void _hookAllCtors(Class<?> clazz, XC_MethodHook hook) {
        if (clazz == null) { Log.w("XP", "_hookAllCtors: null class"); return; }
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            _hookMethod2(ctor, hook);
        }
    }

    /** findAndHookConstructor(className, classLoader, paramTypes..., hook) */
    public static void _findAndHookConstructor(String className, ClassLoader cl, Class<?> p1, Class<?> p2, XC_MethodHook hook) {
        try {
            Class<?> clazz = cl.loadClass(className);
            Constructor<?> ctor = clazz.getDeclaredConstructor(p1, p2);
            _hookMethod2(ctor, hook);
        } catch (Exception e) { Log.w("XP", "_findAndHookConstructor: " + className, e); }
    }

    public static XC_MethodHook _returnConstant(Object value) {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                param.setResult(value);
            }
        };
    }

    private static Method findCallableMethod(Class<?> clazz, String name, int argCount) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterTypes().length == argCount) return m;
            }
        }
        return null;
    }

    private static Field findFieldInHierarchy(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    public static Object _callM(Object obj, String name, Object... more) {
        if (more.length == 0) return _callM(obj, name);
        try {
            Class<?>[] types = new Class<?>[more.length];
            for (int i = 0; i < more.length; i++) types[i] = more[i].getClass();
            Method m = obj.getClass().getDeclaredMethod(name, types);
            m.setAccessible(true); return m.invoke(obj, (Object[]) more);
        } catch (Exception e) { Log.w("XP", "callM: " + name, e); return null; }
    }

    public static Object _callM(Object obj, String name) {
        try {
            Method m = findCallableMethod(obj.getClass(), name, 0);
            if (m != null) { m.setAccessible(true); return m.invoke(obj); }
        } catch (Exception e) { Log.w("XP", "callM: " + name, e); }
        return null;
    }

    public static Object _callM(Object obj, String name, Object arg) {
        try {
            Method m = findCallableMethod(obj.getClass(), name, 1);
            if (m != null) { m.setAccessible(true); return m.invoke(obj, arg); }
        } catch (Exception e) { Log.w("XP", "callM: " + name, e); }
        return null;
    }

    public static Object _callSM(Class<?> clazz, String name) {
        try {
            Method m = findCallableMethod(clazz, name, 0);
            if (m != null) { m.setAccessible(true); return m.invoke(null); }
        } catch (Exception e) { Log.w("XP", "callSM: " + name, e); }
        return null;
    }

    public static Object _callSM(Class<?> clazz, String name, Object... args) {
        try {
            Method m = findCallableMethod(clazz, name, args.length);
            if (m != null) { m.setAccessible(true); return m.invoke(null, args); }
        } catch (Exception e) { Log.w("XP", "callSM: " + name, e); }
        return null;
    }

    public static Object _getObjField(Object obj, String name) {
        try {
            Field f = findFieldInHierarchy(obj.getClass(), name);
            if (f != null) { f.setAccessible(true); return f.get(obj); }
        } catch (Exception e) { Log.w("XP", "getObj: " + name, e); }
        return null;
    }

    public static Object _newInst(Class<?> clazz, Object... args) {
        try {
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                if (c.getParameterTypes().length == args.length) {
                    c.setAccessible(true); return c.newInstance(args);
                }
            }
        } catch (Exception e) { Log.w("XP", "newInst", e); }
        return null;
    }

    // XC_MethodHook.MethodHookParam proxy
    public static Object _this(XC_MethodHook.MethodHookParam param) { return param.thisObject; }
    public static Object _arg(XC_MethodHook.MethodHookParam param, int i) { return param.args[i]; }
    public static void _setRes(XC_MethodHook.MethodHookParam param, Object r) { param.setResult(r); }
    public static Object _getRes(XC_MethodHook.MethodHookParam param) { return param.getResult(); }
}
