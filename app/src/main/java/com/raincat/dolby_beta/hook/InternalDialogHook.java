package com.raincat.dolby_beta.hook;
import android.content.Context;



/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/12/11
 *     desc   : 内测与听歌识曲弹窗
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class InternalDialogHook {
    public InternalDialogHook(Context context, int versionCode, XposedModule module) {
        if (versionCode < 138)
            return;

        Class<?> materialDialogHelperClass = _findClassIfExists("com.netease.cloudmusic.ui.MaterialDiloagCommon.MaterialDialogHelper", context.getClassLoader());
        if (materialDialogHelperClass != null) {
            _hookAllMethods(materialDialogHelperClass, "materialDialog", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (_arg(param,0).getClass().getName().contains("MainActivity")||_arg(param,0).getClass().getName().contains("IdentifyActivity"))
                        _setRes(param,null);
                }
            });
        }
    }
}