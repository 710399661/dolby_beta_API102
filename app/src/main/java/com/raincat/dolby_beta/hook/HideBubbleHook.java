package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.view.View;

import com.raincat.dolby_beta.helper.SettingHelper;



/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/05/23
 *     desc   : 隐藏小红点
 *     version: 1.0
 * </pre>
 */
import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class HideBubbleHook {
    public HideBubbleHook(Context context, XposedModule module) {
        if (!SettingHelper.getInstance().isEnable(SettingHelper.beauty_bubble_hide_key))
            return;
        final Class<?> messageBubbleView = _findClassIfExists("com.netease.cloudmusic.ui.MessageBubbleView", context.getClassLoader());
        final Class<?> messageBubbleView_800 = _findClassIfExists("com.netease.cloudmusic.theme.ui.MessageBubbleView", context.getClassLoader());
        _hookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                if ((messageBubbleView != null && _this(param).getClass() == messageBubbleView) || (messageBubbleView_800 != null && _this(param).getClass() == messageBubbleView_800)) {
                    /* _setArg */; /* FIXME: assignment to _arg */
                }
            }
        });
    }
}