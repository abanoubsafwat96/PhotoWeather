package com.safwat.abanoub.photoweather.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.safwat.abanoub.photoweather.R;
import com.safwat.abanoub.photoweather.history.HistoryActivity;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainContract.View, DroidListener {

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.camera_preview)
    FrameLayout camera_frameLayout;
    @BindView(R.id.image_relative)
    RelativeLayout image_relative;
    @BindView(R.id.button_capture)
    FloatingActionButton capture_fab;
    @BindView(R.id.cancel)
    FloatingActionButton cancel_fab;
    @BindView(R.id.addWeatherData)
    FloatingActionButton addWeatherData_fab;
    @BindView(R.id.done)
    FloatingActionButton done_fab;
    @BindView(R.id.share)
    FloatingActionButton share_fab;


    MainPresenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        DroidNet.init(this);

        mainPresenter = new MainPresenter(this);

        mainPresenter.setupInternetStateChangeListener(this);

        mainPresenter.getCurrentLocation(this);

        mainPresenter.onPictureTaken();

        capture_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainPresenter.onCaputre_fabPressed();
            }
        });

        cancel_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainPresenter.onCancel_fabPressed();
            }
        });

        addWeatherData_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainPresenter.onAddWeatherData_fabPressed(MainActivity.this);
            }
        });

        done_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainPresenter.onDone_fabPressed();
            }
        });

        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainPresenter.onShare_fabPressed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mainPresenter.onStart(this);
    }

    @Override
    public void errorDeviceNotSupportCamera() {
        Toast.makeText(this, "Device doesn't support camera", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addViewToCameraFrameLayout(CameraPreview mPreview) {
        camera_frameLayout.addView(mPreview);
    }

    @Override
    public boolean isAddWeatherData_fabVisible() {
        if (addWeatherData_fab.getVisibility() == View.VISIBLE)
            return true;
        else
            return false;
    }

    @Override
    public void onAddWeatherDataSuccessed(Bitmap realImageWithWeatherData) {

        imageView.setImageBitmap(realImageWithWeatherData);
        addWeatherData_fab.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setBitmapToImageView(Bitmap realImage) {
        imageView.setImageBitmap(realImage);
    }

    @Override
    public Uri onSavePhotoSuccessfully(File pictureFile) {
        Toast.makeText(MainActivity.this, "Photo saved successfully", Toast.LENGTH_SHORT).show();

        done_fab.setVisibility(View.INVISIBLE);
        addWeatherData_fab.setVisibility(View.INVISIBLE);

        return FileProvider.getUriForFile(
                this,
                "com.safwat.abanoub.photoweather.provider", //(use your app signature + ".provider" )
                pictureFile);
    }

    @Override
    public void shareImageUri(Uri uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/jpg");
        startActivity(intent);
    }

    @Override
    public void hideCameraAndShowPhotoViewer() {
        camera_frameLayout.setVisibility(View.GONE);
        capture_fab.setVisibility(View.GONE);
        image_relative.setVisibility(View.VISIBLE);

        if (done_fab.getVisibility() == View.INVISIBLE)
            done_fab.setVisibility(View.VISIBLE);
        if (addWeatherData_fab.getVisibility() == View.INVISIBLE)
            addWeatherData_fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePhotoViewerAndShowCamera() {
        image_relative.setVisibility(View.GONE);
        camera_frameLayout.setVisibility(View.VISIBLE);
        capture_fab.setVisibility(View.VISIBLE);

        if (mainPresenter.mPreview != null) {
            camera_frameLayout.removeView(mainPresenter.mPreview);
            camera_frameLayout.addView(mainPresenter.mPreview);
        }
        mainPresenter.setRealImageAndSavedImageUriWithNull();
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {

        if (isConnected) {
            mainPresenter.getCurrentLocation(this);
        }
    }

    @Override
    public void errorCheckNetworkConnection() {
        Toast.makeText(MainActivity.this, "Check your network connection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void errorPhotoNotSaved() {
        Toast.makeText(MainActivity.this, "You must save photo first", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void printException(Exception e) {
        Toast.makeText(MainActivity.this, e + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(PERMISSIONS[i])) {

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {// user rejected the permission

                    requestPermissionsIfNotGranted();

                } else if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {//user allowed the permission

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                        mainPresenter.setupCamera(MainActivity.this);

                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED)
                        mainPresenter.getCurrentLocation(this);
                }
            }
        }
    }

    @Override
    public void requestPermissionsIfNotGranted() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Define Needed Permissions for android Marshmallow and higher
            // The request code used in ActivityCompat.requestPermissions()
            // and returned in the Activity's onRequestPermissionsResult()
            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {
                    Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mainPresenter.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        if (image_relative.getVisibility() == View.VISIBLE) {
            hidePhotoViewerAndShowCamera();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mainPresenter.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mainPresenter.onDestroy(this);
    }
}