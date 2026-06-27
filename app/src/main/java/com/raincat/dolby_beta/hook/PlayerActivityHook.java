package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Field;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/11/16
 *     desc   : 播放页hook
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class PlayerActivityHook {
    public PlayerActivityHook(Context context, final int versionCode, XposedModule module) {
        final boolean black = SettingHelper.getInstance().isEnable(SettingHelper.beauty_black_hide_key);
        final boolean ksong = SettingHelper.getInstance().isEnable(SettingHelper.beauty_ksong_hide_key);
        _hookMethod(_findClass("com.netease.cloudmusic.activity.PlayerActivity", context.getClassLoader()),
                "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        ViewFlipper playerDiscViewFlipper = null;
                        for (Field field : _this(param).getClass().getDeclaredFields()) {
                            if (black && field.getType().getName().contains("PlayerDiscViewFlipper")) {
                                field.setAccessible(true);
                                playerDiscViewFlipper = (ViewFlipper) field.get(_this(param));
                            }
                            if (ksong && field.getType().getName().contains("ImageView")) {
                                field.setAccessible(true);
                                ImageView imageView = (ImageView) field.get(_this(param));
                                if (imageView != null && imageView.getContentDescription() != null) {
                                    if (imageView.getContentDescription().toString().contains("音街")
                                            || imageView.getContentDescription().toString().contains("铃声")) {
                                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                                        layoutParams.width = 0;
                                        layoutParams.height = 0;
                                        imageView.setLayoutParams(layoutParams);
                                        if (imageView.getParent() != null) {
                                            View parent = (View) imageView.getParent();
                                            layoutParams = parent.getLayoutParams();
                                            layoutParams.width = 0;
                                            layoutParams.height = 0;
                                            parent.setLayoutParams(layoutParams);
                                        }
                                    }
                                }
                            }
                        }
                        if (playerDiscViewFlipper == null)
                            return;
                        for (int i = 0; i < playerDiscViewFlipper.getChildCount(); i++) {
                            View coverView = null, imageView = null;
                            RelativeLayout rotationRelativeLayout = (RelativeLayout) playerDiscViewFlipper.getChildAt(i);
                            for (int j = 0; j < rotationRelativeLayout.getChildCount(); j++) {
                                if (rotationRelativeLayout.getChildAt(j).getClass().getName().contains("ImageView")
                                        && rotationRelativeLayout.getChildAt(j).getClass().getName().contains("android"))
                                    coverView = rotationRelativeLayout.getChildAt(j);
                                else {
                                    imageView = rotationRelativeLayout.getChildAt(j);
                                }
                            }
                            if (coverView != null && imageView != null) {
                                final View coverViewF = coverView;
                                final View imageViewF = imageView;
                                coverView.post(() -> {
                                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageViewF.getLayoutParams();
                                    layoutParams.height = coverViewF.getHeight();
                                    layoutParams.width = coverViewF.getWidth();
                                    imageViewF.setLayoutParams(layoutParams);
                                    coverViewF.setVisibility(View.INVISIBLE);
                                });
                            }
                        }
                    }
                });

        if (SettingHelper.getInstance().isEnable(SettingHelper.beauty_rotation_key))
            if (versionCode >= 123) {
                _hookMethod(_findClass("com.netease.cloudmusic.ui.RotationRelativeLayout$AnimationHolder", context.getClassLoader()), "prepareAnimation", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        _setRes(param,null);
                    }
                });
            } else {
                _hookMethod(_findClass("com.netease.cloudmusic.ui.RotationRelativeLayout$a", context.getClassLoader()), "b", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        _setRes(param,null);
                    }
                });
            }
    }
}
