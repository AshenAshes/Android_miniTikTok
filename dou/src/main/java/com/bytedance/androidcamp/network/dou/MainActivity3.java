package com.bytedance.androidcamp.network.dou;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity3 extends AppCompatActivity implements SurfaceHolder.Callback{
    private Button btnStart;
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private static int mOrientation=0;
    private static int mCameraID=Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceView mSurfaceView;
    private Boolean stap = true;
    private int useHeight,useWidth;
    private boolean havePermission=false;
    private Camera.Size mSize;

    String srcPath= Environment.getExternalStorageDirectory().getPath()+"/mediarecorder/";
    String srcName="video.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        ImageView exitButton=findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stap == true) {
                    initMediaRecord();
                    stap = false;
                    btnStart.setText("STOP");
                } else {
                    if (mMediaRecorder != null) {
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        mMediaRecorder.release();
                        mMediaRecorder=null;
                        mCamera.lock();
                    }
                    stap = true;
                    btnStart.setText("START");
                }
            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                ==PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED){
            havePermission=true;
            init();
        }else{
            havePermission=false;
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO},100);
        }
    }

    public void initMediaRecord(){
        mMediaRecorder=new MediaRecorder();
        mMediaRecorder.reset();
        mCamera.unlock();
//        File mRecorderFile=new File(srcPath+srcName);
//        try{
//            if(!mRecorderFile.getParentFile().exists()) mRecorderFile.getParentFile().mkdirs();
//            mRecorderFile.createNewFile();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile mCamcorderProfile=
                CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK,CamcorderProfile.QUALITY_HIGH);
        System.out.println("============mCamcorderProfile============"+mCamcorderProfile.videoFrameWidth
                +"   "+mCamcorderProfile.videoFrameHeight);
        mMediaRecorder.setProfile(mCamcorderProfile);
//        mMediaRecorder.setVideoSize(mCamcorderProfile.videoFrameWidth,mCamcorderProfile.videoFrameHeight);
//        mMediaRecorder.setVideoEncodingBitRate(mCamcorderProfile.videoFrameWidth*mCamcorderProfile.videoFrameHeight*24*16);
//        mMediaRecorder.setVideoFrameRate(24);
//        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(mOrientation);
        try{
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void init(){
        if(mSurfaceView==null){
            mSurfaceView=findViewById(R.id.sv_videorecord);
            mSurfaceHolder=mSurfaceView.getHolder();
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mSurfaceHolder.addCallback(this);
            WindowManager wm=(WindowManager) MainActivity3.this.getSystemService(Context.WINDOW_SERVICE);
            int width=wm.getDefaultDisplay().getWidth();
            RelativeLayout.LayoutParams layoutParams=(RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
            mSurfaceView.setLayoutParams(layoutParams);
        }
    }

    private void initCamera(){
        if(mCamera!=null){
            mCamera.release();
            System.out.println("===================releaseCamera=============");
        }
        mCamera=Camera.open(mCameraID);
        System.out.println("===================openCamera=============");
        if(mCamera!=null){
            try{
                mCamera.setPreviewDisplay(mSurfaceHolder);
            }catch (IOException e){
                e.printStackTrace();
            }
            Camera.Parameters parameters=mCamera.getParameters();
            parameters.setRecordingHint(true);
            {
                parameters.setPreviewFormat(ImageFormat.NV21);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                    }
                });
            }
            if(mCameraID== Camera.CameraInfo.CAMERA_FACING_BACK){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCamera.setParameters(parameters);
            calculateCameraPreviewOrientation(this);
//            Camera.Size tempSize = setPreviewSize(mCamera, useHeight,useWidth);
//            {
//                //此处可以处理，获取到tempSize，如果tempSize和设置的SurfaceView的宽高冲突，重新设置SurfaceView的宽高
//            }

//            setPictureSize(mCamera,  useHeight,useWidth);
            mCamera.setDisplayOrientation(mOrientation);
            int degree = calculateCameraPreviewOrientation(MainActivity3.this);
            mCamera.setDisplayOrientation(degree);
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //当SurfaceView变化时也需要做相应操作，这里未做相应操作
        if (havePermission){
            initCamera();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
    }

    public File getOutputMediaFile(int type){
        File mediaStoeageDir=new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM),"Camera");
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile=new File(mediaStoeageDir.getPath()+File.separator+"DCIM_"+timeStamp+".mp4");
        try{
            if(!mediaFile.getParentFile().exists()) mediaFile.getParentFile().mkdirs();
            mediaFile.createNewFile();
            MediaScannerConnection.scanFile(MainActivity3.this, new String[] { mediaFile.getAbsolutePath() }, null, null);
        }catch (IOException e){
            e.printStackTrace();
        }
        return mediaFile;
    }


    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     * @param activity
     */
    public static int calculateCameraPreviewOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        mOrientation = result;
        System.out.println("=========orienttaion============="+result);
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (havePermission && mCamera != null)
            mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (havePermission && mCamera != null)
            mCamera.stopPreview();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // 相机权限
            case 100:
                havePermission = true;
                init();
                break;
        }
    }

}
