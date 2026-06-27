package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.helper.ClassHelper;

import java.lang.reflect.Method;
import java.util.List;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/13
 *     desc   : 绕过CDN责任链拦截器检测
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class CdnHook {
    public CdnHook(Context context, int versionCode, XposedModule module) {
        if (versionCode < 138)
            return;
        List<Method> methodList = ClassHelper.HttpInterceptor.getMethodList(context);
        if (methodList == null) return;
        for (Method m : methodList)
            _hookMethod2(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    _setRes(param,_arg(param,2));
                }
            });
    }
}
