package simcpux.sourceforge.net.updatemodule;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.futuremove.update.DownloadApk;
import com.futuremove.update.listener.OnProgressChangeListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String url = "http://eos-beijing-1.cmecloud.cn/web/app/1519634611450EIntelligent-1.0.5-2018%3A02%3A26_15%3A12%3A46.apk?AWSAccessKeyId=I77C69HKC3CFFJKO6R1Q&Expires=11519634678&Signature=nwEvsEBsSAskuIb0YT4joplYlhY%3D";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadApk.download(MainActivity.this, url, "标题", "1.apk");
            }
        });
        findViewById(R.id.force).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("正在下载，请稍后");
                progressDialog.setTitle("升级中");
                DownloadApk.download(MainActivity.this, url, "1.apk", progressDialog);
            }
        });

        findViewById(R.id.progress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DownloadApk.download(MainActivity.this, url, "1.apk", new OnProgressChangeListener() {
                    @Override
                    public void progress(long progress, long max) {
                        Log.d(TAG, "progress: "+progress+Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: "+Thread.currentThread().getName());
                    }
                });
            }
        });
    }
}
