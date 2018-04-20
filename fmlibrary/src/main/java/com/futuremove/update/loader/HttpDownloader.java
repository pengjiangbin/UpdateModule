package com.futuremove.update.loader;

import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.futuremove.update.DownloadResult;
import com.futuremove.update.listener.OnProgressChangeListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class HttpDownloader {
    private static String CacheFile = Environment.getExternalStorageDirectory() + File.separator + "Version";

    private static final int MESSAGE_POST_PROGRESS = 0x01;

    private static final int MESSAGE_POST_RESULT = 0x11;

    private static InnerHandler handler;

    /**
     * 服务端下载(进度回调)
     */
    @WorkerThread
    public static File downloadFromServer(String url, String appName, OnProgressChangeListener listener) throws IOException {
        if (!isSDCardAble()) {
            return null;
        }
        URL loadUrl = new URL(url);
        URLConnection connection = loadUrl.openConnection();
        connection.setConnectTimeout(5000);
        InputStream is = connection.getInputStream();
        int maxLength = connection.getContentLength();
        File file = new File(CacheFile, appName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024];
        int len;
        int progress = 0;
        while ((len = bis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            progress += len;
            getHandler()
                    .obtainMessage(MESSAGE_POST_PROGRESS, new DownloadResult(listener, progress, maxLength))
                    .sendToTarget();
        }
        getHandler().
                obtainMessage(MESSAGE_POST_RESULT, new DownloadResult(listener, maxLength))
                .sendToTarget();
        fos.close();
        bis.close();
        is.close();
        return file;

    }

    /**
     * 服务端下载（默认自带进度条）
     */
    @WorkerThread
    public static File downloadWithProgressBar(String url, String appName, @NonNull ProgressDialog progressDialog) throws IOException {
        if (!isSDCardAble()) {
            return null;
        }
        URL loadUrl = new URL(url);
        URLConnection connection = loadUrl.openConnection();
        connection.setConnectTimeout(5000);
        InputStream is = connection.getInputStream();
        int maxLength = connection.getContentLength();
        progressDialog.setMax(maxLength);
        File file = new File(CacheFile, appName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024];
        int len;
        int progress = 0;
        while ((len = bis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            progress += len;
            progressDialog.setProgress(progress);
        }
        fos.close();
        bis.close();
        is.close();
        return file;
    }


    /**
     *判断sd卡是否可用
     */
    private static boolean isSDCardAble() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    /**
     * 一般不存在并发情况，所以未保证线程安全
     */
    private static Handler getHandler() {
        if (handler == null) {
            handler = new InnerHandler();
        }
        return handler;
    }

    private static class InnerHandler extends Handler {
        InnerHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadResult result = (DownloadResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_PROGRESS:
                    if (result.getListener() != null) {
                        result.getListener().progress(result.getProgress(), result.getMax());
                    }
                    break;
                case MESSAGE_POST_RESULT:
                    if (result.getListener() != null) {
                        result.getListener().onComplete();
                    }
                    break;
            }
        }
    }
}
