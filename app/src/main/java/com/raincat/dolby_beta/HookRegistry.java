package com.raincat.dolby_beta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.helper.FileHelper;
import com.raincat.dolby_beta.helper.NotificationHelper;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.hook.AdAndUpdateHook;
import com.raincat.dolby_beta.hook.AdExtraHook;
import com.raincat.dolby_beta.hook.AutoSignInHook;
import com.raincat.dolby_beta.hook.BlackHook;
import com.raincat.dolby_beta.hook.CdnHook;
import com.raincat.dolby_beta.hook.CommentHotClickHook;
import com.raincat.dolby_beta.hook.DownloadMD5Hook;
import com.raincat.dolby_beta.hook.EAPIHook;
import com.raincat.dolby_beta.hook.GrayHook;
import com.raincat.dolby_beta.hook.HideBannerHook;
import com.raincat.dolby_beta.hook.HideBubbleHook;
import com.raincat.dolby_beta.hook.HideSidebarHook;
import com.raincat.dolby_beta.hook.HideTabHook;
import com.raincat.dolby_beta.hook.InternalDialogHook;
import com.raincat.dolby_beta.hook.ListentogetherHook;
import com.raincat.dolby_beta.hook.LoginFixHook;
import com.raincat.dolby_beta.hook.MagiskFixHook;
import com.raincat.dolby_beta.hook.PlayerActivityHook;
import com.raincat.dolby_beta.hook.ProxyHook;
import com.raincat.dolby_beta.hook.SettingHook;
import com.raincat.dolby_beta.hook.UserProfileHook;
import com.raincat.dolby_beta.utils.Tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

public class HookRegistry {
    private static final String TAG = "HookRegistry";
    private static final String PKG = "com.netease.cloudmusic";
    private static final String PKG_LITE = "com.netease.cloudmusic.lite";

    private final XposedModule module;
    private final PackageReadyParam param;
    private final String packageName;
    private boolean isLite = false;

    private boolean playProcessInit = false;
    private boolean mainProcessInit = false;
    private final String msg_hook_play_process = "hookPlayProcess";
    private final String msg_play_process_init_finish = "playProcessInitFinish";
    public static final String msg_send_notification = "sendNotification";

    public HookRegistry(XposedModule module, PackageReadyParam param) {
        this.module = module;
        this.param = param;
        this.packageName = param.getPackageName();
        this.isLite = packageName.contains("lite");
    }

    public void registerAll() {
        try {
            Class<?> appClass = param.getClassLoader().loadClass("com.netease.cloudmusic.NeteaseMusicApplication");
            Method attachMethod = appClass.getDeclaredMethod("attachBaseContext", Context.class);
            module.hook(attachMethod).intercept(chain -> {
                chain.proceed();
                onAppAttached((Context) chain.getThisObject());
                return null;
            });
            disableTinker();
        } catch (Exception e) {
            Log.e(TAG, "registerAll: " + e.getMessage());
        }
    }

