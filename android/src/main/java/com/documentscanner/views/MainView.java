package com.documentscanner.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.documentscanner.R;

/**
 * Created by andre on 09/01/2018.
 */

public class MainView extends FrameLayout {
    private OpenNoteCameraView view = null;
    private FrameLayout frameLayout = null;

    public static MainView instance = null;

    public static MainView getInstance() {
        return instance;
    }

    public static void createInstance(Context context, Activity activity) {
        instance = new MainView(context, activity);
    }

    private MainView(Context context, Activity activity) {
        super(context);

        LayoutInflater lf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.frameLayout = (FrameLayout) lf.inflate(R.layout.activity_open_note_scanner, null);
        // OpenNoteCameraView.createInstance(context, -1, activity, frameLayout);

        view = new OpenNoteCameraView(context, -1, activity, frameLayout);
        addViewInLayout(view, 0, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addViewInLayout(frameLayout, 1, view.getLayoutParams());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(l, t, r, b);
        }
    }

    public void setDocumentAnimation(boolean animate) {
        view.setDocumentAnimation(animate);
    }

    public void setDetectionCountBeforeCapture(int numberOfRectangles) {
        view.setDetectionCountBeforeCapture(numberOfRectangles);
    }

    public void setEnableTorch(boolean enable) {
        view.setEnableTorch(enable);
    }

    public void setOnScannerListener(OpenNoteCameraView.OnScannerListener listener) {
        view.setOnScannerListener(listener);
    }

    public void removeOnScannerListener() {
        view.removeOnScannerListener();
    }

    public void setOnProcessingListener(OpenNoteCameraView.OnProcessingListener listener) {
        view.setOnProcessingListener(listener);
    }

    public void removeOnProcessingListener() {
        view.removeOnProcessingListener();
    }

    public void setOverlayColor(String rgbaColor) {
        view.setOverlayColor(rgbaColor);
    }

    public void setBrightness(double brightness) {
        view.setBrightness(brightness);
    }

    public void setContrast(double contrast) {
        view.setContrast(contrast);
    }

    public void setManualOnly(boolean manualOnly) {
        view.setManualOnly(manualOnly);
    }

    public void setRemoveGrayScale(boolean grayscale) {
        view.setRemoveGrayScale(grayscale);
    }

    public void capture() {
        view.capture();
    }
}
