package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;

import com.annimon.stream.Stream;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class MagiskFixHook {
    public MagiskFixHook(Context context, XposedModule module) {
        try {
            Class<?> utilsClass = _findClass("com.netease.cloudmusic.utils.NeteaseMusicUtils", context.getClassLoader());
            if (utilsClass == null) return;
            Method[] methods = _findMethods(utilsClass, List.class, boolean.class);
            Method method = Stream.of(methods).sortBy(Method::getName).findFirst().orElse(null);
            if (method == null) return;
            _hookMethod2(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    List<String> list = new ArrayList<>();
                    list.add(Environment.getExternalStorageDirectory().getAbsolutePath());

                    //外置卡
                    String sdCard = getSecondaryStoragePath(context);
                    if (sdCard != null) {
                        String state = getStorageState(context, sdCard);
                        if (state.contains(Environment.MEDIA_MOUNTED))
                            list.add(sdCard);
                    }
                    _setRes(param,list);
                }
            });
        } catch (Exception e) {
            android.util.Log.w("MagiskFixHook", "Failed to hook NeteaseMusicUtils", e);
        }
    }

    // 获取主存储卡路径（不要根据系统推荐改黄色的东西！）
    public String getPrimaryStoragePath(Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm);
            return paths[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取次存储卡路径,一般就是外置 TF 卡了. 不过也有可能是 USB OTG （Environment.MEDIA_MOUNTED_READ_ONLY），OTG为只读
    public String getSecondaryStoragePath(Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm);
            return (paths == null || paths.length <= 1) ? null : paths[1];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取存储卡的挂载状态. path 参数传入上两个方法得到的路径
    public String getStorageState(Context context, String path) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", String.class);
            return (String) getVolumeStateMethod.invoke(sm, path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}





