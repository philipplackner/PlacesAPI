package com.androiddevs.mapsapi;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.androiddevs.mapsapi.places.Location;
import com.androiddevs.mapsapi.places.PlacesResponse;
import com.androiddevs.mapsapi.places.Result;
import com.androiddevs.mapsapi.places.Viewport;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private static double SEARCH_RADIUS = 3000.0;

    private GoogleMap googleMap;
    private static final String API_KEY = "INSERT_YOUR_API_KEY";

    EditText etLocation;
    EditText etTypes;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        etLocation = findViewById(R.id.etLocation);
        etTypes = findViewById(R.id.etTypes);
        btnSearch = findViewById(R.id.btnSearch);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = getLocationFromAddress(etLocation.getText().toString());
                String type = etTypes.getText().toString();
                googleMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(location.getLat(), location.getLng()), 12.0f));

                fetchPlacesResponse(location, type);
            }
        });
    }

    public Location getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> addresses;

        try {
            addresses = coder.getFromLocationName(strAddress, 1);
            if (addresses == null) {
                return null;
            }

            Address addressLocation = addresses.get(0);
            LatLng locationInLatLng = new LatLng(addressLocation.getLatitude(), addressLocation.getLongitude());
            Location location = new Location();
            location.setLat(locationInLatLng.latitude);
            location.setLng(locationInLatLng.longitude);
            return location;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void fetchPlacesResponse(Location location, String types) {
        Retrofit retrofit = RetrofitClient.getRetrofitClient();
        PlacesAPI placesAPI = retrofit.create(PlacesAPI.class);
        Call<PlacesResponse> call = placesAPI.getNearbyPlaces(location, SEARCH_RADIUS, types, API_KEY);

        call.enqueue(new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                if(response.body() != null) {
                    PlacesResponse placesResponse = response.body();
                    showPlacesOnMap(placesResponse);
                }
            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "The request failed", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void showPlacesOnMap(PlacesResponse response) {
        List<Result> results = response.getResults();
        Log.d(TAG, "showPlacesOnMap: RESULT SIZE: " + results.size());
        for(Result result : results) {
            Location location = result.getGeometry().getLocation();
            LatLng locationInLatLng = new LatLng(location.getLat(), location.getLng());
            String name = result.getName();
            Log.d(TAG, "showPlacesOnMap: " + result.getName());
            googleMap.addMarker(new MarkerOptions().position(locationInLatLng).title(name));
        }
    }

}
