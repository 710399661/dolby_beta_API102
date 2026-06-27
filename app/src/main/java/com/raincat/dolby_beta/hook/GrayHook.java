package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.util.Log;

import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;



/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : 不变灰
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class GrayHook {
    public GrayHook(Context context, XposedModule module) {
        if (SettingHelper.getInstance().isEnable(SettingHelper.proxy_gray_key))
            _hookMethod(_findClass("com.netease.cloudmusic.meta.MusicInfo", context.getClassLoader()),
                    "hasCopyRight", _returnConstant(true));

        if (SettingHelper.getInstance().isEnable(SettingHelper.proxy_master_key)) {
            Class<?> songPrivilegeClass = _findClassIfExists("com.netease.cloudmusic.meta.virtual.SongPrivilege", context.getClassLoader());
            if (songPrivilegeClass != null) {
                Method method = null;
                try {
                    method = songPrivilegeClass.getMethod("setDownloadMaxbr", int.class);
                } catch (NoSuchMethodException e) {
                    try {
                        method = songPrivilegeClass.getMethod("setFreeLevel", int.class);
                    } catch (NoSuchMethodException ex) {
                        Log.w("error", ex.getMessage());
                    }
                }
                if (method != null)
                    _hookMethod2(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object object = _this(param);
                            Object idObj = _callM(object, "getId");
                            if (!(idObj instanceof Long) || (Long) idObj == 0)
                                return;

                            Field[] fields = object.getClass().getDeclaredFields();
                            int maxbr = 0;
                            for (Field field : fields) {
                                if (field.getType() == int.class && field.getName().equals("maxbr")) {
                                    field.setAccessible(true);
                                    maxbr = (int) field.get(object);
                                    break;
                                }
                            }
                            if (maxbr == 0)
                                maxbr = 999000;

                            try {
                                param.args[0] = maxbr;
                                _callM(object, "setSubPriv", 1);
                                _callM(object, "setSharePriv", 1);
                                _callM(object, "setCommentPriv", 1);
                                _callM(object, "setDownMaxLevel", maxbr);
                                _callM(object, "setPlayMaxLevel", maxbr);
                                if (object.getClass().getDeclaredMethod("setPlayMaxbr", int.class) != null)
                                    _callM(object, "setPlayMaxbr", maxbr);
                            } catch (Exception e) {
                                Log.w("error", e.getMessage());
                            }
                        }
                    });
            }
        }
    }
}