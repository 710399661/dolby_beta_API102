package com.raincat.dolby_beta.hook;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.SettingHelper;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2022/06/12
 *     desc   : 广告移除增强
 *     version: 1.0
 * </pre>
 */
import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class AdExtraHook {
    public AdExtraHook(XposedModule module) {
        if (SettingHelper.getInstance().isEnable(SettingHelper.black_key)) {
            List<Method> methods = ClassHelper.Ad.getAdMethod();
            if (methods != null) {
                for (Method method : methods) {
                    _hookMethod2(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            for (int i = 0; i < param.args.length; i++) {
                                if (param.args[i] instanceof JSONObject) {
                                    param.args[i] = new JSONObject();
                                    return;
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}
