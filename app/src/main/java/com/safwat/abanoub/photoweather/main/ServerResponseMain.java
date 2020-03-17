package com.safwat.abanoub.photoweather.main;

import com.google.gson.annotations.SerializedName;

class ServerResponseMain {

    @SerializedName("temp")
    String temp;

    public String getTemp() {
        return temp;
    }
}
