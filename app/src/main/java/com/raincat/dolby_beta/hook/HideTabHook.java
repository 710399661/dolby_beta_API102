package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Method;
import java.util.List;

import io.github.libxposed.api.XposedModule;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Trim tabs to only ["我的", "发现"].
 *
 * BottomTabView (com.netease.cloudmusic.m1.z.b):
 *   Static tab IDs: a="main"(发现), b="voice", c="mine"(我的), d="karaoke", e="social"
 *   e()→String[]  — returns all tab IDs (controls tab count)
 *   f(int)→String — returns tab ID at position (used by ViewPager)
 *   g(String)→int — returns position for tab ID (used by TabLayout)
 */
public class HideTabHook {
    private static final String TAG = "HideTabHook";

    public HideTabHook(Context context, int versionCode, XposedModule module) {
        if (!SettingHelper.getInstance().isEnable(SettingHelper.beauty_tab_hide_key) || versionCode < 138)
            return;

        // ---- BottomTabView API (controls tab count and page mapping) ----
        if (versionCode >= 8000010) {
            Class<?> btvClass = ClassHelper.BottomTabView.getClazz(context);
            if (btvClass != null) {
                // e(): returns all tab IDs — only expose "mine" and "main"
                _hookMethod(btvClass, "e", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(new String[] { "mine", "main" });
                    }
                });

                // f(int): returns tab ID for ViewPager position
                _hookMethod(btvClass, "f", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int pos = (int) param.args[0];
                        param.setResult(pos == 0 ? "mine" : "main");
                    }
                });

                // g(String): returns position for tab ID
                _hookMethod(btvClass, "g", String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String id = (String) param.args[0];
                        if ("mine".equals(id) || "我的".equals(id)) param.setResult(0);
                        else if ("main".equals(id) || "发现".equals(id)) param.setResult(1);
                        else param.setResult(-1);
                    }
                });
            }
        }

        // ---- String[] display labels for TabLayout ----
        List<Method> setTabItemMethods = ClassHelper.MainActivitySuperClass.getTabItemStringMethods(context);
        if (setTabItemMethods != null && setTabItemMethods.size() != 0) {
            for (Method method : setTabItemMethods) {
                _hookMethod2(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        try {
                            String[] all = (String[]) param.args[0];
                            if (all == null || all.length < 1) return;
                            boolean hasMine = false, hasMain = false;
                            for (String t : all) {
                                if ("mine".equals(t) || "我的".equals(t)) hasMine = true;
                                if ("main".equals(t) || "发现".equals(t)) hasMain = true;
                            }
                            if (hasMine && hasMain) {
                                param.args[0] = new String[] { "我的", "发现" };
                            }
                        } catch (Exception e) { Log.w(TAG, "labels", e); }
                    }
                });
            }

            // Default to "我的" (position 0 = "mine") on first launch
            Method vpInit = ClassHelper.MainActivitySuperClass.getViewPagerInitMethod(context);
            if (vpInit != null) {
                _hookMethod2(vpInit, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        ((Intent) param.args[0]).putExtra("SELECT_PAGE_INDEX", 0);
                    }
                });
            }
        }
    }
}