    private void onAppAttached(final Context context) {
        final String pkg = isLite ? PKG_LITE : PKG;
        final int versionCode;
        try {
            versionCode = context.getPackageManager().getPackageInfo(pkg, 0).versionCode;
        } catch (Exception e) { return; }

        ExtraHelper.init(context);
        SettingHelper.init(context);

        final String processName = Tools.getCurrentProcessName(context);
        if (processName.equals(pkg)) {
            try { new SettingHook(context, versionCode, module); Log.i(TAG, "SettingHook registered"); } catch (Exception e) { Log.e(TAG, "SettingHook", e); }
            if (!SettingHelper.getInstance().getSetting(SettingHelper.master_key)) return;

            try { new ProxyHook(context, versionCode, module, false); Log.i(TAG, "ProxyHook registered"); } catch (Exception e) { Log.e(TAG, "ProxyHook", e); }
            if (SettingHelper.getInstance().isEnable(SettingHelper.black_key)) {
                try { new BlackHook(context, versionCode, module); Log.i(TAG, "BlackHook registered"); } catch (Exception e) { Log.e(TAG, "BlackHook", e); }
                try { deleteAdAndTinker(context); } catch (IOException ignored) {}
            }
            if (!isLite && SettingHelper.getInstance().isEnable(SettingHelper.listen_key))
                try { new ListentogetherHook(context, versionCode, module); Log.i(TAG, "ListentogetherHook registered"); } catch (Exception e) { Log.e(TAG, "ListentogetherHook", e); }
            try { new GrayHook(context, module); Log.i(TAG, "GrayHook registered"); } catch (Exception e) { Log.e(TAG, "GrayHook", e); }
            try { new AutoSignInHook(context, versionCode, module); Log.i(TAG, "AutoSignInHook registered"); } catch (Exception e) { Log.e(TAG, "AutoSignInHook", e); }
            try { new AdAndUpdateHook(context, versionCode, module); Log.i(TAG, "AdAndUpdateHook registered"); } catch (Exception e) { Log.e(TAG, "AdAndUpdateHook", e); }
            try { new MagiskFixHook(context, module); Log.i(TAG, "MagiskFixHook registered"); } catch (Exception e) { Log.e(TAG, "MagiskFixHook", e); }
            try { new InternalDialogHook(context, versionCode, module); Log.i(TAG, "InternalDialogHook registered"); } catch (Exception e) { Log.e(TAG, "InternalDialogHook", e); }
            if (!isLite) try { new LoginFixHook(context, module); Log.i(TAG, "LoginFixHook registered"); } catch (Exception e) { Log.e(TAG, "LoginFixHook", e); }

            final int vc = versionCode;
            ClassHelper.getCacheClassList(context, versionCode, () -> {
                try { new UserProfileHook(context, module); Log.i(TAG, "UserProfileHook registered"); } catch (Exception e) { Log.e(TAG, "UserProfileHook", e); }
                try { new EAPIHook(context, module); Log.i(TAG, "EAPIHook registered"); } catch (Exception e) { Log.e(TAG, "EAPIHook", e); }
                try { new DownloadMD5Hook(context, module); Log.i(TAG, "DownloadMD5Hook registered"); } catch (Exception e) { Log.e(TAG, "DownloadMD5Hook", e); }
                try { new HideTabHook(context, vc, module); Log.i(TAG, "HideTabHook registered"); } catch (Exception e) { Log.e(TAG, "HideTabHook", e); }
                try { new HideSidebarHook(context, vc, module); Log.i(TAG, "HideSidebarHook registered"); } catch (Exception e) { Log.e(TAG, "HideSidebarHook", e); }
                try { new HideBannerHook(context, vc, module); Log.i(TAG, "HideBannerHook registered"); } catch (Exception e) { Log.e(TAG, "HideBannerHook", e); }
                try { new HideBubbleHook(context, module); Log.i(TAG, "HideBubbleHook registered"); } catch (Exception e) { Log.e(TAG, "HideBubbleHook", e); }
                try { new PlayerActivityHook(context, vc, module); Log.i(TAG, "PlayerActivityHook registered"); } catch (Exception e) { Log.e(TAG, "PlayerActivityHook", e); }
                try { new CommentHotClickHook(context, module); Log.i(TAG, "CommentHotClickHook registered"); } catch (Exception e) { Log.e(TAG, "CommentHotClickHook", e); }
                try { new CdnHook(context, vc, module); Log.i(TAG, "CdnHook registered"); } catch (Exception e) { Log.e(TAG, "CdnHook", e); }
                try { if (!isLite) new AdExtraHook(module); Log.i(TAG, "AdExtraHook registered"); } catch (Exception e) { Log.e(TAG, "AdExtraHook", e); }

                mainProcessInit = true;
                if (mainProcessInit && playProcessInit)
                    context.sendBroadcast(new Intent(msg_hook_play_process));
            });

            final Context ctx = context;
            IntentFilter filter = new IntentFilter();
            filter.addAction(msg_play_process_init_finish);
            filter.addAction(msg_send_notification);
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    if (msg_play_process_init_finish.equals(intent.getAction())) {
                        playProcessInit = true;
                        if (mainProcessInit && playProcessInit)
                            ctx.sendBroadcast(new Intent(msg_hook_play_process));
                    } else if (msg_send_notification.equals(intent.getAction())
                            && SettingHelper.getInstance().isEnable(SettingHelper.warn_key)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            NotificationHelper.getInstance(ctx).sendUnLockNotification(ctx,
                                    intent.getIntExtra("code", 0x10),
                                    intent.getStringExtra("title"),
                                    intent.getStringExtra("title"),
                                    intent.getStringExtra("message"));
                        Log.w(TAG, intent.getStringExtra("title") + ": " + intent.getStringExtra("message"));
                    }
                }
            }, filter);

        } else if (processName.equals(pkg + ":play")
                && SettingHelper.getInstance().getSetting(SettingHelper.master_key)) {
            try { new ProxyHook(context, versionCode, module, true); } catch (Exception e) { Log.e(TAG, "ProxyHook(play)", e); }
            IntentFilter filter = new IntentFilter();
            filter.addAction(msg_hook_play_process);
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    if (msg_hook_play_process.equals(intent.getAction())) {
                        ClassHelper.getCacheClassList(context, versionCode, () -> {
                            try { new EAPIHook(context, module); Log.i(TAG, "EAPIHook(play) registered"); } catch (Exception e) { Log.e(TAG, "EAPIHook(play)", e); }
                            try { new CdnHook(context, versionCode, module); Log.i(TAG, "CdnHook(play) registered"); } catch (Exception e) { Log.e(TAG, "CdnHook(play)", e); }
                        });
                    }
                }
            }, filter);
            context.sendBroadcast(new Intent(msg_play_process_init_finish));
        }
    }

    private void disableTinker() {
        try {
            Class<?> tc = param.getClassLoader().loadClass("com.tencent.tinker.loader.app.TinkerApplication");
            for (Constructor<?> ctor : tc.getDeclaredConstructors()) {
                module.hook(ctor).intercept(chain -> {
                    if (!chain.getArgs().isEmpty()) {
                        Object[] na = chain.getArgs().toArray(new Object[0]);
                        na[0] = 0;
                        return chain.proceed(na);
                    }
                    return chain.proceed();
                });
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            Log.e(TAG, "disableTinker: " + e.getMessage());
        }
    }

    private void deleteAdAndTinker(Context context) throws IOException {
        String c1 = Environment.getExternalStorageDirectory() + "/netease/cloudmusic/" + (isLite ? "lite/" : "") + "Ad";
        String c2 = Environment.getExternalStorageDirectory() + "/Android/data/" + (isLite ? PKG_LITE : PKG) + "/cache/Ad";
        String tp = "data/data/" + (isLite ? PKG_LITE : PKG) + "/tinker";
        FileHelper.deleteDirectory(c1);
        FileHelper.deleteDirectory(c2);
        File tf = new File(tp);
        if (tf.exists() && tf.isDirectory()) FileHelper.deleteDirectory(tp);
        if (!tf.exists()) tf.createNewFile();
        Runtime.getRuntime().exec("chmod 000 " + tf.getAbsolutePath());
    }
}
