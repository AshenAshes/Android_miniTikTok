package com.bytedance.androidcamp.network.dou;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.Response_POST;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity3 extends AppCompatActivity implements SurfaceHolder.Callback {

    private Button btnStart;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private static int mOrientation = 0;
    private static int mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceView mSurfaceView;
    private Boolean stap = true;
    private Button btnTurn;
    private Button btnDelete;
    private Button btnUpload;
    private int useHeight, useWidth;
    private boolean havePermission = false;
    private Camera.Size mSize;
    private boolean isCameraBack = true;
    private String videopath;
    private String imagepath;
    private TextView mLoad;

    String srcPath = Environment.getExternalStorageDirectory().getPath() + "/mediarecorder/";
    String srcName = "video.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        mLoad = findViewById(R.id.tv_LOAD);
        mLoad.setVisibility(View.INVISIBLE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            havePermission = true;
            init();
        } else {
            havePermission = false;
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO}, 100);
        }
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
                        mMediaRecorder = null;
                        mCamera.lock();
                    }
                    btnDelete.setVisibility(View.VISIBLE);
                    btnUpload.setVisibility(View.VISIBLE);
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                    playvideo();
                    stap = true;
                    btnStart.setText("START");
                }
            }
        });
        btnTurn = findViewById(R.id.btn_turn);
        btnTurn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnCamera();
            }
        });
        btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(videopath);
                if (file.isFile() && file.exists()) {
                    Boolean ifDelete = file.delete();
                    if (ifDelete == true) {
                        Toast.makeText(MainActivity3.this, "Delete Successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity3.this, MainActivity1.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity3.this, "Delete Failed!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity3.this, MainActivity1.class);
                        startActivity(intent);
                    }
                }
            }
        });
        btnDelete.setVisibility(View.INVISIBLE);
        btnUpload = findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoad.setVisibility(View.VISIBLE);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                if (videopath.startsWith("http"))
                    mmr.setDataSource(videopath, new HashMap());//获取网络视频第一帧图片
                else mmr.setDataSource(videopath);//本地视频
                Bitmap bitmap = mmr.getFrameAtTime();
                //保存图片
                File f = getOutputMediaFile(2);
                if (f.exists()) {
                    f.delete();
                }
                try {
                    FileOutputStream out = new FileOutputStream(f);
                    if (bitmap == null) {
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mmr.release();
                MultipartBody.Part coverImagePart = getMultipartFromFile("cover_image", imagepath);
                final MultipartBody.Part videoPart = getMultipartFromFile("video", videopath);
                Call<Response_POST> call = getMiniDouyinService().postVideo("3170101510", "goldfische", coverImagePart, videoPart);
                call.enqueue(new Callback<Response_POST>() {
                    @Override
                    public void onResponse(Call<Response_POST> call, Response<Response_POST> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Response_POST videos = response.body();
                            if (videos.getSuccess() == true) {
                                Toast.makeText(MainActivity3.this, "Post Successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity3.this, MainActivity1.class);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Response_POST> call, Throwable throwable) {
                        Toast.makeText(MainActivity3.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity3.this, MainActivity1.class);
                        startActivity(intent);
                    }
                });
            }
        });
        btnUpload.setVisibility(View.INVISIBLE);

    }

    private Retrofit retrofit;
    private IMiniDouyinService miniDouyinService;

    public IMiniDouyinService getMiniDouyinService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(IMiniDouyinService.HOST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (miniDouyinService == null) {
            miniDouyinService = retrofit.create(IMiniDouyinService.class);
        }
        return miniDouyinService;
    }

    public void getImageForVideo(String videoPath, OnLoadVideoImageListener listener) {
        LoadVideoImageTask task = new LoadVideoImageTask(listener);
        task.execute(videoPath);
    }

    public class LoadVideoImageTask extends AsyncTask<String, Integer, File> {
        private OnLoadVideoImageListener listener;

        public LoadVideoImageTask(OnLoadVideoImageListener listener) {
            this.listener = listener;
        }

        @Override
        protected File doInBackground(String... params) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            String path = params[0];
            if (path.startsWith("http"))
                //获取网络视频第一帧图片
                mmr.setDataSource(path, new HashMap());
            else
                //本地视频
                mmr.setDataSource(path);
            Bitmap bitmap = mmr.getFrameAtTime();
            //保存图片
            File f = getOutputMediaFile(2);
            if (f.exists()) {
                f.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(f);
                if (bitmap == null) {
                    return null;
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmr.release();
            return f;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
        }
    }

    public interface OnLoadVideoImageListener {
    }

    private MultipartBody.Part getMultipartFromFile(String name, String path) {
        File f = new File(path);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    public void playvideo() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(videopath);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return false;
                }
            });
            mMediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void turnCamera() {
        if (isCameraBack == true) isCameraBack = false;
        else isCameraBack = true;
        if (isCameraBack == false) {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
            initCamera();
        } else {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
            initCamera();
        }
    }

    public void init() {
        Log.e("test", "init");
        if (mSurfaceView == null) {
            Log.e("test", "init2");
            mSurfaceView = findViewById(R.id.sv_videorecord);
            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mSurfaceHolder.addCallback(this);
            WindowManager wm = (WindowManager) MainActivity3.this.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
            mSurfaceView.setLayoutParams(layoutParams);
        }
        initCamera();
    }

    private void initCamera() {
        Log.e("test", "initCamera");
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            System.out.println("===================releaseCamera=============");
        }
        mCamera = Camera.open(mCameraID);
        System.out.println("===================openCamera=============");
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setRecordingHint(true);
            {
                parameters.setPreviewFormat(ImageFormat.NV21);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                    }
                });
            }
            if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                List<String> supports = parameters.getSupportedFocusModes();
                for (String mode : supports) {
                    if (mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else if (mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                }

            } else {
                List<String> supports = parameters.getSupportedFocusModes();
                for (String mode : supports) {
                    if (mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else if (mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                }
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


    public void initMediaRecord() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile mCamcorderProfile;
        if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCamcorderProfile =
                    CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
        } else {
            mCamcorderProfile =
                    CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_HIGH);
        }

        System.out.println("============mCamcorderProfile============" + mCamcorderProfile.videoFrameWidth
                + "   " + mCamcorderProfile.videoFrameHeight);
        mMediaRecorder.setProfile(mCamcorderProfile);
//        mMediaRecorder.setVideoSize(mCamcorderProfile.videoFrameWidth,mCamcorderProfile.videoFrameHeight);
//        mMediaRecorder.setVideoEncodingBitRate(mCamcorderProfile.videoFrameWidth*mCamcorderProfile.videoFrameHeight*24*16);
//        mMediaRecorder.setVideoFrameRate(24);
//        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setOutputFile(getOutputMediaFile(1).toString());

        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(mOrientation);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getOutputMediaFile(int type) {
        String path;
        if (type == 1) {
            File mediaStoeageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), "Camera");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            videopath = mediaStoeageDir.getPath() + File.separator + "DCIM_" + timeStamp + ".mp4";
            path = videopath;

        } else {
            File mediaStoeageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Camera");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imagepath = mediaStoeageDir.getPath() + File.separator + "JPG_" + timeStamp + ".jpg";
            path = imagepath;
        }
        File mediaFile = new File(path);
        try {
            if (!mediaFile.getParentFile().exists()) mediaFile.getParentFile().mkdirs();
            mediaFile.createNewFile();
            MediaScannerConnection.scanFile(MainActivity3.this, new String[]{mediaFile.getAbsolutePath()}, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaFile;
    }

    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     *
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
        System.out.println("=========orienttaion=============" + result);
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
        Log.e("test", "onRequestPermissionsResult1: ");
        switch (requestCode) {
            // 相机权限
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("test", "onRequestPermissionsResult2: ");
                    havePermission = true;
                    init();
                } else {
                    Log.e("test", "onRequestPermissionsResult3: ");
                }
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //当SurfaceView变化时也需要做相应操作，这里未做相应操作
        Log.e("test", "surfaceChanged");
        if (havePermission) {
            initCamera();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

}