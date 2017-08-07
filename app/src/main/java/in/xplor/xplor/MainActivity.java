package in.xplor.xplor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = "MapsActivity";

    private static final long MAX_TARDINESS = 1000;
    private GoogleMap mMap;
    private UiSettings mUiSettings;

    private boolean mMapReady = false;

    private Timer myTimer;

    private Bitmap mMarkerBitmap;

    HashMap<Marker, Event> mMarkerEventMap;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final String[] PERMISSION_LOCATION = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMarkerBitmap = imageToBitmap(getApplicationContext(), R.drawable.drop_pin_logo);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.my_map);
        mapFragment.getMapAsync(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMapReady = true;
        mMap = googleMap;

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);

        // Add a marker in Sydney and move the camera
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        //Location userLocation = new Location()

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3193, 87.31), 14.0f));

        addMarkers();

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - scheduledExecutionTime() >=
                        MAX_TARDINESS)
                    return;  // Too late; skip this execution.
                // Perform the task
                TimerMethod();
            }
        }, 0, 5 * 60 * 1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void requestLocationPermissions() {
        // BEGIN_INCLUDE(contacts_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            Log.i(LOG_TAG,
                    "Displaying contacts permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
//            Snackbar.make(mLayout, R.string.permission_location_rationale,
//                    Snackbar.LENGTH_INDEFINITE)
//                    .setAction(R.string.ok, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            ActivityCompat
//                                    .requestPermissions(MapActivity.this, PERMISSION_LOCATION,
//                                            MY_PERMISSIONS_REQUEST_LOCATION);
//                        }
//                    })
//                    .show();
        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSION_LOCATION, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        // END_INCLUDE(contacts_permission_request)
    }

    /**
     * Callbacks from request location
     * @param requestCode : code for permission request
     * @param permissions : which permission is considered
     * @param grantResults : result code array
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                Log.i(LOG_TAG, "Received response for contact permissions request.");

                // We have requested multiple permissions for contacts, so all of them need to be
                // checked.
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    // All required permissions have been granted, display contacts fragment.
//                    Snackbar.make(mLayout, R.string.permision_available_location,
//                            Snackbar.LENGTH_SHORT)
//                            .show();
                } else {
                    Log.i(LOG_TAG, "Contacts permissions were NOT granted.");
//                    Snackbar.make(mLayout, R.string.permissions_not_granted,
//                            Snackbar.LENGTH_SHORT)
//                            .show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void addMarkers() {
        mMap.clear();

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mEventReference = mFirebaseDatabase.getReference().child("events");
        mMarkerEventMap = new HashMap<>();

        mEventReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Event event = dataSnapshot.getValue(Event.class);
                long tm = System.currentTimeMillis();
                if (tm >= (event.getStart() - 30 * 60 * 1000) && tm <= event.getFinish()) {
                    Bitmap c = textAsBitmap(event.getTitle());
                    Bitmap markerAndTitle = combineImages(mMarkerBitmap, c);
                    Bitmap emptyBitmap = emptyBitmap(event.getTitle().length());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(event.getLatitude(), event.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromBitmap(combineImages(emptyBitmap, markerAndTitle)))
                    );
                    mMarkerEventMap.put(marker, event);
                } else if (tm > event.getFinish()) {
                    //delete event from the database...
                    String key = dataSnapshot.getKey();
                    mEventReference.child(key).removeValue();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Event event = mMarkerEventMap.get(marker);
                //Toast.makeText(MapsActivity.this, event.getDescription(), Toast.LENGTH_SHORT).show();
                Intent eventIntent = new Intent(MainActivity.this, EventActivity.class);

                eventIntent.putExtra("title", event.getTitle());
                eventIntent.putExtra("stime", event.getStart());
                eventIntent.putExtra("ftime", event.getFinish());
                eventIntent.putExtra("description", event.getDescription());
                eventIntent.putExtra("latitude", event.getLatitude());
                eventIntent.putExtra("longitude", event.getLongitude());
                eventIntent.putExtra("latitude_user", 0);
                eventIntent.putExtra("longitude_user", 0);
                eventIntent.putExtra("venue", event.getVenue());

                startActivity(eventIntent);
                return true;
            }
        });
    }

    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }


    private Runnable Timer_Tick = new Runnable() {
        public void run() {

            //This method runs in the same thread as the UI.

            //Do something to the UI thread here
            addMarkers();
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
          return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Xplor");
                String sAux = "\nLet me recommend you this application\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=in.xplor.xplor \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "Choose one"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_event_list) {
            Intent intent = new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Bitmap imageToBitmap(Context gContext, int gResId) {
        Resources resources = gContext.getResources();
        return BitmapFactory.decodeResource(resources, gResId);
    }

    /*
     */

    public Bitmap emptyBitmap(int len) {
        String text = "";
        for (int i = 0; i < len; i++) {
            text += "  ";
        }
        return textAsBitmap(text);
    }

    public Bitmap textAsBitmap(String text) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(30);
        paint.setColor(Color.RED);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public Bitmap combineImages(Bitmap c, Bitmap s) {
        // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs;

        int width, height = 0;

        width = c.getWidth() + s.getWidth();

        if (c.getHeight() > s.getHeight()) {
            height = c.getHeight();
        } else {
            height = s.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }
}
