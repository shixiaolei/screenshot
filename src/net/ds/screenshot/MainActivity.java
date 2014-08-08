
package net.ds.screenshot;

import java.io.File;

import net.ds.screenshot.core.Snapshot;
import net.ds.screenshot.core.Snapshot.SnapshotToBitmapCallBack;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.start).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
    }
    
    public void start() {
        Snapshot.capture(new SnapshotToBitmapCallBack() {
            
            @Override
            public void onSucceed(Bitmap b) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "onSucceed", Toast.LENGTH_LONG).show();
                    }
                });
                File targetFile = new File(Environment.getExternalStorageDirectory(), "zzz" + System.currentTimeMillis() + ".png");
                BitmapUtils.savePicToPath(b, targetFile, CompressFormat.PNG);
            }
            
            @Override
            public void onFailed() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "onFailed", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }); 
    }
}
