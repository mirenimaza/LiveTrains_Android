package android.trains;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private BottomSheetBehavior mBottomSheetBehavior1;
    LocationManager locationManager;
    Marker userMarker;
    double userLatitude = 52.22986699;
    double userLongitude = 21.01170835;
    int userID = 0;
    String activeStopName;
    String activeStopID;
    String urlAddress = "https://still-reef-32346.herokuapp.com";
    JSONTask trainsTask;
    int trainsPictureInt[] = {R.drawable.train1, R.drawable.train2, R.drawable.train3, R.drawable.train4, R.drawable.train6, R.drawable.train7, R.drawable.train9, R.drawable.train10, R.drawable.train11, R.drawable.train13, R.drawable.train14, R.drawable.train15, R.drawable.train17, R.drawable.train18, R.drawable.train20, R.drawable.train22, R.drawable.train23, R.drawable.train24, R.drawable.train25, R.drawable.train26, R.drawable.train27, R.drawable.train28, R.drawable.train31, R.drawable.train33, R.drawable.train35, R.drawable.train44};
    Integer trainsNumber[] = {1, 2, 3, 4, 6, 7, 9, 10, 11, 13, 14, 15, 17, 18, 20, 22, 23, 24, 25, 26, 27, 28, 31, 33, 35, 44};
    ArrayList<Marker> trainMarkers = new ArrayList<>();
    ArrayList<Marker> stopMarkers = new ArrayList<>();
    Marker oldMarker = null;
    int oldStopID = 0;
    ArrayAdapter<String> adapter;
    private ArrayList<JSONObject> stopList;
    private ArrayList<String> stopListString;
    AutoCompleteTextView editText;
    RadioGroup radioGroup;
    RadioButton rButton;
    EditText mEdit;
    int selectedSourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        stopList = new ArrayList<>();
        stopListString = new ArrayList<>();
        editText = (AutoCompleteTextView) findViewById(R.id.etClinicName);

        View bottomSheet = findViewById(R.id.bottom_sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior1.setPeekHeight(55);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);

        Button bn = (Button) findViewById(R.id.refreshButton);
        bn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshMap();
            }
        });

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
            // to handle the case where the user grants the permission.
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                    if (activeStopID == null) {
                        LatLng user = new LatLng(userLatitude, userLongitude);
                        userMarker.setPosition(user);
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(user, 15);
                        mMap.animateCamera(update);
                    }
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
                    if (activeStopID == null) {
                        LatLng user = new LatLng(userLatitude, userLongitude);
                        userMarker.setPosition(user);
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(user, 15);
                        mMap.animateCamera(update);
                    }

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


        JSONArray array = null;
        try {
            array = (JSONArray) new JSONParser().parse(getResources().getString(R.string.allStops));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int n = 0; n < array.size(); n++) {
            JSONObject object = (JSONObject) array.get(n);
            stopList.add(object);
            stopListString.add(object.get("name") + "\n(kierunek: " + object.get("direction") + ")");
        }

        adapter = new ArrayAdapter<>(this, R.layout.item_view, R.id.nameAndDirection, stopListString);
        editText.setAdapter(adapter);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                MapsActivity.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });


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
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user)).zIndex(userID));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 15));

        for (JSONObject stop : stopList) {
            if (stop != null) {
                double latitude = Double.parseDouble((String) stop.get("lat"));
                double longitude = Double.parseDouble((String) stop.get("lon"));
                String name = (String) stop.get("name");
                String direction = (String) stop.get("direction");
                int StopID = Integer.parseInt((String) stop.get("stop_id"));
                stopMarkers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.far_stop)).zIndex(StopID).snippet(("(kierunek: " + direction + ")"))));

            }
        }
        mMap.setOnMarkerClickListener(this);

    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.settings, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.source:
                        openSourceDialog();
                        return true;
                    default:
                        return false;
                }
            }
        });
        // Handle dismissal with: popup.setOnDismissListener(...);
        // Show the menu
        popup.show();
    }


    public void openSourceDialog()
    {
        AlertDialog.Builder sourceDialog = new AlertDialog.Builder(this);
        sourceDialog.setTitle("Choose source");
        View view = this.getLayoutInflater().inflate(R.layout.dialog_layout, null);
        sourceDialog.setView(view);
        radioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                selectedSourceId = radioGroup.getCheckedRadioButtonId();

            }
        });
        mEdit = (EditText)view.findViewById(R.id.edit_source);


        sourceDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                View view = getLayoutInflater().inflate(R.layout.dialog_layout, null);
                rButton = (RadioButton)view.findViewById(selectedSourceId);
                if(rButton!=null)
                {
                    if (rButton.getText().toString().equals("Heroku")) {
                        urlAddress = "https://still-reef-32346.herokuapp.com";
                    }
                    if (rButton.getText().toString().equals("Write url address")) {
                        urlAddress = mEdit.getText().toString();
                    }
                }
            }
        });

        sourceDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        sourceDialog.show();
    }

    /**
     * Called when the user clicks a marker.
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        findStop(marker);
        return true;
    }

    /**
     * Called when the user writes stop's name. Program has to guest which marker corresponds to the query
     */
    public void findMarker(View v) {
        Marker chosenMarker = null;
        TextView tv = (TextView) v;
        String nameAndDirection = tv.getText().toString();
        String[] table = nameAndDirection.split("\n");
        String name = table[0];
        String direction = table[1];
        for (Marker marker : stopMarkers) {
            if (marker.getTitle().equals(name) && marker.getSnippet().equals(direction)) {
                chosenMarker = marker;
                break;
            }
        }

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        editText.getText().clear();
        editText.clearFocus();
        findStop(chosenMarker);
    }

    /**
     * Locates train's stop, change this stop on red color
     */
    public void findStop(final Marker marker) {

        if (stopMarkers.contains(marker)) {
            marker.showInfoWindow();

            mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
            CameraUpdate update = CameraUpdateFactory.newLatLng(marker.getPosition());
            mMap.animateCamera(update);
            TextView TVname = (TextView) findViewById(R.id.stopName);
            TVname.setText(marker.getTitle() + " ");
            activeStopName = marker.getTitle();
            TextView TVdirection = (TextView) findViewById(R.id.stopDirection);
            TVdirection.setText(marker.getSnippet());
            LinearLayout LU = (LinearLayout) findViewById(R.id.linear_up);
            LU.removeAllViews();
            LinearLayout LD = (LinearLayout) findViewById(R.id.linear_down);
            LD.removeAllViews();

            int stopIDint = (int) marker.getZIndex();
            String stopID = String.valueOf(stopIDint);
            if (stopIDint < 1000) {
                stopID = "R-0" + stopID;
            }
            activeStopID = stopID;

            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.active_stop));
            marker.setZIndex(2000000000);
            if (oldMarker != null && oldStopID != 0) {
                oldMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.far_stop));
                oldMarker.setZIndex(oldStopID);
            }

            oldMarker = marker;
            oldStopID = Integer.parseInt(stopID);

            showTrains();
            JSONTask task = new JSONTask(urlAddress + "/stop/" + stopID + "/lines");
            task.execute();
            String jsonString = task.jsonResult;
            JSONTask task2 = new JSONTask(urlAddress + "/stop/" + stopID + "/lines/info");
            task2.execute();
            String jsonString2 = task2.jsonResult;

            while (jsonString == null || jsonString2 == null) {
                jsonString = task.jsonResult;
                jsonString2 = task2.jsonResult;
            }
            task.cancel(false);
            task2.cancel(false);
            if (jsonString != "error" && jsonString2 != "error") {
                JSONArray array = null;
                JSONArray array2 = null;
                try {
                    array = (JSONArray) new JSONParser().parse(jsonString);
                    array2 = (JSONArray) new JSONParser().parse(jsonString2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                for (int n = 0; n < array.size(); n++) {
                    JSONObject object = (JSONObject) array.get(n);
                    Button button = new Button(this);
                    button.setText((String) object.get("lines"));
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            Button b = (Button) view;
                            String buttonText = b.getText().toString();
                            openTimetable(buttonText, activeStopID, activeStopName);
                        }
                    });
                    LU.addView(button);

                }
                for (int n = 0; n < array2.size(); n++) {
                    JSONObject object = (JSONObject) array2.get(n);
                    TextView text = new TextView(this);
                    if (object.get("time") != null) {
                        String textString = (String) object.get("time");
                        String[] parts = textString.split(":");
                        text.setText(parts[0] + ":" + parts[1]);
                        text.setPadding(50, 5, 30, 5);
                    } else {
                        text.setText("    ");
                        text.setPadding(50, 5, 40, 5);
                    }

                    LD.addView(text);

                }
            }
        }
        else
        {
            Toast.makeText(this, "Wrong url address",
                    Toast.LENGTH_LONG).show();
        }

    }
    /**
     * Called when the user click on line, changes activity
     */
    public void openTimetable(String line, String stopID, String stopName) {
        Intent myIntent = new Intent(this, TimetableActivity.class);
        myIntent.putExtra("line", line);
        myIntent.putExtra("stopID", stopID);
        myIntent.putExtra("stopName", stopName);
        startActivity(myIntent);
    }

    /**
     * Clean old trains and shows new trains
     */
    public void showTrains() {
        for (Marker marker : trainMarkers) {
            marker.remove();
        }
        trainMarkers.clear();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                trainsTask = new JSONTask(urlAddress + "/stop/" + activeStopID + "/lines/info/position");
                trainsTask.execute();
                String jsonString = trainsTask.jsonResult;
                JSONArray bigArray = null;
                JSONArray array = null;
                while (jsonString == null) {
                    jsonString = trainsTask.jsonResult;
                }
                trainsTask.cancel(false);
                if (jsonString!="error") {
                    try {
                        bigArray = (JSONArray) new JSONParser().parse(jsonString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    for (int m = 0; m < bigArray.size(); m++) {
                        array = (JSONArray) bigArray.get(m);

                        for (int n = 0; n < array.size(); n++) {
                            JSONObject train = (JSONObject) array.get(n);

                            double latitude = (double) train.get("lat");
                            double longitude = (double) train.get("lon");
                            int lineName = Integer.parseInt((String) train.get("lines"));
                            int index = Arrays.asList(trainsNumber).indexOf(lineName);
                            Bitmap bMap = BitmapFactory.decodeResource(getResources(), trainsPictureInt[index]);
                            Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 30, 30, true);
                            trainMarkers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                    .icon(BitmapDescriptorFactory.fromBitmap(bMapScaled)).zIndex(lineName + 1000000)));


                        }
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Wrong url address",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        if (trainsTask != null)
            trainsTask.cancel(false);
    }
    /**
     * Reshow user position and trains
     */
    public void refreshMap() {
        LatLng user = new LatLng(userLatitude, userLongitude);
        userMarker.setPosition(user);
        if (activeStopID != null) {
            showTrains();
            CameraUpdate update = CameraUpdateFactory.newLatLng(oldMarker.getPosition());
            mMap.animateCamera(update);
        } else {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(user, 15);
            mMap.animateCamera(update);
        }

    }

}
