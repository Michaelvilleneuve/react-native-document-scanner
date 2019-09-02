package com.documentscanner.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.documentscanner.BuildConfig;
import com.documentscanner.ImageProcessor;
import com.documentscanner.R;
import com.documentscanner.helpers.CustomOpenCVLoader;
import com.documentscanner.helpers.OpenNoteMessage;
import com.documentscanner.helpers.PreviewFrame;
import com.documentscanner.helpers.ScannedDocument;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import java.io.File;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Base64;

import static com.documentscanner.helpers.Utils.addImageToGallery;
import static com.documentscanner.helpers.Utils.decodeSampledBitmapFromUri;

public class OpenNoteCameraView extends JavaCameraView implements PictureCallback {

    private static final String TAG = "JavaCameraView";
    private String mPictureFileName;
    private Context mContext;
    SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private HUDCanvasView mHud;
    private boolean mBugRotate = false;
    private ImageProcessor mImageProcessor;
    private boolean mFocused;
    private boolean safeToTakePicture;
    private Activity mActivity;
    private boolean mFlashMode = false;
    private SharedPreferences mSharedPref;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private boolean autoMode = true;
    private HandlerThread mImageThread;
    private View mWaitSpinner;
    private PictureCallback pCallback;
    private boolean scanClicked = false;
    private boolean mVisible;

    private boolean documentAnimation = false;
    private int numberOfRectangles = 15;
    private Boolean enableTorch = false;
    public String overlayColor = null;
    private View blinkView = null;
    private View mView = null;
    private boolean manualCapture = false;

    public static OpenNoteCameraView mThis;

    private OnScannerListener listener = null;
    private OnProcessingListener processingListener = null;

    public interface OnScannerListener {
        void onPictureTaken(WritableMap path);
    }

    public interface OnProcessingListener {
        void onProcessingChange(WritableMap path);
    }

    public void setOnScannerListener(OnScannerListener listener) {
        this.listener = listener;
    }

    public void removeOnScannerListener() {
        this.listener = null;
    }

    public void setOnProcessingListener(OnProcessingListener processingListener) {
        this.processingListener = processingListener;
    }

    public void removeOnProcessingListener() {
        this.processingListener = null;
    }

    public OpenNoteCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OpenNoteCameraView(Context context, Integer numCam, Activity activity, FrameLayout frameLayout) {
        super(context, numCam);
        this.mContext = context;
        this.mActivity = activity;
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        pCallback = this;
        mView = frameLayout;

        LayoutInflater lf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initOpenCv(context);
    }

    public static OpenNoteCameraView getInstance() {
        return mThis;
    }

    public void setDocumentAnimation(boolean animate) {
        this.documentAnimation = animate;
    }

    public void setOverlayColor(String rgbaColor) {
        this.overlayColor = rgbaColor;
    }

    public void setDetectionCountBeforeCapture(int numberOfRectangles) {
        this.numberOfRectangles = numberOfRectangles;
    }

