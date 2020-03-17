package com.safwat.abanoub.photoweather.main;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainPresenter {
    MainContract.View view;

    private Camera mCamera;
    public CameraPreview mPreview;
    private int correctCameraOrientation;
    Camera.PictureCallback mPicture;

    public static final int MEDIA_TYPE_IMAGE = 1;

    private File pictureFile;
    private Bitmap realImage;

    private FusedLocationProviderClient fusedLocationClient;

    Retrofit.Builder builder;
    private ServerResponse serverResponse;
    private String weatherData;
    private Bitmap realImageWithWeatherData;
    private Uri savedImageUri;

    private DroidNet mDroidNet;

    public MainPresenter(MainContract.View view) {
        this.view = view;
    }

    public void setupInternetStateChangeListener(DroidListener droidListener) {
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(droidListener);
    }

    public void onCaputre_fabPressed() {
        if (mCamera != null && mPicture != null) {
            try {
                mCamera.takePicture(null, null, mPicture); // get an image from the camera
            } catch (Exception e) {
                view.printException(e);
            }
        }
    }

    public void onCancel_fabPressed() {
        view.hidePhotoViewerAndShowCamera();
    }

    public void onAddWeatherData_fabPressed(Context context) {
        if (isNetworkAvailable(context)) {
            if (realImage != null && weatherData != null) {

                realImageWithWeatherData = writeTextOnDrawable(context, realImage, weatherData);

                view.onAddWeatherDataSuccessed(realImageWithWeatherData);
            }
        } else {
            view.errorCheckNetworkConnection();
        }
    }

    public void onDone_fabPressed() {
        if (pictureFile != null && realImage != null) {

            if (isExternalStorageWritable()) {

                saveImageToFile();
            }
        }
    }

    public void onShare_fabPressed() {
        if (savedImageUri != null)
            view.shareImageUri(savedImageUri);
        else
            view.errorPhotoNotSaved();
    }

    public void onStart(Activity activity) {
        //Setup camera in onStart() to get camera back if released in any other app
        if (checkCameraHardware(activity)) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                view.requestPermissionsIfNotGranted();
            }
            setupCamera(activity);
        } else
            view.errorDeviceNotSupportCamera();
    }

    public void onPictureTaken() {

        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d("Errorcreatingmediafile", "check storage permissions");
                    return;
                }

                realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                //rotate image with same camera angle
                realImage = rotate(realImage, correctCameraOrientation);

                view.setBitmapToImageView(realImage);
                view.hideCameraAndShowPhotoViewer();
            }
        };
    }

    /* Check if this device has a camera */
    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void setupCamera(Activity activity) {
        // Create an instance of Camera
        mCamera = getCameraInstance();

        if (mCamera != null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

            correctCameraOrientation = getCorrectCameraOrientation(activity, info);
            mCamera.setDisplayOrientation(correctCameraOrientation);
            mCamera.getParameters().setRotation(correctCameraOrientation);

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(activity, mCamera);

            view.addViewToCameraFrameLayout(mPreview);
        }
    }

    //A safe way to get an instance of the Camera object.
    public static Camera getCameraInstance() {
        Camera camera = null;

        try {
            camera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return camera; // returns null if camera is unavailable
    }

    public int getCorrectCameraOrientation(Activity activity, Camera.CameraInfo info) {

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
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

        return result;
    }

    //Create a File for saving an image
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PhotoWeatherApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("PhotoWeatherApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public void getCurrentLocation(Activity activity) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location currentLocation) {
                        // Got last known location. In some rare situations this can be null.
                        if (currentLocation != null) {

                            setupRetrofit(currentLocation);
                        }
                    }
                });
    }

    private void setupRetrofit(Location currentLocation) {

        builder = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        // Create a very simple REST adapter which points the GitHub API endpoint.
        ApiClient client = retrofit.create(ApiClient.class);

        // Execute the call asynchronously. Get a positive or negative callback.
        client.getDataFromServer(currentLocation.getLatitude(), currentLocation.getLongitude()
                , 1, "e2a184cdb6b3cd8caf38d34f8401425f")
                .enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        Log.e("onResponse: ", "success");

                        serverResponse = response.body();

                        if (serverResponse != null) {
                            ServerResponseList serverResponseList = serverResponse.getResult().get(0);
                            weatherData = serverResponseList.name + " , " + Math.round((Double.parseDouble(
                                    serverResponseList.main.temp)) - 273.15) + "°C" + " , "
                                    + serverResponseList.weather.get(0).main;

                        } else
                            Log.e("onResponse: ", "weather data = null");
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        Log.e("onResponse: ", t.getMessage());

                    }
                });
    }

    private Bitmap writeTextOnDrawable(Context context, Bitmap bitmap, String text) {

        Typeface typeface = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.YELLOW);
        paint.setTypeface(typeface);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(context, 45));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        if (!bitmap.isMutable()) {
            bitmap = convertToMutable(bitmap);
        }
        Canvas canvas = new Canvas(bitmap);

        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = canvas.getWidth() / 2;

        int yPos = (int) (canvas.getHeight() * 0.1);
        canvas.drawText(text, xPos, yPos, paint);

        return bitmap;
    }

    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void saveImageToFile() {
        if (pictureFile != null) {
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);

                if (view.isAddWeatherData_fabVisible()) {

                    if (realImage != null)
                        realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                } else {
                    if (realImageWithWeatherData != null) {
                        realImageWithWeatherData.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    } else
                        return;
                }
                savedImageUri = view.onSavePhotoSuccessfully(pictureFile);
                fos.close();
                fos = null;
                realImage = null;

            } catch (FileNotFoundException e) {
                Log.d("Info", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
            }
        }
    }

    /**
     * Converts a immutable bitmap to a mutable bitmap. This operation doesn't allocates
     * more memory that there is already allocated.
     *
     * @param imgIn - Source image. It will be released, and should not be used more
     * @return a copy of imgIn, but muttable.
     */
    public static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes() * height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setRealImageAndSavedImageUriWithNull() {

        realImage = null;
        savedImageUri = null;
    }

    public void onLowMemory() {
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();
    }

    public void onStop() {
//        if (mCamera != null) {
//            mCamera.release();        // release the camera for other applications
//        }
    }

    public void onDestroy(DroidListener droidListener) {
        mCamera = null;
        realImage = null;
        realImageWithWeatherData = null;
        mDroidNet.removeInternetConnectivityChangeListener(droidListener);
    }
}
