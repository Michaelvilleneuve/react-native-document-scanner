package com.documentscanner;

import android.app.Activity;

import com.documentscanner.views.MainView;
import com.documentscanner.views.OpenNoteCameraView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import javax.annotation.Nullable;

/**
 * Created by Andre on 29/11/2017.
 */

public class DocumentScannerViewManager extends ViewGroupManager<MainView> {

    public static final String REACT_CLASS = "RNPdfScanner";
    private MainView view = null;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected MainView createViewInstance(final ThemedReactContext reactContext) {
        // OpenNoteCameraView view = new OpenNoteCameraView(reactContext, -1,
        // reactContext.getCurrentActivity());
        MainView.createInstance(reactContext, (Activity) reactContext.getBaseContext());

        view = MainView.getInstance();
        view.setOnProcessingListener(new OpenNoteCameraView.OnProcessingListener() {
            @Override
            public void onProcessingChange(WritableMap data) {
                dispatchEvent(reactContext, "onProcessingChange", data);
            }
        });

        view.setOnScannerListener(new OpenNoteCameraView.OnScannerListener() {
            @Override
            public void onPictureTaken(WritableMap data) {
                dispatchEvent(reactContext, "onPictureTaken", data);
            }
        });

        return view;
    }

    private void dispatchEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    @ReactProp(name = "documentAnimation", defaultBoolean = false)
    public void setDocumentAnimation(MainView view, boolean animate) {
        view.setDocumentAnimation(animate);
    }

    @ReactProp(name = "overlayColor")
    public void setOverlayColor(MainView view, String rgbaColor) {
        view.setOverlayColor(rgbaColor);
    }

    @ReactProp(name = "detectionCountBeforeCapture", defaultInt = 15)
    public void setDetectionCountBeforeCapture(MainView view, int numberOfRectangles) {
        view.setDetectionCountBeforeCapture(numberOfRectangles);
    }

    @ReactProp(name = "enableTorch", defaultBoolean = false)
    public void setEnableTorch(MainView view, Boolean enable) {
        view.setEnableTorch(enable);
    }

    @ReactProp(name = "manualOnly", defaultBoolean = false)
    public void setManualOnly(MainView view, Boolean manualOnly) {
        view.setManualOnly(manualOnly);
    }

    @ReactProp(name = "brightness", defaultDouble = 10)
    public void setBrightness(MainView view, double brightness) {
        view.setBrightness(brightness);
    }

    @ReactProp(name = "contrast", defaultDouble = 1)
    public void setContrast(MainView view, double contrast) {
        view.setContrast(contrast);
    }

    @ReactProp(name = "noGrayScale", defaultBoolean = false)
    public void setRemoveGrayScale(MainView view, boolean bw) {
        view.setRemoveGrayScale(bw);
    }

}
