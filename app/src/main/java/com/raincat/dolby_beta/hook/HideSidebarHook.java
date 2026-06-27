package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.model.SidebarEnum;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/22
 *     desc   : 侧边栏精简
 *     version: 1.0
 * </pre>
 */
import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class HideSidebarHook {
    private Class<?> classDrawerItemEnum;
    private HashMap<String, Boolean> sidebarSettingMap = new HashMap<>();

    private String classMainDrawerString = "com.netease.cloudmusic.ui.MainDrawer";
    private String classDrawerItemEnumString = "com.netease.cloudmusic.ui.MainDrawer$DrawerItemEnum";
    private String methodRefreshDrawerString = "refreshDrawer";
    private String objectMDrawerContainerString = "mDrawerContainer";

    public HideSidebarHook(Context context, int versionCode, XposedModule module) {
        if (versionCode < 138) {
            classMainDrawerString = "com.netease.cloudmusic.ui.l";
            classDrawerItemEnumString = "com.netease.cloudmusic.ui.l$b";
            methodRefreshDrawerString = "m";
            objectMDrawerContainerString = "i";
        }

        classDrawerItemEnum = _findClassIfExists(classDrawerItemEnumString, context.getClassLoader());
        if (classDrawerItemEnum == null)
            classDrawerItemEnum = _findClassIfExists("com.netease.cloudmusic.music.biz.sidebar.ui.MainDrawer$DrawerItemEnum", context.getClassLoader());
        if (classDrawerItemEnum != null && classDrawerItemEnum.isEnum()) {
            Object[] enumConstants = classDrawerItemEnum.getEnumConstants();
            SidebarEnum.setSidebarEnum(enumConstants);
            sidebarSettingMap = SettingHelper.getInstance().getSidebarSetting(SidebarEnum.getSidebarEnum());
        }

        Class<?> sidebarItemClass = (versionCode >= 7003010) ? ClassHelper.SidebarItem.getClazz(context) : null;
        if (sidebarItemClass != null) {
            _hookAllCtors(sidebarItemClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (param.args.length == 2 && _arg(param,1) instanceof List) {
                        List<Object> objectList = (List<Object>) _arg(param,1);
                        for (Iterator<Object> iterator = objectList.iterator(); iterator.hasNext(); ) {
                            try {
                                Object object = iterator.next();
                                String enumString = _callM(object, "getEnumType").toString();
                                if (!TextUtils.isEmpty(enumString) && !enumString.equals("SETTING")) {
                                    if (enumString.equals("GROUP")) {
                                        int group = (int) _callM(object, "getGroup");
                                        if (group == 1 && sidebarSettingMap.get("GROUP1") != null || sidebarSettingMap.get("GROUP1")) {
                                            iterator.remove();
                                        } else if (group == 2 && sidebarSettingMap.get("GROUP2") != null || sidebarSettingMap.get("GROUP2")) {
                                            iterator.remove();
                                        }
                                    } else if (sidebarSettingMap.get(enumString) != null && sidebarSettingMap.get(enumString))
                                        iterator.remove();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } else {
            // Fall back to old approach when SidebarItem class not found
            Class<?> mainDrawerClass = _findClassIfExists(classMainDrawerString, context.getClassLoader());
            if (mainDrawerClass != null)
                _hookMethod(mainDrawerClass, methodRefreshDrawerString, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                        removeUselessItem(param, versionCode);
                    }
                });
        }
    }

    private void removeUselessItem(XC_MethodHook.MethodHookParam param, int versionCode) {
        LinearLayout drawerContainer;
        LinearLayout dynamicContainer = null;
        drawerContainer = (LinearLayout) _getObjField(_this(param), objectMDrawerContainerString);
        if (versionCode >= 138)
            dynamicContainer = (LinearLayout) _getObjField(_this(param), "mDynamicContainer");
        removeItemInner(drawerContainer);
        removeItemInner(dynamicContainer);

        if (sidebarSettingMap.get("DIV2") != null && sidebarSettingMap.get("DIV2")) {
            View div2 = (View) _getObjField(_this(param), "div2");
            if (div2 != null) div2.setVisibility(View.GONE);
        }
        if (sidebarSettingMap.get("DIV3") != null && sidebarSettingMap.get("DIV3")) {
            View div3 = (View) _getObjField(_this(param), "div3");
            if (div3 != null) div3.setVisibility(View.GONE);
        }
        if (sidebarSettingMap.get("DIV4") != null && sidebarSettingMap.get("DIV4")) {
            View div4 = (View) _getObjField(_this(param), "div4");
            if (div4 != null) div4.setVisibility(View.GONE);
        }

        if (sidebarSettingMap.get("VIP") != null && sidebarSettingMap.get("VIP")) {
            View headerCard = (View) _getObjField(_this(param), "mMainActivityDrawerHeaderCard");
            if (headerCard != null) headerCard.setVisibility(View.GONE);
        }
    }

    private void removeItemInner(LinearLayout container) {
        if (container == null) return;
        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            Object tag = v.getTag();
            if (tag != null && shouldRemove(tag)) {
                v.setVisibility(View.GONE);
            }
        }
    }

    private boolean shouldRemove(Object drawerItemEnum) {
        if (drawerItemEnum.getClass().getName().equals("com.netease.cloudmusic.ui.MainDrawer$DrawerItemEnum")
                || drawerItemEnum.getClass().getName().equals("com.netease.cloudmusic.ui.l$b")) {
            String name = drawerItemEnum.toString();
            return sidebarSettingMap.get(name) != null && sidebarSettingMap.get(name);
        } else
            return false;
    }
}
