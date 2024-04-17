package com.example.trailx;

import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


    public class RegisterActivity extends AppCompatActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        private LatLng originLatLng;
        private LatLng destinationLatLng;
        private Polyline polyline;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);

            String apiKey = getString(R.string.google_maps_key);
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), apiKey);
            }

            AutocompleteSupportFragment autocompleteOriginFragment = (AutocompleteSupportFragment)
                    getSupportFragmentManager().findFragmentById(R.id.autocomplete_origin);
            AutocompleteSupportFragment autocompleteDestinationFragment = (AutocompleteSupportFragment)
                    getSupportFragmentManager().findFragmentById(R.id.autocomplete_destination);

            autocompleteOriginFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteOriginFragment.setTypeFilter(TypeFilter.ADDRESS);
            autocompleteDestinationFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteDestinationFragment.setTypeFilter(TypeFilter.ADDRESS);

            autocompleteOriginFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    originLatLng = place.getLatLng();
                    updateMap();
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    Toast.makeText(RegisterActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            autocompleteDestinationFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    destinationLatLng = place.getLatLng();
                    updateMap();
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    Toast.makeText(RegisterActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_fragment);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
            findViewById(R.id.btnRegistrarTrilha).setOnClickListener(view -> registerTrail());
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
        }

        private void updateMap() {
            mMap.clear();
            if (polyline != null) {
                polyline.remove();
            }

            if (originLatLng != null) {
                mMap.addMarker(new MarkerOptions().position(originLatLng).title("Origem").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
            if (destinationLatLng != null) {
                mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destino").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }

            // Draw route between origin and destination
            if (originLatLng != null && destinationLatLng != null) {

                List<LatLng> routePoints = getRoutePoints(originLatLng, destinationLatLng);
                if (routePoints != null) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.addAll(routePoints);
                    polylineOptions.width(10);
                    polylineOptions.color(android.graphics.Color.RED);
                    polyline = mMap.addPolyline(polylineOptions);
                }
            }

            if (originLatLng != null && destinationLatLng != null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(originLatLng);
                builder.include(destinationLatLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            }
        }
        private void registerTrail() {
            if (originLatLng != null && destinationLatLng != null) {

                Toast.makeText(this, "Trilha registrada com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Por favor, selecione tanto a origem quanto o destino", Toast.LENGTH_SHORT).show();
            }
        }

        private List<LatLng> getRoutePoints(LatLng origin, LatLng destination) {

            List<LatLng> routePoints = new ArrayList<>();
            routePoints.add(origin);
            routePoints.add(new LatLng((origin.latitude + destination.latitude) / 2, (origin.longitude + destination.longitude) / 2));
            routePoints.add(destination);
            return routePoints;
        }
}