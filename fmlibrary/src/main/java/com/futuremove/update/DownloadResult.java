package com.futuremove.update;

import com.futuremove.update.listener.OnProgressChangeListener;

public class DownloadResult {

    private OnProgressChangeListener listener;

    private int progress;

    private int max;

    public DownloadResult(OnProgressChangeListener listener, int progress,int max) {
        this.listener = listener;
        this.progress = progress;
        this.max=max;
    }

    public DownloadResult(OnProgressChangeListener listener, int max) {
        this.listener = listener;
        this.max = max;
    }

    public OnProgressChangeListener getListener() {
        return listener;
    }

    public void setListener(OnProgressChangeListener listener) {
        this.listener = listener;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
