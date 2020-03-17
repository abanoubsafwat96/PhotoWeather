package com.safwat.abanoub.photoweather.main;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ServerResponseList {

    @SerializedName("name")
    String name;

    @SerializedName("main")
    ServerResponseMain main;

    @SerializedName("weather")
    ArrayList<ServerResponseWeather> weather;

    public String getName() {
        return name;
    }

    public ServerResponseMain getMain() {
        return main;
    }

    public ArrayList<ServerResponseWeather> getWeather() {
        return weather;
    }
}
