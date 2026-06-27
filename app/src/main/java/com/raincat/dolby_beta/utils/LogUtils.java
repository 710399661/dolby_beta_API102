package com.raincat.dolby_beta.utils;

import com.raincat.dolby_beta.BuildConfig;

import android.util.Log;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2022/06/25
 *     desc   : 日志
 *     version: 1.0
 * </pre>
 */

public class LogUtils {
    public static void log(String text) {
        if (BuildConfig.DEBUG)
            android.util.Log.w("XP", text);
    }
}
