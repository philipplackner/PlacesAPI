package com.androiddevs.mapsapi;

import com.androiddevs.mapsapi.places.Location;
import com.androiddevs.mapsapi.places.PlacesResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesAPI {

    @GET("/maps/api/place/nearbysearch/json")
    Call<PlacesResponse> getNearbyPlaces(
            @Query("location") Location location,
            @Query("radius") double radius,
            @Query("type") String type,
            @Query("key") String apiKey
    );
}
