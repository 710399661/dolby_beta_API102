package com.raincat.dolby_beta.hook;

import android.content.Context;




import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;

public class ListentogetherHook {
    public ListentogetherHook(Context context, int versionCode, XposedModule module) {
        if (versionCode > 8007090) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.f2", context.getClassLoader()),
                    "v", _returnConstant(true));
        }else if (versionCode > 8007075) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.x", context.getClassLoader()),
                    "v", _returnConstant(true));
        }else if (versionCode > 8007070) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.y", context.getClassLoader()),
                    "u", _returnConstant(true));
        }else if (versionCode > 8007055) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.x", context.getClassLoader()),
                    "u", _returnConstant(true));
        }else if (versionCode > 8007026) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.w", context.getClassLoader()),
                    "o", _returnConstant(true));
        }else if (versionCode > 8007004) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.w", context.getClassLoader()),
                    "n", _returnConstant(true));
        }else if (versionCode > 8006076) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.u", context.getClassLoader()),
                    "m", _returnConstant(true));
        }else if (versionCode > 8006045) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.r", context.getClassLoader()),
                    "l1", _returnConstant(true));
        }else if (versionCode > 8006040) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.p", context.getClassLoader()),
                    "h1", _returnConstant(true));
        }else if (versionCode > 8006019) {
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.x", context.getClassLoader()),
                    "n1", _returnConstant(true));
        }else if (versionCode >= 8006000){
            _hookMethod(_findClass("com.netease.cloudmusic.module.listentogether.x", context.getClassLoader()),
                    "m1", _returnConstant(true));
        }
    }
}
