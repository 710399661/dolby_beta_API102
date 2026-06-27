package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.google.gson.Gson;
import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.model.UserPrivilegeBean;

import org.json.JSONObject;



/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/26
 *     desc   : 黑胶，100黑胶，220音乐包
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class BlackHook {
    public BlackHook(Context context, int versionCode, XposedModule module) {
        if (versionCode < 138) {
            _hookAllMethods(_findClass("com.netease.cloudmusic.meta.Profile", context.getClassLoader()), "setUserPoint", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object userIdObj = _callM(_this(param), "getUserId");
                    if (userIdObj instanceof Long && ((Long) userIdObj) == Long.parseLong(ExtraHelper.getExtraDate(ExtraHelper.USER_ID))) {
                        _callM(_this(param), "setVipType", 100);
                        _callM(_this(param), "setVipProExpireTime", System.currentTimeMillis() + 31536000000L);
                        _callM(_this(param), "setExpireTime", System.currentTimeMillis() + 31536000000L);
                    }
                }
            });

            //主题
            _hookMethod(_findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "i", _returnConstant(0));
            _hookMethod(_findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "j", _returnConstant("免费"));
            _hookMethod(_findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "o", _returnConstant(false));
            _hookMethod(_findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "s", _returnConstant(false));
        } else {
            _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.UserPrivilege", context.getClassLoader()),
                    "fromJson", JSONObject.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            JSONObject object = (JSONObject) _arg(param,0);
                            if (object.optInt("code") == 200 && !object.isNull("data") && !object.getJSONObject("data").isNull("userId") &&
                                    object.getJSONObject("data").optLong("userId") == Long.parseLong(ExtraHelper.getExtraDate(ExtraHelper.USER_ID))) {
                                Gson gson = new Gson();
                                UserPrivilegeBean userPrivilegeBean = gson.fromJson(object.toString(), UserPrivilegeBean.class);
                                userPrivilegeBean.getData().getAssociator().setExpireTime(System.currentTimeMillis() + 31536000000L);
                                userPrivilegeBean.getData().getAssociator().setVipCode(100);
                                userPrivilegeBean.getData().getMusicPackage().setExpireTime(System.currentTimeMillis() + 31536000000L);
                                userPrivilegeBean.getData().getMusicPackage().setVipCode(220);
                                userPrivilegeBean.getData().setRedVipAnnualCount(1);
                                userPrivilegeBean.getData().setRedVipLevel(9);
                                object = new JSONObject(gson.toJson(userPrivilegeBean));
                                param.args[0] = object;
                            }
                        }
                    });

            //主题
            _hookMethod(_findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "getPoints", _returnConstant(0));
            _hookMethod(_findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "getPrice", _returnConstant("免费"));
            _hookMethod(_findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "isVip", _returnConstant(false));
            _hookMethod(_findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "isDigitalAlbum", _returnConstant(false));
        }

        //音质切换
        _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "isVipFee", _returnConstant(false));
        _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getPlayMaxLevel", _returnConstant(999000));
        _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getDownMaxLevel", _returnConstant(999000));
        _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getFee", _returnConstant(0));
        _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getPayed", _returnConstant(0));
        _hookAllMethods(_findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "isFee", _returnConstant(false));
        _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.SongPrivilege", context.getClassLoader()),
                "canShare", _returnConstant(true));
        _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.SongPrivilege", context.getClassLoader()),
                "getFreeLevel", _returnConstant(999000));
        _hookMethod(_findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getFlag", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        //云盘歌曲&运算0x8不等于0
                        _setRes(param,((int) _getRes(param) & 0x8) == 0 ? 0 : _getRes(param));
                    }
                });
    }
}
