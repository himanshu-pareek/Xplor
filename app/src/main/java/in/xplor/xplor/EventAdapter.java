package in.xplor.xplor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by himanshu on 24/2/17.
 * This is adapter for events
 */

public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(Context context, ArrayList<Event> resources) {
        super(context, 0, resources);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.layout_card_view, parent, false
            );
        }

        Event currentEvent = getItem(position);

        TextView eventNameTextView = (TextView) listItemView.findViewById(R.id.event_name_text);
        TextView eventDateTextView = (TextView) listItemView.findViewById(R.id.event_date_text);

        eventNameTextView.setText(currentEvent.getTitle());

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM hh:mm aaa");
        String dateString = formatter.format(new Date(currentEvent.getStart()));

        eventDateTextView.setText("Starting : " + dateString);

        return listItemView;
    }
}
