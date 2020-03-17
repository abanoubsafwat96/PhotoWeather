package com.safwat.abanoub.photoweather.history;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class HistoryPresenter {

    HistoryContract.View view;

    public HistoryPresenter(HistoryContract.View view) {
        this.view = view;
    }

    public ArrayList<String> getDataFromFile() {
        ArrayList<String> history_list = new ArrayList<>();

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PhotoWeatherApp");
        File[] files = mediaStorageDir.listFiles();

        if (files == null)
            view.errorAllowStoragePermission();
        else {
            for (int i = 0; i < files.length; i++) {
                history_list.add(files[i].getPath());
            }
        }
        return history_list;
    }
}
