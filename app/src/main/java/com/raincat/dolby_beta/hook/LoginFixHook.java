package com.raincat.dolby_beta.hook;

import android.content.Context;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2022/05/03
 *     desc   : 修复登录
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class LoginFixHook {
    public LoginFixHook(Context context, XposedModule module) {
        Class<?> neteaseMusicUtilsClass = _findClassIfExists("com.netease.cloudmusic.utils.NeteaseMusicUtils", context.getClassLoader());
        if (neteaseMusicUtilsClass != null) {
            _hookMethod(neteaseMusicUtilsClass, "serialdata", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (_arg(param,0).equals("/api/login/cellphone")
                            || _arg(param,0).equals("/api/login")
                            || _arg(param,0).equals("/api/login/sns")) {
                        if (((String) _arg(param,1)).contains("\"checkToken\":\"\"")) {
                            Class<?> watchmanClass = _findClassIfExists("com.netease.mobsecurity.rjsb.watchman", context.getClassLoader());
                            if (watchmanClass == null)
                                watchmanClass = _findClassIfExists("com.netease.mobsec.rjsb.watchman", context.getClassLoader());
                            if (watchmanClass != null) {
                                _callSM(watchmanClass, "init", context, "YD00000558929251");
                                String checkToken = (String) _callSM(watchmanClass, "getToken", "30b0cdd23ed1144a0b78de049edc09824", 500, 2);
                                param.args[1] = ((String) _arg(param,1)).replaceAll("\"checkToken\":\"\"", "\"checkToken\":\"" + checkToken + "\"");
                            }
                        }
                    }
                }
            });
        }
    }
}
