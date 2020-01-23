import * as React from "react";

declare module "react-native-document-scanner" {

    interface ScannerProps {
        useBase64?: boolean;
        captureMultiple?: boolean;
        onPictureTaken?: (data: any) => void;
        onRectangleDetect?: (data: {stableCounter: number, lastDetectionType: number}) => void;
        overlayColor?: number|string;
        enableTorch?: boolean;
        useFrontCam?: boolean;
        saturation?: number;
        brightness?: number;
        contrast?: number;
        detectionCountBeforeCapture?: number;
        detectionRefreshRateInMS?: number;
        quality?: number;
        style?: any;
    }

    export default class Scanner extends React.Component<ScannerProps, any> { }
}
