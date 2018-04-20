
package com.futuremove.update;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.futuremove.update.listener.OnProgressChangeListener;
import com.futuremove.update.loader.FMDownloadManager;
import com.futuremove.update.loader.HttpDownloader;

import java.io.File;
import java.io.IOException;

public class DownloadApk {


    /**
     * 对外提供的下载或安装服务(强制下载不建议使用此方法)
     */
    @MainThread
    public static void download(Context context, String url, String title, String appName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        long downloadId = sharedPreferences.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
        if (downloadId != -1L) {
            FMDownloadManager downloadManager = FMDownloadManager.getInstance(context);
            int status = downloadManager.getDownloadStatus(downloadId);
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Uri uri = Uri.parse(downloadManager.getDownloadPath(downloadId));
                if (uri != null) {
                    if (compareApk(getApkInfo(uri.getPath(), context), context)) {
                        startInstall(context, uri);
                        return;
                    } else {
                        downloadManager.getDownloadManager().remove(downloadId);
                    }
                }
                startDownload(context, url, title, appName);
            } else if (status == DownloadManager.STATUS_FAILED) {
                startDownload(context, url, title, appName);
            }
        } else {
            startDownload(context, url, title, appName);
        }
    }

    /**
     * 使用httpUrlConnection进行下载
     */

    public static void download(final Context context, final String url, final String appName, final OnProgressChangeListener listener) {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file= HttpDownloader.downloadFromServer(url, appName, listener);
                    startInstall(context, Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    @MainThread
    public static void download(final Context context, final String url, final String appName, @NonNull final ProgressDialog progressDialog) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    File file = HttpDownloader.downloadWithProgressBar(url, appName, progressDialog);
                    startInstall(context, Uri.fromFile(file));
                    progressDialog.dismiss();
                } catch (Exception e) {
                    progressDialog.dismiss();
                }

            }
        };
        progressDialog.show();
        Thread thread = new Thread(task);
        thread.start();


    }

    /**
     * 开始下载
     */
    private static void startDownload(Context context, String url, String title, String appName) {
        FMDownloadManager fmDownloadManager = FMDownloadManager.getInstance(context);
        long downloadId = fmDownloadManager.download(url, title, "下载完成后打开", appName);
        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId).apply();
    }

    /**
     * 开始安装
     */
    public static void startInstall(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            install(context, uri);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            installForN(context, uri);
        } else {
            installForO(context, uri);
        }
    }

    /**
     * 7.0以下安装
     */
    private static void install(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
    /**
     * 7.0到8.0以下安装
     */
    private static void installForN(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", new File(uri.getPath()));
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
    /**
     * 8.0安装
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static void installForO(Context context, Uri uri) {
        boolean canInstall = context.getPackageManager().canRequestPackageInstalls();
        if (canInstall) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", new File(uri.getPath()));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }


    /**
     * 当前APK与下载APK进行比对
     * @return 如果下载的APK版本高于本机安装则返回true, 否则为false
     */

    private static boolean compareApk(PackageInfo packageInfo, Context context) {
        if (packageInfo == null) {
            return false;
        }
        String localName = context.getPackageName();
        if (localName.equals(packageInfo.packageName)) {
            try {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo localInfo = packageManager.getPackageInfo(localName, 0);
                if (localInfo.versionCode < packageInfo.versionCode) {
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取指定安装包的信息
     */
    private static PackageInfo getApkInfo(String path, Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            return packageInfo;
        }
        return null;
    }


}
