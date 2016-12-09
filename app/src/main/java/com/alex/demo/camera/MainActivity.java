package com.alex.demo.camera;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button startCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews () {
        this.startCameraButton = (Button) this.findViewById(R.id.start_camera_activity);
        this.startCameraButton.setOnClickListener(onClickListener);
    }



    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.start_camera_activity:
                    startCameraActivity();
                    break;
                default:
                    break;
            }
        }
    };

    private void startCameraActivity () {
        Intent intent = new Intent();
        intent.setClass(this, CameraActivity.class);

        this.startActivity(intent);
    }
}
