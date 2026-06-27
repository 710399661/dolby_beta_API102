package com.raincat.dolby_beta.hook;


import android.content.Context;

import com.raincat.dolby_beta.helper.ClassHelper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;



/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : 下载强制返回正确MD5
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class DownloadMD5Hook {
    public DownloadMD5Hook(Context context, XposedModule module) {
        Method checkMd5Method = ClassHelper.DownloadTransfer.getCheckMd5Method(context);
        if (checkMd5Method != null) {
            _hookMethod2(checkMd5Method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    if (param.args == null || param.args.length < 4) return;
                    Object[] array = (Object[]) _arg(param, 3);
                    if (array == null || array.length < 6) return;
                    String path = _arg(param, 0).toString();
                    array[5] = fileToMD5(path);
                    param.args[3] = array;
                }
            });
        }

        Method checkDownloadMethod = ClassHelper.DownloadTransfer.getCheckDownloadStatusMethod(context);
        if (checkDownloadMethod != null) {
            _hookMethod2(checkDownloadMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    if (param.args == null || param.args.length == 0) return;
                    Object arg0 = _arg(param, 0);
                    if (arg0 == null) return;
                    Method[] methods = arg0.getClass().getDeclaredMethods();
                    for (Method m : methods) {
                        if (m.getReturnType() == long.class) {
                            long length = (long) _callM(arg0, m.getName());
                            _setRes(param, length);
                            break;
                        }
                    }
                }
            });
        }
    }

    private String fileToMD5(String filePath) {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert the hash bytes to hex digits string
     *
     * @return The converted hex digits string
     */
    private String convertHashToString(byte[] hashBytes) {
        StringBuilder returnVal = new StringBuilder();
        for (byte hashByte : hashBytes) {
            returnVal.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return returnVal.toString().toLowerCase();
    }
}
