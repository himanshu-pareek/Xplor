package in.xplor.xplor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class EventListActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEventReference;
    private ChildEventListener mChildListener;

    private EventAdapter mEventAdapter;
    private ProgressBar mEventProgressBar;
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEventReference = mFirebaseDatabase.getReference().child("events");

        ListView eventListView = (ListView) findViewById(R.id.event_list);
        mEventAdapter = new EventAdapter(this, new ArrayList<Event>());
        mEventProgressBar = (ProgressBar) findViewById(R.id.event_loading_spanner);
        mEmptyStateTextView = (TextView) findViewById(R.id.event_empty_state_text_view);

        eventListView.setAdapter(mEventAdapter);
        eventListView.setEmptyView(mEmptyStateTextView);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event event = mEventAdapter.getItem(position);
                Intent eventIntent = new Intent(EventListActivity.this, EventActivity.class);

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
            }
        });

        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Event event = dataSnapshot.getValue(Event.class);
                long tm = System.currentTimeMillis();
                if (tm >= (event.getStart() - 48 * 60 * 60 * 1000) && tm <= event.getFinish()) {
                    mEventAdapter.add(event);
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
                Event event = dataSnapshot.getValue(Event.class);
                mEventAdapter.remove(event);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Request has been canceled", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void attachChildEventListener() {
        mEventAdapter.clear();
        mEventReference.addChildEventListener(mChildListener);
    }

    private void detachChildEventListener () {
        if (mChildListener != null) {
            mEventReference.removeEventListener(mChildListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventProgressBar.setVisibility(View.GONE);
        attachChildEventListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachChildEventListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachChildEventListener();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // do something on back.
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
