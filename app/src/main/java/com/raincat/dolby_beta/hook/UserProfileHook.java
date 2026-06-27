package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.os.Bundle;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.helper.UserHelper;



/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/17
 *     desc   : 获取账号信息
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class UserProfileHook {
    public UserProfileHook(Context context, XposedModule module) {
        //获取用户id
        Class<?> userProfileClass = _findClassIfExists("com.netease.cloudmusic.meta.Profile", context.getClassLoader());
        if (userProfileClass != null) {
            _hookMethod(userProfileClass, "setNickname", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String nickName = (String) _arg(param,0);
                    if (nickName.equals("未登录") || nickName.length() == 0)
                        return;
                    if ((boolean) _callM(_this(param), "isMe") && ExtraHelper.getExtraDate(ExtraHelper.USER_ID).equals("-1"))
                        ExtraHelper.setExtraDate(ExtraHelper.USER_ID, _callM(_this(param), "getUserId"));
                }
            });
        }

        Class<?> mainActivityClass = _findClassIfExists("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader());
        if (mainActivityClass != null) {
            _hookMethod(mainActivityClass, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    new Thread(() -> {
                        if (ExtraHelper.getExtraDate(ExtraHelper.COOKIE).equals("-1"))
                            ExtraHelper.setExtraDate(ExtraHelper.COOKIE, ClassHelper.Cookie.getCookie(context));
                        if (ExtraHelper.getExtraDate(ExtraHelper.USER_ID).equals("-1"))
                            UserHelper.getUserInfo();
                    }).start();
                }
            });
        }

        //登录页被创建的时候说明用户数据需要被刷新
        Class<?> loginActivityClass = _findClassIfExists("com.netease.cloudmusic.activity.LoginActivity", context.getClassLoader());
        if (loginActivityClass != null) {
            _hookMethod(loginActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    ExtraHelper.cleanUserData();
                }
            });
        }

        //获取我喜欢的歌单id
        Class<?> playListClass = _findClassIfExists("com.netease.cloudmusic.meta.PlayList", context.getClassLoader());
        if (playListClass != null) {
            _hookMethod(playListClass, "setSpecialType", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if ((int) param.args[0] == 5 && ExtraHelper.getExtraDate(ExtraHelper.LOVE_PLAY_LIST).equals("-1"))
                        ExtraHelper.setExtraDate(ExtraHelper.LOVE_PLAY_LIST, _callM(_this(param), "getId"));
                }
            });
        }
    }
}
