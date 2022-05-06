package uk.ac.tees.aad.b1143506;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import uk.ac.tees.aad.b1143506.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapClickListener {

    private GoogleMap mMap;

    Marker marker;
    LatLng touchCoordinates;
    Button chooseLocation;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        chooseLocation = findViewById(R.id.saveLocationButton);


        chooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude = touchCoordinates.latitude;
                double longitude = touchCoordinates.longitude;
                Geocoder geocoder = new Geocoder(getApplicationContext());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    Log.d("Maps: ","could not find location based on coordinates.");
                    e.printStackTrace();
                }
                String address = addresses.get(0).getLocality();
                if(address==""){
                    address+="_";
                }
                Log.d("the chosen location: ","="+address);
                MainActivity.chosenCustomLocation = address;
                finish();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Maps: ","maps activity is completed properly.");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setOnMapClickListener(this);

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("touch coordinate from the map: ", latLng.toString());
        touchCoordinates = latLng;
        try {
            if (marker != null) {
                marker.remove();
            }
            marker = mMap.addMarker(new MarkerOptions().position(touchCoordinates).title("location ???"));
            mMap.setMaxZoomPreference(10);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(touchCoordinates, 10.0f));
        }catch(Exception e){
            Log.e("maps marker: ","marker problem");
        }
    }
}