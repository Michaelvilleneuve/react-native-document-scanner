package com.documentscanner.helpers;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

/**
 * Created by allgood on 05/03/16.
 */
public class ScannedDocument {

    public Mat original;
    public Mat processed;
    public Quadrilateral quadrilateral;
    public Point[] previewPoints;
    public Size previewSize;
    public Size originalSize;

    public Point[] originalPoints;

    public int heightWithRatio;
    public int widthWithRatio;

    public ScannedDocument(Mat original) {
        this.original = original;
    }

    public Mat getProcessed() {
        return processed;
    }

    public ScannedDocument setProcessed(Mat processed) {
        this.processed = processed;
        return this;
    }

    public WritableMap previewPointsAsHash() {
        if (this.previewPoints == null) return null;
        WritableMap rectangleCoordinates = new WritableNativeMap();

        WritableMap topLeft = new WritableNativeMap();
        topLeft.putDouble("x", this.originalPoints[0].x);
        topLeft.putDouble("y", this.originalPoints[0].y);

        WritableMap topRight = new WritableNativeMap();
        topRight.putDouble("x", this.originalPoints[1].x);
        topRight.putDouble("y", this.originalPoints[1].y);

        WritableMap bottomRight = new WritableNativeMap();
        bottomRight.putDouble("x", this.originalPoints[2].x);
        bottomRight.putDouble("y", this.originalPoints[2].y);

        WritableMap bottomLeft = new WritableNativeMap();
        bottomLeft.putDouble("x", this.originalPoints[3].x);
        bottomLeft.putDouble("y", this.originalPoints[3].y);



        rectangleCoordinates.putMap("topLeft", topLeft);
        rectangleCoordinates.putMap("topRight", topRight);
        rectangleCoordinates.putMap("bottomRight", bottomRight);
        rectangleCoordinates.putMap("bottomLeft", bottomLeft);

        return rectangleCoordinates;
    }

    public void release() {
        if (processed != null) {
            processed.release();
        }
        if (original != null) {
            original.release();
        }

        if (quadrilateral != null && quadrilateral.contour != null) {
            quadrilateral.contour.release();
        }
    }
}
