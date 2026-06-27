package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.os.Bundle;

import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.helper.ScriptHelper;
import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;



/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/08
 *     desc   : 代理
 *     version: 1.0
 * </pre>
 */

import io.github.libxposed.api.XposedModule;

import io.github.libxposed.api.XposedInterface;
import static com.raincat.dolby_beta.XposedAdapter.*;
import de.robv.android.xposed.XC_MethodHook;

public class ProxyHook {
    private static SSLSocketFactory socketFactory;
    private static Object objectProxy;
    private static Object objectSSLSocketFactory;

    private String fieldSSLSocketFactory;
    private String fieldHttpUrl = "url";
    private String fieldProxy = "proxy";

    private final List<String> whiteUrlList = Arrays.asList("song/enhance/player/url", "song/enhance/download/url");

    public ProxyHook(Context context, int versionCode, XposedModule module, boolean isPlayProcess) {
        Class<?> realCallClass = _findClassIfExists("okhttp3.internal.connection.RealCall", context.getClassLoader());
        if (realCallClass != null) {
            fieldSSLSocketFactory = "sslSocketFactoryOrNull";
        } else {
            realCallClass = _findClassIfExists("okhttp3.RealCall", context.getClassLoader());
            if (realCallClass != null)
                fieldSSLSocketFactory = "sslSocketFactory";
            else {
                realCallClass = _findClassIfExists("okhttp3.z", context.getClassLoader());
                if (realCallClass != null) {
                    fieldSSLSocketFactory = "o";
                    fieldHttpUrl = "a";
                    fieldProxy = "d";
                }
            }
        }

        if (realCallClass != null) _hookAllCtors(realCallClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (param.args.length == 3) {
                    Object client = _arg(param,0);
                    Object request = _arg(param,1);

                    Field urlField = request.getClass().getDeclaredField(fieldHttpUrl);
                    urlField.setAccessible(true);
                    Object urlObj = urlField.get(request);
                    for (String url : whiteUrlList) {
                        if (urlObj.toString().contains(url)) {
                            setProxy(context, client);
                            break;
                        }
                    }
                }
            }
        });

        Class<?> okHttpClientBuilderClass = _findClassIfExists("okhttp3.OkHttpClient$Builder", context.getClassLoader());
        if (okHttpClientBuilderClass != null) {
            _hookAllMethods(okHttpClientBuilderClass, "addInterceptor", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (_arg(param,0).getClass().getName().contains("com.netease.cloudmusic.network.cronet"))
                        _setRes(param,_this(param));
//                        _hookAllMethods(_arg(param,0).getClass(), "intercept", new XC_MethodHook() {
//                            @Override
//                            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                                super.beforeHookedMethod(param);
//                                Object object = _arg(param,0);
//                                if (object != null && object.getClass().getName().contains("Chain")) {
//                                    Object request = _callM(object, "request");
//                                    if (request.toString().contains("song/enhance/player/url") || request.toString().contains("song/enhance/download/url")) {
//                                        Object response = _callM(object, "proceed", request);
//                                        _setRes(param,response);
//                                    }
//                                }
//                            }
//                        });
                }
            });
        }

        if (!isPlayProcess)
            _hookMethod("com.netease.cloudmusic.activity.LoadingActivity", context.getClassLoader(), "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                    ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "0");
                    if (SettingHelper.getInstance().getSetting(SettingHelper.proxy_master_key)) {
                        ScriptHelper.initScript(context, false);
                        if (SettingHelper.getInstance().getSetting(SettingHelper.proxy_server_key)) {
                            ScriptHelper.startHttpProxyMode(context);
                        } else {
                            ScriptHelper.startScript();
                        }
                    }
                }
            });
    }

    /**
     * 设置代理
     */
    private void setProxy(Context context, Object client) throws Exception {
        //保存正常的代理与SSL
        Field sslSocketFactoryField = client.getClass().getDeclaredField(fieldSSLSocketFactory);
        sslSocketFactoryField.setAccessible(true);
        Field proxyField = client.getClass().getDeclaredField(fieldProxy);
        proxyField.setAccessible(true);
        if (objectProxy == null)
            objectProxy = proxyField.get(client);
        if (objectSSLSocketFactory == null)
            objectSSLSocketFactory = sslSocketFactoryField.get(client);

        if (ExtraHelper.getExtraDate(ExtraHelper.SCRIPT_STATUS).equals("1")) {
            String httpUrlHost = SettingHelper.getInstance().getSetting(SettingHelper.proxy_server_key) ?
                    SettingHelper.getInstance().getHttpProxy() : "127.0.0.1";
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpUrlHost, SettingHelper.getInstance().getProxyPort()));
            proxyField.set(client, proxy);
            if (socketFactory == null)
                socketFactory = ScriptHelper.getSSLSocketFactory(context);
            if (socketFactory != null)
                sslSocketFactoryField.set(client, socketFactory);
        } else {
            proxyField.set(client, objectProxy);
            sslSocketFactoryField.set(client, objectSSLSocketFactory);
        }
    }
}