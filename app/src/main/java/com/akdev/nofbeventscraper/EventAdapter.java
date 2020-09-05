package com.akdev.nofbeventscraper;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;


public class EventAdapter extends
        RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<FbEvent> events;

    // Pass in the contact array into the constructor
    public EventAdapter(List<FbEvent> events) {
        this.events = events;
    }

    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int view_type) {
        final Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contact_view = inflater.inflate(R.layout.item_event, parent, false);



        // Return a new holder instance
        final ViewHolder holder = new ViewHolder(contact_view);

        /*
         * Maps button: launch maps intent
         */

        holder.layout_event_location.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String map_search = "geo:0,0?q=" + holder.edit_text_event_location.getText();

                Uri intent_uri = Uri.parse(map_search);
                Intent map_intent = new Intent(Intent.ACTION_VIEW, intent_uri);
                if (map_intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(map_intent);
                }
            }
        });

        return new ViewHolder(contact_view);
    }
    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        FbEvent event = events.get(position);

        // Set item views based on your views and data model
        if (event.name.equals("")) {
            //holder.edit_text_event_name.setError(R.string.error_no_name);
        } else {
            holder.edit_text_event_name.setText(event.name);
        }

        if (event.start_date == null) {
            //holder.edit_text_event_start.setError(getString(R.string.error_no_start_date));
        } else {
            String str = FbEvent.dateTimeToString(event.start_date);
            holder.edit_text_event_start.setText(str);
        }

        if (event.end_date == null) {
            //edit_text_event_end.setError(getString(R.string.error_no_end_date));
        } else {
            String str = FbEvent.dateTimeToString(event.end_date);
            holder.edit_text_event_end.setText(str);
        }

        if (event.location.equals("")) {
            //edit_text_event_location.setError(getString(R.string.error_no_location));
        } else {
            holder.edit_text_event_location.setText(event.location);
            //layout_event_location.setEndIconVisible(true);
        }

        if (event.description.equals("")) {
            //holder.edit_text_event_description.setError(getString(R.string.error_no_description));
        } else {
            holder.edit_text_event_description.setText(event.description);
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        protected TextInputEditText edit_text_event_name;
        protected TextInputEditText edit_text_event_start;
        protected TextInputEditText edit_text_event_end;
        protected TextInputEditText edit_text_event_location;
        protected TextInputEditText edit_text_event_description;
        protected TextInputLayout   layout_event_location;


        public ViewHolder(View item_view) {
            super(item_view);

            edit_text_event_name = (TextInputEditText) item_view.findViewById(R.id.edit_text_event_name);
            edit_text_event_start = (TextInputEditText) item_view.findViewById(R.id.edit_text_event_start);
            edit_text_event_end = (TextInputEditText) item_view.findViewById(R.id.edit_text_event_end);
            edit_text_event_location = (TextInputEditText) item_view.findViewById(R.id.edit_text_event_location);
            edit_text_event_description = (TextInputEditText) item_view.findViewById(R.id.edit_text_event_description);
            layout_event_location = (TextInputLayout) item_view.findViewById(R.id.layout_event_location);
        }
    }
}