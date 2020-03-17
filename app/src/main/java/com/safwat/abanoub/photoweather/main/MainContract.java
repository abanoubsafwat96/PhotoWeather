package com.safwat.abanoub.photoweather.main;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

public interface MainContract {
    interface View {

        void errorPhotoNotSaved();

        void printException(Exception e);

        void errorCheckNetworkConnection();

        void hideCameraAndShowPhotoViewer();

        void setBitmapToImageView(Bitmap realImage);

        void hidePhotoViewerAndShowCamera();

        void requestPermissionsIfNotGranted();

        void errorDeviceNotSupportCamera();


        void addViewToCameraFrameLayout(CameraPreview mPreview);

        void shareImageUri(Uri savedImageUri);

        boolean isAddWeatherData_fabVisible();

        Uri onSavePhotoSuccessfully(File pictureFile);

        void onAddWeatherDataSuccessed(Bitmap realImageWithWeatherData);
    }
}
