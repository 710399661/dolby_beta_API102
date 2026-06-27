package com.raincat.dolby_beta;

import android.util.Log;
import com.raincat.dolby_beta.helper.ScriptHelper;
import de.robv.android.xposed.XposedBridge;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.*;

public class ModuleEntry extends XposedModule {
    private static final String TAG = "DolbyBeta";
    private static final String PKG_MAIN = "com.netease.cloudmusic";
    private static final String PKG_LITE = "com.netease.cloudmusic.lite";

    private volatile boolean hooksInstalled = false;

    public ModuleEntry() { super(); }

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(Log.INFO, TAG, "Module loaded in: " + param.getProcessName());
        // Set module instance for legacy Xposed API delegation
        XposedBridge.setModule(this);
    }

    @Override
    public void onPackageLoaded(PackageLoadedParam param) {
        String pkg = param.getPackageName();
        if (!PKG_MAIN.equals(pkg) && !PKG_LITE.equals(pkg)) return;
        ScriptHelper.modulePath = getModuleApplicationInfo().sourceDir;
    }

    @Override
    public void onPackageReady(PackageReadyParam param) {
        String pkg = param.getPackageName();
        if (!PKG_MAIN.equals(pkg) && !PKG_LITE.equals(pkg)) return;
        // Prevent duplicate hook installation across repeated callbacks
        if (hooksInstalled) return;
        hooksInstalled = true;
        new HookRegistry(this, param).registerAll();
    }

    public static void logd(String msg) { Log.d(TAG, msg); }
    public static void logw(String msg) { Log.w(TAG, msg); }
    public static void loge(String msg) { Log.e(TAG, msg); }
}
