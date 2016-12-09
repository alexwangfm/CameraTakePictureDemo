package com.alex.demo.camera;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = CameraActivity.class.getSimpleName();

    private Button takePictureButton;
    private Button switchButton;
    private Button reviewButton;

    private SurfaceView previewSurfaceView;
    private SurfaceHolder previewSurfaceHolder;

    private FrameLayout previewContainer;

    private boolean isPreviewing = false;

    private int cameraID = 0;

    private int frontCameraID = 0;
    private int backCameraID = 0;

    private Camera cameraDevice;

    private ArrayList<String> pictureSizeList = new ArrayList<String>();
    private ArrayList<String> previewSizeList = new ArrayList<String>();
    private List<Camera.Size> previewSizes;
    private List<Camera.Size> pictureSizes;

    private int selectedPreviewSizeIndex = 0;
    private int selectedPictureSizeIndex = 0;

    private int screenOrientation = 0;
    private int displayOrientation = 0;

    private String[] orientaionStringList = {"Portrat", "Reverse Landscape", "Reverse Portrat", "Landscape"};
    private int[] orientaionDegreeList = {0, 90, 180, 270};
    private int[] orientaionTypeList = {ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE};

    private int selectedOrientationIndex = 0;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "camera trace -- > onCreate : enter !!!");

        setContentView(R.layout.activity_camera);

        initViews();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "camera trace -- > onStart : enter !!!");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "camera trace -- > onResume : enter !!!");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "camera trace -- > onPause : enter !!!");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "camera trace -- > onStop : enter !!!");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "camera trace -- > onDestroy : enter !!!");
        super.onDestroy();
    }


    private void initViews () {

        this.takePictureButton = (Button) this.findViewById(R.id.camera_take_picture_button);
        this.takePictureButton.setOnClickListener(onClickListener);

        this.reviewButton = (Button) this.findViewById(R.id.camera_review_picture);
        this.reviewButton.setOnClickListener(onClickListener);

        this.switchButton = (Button) this.findViewById(R.id.camrea_switch_camera_button);
        this.switchButton.setOnClickListener(onClickListener);


        this.previewContainer = (FrameLayout) this.findViewById(R.id.camera_preview_surfaceview_container);

        this.previewSurfaceView = (SurfaceView) this.findViewById(R.id.camera_preview_surfaceview);
        this.previewSurfaceHolder = previewSurfaceView.getHolder();
        this.previewSurfaceHolder.addCallback(surfaceHolderCallback);


    }

    private void updatePreviewSurfaceSize () {
        Log.d(TAG, "camera trace -- > updatePreviewSurfaceSize : enter !!!");

        int containerWidth = previewContainer.getWidth();
        int containerHeight = previewContainer.getHeight();
        Log.d(TAG, "camera trace -- > updatePreviewSurfaceSize : enter !!! containerWidth = " + containerWidth + ", containerHeight = " + containerHeight);

        int previewWidth = 0;
        int previewHeight = 0;

        Camera.Size sizePreview = previewSizes.get(selectedPreviewSizeIndex);
        if (selectedOrientationIndex == 0 || selectedOrientationIndex == 2) {
            previewWidth = sizePreview.height;
            previewHeight = sizePreview.width;
        } else {
            previewWidth = sizePreview.width;
            previewHeight = sizePreview.height;
        }

        Log.d(TAG, "camera trace -- > updatePreviewSurfaceSize : enter !!! previewWidth = " + previewWidth + ", previewHeight = " + previewHeight);

        int width = 0;
        int height = 0;

        if (containerHeight > (1f*containerWidth*previewHeight/previewWidth)) {
            width = containerWidth;
            height = (int)(1f*width*previewHeight/previewWidth);
        } else {
            height = containerHeight;
            width = (int)(1f*height*previewWidth/previewHeight);
        }



        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)previewSurfaceView.getLayoutParams();
        params.width = width;
        params.height = height;
        previewSurfaceView.setLayoutParams(params);


    }


    private void initCameraInfo () {
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.d(TAG, "camera trace -- > initCameraInfo : camera count = " + numberOfCameras);
        Camera.CameraInfo[] cameraInfos = new Camera.CameraInfo[numberOfCameras];
        for (int i = 0; i < numberOfCameras; i++) {
            cameraInfos[i] = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfos[i]);
        }
        Log.d(TAG, "camera trace -- > initCameraInfo : CAMERA_FACING_BACK = " + Camera.CameraInfo.CAMERA_FACING_BACK);
        Log.d(TAG, "camera trace -- > initCameraInfo : CAMERA_FACING_FRONT = " + Camera.CameraInfo.CAMERA_FACING_FRONT);
        // get the first (smallest) back and first front camera id
        for (int i = 0; i < numberOfCameras; i++) {
            Log.d(TAG, "camera trace -- > initCameraInfo : camerainfo[" + i + "]-{facing = " + cameraInfos[i].facing
                    + ", canDisableShutterSound = " + cameraInfos[i].canDisableShutterSound
                    + ", orientation = " + cameraInfos[i].orientation + "}"
            );

            if (cameraInfos[i].facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backCameraID = i;
            } else if (cameraInfos[i].facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontCameraID = i;
            }

            if (backCameraID >= 0) {
                cameraID = backCameraID;
            } else if (frontCameraID >= 0) {
                cameraID = frontCameraID;
            }

        }
    }

    private void openCamera () {

        Log.d(TAG, "camera trace -- > openCamera : enter !!!");

        if (cameraID < 0) {
            return;
        }
        cameraDevice = Camera.open(cameraID);
        Log.d(TAG, "camera trace -- > openCamera : camera opened ? " + (null == cameraDevice));

        dumpCameraParameters();

    }

    private void dumpCameraParameters () {

        if (null == cameraDevice) {
            Log.d(TAG, "camera trace -- > dumpCameraParameters : camera is null ");
            return;
        }

        Camera.Parameters cameraParameters = cameraDevice.getParameters();

        Log.d(TAG, "camera trace -- > dumpCameraParameters : camera id = " + 0);

        previewSizes = cameraParameters.getSupportedPreviewSizes();
        if (null != previewSizes) {
            for (int i = 0; i < previewSizes.size(); ++i) {
                Camera.Size size = previewSizes.get(i);
                Log.d(TAG, "camera trace -- > dumpCameraParameters : previewSizes[" + i + "] = " +
                        "[" + size.width + ", " + size.height + "] - " + getRadio(size.width, size.height));

                previewSizeList.add("[" + size.width + ", " + size.height + "] - " + getRadio(size.width, size.height));
            }
        }

        List<Camera.Size> videoSizes = cameraParameters.getSupportedVideoSizes();
        if (null != videoSizes) {
            for (int i = 0; i < videoSizes.size(); ++i) {
                Camera.Size size = videoSizes.get(i);
                Log.d(TAG, "camera trace -- > dumpCameraParameters : videoSizes[" + i + "] = " +
                        "[" + size.width + ", " + size.height + "] - " + getRadio(size.width, size.height));
            }
        }

        pictureSizes = cameraParameters.getSupportedPictureSizes();
        if (null != videoSizes) {
            for (int i = 0; i < pictureSizes.size(); ++i) {
                Camera.Size size = pictureSizes.get(i);
                Log.d(TAG, "camera trace -- > dumpCameraParameters : pictureSizes[" + i + "] = " +
                        "[" + size.width + ", " + size.height + "] - " + getRadio(size.width, size.height));
                pictureSizeList.add("[" + size.width + ", " + size.height + "] - " + getRadio(size.width, size.height));
            }
        }

    }

    private void setupCameraParameters () {
        Log.d(TAG, "camera trace -- > setupCameraParameters : enter !!!");
        try {

            if (null == cameraDevice) {
                return;
            }

            updatePreviewSurfaceSize();

            cameraDevice.setPreviewDisplay(previewSurfaceHolder);
            cameraDevice.setDisplayOrientation(getDisplayOrientation());

            Camera.Parameters cameraParameters = cameraDevice.getParameters();
            Camera.Size sizePreview = previewSizes.get(selectedPreviewSizeIndex);
            cameraParameters.setPreviewSize(sizePreview.width, sizePreview.height);
            Camera.Size sizePicture = pictureSizes.get(selectedPictureSizeIndex);
            cameraParameters.setPictureSize(sizePicture.width, sizePicture.height);

            cameraDevice.setParameters(cameraParameters);


        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "camera trace -- > setupCameraParameters : failed !!!");
        }


    }

    private int getDisplayOrientation () {
        Log.e(TAG, "camera trace -- > getDisplayOrientation : enter !!! selectedOrientationIndex = " + selectedOrientationIndex);
        int screenOrientaion = orientaionDegreeList[selectedOrientationIndex];
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraID, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - screenOrientaion + 360) % 360;
        } else {  // back-facing camera
            rotation = (info.orientation + screenOrientaion) % 360;
        }
        return rotation;
    }

    private void startPreview () {
        Log.d(TAG, "camera trace -- > startPreview : enter !!!");

        if (null != cameraDevice) {
            cameraDevice.startPreview();
        }
        isPreviewing = true;
    }

    private void stopPreview () {

        if (null != cameraDevice) {
            cameraDevice.stopPreview();
        }

        isPreviewing = false;
    }

    private void closeCamera () {
        Log.d(TAG, "camera trace -- > closeCamera : enter !!!");

        if (null != cameraDevice) {
            cameraDevice.release();
        }
    }

    private void takePicture () {
        Log.d(TAG, "camera trace -- > takePicture : enter !!!");
        if (null != cameraDevice) {
            Camera.Parameters params = cameraDevice.getParameters();
            params.setRotation(getDisplayOrientation());
            cameraDevice.setParameters(params);
            cameraDevice.takePicture(null, null, jpegCallback);
        }
    }

    public void showPictureSizeDialog() {
        new MaterialDialog.Builder(this)
                .title("Picture Size")
                .items(pictureSizeList)
                .itemsCallbackSingleChoice(selectedPictureSizeIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Toast.makeText(CameraActivity.this, "select picture size : " + text.toString(), Toast.LENGTH_SHORT).show();

                        selectedPictureSizeIndex = which;
                        stopPreview();
                        setupCameraParameters();
                        startPreview();

                        return true; // allow selection
                    }
                })
                .positiveText(R.string.md_choose_label)
                .show();
    }

    public void showPreviewSizeDialog() {
        new MaterialDialog.Builder(this)
                .title("Preview Size")
                .items(previewSizeList)
                .itemsCallbackSingleChoice(selectedPreviewSizeIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Toast.makeText(CameraActivity.this, "select preview size : " + text.toString(), Toast.LENGTH_SHORT).show();

                        selectedPreviewSizeIndex = which;
                        stopPreview();
                        setupCameraParameters();
                        startPreview();

                        return true; // allow selection
                    }
                })
                .positiveText(R.string.md_choose_label)
                .show();
    }

    public void showDisplayOrientationDialog() {
        new MaterialDialog.Builder(this)
                .title("Display Orientation")
                .items(orientaionStringList)
                .itemsCallbackSingleChoice(selectedOrientationIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Toast.makeText(CameraActivity.this, "select orientation : " + text.toString(), Toast.LENGTH_SHORT).show();

                        selectedOrientationIndex = which;
                        setDisplayOrientation();

                        return true; // allow selection
                    }
                })
                .positiveText(R.string.md_choose_label)
                .show();
    }

    private void switchCamera () {
        if (null != cameraDevice) {
            closeCamera();
        }

        if (cameraID < 0) {
            return;
        }
        int id = -1;
        if (cameraID == backCameraID && frontCameraID >= 0) {
            id = frontCameraID;
        } else if (cameraID == frontCameraID && backCameraID >= 0) {
            id = backCameraID;
        }

        if (id >= 0) {
            cameraID = id;
            openCamera();
            setupCameraParameters();
            startPreview();
        }
    }

    private void reviewPicture () {

    }

    private void setDisplayOrientation () {
        stopPreview();
        setRequestedOrientation(orientaionTypeList[selectedOrientationIndex]);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupCameraParameters();
                startPreview();
            }
        }, 500);
    }




    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "camera trace -- > surfaceCreated : enter !!!");

            initCameraInfo();
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            Log.d(TAG, "camera trace -- > surfaceChanged : enter !!! width = " + width + ", height = " + height + ", format = " + format );

            if (isPreviewing) {
                stopPreview();
            }

            setupCameraParameters();
            startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "camera trace -- > surfaceDestroyed : enter !!!");
            closeCamera();
        }
    };


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.camera_take_picture_button:
                    takePicture();
                    break;
                case R.id.camera_review_picture:
                    reviewPicture();
                    break;
                case R.id.camrea_switch_camera_button:
                    switchCamera();
                    break;
                default:
                    break;

            }
        }
    };

    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "camera trace -- > onPictureTaken : enter !!!");

            byteData2JPEG(data);

            startPreview();
        }
    };




    private String getRadio (int width, int height) {
        if (width/16 * 9 == height) {
            return "16:9";
        } else if (width/16 * 10 == height) {
            return "16:10";
        } else if (width/4 * 3 == height) {
            return "4:3";
        } else if (width/5 * 3 == height) {
            return "5:3";
        } else if (width/3 * 2 == height) {
            return "3:2";
        } else if (width/11 * 9 == height) {
            return "11:9";
        } else if (width == height) {
            return "1:1";
        } else {
            return "";
        }

    }


    public void byteData2JPEG(byte[] data) {
        final File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

//        data = null;
//        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "zeus_camera");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_picture_size:
                showPictureSizeDialog();
                return true;
            case R.id.action_preview_size:
                showPreviewSizeDialog();
                return true;
            case R.id.action_display_orentaion:
                showDisplayOrientationDialog();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }





}