    public void setEnableTorch(boolean enableTorch) {
        this.enableTorch = enableTorch;

        if (mCamera != null) {
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(enableTorch ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(p);
        }
    }

    public void capture() {
        this.requestManualPicture();
    }

    public void setManualOnly(boolean manualOnly) {
        this.manualCapture = manualOnly;
    }

    public void setBrightness(double brightness) {
        if (mImageProcessor != null) {
            mImageProcessor.setBrightness(brightness);
        }
    }

    public void setContrast(double contrast) {
        if (mImageProcessor != null) {
            mImageProcessor.setContrast(contrast);
        }
    }

    public void setRemoveGrayScale(boolean grayscale) {
        mImageProcessor.setRemoveGrayScale(grayscale);
    }

    public void initOpenCv(Context context) {

        mThis = this;

        mHud = (HUDCanvasView) mView.findViewById(R.id.hud);
        mWaitSpinner = mView.findViewById(R.id.wait_spinner);
        blinkView = mView.findViewById(R.id.blink_view);
        blinkView.setBackgroundColor(Color.WHITE);

        mVisible = true;

        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Display display = mActivity.getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getRealSize(size);

        BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "SUCCESS init openCV: " + status);
                    // System.loadLibrary("ndklibrarysample");
                    enableCameraView();
                }
                    break;
                default: {
                    Log.d(TAG, "ERROR init Opencv: " + status);
                    super.onManagerConnected(status);
                }
                    break;
                }
            }
        };

        if (!OpenCVLoader.initDebug()) {
            CustomOpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, context, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if (mImageThread == null) {
            mImageThread = new HandlerThread("Worker Thread");
            mImageThread.start();
        }

        if (mImageProcessor == null) {
            mImageProcessor = new ImageProcessor(mImageThread.getLooper(), new Handler(), this, mContext);
        }
        this.setImageProcessorBusy(false);

    }

    public HUDCanvasView getHUD() {
        return mHud;
    }

    private boolean imageProcessorBusy = true;
    private boolean attemptToFocus = false;

    public void setImageProcessorBusy(boolean imageProcessorBusy) {
        this.imageProcessorBusy = imageProcessorBusy;
    }

    public void setAttemptToFocus(boolean attemptToFocus) {
        this.attemptToFocus = attemptToFocus;
    }

    public boolean isFocused() {
        return this.mFocused;
    }

    public void turnCameraOn() {
        mSurfaceView = (SurfaceView) mView.findViewById(R.id.surfaceView);
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.setVisibility(SurfaceView.VISIBLE);
    }

    public void enableCameraView() {
        if (mSurfaceView == null) {
            turnCameraOn();
        }
    }

    private int findBestCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
            cameraId = i;
        }
        return cameraId;
    }

    public Camera.Size getMaxPreviewResolution() {
        int maxWidth = 0;
        Camera.Size curRes = null;

        mCamera.lock();

        for (Camera.Size r : getResolutionList()) {
            if (r.width > maxWidth) {
                Log.d(TAG, "supported preview resolution: " + r.width + "x" + r.height);
                maxWidth = r.width;
                curRes = r;
            }
        }

        return curRes;
    }

    public Camera.Size getMaxPictureResolution(float previewRatio) {
        int maxPixels = 0;
        int ratioMaxPixels = 0;
        Camera.Size currentMaxRes = null;
        Camera.Size ratioCurrentMaxRes = null;
        for (Camera.Size r : getPictureResolutionList()) {
            float pictureRatio = (float) r.width / r.height;
            Log.d(TAG, "supported picture resolution: " + r.width + "x" + r.height + " ratio: " + pictureRatio);
            int resolutionPixels = r.width * r.height;

            if (resolutionPixels > ratioMaxPixels && pictureRatio == previewRatio) {
                ratioMaxPixels = resolutionPixels;
                ratioCurrentMaxRes = r;
            }

            if (resolutionPixels > maxPixels) {
                maxPixels = resolutionPixels;
                currentMaxRes = r;
            }
        }

        // boolean matchAspect = mSharedPref.getBoolean("match_aspect", true);

        if (ratioCurrentMaxRes != null && true) {

            Log.d(TAG, "Max supported picture resolution with preview aspect ratio: " + ratioCurrentMaxRes.width + "x"
                    + ratioCurrentMaxRes.height);
            return ratioCurrentMaxRes;

        }

        return currentMaxRes;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            int cameraId = findBestCamera();
            mCamera = Camera.open(cameraId);
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        Camera.Parameters param;
        param = mCamera.getParameters();

        Camera.Size pSize = getMaxPreviewResolution();
        param.setPreviewSize(pSize.width, pSize.height);
        param.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        float previewRatio = (float) pSize.width / pSize.height;

        Display display = mActivity.getWindowManager().getDefaultDisplay();
        // Display display =
        android.graphics.Point size = new android.graphics.Point();
        display.getRealSize(size);

        int displayWidth = Math.min(size.y, size.x);
        int displayHeight = Math.max(size.y, size.x);

        float displayRatio = (float) displayHeight / displayWidth;

        int previewHeight = displayHeight;

        if (displayRatio > previewRatio) {
            ViewGroup.LayoutParams surfaceParams = mSurfaceView.getLayoutParams();
            previewHeight = (int) ((float) size.y / displayRatio * previewRatio);
            surfaceParams.height = previewHeight;
            mSurfaceView.setLayoutParams(surfaceParams);

            mHud.getLayoutParams().height = previewHeight;
        }

        int hotAreaWidth = displayWidth / 4;
        int hotAreaHeight = previewHeight / 2 - hotAreaWidth;

        Camera.Size maxRes = getMaxPictureResolution(previewRatio);
        if (maxRes != null) {
            param.setPictureSize(maxRes.width, maxRes.height);
            Log.d(TAG, "max supported picture resolution: " + maxRes.width + "x" + maxRes.height);
        }

        PackageManager pm = mActivity.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else {
            mFocused = true;
        }
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            param.setFlashMode(enableTorch ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
        }
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCamera.setParameters(param);
        // mBugRotate = mSharedPref.getBoolean("bug_rotate", false);

        if (mBugRotate) {
            mCamera.setDisplayOrientation(270);
        } else {
            mCamera.setDisplayOrientation(90);
        }

        if (mImageProcessor != null) {
            mImageProcessor.setBugRotate(mBugRotate);
            mImageProcessor.setNumOfRectangles(numberOfRectangles);
        }

        try {
            mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
                @Override
                public void onAutoFocusMoving(boolean start, Camera camera) {
                    mFocused = !start;
                    Log.d(TAG, "focusMoving: " + mFocused);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "failed setting AutoFocusMoveCallback");
        }

        // some devices doesn't call the AutoFocusMoveCallback - fake the
        // focus to true at the start
        mFocused = true;

        safeToTakePicture = true;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    private void refreshCamera() {
        final boolean torchEnabled = this.enableTorch;

        try {
            mCamera.stopPreview();
        }

        catch (Exception e) {
        }

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mThis.setEnableTorch(torchEnabled);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Camera.Size pictureSize = camera.getParameters().getPreviewSize();

        if (mFocused && !imageProcessorBusy) {
            setImageProcessorBusy(true);
            Mat yuv = new Mat(new org.opencv.core.Size(pictureSize.width, pictureSize.height * 1.5), CvType.CV_8UC1);
            yuv.put(0, 0, data);

            Mat mat = new Mat(new org.opencv.core.Size(pictureSize.width, pictureSize.height), CvType.CV_8UC4);
            Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGBA_NV21, 4);

            yuv.release();

            if (!manualCapture) {
                sendImageProcessorMessage("previewFrame", new PreviewFrame(mat, autoMode, !(autoMode)));
            }

        }

    }

    public void invalidateHUD() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHud.invalidate();
            }
        });
    }

    public void sendImageProcessorMessage(String messageText, Object obj) {
        Log.d(TAG, "sending message to ImageProcessor: " + messageText + " - " + obj.toString());
        Message msg = mImageProcessor.obtainMessage();
        msg.obj = new OpenNoteMessage(messageText, obj);
        mImageProcessor.sendMessage(msg);
    }

    public void blinkScreenAndShutterSound() {
        AudioManager audio = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        switch (audio.getRingerMode()) {
        case AudioManager.RINGER_MODE_NORMAL:
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
            break;
        case AudioManager.RINGER_MODE_SILENT:
            break;
        case AudioManager.RINGER_MODE_VIBRATE:
            break;
        }
    }

    public void waitSpinnerVisible() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWaitSpinner.setVisibility(View.VISIBLE);
                WritableMap data = new WritableNativeMap();
                data.putBoolean("processing", true);
                mThis.processingListener.onProcessingChange(data);
            }
        });
    }

    public void waitSpinnerInvisible() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // blinkView.setVisibility(View.INVISIBLE);
                mWaitSpinner.setVisibility(View.INVISIBLE);
                WritableMap data = new WritableNativeMap();
                data.putBoolean("processing", false);
                mThis.processingListener.onProcessingChange(data);
            }
        });
    }

    public void blinkScreen() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                blinkView.bringToFront();
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.blink);
                blinkView.startAnimation(animation);
                blinkView.setVisibility(View.INVISIBLE);

            }
        });
    }

    public boolean requestManualPicture() {
        this.blinkScreenAndShutterSound();
        this.waitSpinnerVisible();

        if (safeToTakePicture) {

            safeToTakePicture = false;

            try {
                mCamera.takePicture(null, null, pCallback);
            } catch (Exception e) {
                this.waitSpinnerInvisible();
            }
            return true;
        }
        return false;
    }

    public boolean requestPicture() {
        PackageManager pm = mActivity.getPackageManager();
        if (safeToTakePicture) {

            safeToTakePicture = false;

            try {
                if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                mCamera.takePicture(null, null, pCallback);
                                blinkScreen();
                                blinkScreenAndShutterSound();
                            }
                            if (attemptToFocus) {
                                return;
                            } else {
                                attemptToFocus = true;
                            }
                        }
                    });
                } else {
                    mCamera.takePicture(null, null, pCallback);
                    blinkScreen();
                    blinkScreenAndShutterSound();
                }
            } catch (Exception e) {
                waitSpinnerInvisible();
            } finally {
                waitSpinnerInvisible();
            }
            return true;
        }
        return false;
    }

    public String saveToDirectory(Mat doc) {
        String fileName;
        boolean isIntent = false;
        Uri fileUri = null;
        String folderName = "documents";
        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/" + folderName);
        if (!folder.exists()) {
            folder.mkdirs();
            Log.d(TAG, "wrote: created folder " + folder.getPath());
        }
        fileName = Environment.getExternalStorageDirectory().toString() + "/" + folderName + "/" + UUID.randomUUID()
                + ".jpg";

        Mat endDoc = new Mat(Double.valueOf(doc.size().width).intValue(), Double.valueOf(doc.size().height).intValue(),
                CvType.CV_8UC4);

        Core.flip(doc.t(), endDoc, 1);

        Imgcodecs.imwrite(fileName, endDoc);

        endDoc.release();

        return fileName;
    }

    public void saveDocument(ScannedDocument scannedDocument) {

        Mat doc = (scannedDocument.processed != null) ? scannedDocument.processed : scannedDocument.original;

        Intent intent = mActivity.getIntent();
        boolean isIntent = false;
        Uri fileUri = null;

        String fileName = this.saveToDirectory(doc);
        String initialFileName = this.saveToDirectory(scannedDocument.original);

        WritableMap data = new WritableNativeMap();

        if (this.listener != null) {
            data.putInt("height", scannedDocument.heightWithRatio);
            data.putInt("width", scannedDocument.widthWithRatio);
            data.putString("croppedImage", "file://" + fileName);
            data.putString("initialImage", "file://" + initialFileName);
            data.putMap("rectangleCoordinates", scannedDocument.previewPointsAsHash());

            this.listener.onPictureTaken(data);
        }

        if (isIntent) {
            InputStream inputStream = null;
            OutputStream realOutputStream = null;
            try {
                inputStream = new FileInputStream(fileName);
                realOutputStream = mActivity.getContentResolver().openOutputStream(fileUri);
                // Transfer bytes from in to out
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    realOutputStream.write(buffer, 0, len);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {
                try {
                    inputStream.close();
                    realOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        Log.d(TAG, "wrote: " + fileName);

        if (isIntent) {
            new File(fileName).delete();
            mActivity.setResult(Activity.RESULT_OK, intent);
            mActivity.finish();
        } else {
            animateDocument(fileName, scannedDocument);
            addImageToGallery(fileName, mContext);
        }

        // Record goal "PictureTaken"
        // ((OpenNoteScannerApplication) getApplication()).getTracker().trackGoal(1);

        refreshCamera();

    }

    private void animateDocument(String filename, ScannedDocument quadrilateral) {

        // if(!documentAnimation) return;

        OpenNoteCameraView.AnimationRunnable runnable = new OpenNoteCameraView.AnimationRunnable(filename,
                quadrilateral);
        mActivity.runOnUiThread(runnable);
        this.waitSpinnerInvisible();

    }

    class AnimationRunnable implements Runnable {

        private org.opencv.core.Size imageSize;
        private Point[] previewPoints = null;
        public org.opencv.core.Size previewSize = null;
        public String fileName = null;
        public int width;
        public int height;
        private Bitmap bitmap;

        public AnimationRunnable(String filename, ScannedDocument document) {
            this.fileName = filename;
            this.imageSize = document.processed.size();

            if (document.quadrilateral != null) {
                this.previewPoints = document.previewPoints;
                this.previewSize = document.previewSize;
            }
        }

        public double hipotenuse(Point a, Point b) {
            return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
        }

        @Override
        public void run() {
            final ImageView imageView = (ImageView) mView.findViewById(R.id.scannedAnimation);

            Display display = mActivity.getWindowManager().getDefaultDisplay();
            android.graphics.Point size = new android.graphics.Point();
            display.getRealSize(size);

            int width = Math.min(size.x, size.y);
            int height = Math.max(size.x, size.y);

            // ATENTION: captured images are always in landscape, values should be swapped
            double imageWidth = imageSize.height;
            double imageHeight = imageSize.width;

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();

            if (previewPoints != null) {
                double documentLeftHeight = hipotenuse(previewPoints[0], previewPoints[1]);
                double documentBottomWidth = hipotenuse(previewPoints[1], previewPoints[2]);
                double documentRightHeight = hipotenuse(previewPoints[2], previewPoints[3]);
                double documentTopWidth = hipotenuse(previewPoints[3], previewPoints[0]);

                double documentWidth = Math.max(documentTopWidth, documentBottomWidth);
                double documentHeight = Math.max(documentLeftHeight, documentRightHeight);

                Log.d(TAG, "device: " + width + "x" + height + " image: " + imageWidth + "x" + imageHeight
                        + " document: " + documentWidth + "x" + documentHeight);

                Log.d(TAG, "previewPoints[0] x=" + previewPoints[0].x + " y=" + previewPoints[0].y);
                Log.d(TAG, "previewPoints[1] x=" + previewPoints[1].x + " y=" + previewPoints[1].y);
                Log.d(TAG, "previewPoints[2] x=" + previewPoints[2].x + " y=" + previewPoints[2].y);
                Log.d(TAG, "previewPoints[3] x=" + previewPoints[3].x + " y=" + previewPoints[3].y);

                // ATENTION: again, swap width and height
                double xRatio = width / previewSize.height;
                double yRatio = height / previewSize.width;

                params.topMargin = (int) (previewPoints[3].x * yRatio);
                params.leftMargin = (int) ((previewSize.height - previewPoints[3].y) * xRatio);
                params.width = (int) (documentWidth * xRatio);
                params.height = (int) (documentHeight * yRatio);
            } else {
                params.topMargin = height / 4;
                params.leftMargin = width / 4;
                params.width = width / 2;
                params.height = height / 2;
            }

            bitmap = decodeSampledBitmapFromUri(fileName, params.width, params.height);

            imageView.setImageBitmap(bitmap);

            imageView.setVisibility(View.VISIBLE);

            TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE,
                    -params.leftMargin, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, height - params.topMargin);

            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0, 1, 0);

            AnimationSet animationSet = new AnimationSet(true);

            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(translateAnimation);

            animationSet.setDuration(600);
            animationSet.setInterpolator(new AccelerateInterpolator());

            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageView.setVisibility(View.INVISIBLE);
                    imageView.setImageBitmap(null);
                    OpenNoteCameraView.AnimationRunnable.this.bitmap.recycle();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            imageView.startAnimation(animationSet);

        }
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public boolean isEffectSupported(String effect) {
        List<String> effectList = getEffectList();
        for (String str : effectList) {
            if (str.trim().contains(effect))
                return true;
        }
        return false;
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public List<Size> getPictureResolutionList() {
        return mCamera.getParameters().getSupportedPictureSizes();
    }

    public void setMaxPictureResolution() {
        int maxWidth = 0;
        Size curRes = null;
        for (Size r : getPictureResolutionList()) {
            Log.d(TAG, "supported picture resolution: " + r.width + "x" + r.height);
            if (r.width > maxWidth) {
                maxWidth = r.width;
                curRes = r;
            }
        }

        if (curRes != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureSize(curRes.width, curRes.height);
            mCamera.setParameters(parameters);
            Log.d(TAG, "selected picture resolution: " + curRes.width + "x" + curRes.height);
        }

        return;
    }

    public void setMaxPreviewResolution() {
        int maxWidth = 0;
        Size curRes = null;

        mCamera.lock();

        for (Size r : getResolutionList()) {
            if (r.width > maxWidth) {
                Log.d(TAG, "supported preview resolution: " + r.width + "x" + r.height);
                maxWidth = r.width;
                curRes = r;
            }
        }

        if (curRes != null) {
            setResolution(curRes);
            Log.d(TAG, "selected preview resolution: " + curRes.width + "x" + curRes.height);
        }

        return;
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
        Log.d(TAG, "resolution: " + resolution.width + " x " + resolution.height);
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public void setFlash(boolean stateFlash) {
        /* */
        Camera.Parameters par = mCamera.getParameters();
        par.setFlashMode(stateFlash ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(par);
        Log.d(TAG, "flash: " + (stateFlash ? "on" : "off"));
        // */
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Camera.Size pictureSize = camera.getParameters().getPictureSize();

        Mat mat = new Mat(new org.opencv.core.Size(pictureSize.width, pictureSize.height), CvType.CV_8U);
        mat.put(0, 0, data);

        setImageProcessorBusy(true);
        sendImageProcessorMessage("pictureTaken", mat);

        safeToTakePicture = true;

    }

    public int parsedOverlayColor() {
        {
            Pattern c = Pattern.compile("rgba *\\( *([0-9]+), *([0-9]+), *([0-9]+), *([0-9]\\.?[0-9]?)*\\)");
            Matcher m = c.matcher(this.overlayColor);

            if (m.matches()) {
                return Color.argb((int) (255 * Float.valueOf(m.group(4))), Integer.valueOf(m.group(1)),
                        Integer.valueOf(m.group(2)), Integer.valueOf(m.group(3)));
            }

            return Color.argb(180, 66, 165, 245);

        }
    }
}