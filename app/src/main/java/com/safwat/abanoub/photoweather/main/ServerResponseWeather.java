package com.safwat.abanoub.photoweather.main;

import com.google.gson.annotations.SerializedName;

class ServerResponseWeather {

    @SerializedName("main")
    String main;

    public String getMain() {
        return main;
    }
}