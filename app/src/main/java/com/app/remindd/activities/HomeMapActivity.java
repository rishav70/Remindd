package com.app.remindd.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.app.remindd.R;
import com.app.remindd.database.Config;
import com.app.remindd.database.Longtap;
import com.app.remindd.database.MyDatabase;
import com.app.remindd.util.IabHelper;
import com.app.remindd.util.IabResult;
import com.app.remindd.util.Inventory;
import com.app.remindd.util.Purchase;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;
import static com.app.remindd.R.id.journeyType;
import static com.app.remindd.activities.BuyActivity.ITEMS;

public class HomeMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Activity mActivity;
    private GoogleMap mMap;
    private MyDatabase database;
    String toggleStatus = "Going Out";

    //views for journey
    Dialog journeyDialog;
    EditText journeyName, journeyDescription, journeyRadius, startDate, endDate;
    TextView radiusUnits;
    ToggleButton toggleButton;
    Button cancel, done;
    String playPauseStatus = "play";

    RelativeLayout markerClickLayout;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;

    private int STORAGE_PERMISSION_CODE = 23;

    private Marker marker1;

    String latitude, longitude;

    ImageView img_map, img_customLayout, img_locationsList;

    String animate = "yes";

    private int mYear, mMonth, mDay, mHour, mMinute;

    String dateTime;
    RelativeLayout custom_location;

    ImageView img_play, img_pause, img_edit, img_done;

    TextView marker_name, marker_description, marker_toggle, marker_radius, marker_startDate, marker_endDate;

    String value = "add";

    int update_id;

    Calendar calendar;

    AlertDialog.Builder alertDialog;

    LocationManager locationManager;

    private SimpleDateFormat mFormatter = new SimpleDateFormat("MM/dd/yyyy h:mm");

    public Dialog dialog;
    Button btnOk, btnPause, btnComplete;
    TextView locName, locDescription;

    MyReceiver myReceiver;

    IabHelper mHelper;

    int reminder_purchased = 0;

    String str_name, str_dec, str_lat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_map);

        mActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();

        str_name = i.getStringExtra("name");
        str_dec = i.getStringExtra("dec");
        str_lat = i.getStringExtra("lat");

        img_play = (ImageView) findViewById(R.id.img_play);
        img_pause = (ImageView) findViewById(R.id.img_pause);
        img_edit = (ImageView) findViewById(R.id.img_edit);
        img_done = (ImageView) findViewById(R.id.img_done);

        marker_name = (TextView) findViewById(R.id.marker_name);
        marker_description = (TextView) findViewById(R.id.marker_description);
        marker_toggle = (TextView) findViewById(journeyType);
        marker_radius = (TextView) findViewById(R.id.radius);
        marker_startDate = (TextView) findViewById(R.id.startDate);
        marker_endDate = (TextView) findViewById(R.id.endDate);

        ActivityCompat.requestPermissions(mActivity,
                new String[]{Manifest.permission
                        .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                STORAGE_PERMISSION_CODE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        database = new MyDatabase(this);


        markerClickLayout = (RelativeLayout) findViewById(R.id.markerClickLayout);

        if (markerClickLayout.getVisibility() == View.VISIBLE) {
            markerClickLayout.setVisibility(GONE);
        }

        setJourneyDialog();

        bottomButtonsLayout();

        alertDialog = new AlertDialog.Builder(this);

        alarmDialog();

        checkInApp();

    }

    void checkInApp() {
        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApBA3MPUXLXn7WiR+WkI/0N+cs5u210gZgpbC5/tjgz4gjubK4TcSbEvNigpMAtofe2yrHIe9AUT+ZI+RXP8sgl4DmRR+V/g2rQEZxMQaP0rmTK7mSWcJOdpd0VQcjxRXO3C4usBV54Q1veJPPbfsCqDE46/n2MwLAId5OyrWtxIXEaUgQDWCDd+XtvcbFAOrvS8IXpzoR2aV0I9e22fZebOO611XAZp3OmhlSlMkME57BAOteH8rl/+whn55A+kA6rcvVdCvsXl6W53OjD+mSi/hlhah6/7SAkMoiCpNWNkumeW7G5KuuoC+ALahVENHYFX4KpM2qRGrc85xwHG33wIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       public void onIabSetupFinished(IabResult result)
                                       {

                                           if (!result.isSuccess()) {
                                               Log.e("hu", ""+result);
                                           } else {
                                               Log.e("ha", "ok");
                                               try {
                                                   mHelper.queryInventoryAsync(mGotInventoryListener);
                                               } catch (IabHelper.IabAsyncInProgressException e) {
                                                   e.printStackTrace();
                                               }
                                           }
                                       }
                                   });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d("PAY", "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null)
                return;

            Purchase purchase1 = inventory.getPurchase(ITEMS[0]);
            Purchase purchase2 = inventory.getPurchase(ITEMS[1]);
            Purchase purchase3 = inventory.getPurchase(ITEMS[2]);


            //purchased
            if (purchase1 != null) {

                reminder_purchased = 10;

            }
            if (purchase2 != null) {
                reminder_purchased = 20;
            }

            if (purchase3 != null) {

                reminder_purchased = 100;
            }
            /*else {
                Toast.makeText(BuyActivity.this, "Not purchased", Toast.LENGTH_SHORT).show();
            }*/
        }
    };

    public void alarmDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alarm_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnOk = (Button) dialog.findViewById(R.id.btnOk);
        btnPause = (Button) dialog.findViewById(R.id.btnPause);
        btnComplete = (Button) dialog.findViewById(R.id.btnComplete);
        locName = (TextView) dialog.findViewById(R.id.locationName);
        locDescription = (TextView) dialog.findViewById(R.id.locationDescription);
    }


    public void showMarkers() {
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        String count = "SELECT count(*) FROM taplisttable";
        Cursor mCursor = sqLiteDatabase.rawQuery(count, null);
        mCursor.moveToFirst();
        int icount = mCursor.getInt(0);

        if (icount > 0) {

            ArrayList<Longtap> values = database.getLoginData();

            for (Longtap longtap : values) {

                String lat = longtap.getLat();
                String log = longtap.getLog();
                String status = longtap.getPlayPauseStatus();

                Config.units = longtap.getUnits();

                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(log));

                if (status.equals("pause"))

                    mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.
                            fromResource(R.drawable.ic_icon_pin_gray)));

                else

                    mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.
                            fromResource(R.drawable.ic_icon_pin)));

            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (animate.equals("yes"))
                    markerClickLayout.setVisibility(GONE);

                animate = "yes";
            }
        });

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        /*if (result == PackageManager.PERMISSION_GRANTED) {
            // Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
        }*/

        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        showMarkers();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        getCurrentLoc();

    }


    public void bottomButtonsLayout() {
        img_map = (ImageView) findViewById(R.id.img_map);
        img_customLayout = (ImageView) findViewById(R.id.img_customLayout);
        img_locationsList = (ImageView) findViewById(R.id.img_locationsList);

        custom_location = (RelativeLayout) findViewById(R.id.custom_location);

        img_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_map.setImageResource(R.drawable.ic_icon_map);
                img_customLayout.setImageResource(R.drawable.ic_icon_location_black);
                img_locationsList.setImageResource(R.drawable.ic_icon_alarmlist_black);

                markerClickLayout.setVisibility(GONE);

                if (custom_location.getVisibility() == View.VISIBLE)
                    custom_location.setVisibility(GONE);
            }
        });

        img_customLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                img_map.setImageResource(R.drawable.ic_icon_map_black);
                img_customLayout.setImageResource(R.drawable.ic_icon_location);
                img_locationsList.setImageResource(R.drawable.ic_icon_alarmlist_black);

                markerClickLayout.setVisibility(GONE);

                custom_location.setVisibility(View.VISIBLE);

            }
        });

        img_locationsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                img_map.setImageResource(R.drawable.ic_icon_map_black);
                img_customLayout.setImageResource(R.drawable.ic_icon_location_black);
                img_locationsList.setImageResource(R.drawable.ic_icon_alarmlist);

                if (custom_location.getVisibility() == View.VISIBLE)
                    custom_location.setVisibility(GONE);

                Intent i = new Intent(mActivity, AlarmListActivity.class);
                startActivity(i);
                markerClickLayout.setVisibility(GONE);
            }
        });


        Button customAddress = (Button) findViewById(R.id.customAddress);
        Button currentLocation = (Button) findViewById(R.id.currentLocation);

        customAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mActivity, SearchActivity.class);
                startActivity(i);
            }
        });

        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                custom_location.setVisibility(GONE);
                currentLocationAddress();
            }
        });


    }

    public void currentLocationAddress() {
        journeyDialog.show();
        value = "add";

        initCamera(mCurrentLocation);
    }


    public void customAddress() {

        value = "add";

        Log.e("Searched LatLng: ", "Lat: " + latitude + "\n Lng: " + longitude);

        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);
        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(lat,
                        lng))
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        mMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(position));

        journeyDialog.show();

    }


    public void setJourneyDialog() {
        journeyDialog = new Dialog(this);
        journeyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        journeyDialog.setContentView(R.layout.journey_dialog);
        journeyDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        journeyName = (EditText) journeyDialog.findViewById(R.id.journeyName);
        journeyDescription = (EditText) journeyDialog.findViewById(R.id.journeyDescription);
        journeyRadius = (EditText) journeyDialog.findViewById(R.id.journeyRadius);

        radiusUnits = (TextView) journeyDialog.findViewById(R.id.units);
        toggleButton = (ToggleButton) journeyDialog.findViewById(R.id.toggleButton);
        startDate = (EditText) journeyDialog.findViewById(R.id.fromDate);
        endDate = (EditText) journeyDialog.findViewById(R.id.toDate);

        cancel = (Button) journeyDialog.findViewById(R.id.btn_cancel);
        done = (Button) journeyDialog.findViewById(R.id.btn_done);

        calendar = Calendar.getInstance();

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTime = "START";
                calendar = Calendar.getInstance();
                //dateTimeDialog();
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .setIndicatorColor(Color.parseColor("#f46737"))
                        .build()
                        .show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTime = "END";
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .setIs24HourTime(true)
                        .setIndicatorColor(Color.parseColor("#f46737"))
                        .build()
                        .show();

            }
        });


        radiusUnits.setText(Config.units);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked)
                    toggleStatus = "Coming In";
                else
                    toggleStatus = "Going Out";

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                journeyDialog.dismiss();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMarkerData();
            }
        });
    }

    SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            String[] split = mFormatter.format(date).split(" ");
            if (dateTime.equals("START")) {
                startDate.setText(split[split.length-2]);
            } else {
                endDate.setText(split[split.length-2]);
            }

        }

    };


    public void setMarkerData() {
        String journey_name = journeyName.getText().toString();
        String journey_description = journeyDescription.getText().toString();
        String journey_radius = journeyRadius.getText().toString();
        String start_date = startDate.getText().toString();
        String end_date = endDate.getText().toString();


        if (journey_name.isEmpty()) {
            journeyName.setError("Please enter the journey name");
            return;
        }
        if (journey_description.isEmpty()) {
            journeyDescription.setError("Please enter the journey description");
            return;
        }
        if (journey_radius.isEmpty()) {
            journeyRadius.setError("Please enter the journey radius");
            return;
        }

        if (start_date.isEmpty()) {
            startDate.setError("Please enter the start date");
            return;
        }

        if (end_date.isEmpty()) {
            endDate.setError("Please enter the end date");
            return;
        }

        journeyDialog.dismiss();

        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        String count = "SELECT count(*) FROM taplisttable";
        Cursor mCursor = sqLiteDatabase.rawQuery(count, null);
        mCursor.moveToFirst();
        final int icount = mCursor.getInt(0);

        alertDialog.setTitle("Buy Reminders");
        alertDialog.setMessage("Buy reminders to add more locations");
        alertDialog.setPositiveButton("Buy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Intent intent = new Intent(mActivity, BuyActivity.class);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });


        if (reminder_purchased == 0) {
            if (icount > 4) {
                alertDialog.create().show();
                return;
            }
        }
        else if (reminder_purchased == 10) {
            if (icount > 9) {
                alertDialog.create().show();
                return;
            }
        }
        else if (reminder_purchased == 20) {
            if (icount > 19) {
                alertDialog.create().show();
                return;
            }
        }


        if (value.equals("add")) {

            long insert = database.insertData(new Longtap(journey_radius, journey_name, journey_description,
                    start_date, end_date, toggleStatus, latitude, longitude, playPauseStatus, Config.units));

            if (insert > 0) {

                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                marker1 = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.
                        fromResource(R.drawable.ic_icon_pin)));

                Intent i = new Intent(mActivity, LocationService.class);
                startService(i);

            }
        } else {
            boolean update = database.updateValues(latitude, new Longtap(journey_radius, journey_name, journey_description,
                    start_date, end_date, toggleStatus, Config.units));
        }



    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (markerClickLayout.getVisibility() == View.VISIBLE) {
            markerClickLayout.setVisibility(GONE);
        }

        if (journeyDialog.isShowing()) {
            journeyDialog.dismiss();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if (markerClickLayout.getVisibility() == View.VISIBLE) {
            markerClickLayout.setVisibility(GONE);
        }

        value = "add";

        latitude = String.valueOf(latLng.latitude);
        longitude = String.valueOf(latLng.longitude);

        radiusUnits.setText(Config.units);

        // marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_pin)));

        journeyDialog.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (custom_location.getVisibility() == View.VISIBLE) {
            custom_location.setVisibility(GONE);
        }
        markersButton(marker);

        marker1 = marker;

        animate = "no";

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
        mMap.moveCamera(center);

        ArrayList<Longtap> values = database.getLoginData();

        String latitude = String.valueOf(marker.getPosition().latitude);
        String longitude = String.valueOf(marker.getPosition().longitude);

        for (Longtap cn : values) {

            String db_latitude = cn.getLat();
            String db_longitude = cn.getLog();

            if (latitude.equals(db_latitude) && longitude.equals(db_longitude)) {

                if (cn.getPlayPauseStatus().equals("play")) {
                    img_play.setVisibility(View.VISIBLE);
                    img_pause.setVisibility(View.GONE);
                } else {
                    img_play.setVisibility(View.GONE);
                    img_pause.setVisibility(View.VISIBLE);
                }

                Log.e("Marker Status: ", "" + cn.getPlayPauseStatus());

                marker_name.setText(cn.getName());
                marker_description.setText(cn.getDec());
                marker_endDate.setText(cn.getTdate());
                marker_toggle.setText(cn.getTogglebutton());
                marker_startDate.setText(cn.getFdate());
                marker_radius.setText(cn.getRedious() + " " + cn.getUnits());
            }

        }

        markerClickLayout.setVisibility(View.VISIBLE);

        return true;
    }

    public void markersButton(final Marker marker) {

        final String marker_lat = String.valueOf(marker.getPosition().latitude);
        final String marker_lng = String.valueOf(marker.getPosition().longitude);


        final ArrayList<Longtap> values = database.getLoginData();

        img_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.setTitle("Mark complete");
                alertDialog.setMessage("Marking complete will delete location permanently");
                alertDialog.setPositiveButton("Go ahead", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        marker1.remove();
                        markerClickLayout.setVisibility(GONE);

                        for (Longtap longtap : values) {
                            String db_latitude = longtap.getLat();
                            String db_longitude = longtap.getLog();

                            Log.e("Marker lat: ", "" + marker_lat);
                            Log.e("db lat:", "" + db_latitude);

                            if (marker_lat.equals(db_latitude) && marker_lng.equals(db_longitude)) {
                                int id = longtap.get_id();

                                Log.e("ID: ", "" + id);

                                boolean delete = database.deleteRow(marker_lat);

                                if (delete) {
                                    Log.e("Row Deleted", "successfully");
                                }

                            }
                        }
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create().show();

            }
        });

        img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                journeyDialog.show();
                radiusUnits.setText(Config.units);

                markerClickLayout.setVisibility(GONE);
                for (Longtap longtap : values) {
                    String db_latitude = longtap.getLat();
                    String db_longitude = longtap.getLog();
                    if (marker_lat.equals(db_latitude) && marker_lng.equals(db_longitude)) {

                        value = "update";

                        update_id = longtap.get_id();
                        Log.e("Update id: ", "" + update_id);

                        String name = longtap.getName();
                        String des = longtap.getDec();
                        String radius = longtap.getRedious().replace("Miles", "").replace("Kilometer", "").replace("Meter", "");
                        String fDate = longtap.getFdate();
                        String tDate = longtap.getTdate();
                        String toggle = longtap.getTogglebutton();
                        journeyName.setText(name);
                        journeyDescription.setText(des);
                        journeyRadius.setText(radius);
                        startDate.setText(fDate);
                        endDate.setText(tDate);
                        if (toggle.equals("Coming In")) {
                            toggleButton.setChecked(true);
                        } else {
                            toggleButton.setChecked(false);
                        }
                    }
                }
            }
        });

        img_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.setMessage("Would you like to pause the reminder?");

                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        img_play.setVisibility(GONE);
                        img_pause.setVisibility(View.VISIBLE);
                        marker1.setVisible(false);
                        markerClickLayout.setVisibility(GONE);

                        for (Longtap longtap : values) {
                            int id = longtap.get_id();

                            String db_latitude = longtap.getLat();
                            String db_longitude = longtap.getLog();
                            if (marker_lat.equals(db_latitude) && marker_lng.equals(db_longitude)) {
                                Log.e("Marker Lat: ", "" + marker_lat);
                                database.updateStatus(marker_lat, "pause");

                                LatLng latLng = new LatLng(Double.parseDouble(marker_lat), Double.parseDouble(marker_lng));

                                marker1 = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.
                                        fromResource(R.drawable.ic_icon_pin_gray)));
                            }
                        }


                    }
                }).create().show();
            }
        });

        img_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.setMessage("Would you like to start the reminder?");

                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        img_pause.setVisibility(GONE);
                        img_play.setVisibility(View.VISIBLE);
                        marker1.setVisible(false);
                        markerClickLayout.setVisibility(GONE);

                        for (Longtap longtap : values) {
                            int id = longtap.get_id();

                            String db_latitude = longtap.getLat();
                            String db_longitude = longtap.getLog();
                            if (marker_lat.equals(db_latitude) && marker_lng.equals(db_longitude)) {

                                Log.e("Marker Lat PLAY: ", "" + marker_lat);
                                database.updateStatus(marker_lat, "play");

                                LatLng latLng = new LatLng(Double.parseDouble(marker_lat), Double.parseDouble(marker_lng));

                                marker1 = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.
                                        fromResource(R.drawable.ic_icon_pin)));

                            }
                        }


                    }
                }).create().show();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                SupportMapFragment fmmn = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                // Getting GoogleMap object from the fragment
                fmmn.getMapAsync(this);

            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

      /*  if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();*/

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }


        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApplication.activityResumed();



        if(str_name != null) {
            showAlert(str_name, str_dec, str_lat);
        }

        if (mMap != null) {
            mMap.clear();
            showMarkers();
        }

        if (Config.icon_status.equals("map")) {
            img_map.setImageResource(R.drawable.ic_icon_map);
            img_customLayout.setImageResource(R.drawable.ic_icon_location_black);
            img_locationsList.setImageResource(R.drawable.ic_icon_alarmlist_black);
        } else {
            img_map.setImageResource(R.drawable.ic_icon_map_black);
            img_customLayout.setImageResource(R.drawable.ic_icon_location);
            img_locationsList.setImageResource(R.drawable.ic_icon_alarmlist_black);
            custom_location.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApplication.activityPaused();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {



    }

    void getCurrentLoc() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);


        if (result == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
        }

        mCurrentLocation = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        Intent intent = getIntent();
        if (intent != null) {
            String lat = intent.getStringExtra("latitude");
            String lng = intent.getStringExtra("longitude");

            if (lat != null) {

                latitude = lat;
                longitude = lng;

                customAddress();

            } else {
                if (mCurrentLocation == null) {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Criteria locationCritera = new Criteria();
                    locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
                    locationCritera.setAltitudeRequired(false);
                    locationCritera.setBearingRequired(false);
                    locationCritera.setCostAllowed(true);
                    locationCritera.setPowerRequirement(Criteria.NO_REQUIREMENT);

                    String provider = locationManager.getBestProvider(locationCritera, true);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mCurrentLocation = locationManager.getLastKnownLocation(provider);
                    initCamera(mCurrentLocation);
                } else {
                    initCamera(mCurrentLocation);
                }
            }

        } else {
            Log.e("there is nothing", "");
        }
    }

    private void initCamera(Location location) {

        if (location != null) {

            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            CameraPosition position = CameraPosition.builder()
                    .target(new LatLng(location.getLatitude(),
                            location.getLongitude()))
                    .zoom(16f)
                    .bearing(0.0f)
                    .tilt(0.0f)
                    .build();

            mMap.moveCamera(CameraUpdateFactory
                    .newCameraPosition(position));

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentManager fm = getFragmentManager();
        // FragmentManager fmm = getFragmentManager();
        final android.support.v4.app.FragmentManager fma = getSupportFragmentManager();
        android.support.v4.app.FragmentManager sFm = getSupportFragmentManager();

        int id = item.getItemId();

       /* if (sMapFragment.isAdded())
            sFm.beginTransaction().hide(sMapFragment).commit();*/
      /*

        }*/


        if (id == R.id.rate) {

          /*  final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }*/


            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mActivity.getPackageName()));
            boolean marketFound = false;

            // find all applications able to handle our rateIntent
            final List<ResolveInfo> otherApps = HomeMapActivity.this.getPackageManager().queryIntentActivities(rateIntent, 0);
            for (ResolveInfo otherApp: otherApps) {
                // look for Google Play application
                if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {

                    ActivityInfo otherAppActivity = otherApp.activityInfo;
                    ComponentName componentName = new ComponentName(
                            otherAppActivity.applicationInfo.packageName,
                            otherAppActivity.name
                    );
                    rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    rateIntent.setComponent(componentName);
                    startActivity(rateIntent);
                    marketFound = true;
                    break;

                }
            }

            // if GP not present on device, open web browser
            if (!marketFound) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+mActivity.getPackageName()));
                startActivity(webIntent);
            }

        } else if (id == R.id.share) {

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/*");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<p>Hey, use this amazing app to set location based reminders on the go." +
                    "It's very easy to use. Here is the link : https://play.google.com/store/apps/details?id=com.app.remindd</p>"));
            startActivity(Intent.createChooser(sharingIntent, "Share using..."));
        } else if (id == R.id.buy) {
            Intent i = new Intent(mActivity, BuyActivity.class);
            startActivity(i);
        } else if (id == R.id.settings) {
            Intent i = new Intent(mActivity, SettingsActivity.class);
            startActivity(i);
            markerClickLayout.setVisibility(View.GONE);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

  /*  public void setFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(map, fragment).commit();
    }*/

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context arg0, Intent intent) {
            // TODO Auto-generated method stub

            String name = intent.getStringExtra("name1");
            String dec = intent.getStringExtra("dec1");
            final String lat = intent.getStringExtra("lat1");

            if (name != null)
                showAlert(name, dec, lat);
        }

    }


    void showAlert(String name, String dec, final String lat) {

        str_name = null;
        str_dec = null;
        str_lat = null;

        final MediaPlayer media = MediaPlayer.create(mActivity, R.raw.alerttone);
        if (!media.isPlaying() && !dialog.isShowing()) {
            dialog.show();
            media.start();
            media.setLooping(true);
              locName.setText(name);
            locDescription.setText(dec);
        }

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean delete = database.deleteRow(lat);
                if (delete) {
                    //  Toast.makeText(arg0, "Marked complete", Toast.LENGTH_SHORT).show();

                    mMap.clear();
                    showMarkers();

                    if(media.isPlaying()) {
                        media.stop();
                    }
                    dialog.dismiss();

                    Config.value = 0;
                }

            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean update = database.updateStatus(lat, "pause");

                if (update) {
                    // Toast.makeText(arg0, "Marker paused", Toast.LENGTH_SHORT).show();

                    mMap.clear();
                    showMarkers();

                    if(media.isPlaying()) {
                        media.stop();
                    }

                    dialog.dismiss();

                    Config.value = 0;
                }
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(media.isPlaying()) {
                    media.stop();
                }

                dialog.dismiss();

                Config.value = 0;
            }
        });


    }


}
