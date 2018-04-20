
package com.futuremove.update.loader;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


public class FMDownloadManager {
    private static final String TAG = "FMDownloadManager";
    private DownloadManager downloadManager;
    private static volatile FMDownloadManager fmDownloadManager;

    private FMDownloadManager(Context context) {
        downloadManager = (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public static FMDownloadManager getInstance(Context context) {
        if (fmDownloadManager == null) {
            synchronized (FMDownloadManager.class) {
                if (fmDownloadManager == null) {
                    fmDownloadManager = new FMDownloadManager(context);
                }
            }
        }
        return fmDownloadManager;
    }

    /**
     * 获取下载管理器
     *
     * @return 下载管理器
     */
    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    /**
     * 执行下载，返回downloadId,可以通过downloadId去查看文件的下载状态以及下载路径
     *
     * @param url 下载Url
     * @param title 通知栏标题
     * @param desc  描述
     * @param appName 下载到本地apk的名称，目前需要带后缀
     * @return downloadId
     */
    public long download(String url, String title, String desc, String appName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setMimeType("application/vnd.android.package-archive");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, appName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle(title);
        request.setDescription(desc);
        return downloadManager.enqueue(request);
    }

    /**
     * 获取下载文件的路径（路径到具体的文件）
     *
     * @param downloadId 下载id
     * @return 下载文件路径
     */
    public String getDownloadPath(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 获取当前下载的状态
     * @param downloadId 下载id
     * @return 下载状态
     */
    public int getDownloadStatus(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                cursor.close();
            }
        }
        return -1;
    }

}
