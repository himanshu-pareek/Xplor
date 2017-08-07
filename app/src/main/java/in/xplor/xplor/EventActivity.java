package in.xplor.xplor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.xplor.xplor.services.ScheduleClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private ScheduleClient mScheduleClient;

    private long startTime, finishTime;
    private String mTitle, mDescription;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    private double eventLatitude;
    private double eventLongitude;
    private String venue;

    private TextView distanceTextView, venueTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mScheduleClient = new ScheduleClient(this);
        mScheduleClient.doBindService();

        Intent intent = getIntent();
        mTitle = intent.getStringExtra("title");
        startTime = intent.getLongExtra("stime", System.currentTimeMillis());
        finishTime = intent.getLongExtra("ftime", System.currentTimeMillis());
        mDescription = intent.getStringExtra("description");
        eventLatitude = intent.getDoubleExtra("latitude", 0);
        eventLongitude = intent.getDoubleExtra("longitude", 0);
        venue = intent.getStringExtra("venue");

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM hh:mm aaa");

        TextView titleTextView, timeDurationTextView, venueTextView, descTextView;
        titleTextView = (TextView) findViewById(R.id.event_title_text);
        timeDurationTextView = (TextView) findViewById(R.id.time_duration_text_view);
        distanceTextView = (TextView) findViewById(R.id.distance_text_view);
        venueTextView = (TextView) findViewById(R.id.venue_text_view);
        venueTimeTextView = (TextView) findViewById(R.id.venue_time_text_view);
        descTextView = (TextView) findViewById(R.id.event_description_text);

        titleTextView.setText(mTitle);
        timeDurationTextView.setText(formatter.format(startTime) + "\n" + formatter.format(finishTime));
        distanceTextView.setText("distance");
        venueTextView.setText(venue);
        venueTimeTextView.setText("time");
        descTextView.setText(mDescription);

        (findViewById(R.id.save_event_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new calendar set to the date chosen
                // we set the time to midnight (i.e. the first minute of that day)
                Calendar d = Calendar.getInstance();
                d.setTimeInMillis(startTime - 30 * 60 * 1000);

                // Ask our service to set an alarm for that date, this activity talks to the client that talks to the service
                mScheduleClient.setAlarmForNotification(
                        d,
                        mTitle,
                        startTime,
                        finishTime,
                        mDescription,
                        eventLatitude,
                        eventLongitude,
                        venue);
                // Notify the user what they just did
                Toast.makeText(getApplicationContext(), "Reminder set successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*
        if (mScheduleClient != null)
            mScheduleClient.doUnbindService();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + currentLatitude + "," + currentLongitude + "&destinations=" + eventLatitude + "," + eventLongitude + "&mode=walking&key=AIzaSyB6fSuHN3NG9P0HnFCMcm68b-fkjpqKimo";
            new GeoTask(EventActivity.this).execute(url);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
/*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
    }

    public void setDouble(String result) {
        String res[]=result.split(",");
        Double min=Double.parseDouble(res[0])/60;
        double dist=(Integer.parseInt(res[1])/100) / 10.0;
        venueTimeTextView.setText((int) (min / 60) + " hr " + (int) (min % 60) + " mins");
        distanceTextView.setText(dist + " km");

    }

    private class GeoTask extends AsyncTask<String, Void, String> {

        private Context mContext;

        public GeoTask(Context ctx) {
            mContext = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url=new URL(params[0]);
                HttpURLConnection con= (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                int statuscode=con.getResponseCode();
                if(statuscode==HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb=new StringBuilder();
                    String line=br.readLine();
                    while(line!=null)
                    {
                        sb.append(line);
                        line=br.readLine();
                    }
                    String json=sb.toString();
                    JSONObject root=new JSONObject(json);
                    JSONArray array_rows=root.getJSONArray("rows");
                    JSONObject object_rows=array_rows.getJSONObject(0);
                    JSONArray array_elements=object_rows.getJSONArray("elements");
                    JSONObject  object_elements=array_elements.getJSONObject(0);
                    JSONObject object_duration=object_elements.getJSONObject("duration");
                    JSONObject object_distance=object_elements.getJSONObject("distance");

                    return object_duration.getString("value")+","+object_distance.getString("value");

                }
            } catch (MalformedURLException e) {
                Log.d("error", "error1");
            } catch (IOException e) {
                Log.d("error", "error2");
            } catch (JSONException e) {
                Log.d("error","error3");
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s != null) {
                setDouble(s);
            }
            else
                Toast.makeText(mContext, "Unable to get distance and time", Toast.LENGTH_SHORT).show();
        }
    }
}
