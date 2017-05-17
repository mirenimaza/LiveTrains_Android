package android.trains;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private BottomSheetBehavior mBottomSheetBehavior1;
    LocationManager locationManager;
    Marker userMarker;
    double userLatitude, userLongitude;
    int userID = 0;
    JSONObject[] stops = new JSONObject[600];
    String activeStopName;
    String activeStopID;
    String urlAddress = "https://still-reef-32346.herokuapp.com";
    Spinner spinnerStops;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        android.view.View bottomSheet = findViewById(R.id.bottom_sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);

        SharedPreferences sharedPref = this.getSharedPreferences("userPref", Context.MODE_PRIVATE);
        if (!sharedPref.contains("userID")) {
            Random generator = new Random();
            int generatedID = generator.nextInt(2000000000);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("userID", generatedID);
            editor.commit();
            userID = generatedID;
        } else {
            userID = sharedPref.getInt("userID", 0);
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                    LatLng user = new LatLng(userLatitude, userLongitude);
                    userMarker.setPosition(user);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(user, 15);
                    mMap.animateCamera(update);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                    LatLng user = new LatLng(userLatitude, userLongitude);
                    userMarker.setPosition(user);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(user, 15);
                    mMap.animateCamera(update);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }


        if (!sharedPref.contains("allTrams")) {
            //request do serwera po wszystkie przystanki
           // int allStopsID;
            // wpisz do tablicy
            // zapisz w pliku
            try {
                JSONArray array = (JSONArray) new JSONParser().parse(getResources().getString(R.string.allStops));
                for (int n = 0; n < array.size(); n++) {
                    JSONObject object = (JSONObject) array.get(n);
                    stops[n] = object;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            //request do serwera czy aktualny allStopsID
            //jeśli aktualne wczytaj z pliku do tablicy

            //jeśli nieaktualne, request do serwera po wszystkie przystanki itd.
        }





     /*   spinnerStops = (Spinner)findViewById(R.id.spinnerTranStop);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.stops, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStops.setAdapter(adapter);*/
    }


    /**
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng user = new LatLng(userLatitude, userLongitude);
        userMarker = mMap.addMarker(new MarkerOptions().position(user).title(String.valueOf(userID))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 15));

        for (JSONObject stop : stops) {
            try {
                if (stop != null) {
                    double latitude = Double.parseDouble((String) stop.get("lat"));
                    double longitude = Double.parseDouble((String) stop.get("lon"));
                    String name = (String) stop.get("name");
                    String direction = (String) stop.get("direction");
                    int StopID = Integer.parseInt((String) stop.get("stop_id"));
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.user)).zIndex(StopID).snippet(("(kierunek: " + direction + ")")));

                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        mMap.setOnMarkerClickListener(this);
    }

    /**
     * Called when the user clicks a marker.
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
         marker.showInfoWindow();
            mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);

        TextView TVname = (TextView) findViewById(R.id.stopName);
        TVname.setText(marker.getTitle() + " ");
        activeStopName = marker.getTitle();
        TextView TVdirection = (TextView) findViewById(R.id.stopDirection);
        TVdirection.setText(marker.getSnippet());
        LinearLayout LU = (LinearLayout) findViewById(R.id.linear_up);
        LU.removeAllViews();
        LinearLayout LD = (LinearLayout) findViewById(R.id.linear_down);
        LD.removeAllViews();

            try {
                int stopIDint = (int)marker.getZIndex();
                String stopID = String.valueOf(stopIDint);
                if (stopIDint < 1000)
                {
                    stopID = "R-0" + stopID;
                }
                activeStopID = stopID;
                JSONTask task = new JSONTask(urlAddress + "/stop/"+ stopID +"/lines");
                task.execute();
                String jsonString = task.jsonResult;
                JSONTask task2 = new JSONTask(urlAddress + "/stop/"+ stopID +"/lines/info");
                task2.execute();
                String jsonString2 = task2.jsonResult;

                while (jsonString == null || jsonString2 == null)
                {
                    jsonString = task.jsonResult;
                    jsonString2 = task2.jsonResult;
                }
                task.cancel(true);
                task2.cancel(true);
                JSONArray array = (JSONArray) new JSONParser().parse(jsonString);
                JSONArray array2 = (JSONArray) new JSONParser().parse(jsonString2);
                for (int n = 0; n < array.size(); n++) {
                    JSONObject object = (JSONObject) array.get(n);
                    Button button = new Button(this);
                    button.setText((String)object.get("lines"));
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            Button b = (Button)view;
                            String buttonText = b.getText().toString();
                            openTimetable(buttonText, activeStopID, activeStopName);
                        }
                    });
                    LU.addView(button);

                }
                for (int n = 0; n < array2.size(); n++) {
                    JSONObject object = (JSONObject) array2.get(n);
                    TextView text = new TextView(this);
                    if (object.get("time")!= null)
                    {
                        String textString = (String)object.get("time");
                        String[] parts = textString.split(":");
                        text.setText(parts[0]+ ":" + parts[1]);
                        text.setPadding(50, 5, 30, 5);
                    }
                    else
                    {
                        text.setText("brak");
                        text.setPadding(50, 5, 40, 5);
                    }

                    LD.addView(text);

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        return true;
    }
    public void openTimetable(String line, String stopID, String stopName)
    {
        Intent myIntent = new Intent(this, TimetableActivity.class);
        myIntent.putExtra("line", line); //Optional parameters
        myIntent.putExtra("stopID", stopID); //Optional parameters
        myIntent.putExtra("stopName", stopName); //Optional parameters
        startActivity(myIntent);
    }

}
