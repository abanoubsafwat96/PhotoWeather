package com.safwat.abanoub.photoweather.main;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ServerResponse {

    @SerializedName("list")
    private ArrayList<ServerResponseList> mResult;

    public ArrayList<ServerResponseList> getResult() {
        return mResult;
    }
}