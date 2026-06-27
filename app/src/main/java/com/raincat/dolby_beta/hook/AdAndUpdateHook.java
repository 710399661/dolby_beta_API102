package com.raincat.dolby_beta.hook;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Field;



/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : 去广告和升级
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class AdAndUpdateHook {
    private static String okHttpClientClassString = "okhttp3.OkHttpClient";
    private static String newCallMethodString = "newCall";
    private static String httpUrlFieldString = "url";
    private static String urlFieldString = "url";

    public AdAndUpdateHook(Context context, final int versionCode, XposedModule module) {
        if (versionCode < 138) {
            okHttpClientClassString = "okhttp3.x";
            newCallMethodString = "a";
            httpUrlFieldString = "a";
            urlFieldString = "j";
        }

        //去广告和升级
        Class<?> okHttpClientClass = _findClassIfExists(okHttpClientClassString, context.getClassLoader());
        if (okHttpClientClass != null)
            _hookAllMethods(okHttpClientClass, newCallMethodString, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    if (param.args != null && param.args.length == 1 && _arg(param,0).getClass().getName().contains("okhttp")) {
                        Object request = _arg(param,0);
                        Field httpUrl = request.getClass().getDeclaredField(httpUrlFieldString);
                        httpUrl.setAccessible(true);
                        Object urlObj = httpUrl.get(request);
                        //加了一个反营销版权保护的URL，暂时作用未知
                        if (urlObj.toString().contains("appcustomconfig/get")
                                //去广告
                                || (SettingHelper.getInstance().isEnable(SettingHelper.black_key) && !urlObj.toString().contains("music.126.net") && (urlObj.toString().contains("resource-exposure/config") || urlObj.toString().contains("api/ad") || urlObj.toString().endsWith(".jpg") || urlObj.toString().endsWith(".mp4")))
                                //去升级
                                || (SettingHelper.getInstance().isEnable(SettingHelper.update_key) && (urlObj.toString().contains("android/version") || urlObj.toString().contains("android/upgrade")))) {
                            Field url = urlObj.getClass().getDeclaredField(urlFieldString);
                            boolean urlAccessible = url.isAccessible();
                            url.setAccessible(true);
                            url.set(urlObj, "https://999.0.0.1/");
                            url.setAccessible(urlAccessible);
                            param.args[0] = request;
                        }
                    }
                }
            });

        if (SettingHelper.getInstance().isEnable(SettingHelper.black_key) && _findClassIfExists("com.netease.cloudmusic.activity.LoadingAdActivity", context.getClassLoader()) != null)
            _hookMethod("com.netease.cloudmusic.activity.LoadingAdActivity", context.getClassLoader(),
                    "onCreate", Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                            ((Activity) _this(param)).finish();
                            _setRes(param,null);
                        }
                    });
    }
}
