package de.robv.android.xposed;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

/**
 * Stub for legacy Xposed API that delegates to Libxposed API at runtime.
 * This provides backward compatibility during migration.
 */
public class XposedBridge {

    private static final String TAG = "XposedBridge";
    private static XposedModule moduleInstance = null;

    /**
     * Set by ModuleEntry during initialization.
     * This allows all legacy hook calls to delegate to Libxposed.
     */
    public static void setModule(XposedModule module) {
        moduleInstance = module;
    }

    /**
     * Hook any method/constructor (supports methods and constructors).
     * Delegates to Libxposed's module.hook() internally.
     */
    public static void hookMethod(Executable executable, XC_MethodHook callback) {
        if (moduleInstance == null) {
            Log.w(TAG, "Module not initialized, cannot hook: " + executable.getName());
            return;
        }

        final XC_MethodHook hook = callback;
        String id = executable.getDeclaringClass().getSimpleName() + "." + executable.getName();

        moduleInstance.hook(executable)
                .setId(id)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
            XC_MethodHook.MethodHookParam param = new XC_MethodHook.MethodHookParam();
            param.thisObject = chain.getThisObject();
            param.args = chain.getArgs().toArray();

            // Call before hook (log but don't crash if hook code is buggy)
            try {
                hook.beforeHookedMethod(param);
            } catch (Throwable t) {
                Log.e(TAG, "beforeHookedMethod error in " + executable.getName(), t);
            }

            // If before hook set result, return it without proceeding
            if (param.hasResult()) {
                return param.getResult();
            }

            // Execute original method, passing (possibly modified) args directly.
            // This avoids depending on chain.getArgs() being mutable.
            Object result = chain.proceed(param.args);

            // Prepare param for after hook
            param.setResult(result);

            // Call after hook (log but don't crash if hook code is buggy)
            try {
                hook.afterHookedMethod(param);
            } catch (Throwable t) {
                Log.e(TAG, "afterHookedMethod error in " + executable.getName(), t);
            }

            return param.hasResult() ? param.getResult() : result;
        });
    }

    /**
     * Hook a specific method (convenience wrapper).
     */
    public static void hookMethod(Method method, XC_MethodHook callback) {
        hookMethod((Executable) method, callback);
    }

    /**
     * Hook a specific constructor (convenience wrapper).
     */
    public static void hookMethod(Constructor<?> constructor, XC_MethodHook callback) {
        hookMethod((Executable) constructor, callback);
    }

    /**
     * Hook all methods with the given name in the class.
     */
    public static void hookAllMethods(Class<?> clazz, String methodName, XC_MethodHook callback) {
        if (clazz == null) return;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                hookMethod(method, callback);
            }
        }
    }

    /**
     * Hook all constructors in the class.
     */
    public static void hookAllConstructors(Class<?> clazz, XC_MethodHook callback) {
        if (clazz == null) return;
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            hookMethod(ctor, callback);
        }
    }

    /**
     * Log a message. Delegates to android.util.Log.
     */
    public static void log(String message) {
        Log.i(TAG, message);
    }
}
