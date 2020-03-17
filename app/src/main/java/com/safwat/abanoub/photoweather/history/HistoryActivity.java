package com.safwat.abanoub.photoweather.history;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.safwat.abanoub.photoweather.R;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity implements HistoryContract.View, Communicator {

    @BindView(R.id.noData)
    TextView noData;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    RecyclerView.LayoutManager layoutManager;
    HistoryAdapter adapter;
    private ViewImageFragment viewImageFragment;

    private HistoryPresenter historyPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ButterKnife.bind(this);

        requestPermissionsIfNotGranted();

        historyPresenter = new HistoryPresenter(this);

        ArrayList<String> history_list = historyPresenter.getDataFromFile();

        setupRecyclerView(history_list);
    }

    private void setupRecyclerView(ArrayList<String> history_list) {
        if (history_list.size() == 0)
            noData.setVisibility(View.VISIBLE);
        else
            noData.setVisibility(View.GONE);

        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new HistoryAdapter(this, history_list);
        adapter.setCommunicator(this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void setItemClicked(String history_item) {

        viewImageFragment = new ViewImageFragment();
        Bundle bundle = new Bundle();
        bundle.putString("image_path", history_item);
        viewImageFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, viewImageFragment, "viewImageFragment").commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (permissions[0].equals(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // user rejected the permission

                requestPermissionsIfNotGranted();

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //user allowed the permission

                ArrayList<String> history_list = historyPresenter.getDataFromFile();

                setupRecyclerView(history_list);
            }
        }
    }

    private void requestPermissionsIfNotGranted() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Define Needed Permissions for android Marshmallow and higher
            // The request code used in ActivityCompat.requestPermissions()
            // and returned in the Activity's onRequestPermissionsResult()
            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
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
    public void onBackPressed() {

        if (viewImageFragment == null)
            super.onBackPressed();
        else {
            getSupportFragmentManager().beginTransaction().remove(viewImageFragment).commit();
            viewImageFragment = null;
        }
    }

    @Override
    public void errorAllowStoragePermission() {
        Toast.makeText(this, "Allow reading storage permission to be worked", Toast.LENGTH_LONG).show();
    }
}