package com.example.trailx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;


public class DoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextView speedTextView, timerTextView, distanceTextView;
    private long startTime;
    private Handler timerHandler = new Handler();
    private float totalDistance = 0;
    private Location lastLocation;
    private SQLiteHelper dbHelper;
    private LatLng destinationLocation;
    private Marker currentMarker;
    private Polyline routePolyline;
    private boolean isTrackingStarted = false;
    private final DecimalFormat decimalFormat;
    public DoActivity() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("0.000", symbols);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBOVKqi9IqjwG3tTUzcnoBObMORzY0-HeU");
        }

        speedTextView = findViewById(R.id.speed_text);
        timerTextView = findViewById(R.id.timer_text);
        distanceTextView = findViewById(R.id.distance_text);
        Button btnRegistrarTrilha = findViewById(R.id.btnRegistrarTrilha);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dbHelper = new SQLiteHelper(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (isTrackingStarted) {
                        updateUI(location);
                    }
                }
            }
        };

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_destination);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    destinationLocation = place.getLatLng();
                    if (destinationLocation != null) {
                        mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Destino"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
                        drawRouteToDestination();
                        startTracking();
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(DoActivity.this, "Erro ao selecionar local: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            autocompleteFragment.getView().setOnTouchListener((v, event) -> true);
        } else {
            Log.e("DoActivity", "Autocomplete fragment is null");
        }

        btnRegistrarTrilha.setOnClickListener(v -> {
            if (destinationLocation != null) {
                long endTime = System.currentTimeMillis();
                dbHelper.registerTrail(startTime, endTime, totalDistance, destinationLocation.latitude, destinationLocation.longitude);
                Toast.makeText(DoActivity.this, "Trilha registrada com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(DoActivity.this, "Por favor, selecione um destino.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            currentMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Localização Atual"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            lastLocation = location; // Set the last location
                        } else {
                            Toast.makeText(DoActivity.this, "Não foi possível obter a localização", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateUI(Location location) {
        if (location != null) {
            if (lastLocation != null) {
                float distance = lastLocation.distanceTo(location);
                totalDistance += distance;
                distanceTextView.setText("Distância: " + decimalFormat.format(totalDistance) + " metros");
            }
            lastLocation = location;

            float speed = location.getSpeed(); // m/s
            speedTextView.setText("Velocidade: " + decimalFormat.format(speed) + " m/s");
        }
    }

    private void drawRouteToDestination() {
        if (lastLocation != null && destinationLocation != null) {
            LatLng origin = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            String url = getDirectionsUrl(origin, destinationLocation);

            new Thread(() -> {
                try {
                    URL urlObject = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
                    connection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    String response = sb.toString();
                    List<LatLng> points = parseDirections(response);

                    runOnUiThread(() -> {
                        if (routePolyline != null) {
                            routePolyline.remove();
                        }
                        routePolyline = mMap.addPolyline(new PolylineOptions().addAll(points).color(ContextCompat.getColor(DoActivity.this, R.color.blue)));

                        simulateMarkerMovement(points);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=walking";
        String key = "key=AIzaSyBOVKqi9IqjwG3tTUzcnoBObMORzY0-HeU";
        String parameters = strOrigin + "&" + strDest + "&" + sensor + "&" + mode + "&" + key;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private List<LatLng> parseDirections(String jsonData) {
        List<LatLng> points = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray routes = jsonObject.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedString = overviewPolyline.getString("points");
            points = decodePoly(encodedString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return points;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((lat / 1E5)), ((lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void simulateMarkerMovement(List<LatLng> points) {
        if (currentMarker != null) {
            Handler handler = new Handler();
            final int[] index = {0};

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (index[0] < points.size()) {
                        currentMarker.setPosition(points.get(index[0]));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(index[0])));
                        index[0]++;
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.post(runnable);
        }
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTrackingStarted) {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                timerTextView.setText(String.format("Duração: %02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    private void startTracking() {
        isTrackingStarted = true;
        startTime = System.currentTimeMillis();
        totalDistance = 0;
        lastLocation = null;
        timerHandler.post(timerRunnable);
        requestLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        }
    }
}