package com.safwat.abanoub.photoweather.main;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiClient {
    //@GET(".") mean that the base URL is the full path

    //find?lat=currentLocation.getLatitude()&lon=currentLocation.getLongitude()&cnt=7&appid=e2a184cdb6b3cd8caf38d34f8401425f
    @GET("find")
    Call<ServerResponse> getDataFromServer(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("cnt") int count,
            @Query("appid") String api_key);// Url remaining path

    //Use Call<List<>> when you have more than one json object on the server
//    @GET(".") // Url remaining path
//    Call<List<LoginResponse>> getDataFromServer();
}
