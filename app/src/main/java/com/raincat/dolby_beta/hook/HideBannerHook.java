package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.raincat.dolby_beta.helper.SettingHelper;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/23
 *     desc   : 移除Banner
 *     version: 1.0
 * </pre>
 */
import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class HideBannerHook {
    private String mainBannerContainerClassString = "com.netease.cloudmusic.ui.MainBannerContainer";
    public HideBannerHook(Context context, final int versionCode, XposedModule module) {
        if (!SettingHelper.getInstance().isEnable(SettingHelper.beauty_banner_hide_key))
            return;
        if (versionCode < 138)
            mainBannerContainerClassString = "com.netease.cloudmusic.ui.NeteaseMusicViewFlipper";

        if (_findClassIfExists(mainBannerContainerClassString, context.getClassLoader()) != null)
            _hookMethod(mainBannerContainerClassString, context.getClassLoader(), "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    View view = (View) _this(param);
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = 1;//改成0将导致无法下滑刷新
                    view.setLayoutParams(layoutParams);
                    view.setVisibility(View.GONE);
                }
            });

        String playlistBannerContainerClassString = "com.netease.cloudmusic.ui.PlaylistBanner";
        if (_findClassIfExists(playlistBannerContainerClassString, context.getClassLoader()) != null)
            _findAndHookConstructor(playlistBannerContainerClassString, context.getClassLoader(), Context.class, AttributeSet.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    final View view = (View) _this(param);
                    view.post(() -> {
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        layoutParams.height = 0;
                        view.setLayoutParams(layoutParams);
                        view.setVisibility(View.GONE);
                    });
                }
            });
    }
}
