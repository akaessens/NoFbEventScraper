package com.akdev.nofbeventscraper;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static com.akdev.nofbeventscraper.FbEvent.dateTimeToEpoch;


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
        View view = inflater.inflate(R.layout.item_event, parent, false);


        // Return a new holder instance
        final ViewHolder holder = new ViewHolder(view);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final EventAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        final FbEvent event = events.get(position);


        // Set item views based on your views and data model
        if (!event.name.equals("")) {
            holder.text_view_event_name.setText(event.name);
        }

        if (event.start_date != null) {
            String str = FbEvent.dateTimeToString(event.start_date);
            holder.text_view_event_start.setText(str);
        }

        if (event.end_date != null) {
            String str = FbEvent.dateTimeToString(event.end_date);
            holder.text_view_event_end.setText(str);
        }

        if (!event.location.equals("")) {
            holder.text_view_event_location.setText(event.location);
        }

        if (!event.description.equals("")) {
            holder.text_view_event_description.setText(event.description);
        }

        try {
            Picasso.get()
                    .load(event.image_url)
                    .placeholder(R.mipmap.ic_launcher)
                    .transform(new CropCircleTransformation())
                    .into(holder.image_view_event_image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Maps button: launch maps intent
         */
        View.OnClickListener location_click_listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String map_search = "geo:0,0?q=" + event.location;

                Uri intent_uri = Uri.parse(map_search);
                Intent map_intent = new Intent(Intent.ACTION_VIEW, intent_uri);
                if (map_intent.resolveActivity(view.getContext().getPackageManager()) != null) {
                    view.getContext().startActivity(map_intent);
                }
            }
        };
        holder.image_view_event_location.setOnClickListener(location_click_listener);
        holder.text_view_event_location.setOnClickListener(location_click_listener);
        /*
         * Add to calendar button: launch calendar application with current event
         */
        holder.button_add_to_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Long start_epoch = dateTimeToEpoch(event.start_date);
                Long end_epoch = dateTimeToEpoch(event.end_date);

                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, event.name);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start_epoch);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end_epoch);
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.location);

                // prepend url in description
                String desc = event.url + "\n\n" + event.description;
                intent.putExtra(CalendarContract.Events.DESCRIPTION, desc);

                if (intent.resolveActivity(view.getContext().getPackageManager()) != null) {
                    view.getContext().startActivity(intent);
                }
            }
        });
        /*
         * Expand and collapse description
         */
        holder.text_view_event_description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.description_collapsed) {
                    holder.description_collapsed = false;
                    holder.text_view_event_description.setMaxLines(Integer.MAX_VALUE);
                } else {
                    holder.description_collapsed = true;
                    holder.text_view_event_description.setMaxLines(5);
                }
            }
        });

        /*
         * Image dialog
         */
        holder.image_view_event_image.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {


                     final Dialog dialog = new Dialog(view.getContext(),android.R.style.Theme_Translucent_NoTitleBar);
                     dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                     dialog.setContentView(R.layout.dialog_image);
                     dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                     ImageView image = (ImageView) dialog.findViewById(R.id.image_view_event_image_fullscreen);
                     image.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             dialog.dismiss();
                         }
                     });

                     try {
                         Picasso.get()
                                 .load(event.image_url)
                                 .into(image);
                         dialog.show();
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }

        );


    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView text_view_event_name;
        protected TextView text_view_event_start;
        protected TextView text_view_event_end;
        protected TextView text_view_event_location;
        protected TextView text_view_event_description;
        protected ImageView image_view_event_image;
        protected ImageView image_view_event_location;
        protected Button button_add_to_calendar;

        protected boolean description_collapsed = true;

        public ViewHolder(View item_view) {
            super(item_view);

            text_view_event_name = item_view.findViewById(R.id.text_view_event_name);
            text_view_event_start = item_view.findViewById(R.id.text_view_event_start);
            text_view_event_end = item_view.findViewById(R.id.text_view_event_end);
            text_view_event_location = item_view.findViewById(R.id.text_view_event_location);
            text_view_event_description = item_view.findViewById(R.id.text_view_event_description);
            image_view_event_image = item_view.findViewById(R.id.image_view_event_image);
            image_view_event_location = item_view.findViewById(R.id.image_view_event_location);
            button_add_to_calendar = item_view.findViewById(R.id.button_add_to_calendar);

        }
    }
}