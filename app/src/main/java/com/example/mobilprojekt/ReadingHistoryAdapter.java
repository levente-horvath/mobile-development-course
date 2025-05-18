package com.example.mobilprojekt;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import android.view.animation.DecelerateInterpolator;

public class ReadingHistoryAdapter extends RecyclerView.Adapter<ReadingHistoryAdapter.ReadingViewHolder> {
    private static final String TAG = "ReadingHistoryAdapter";
    private List<MeterReading> readings;
    private int lastPosition = -1;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public ReadingHistoryAdapter(List<MeterReading> readings) {
        this.readings = readings;
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading_history, parent, false);
        return new ReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        MeterReading reading = readings.get(position);
        
        // Set reading value and date
        holder.readingValue.setText(reading.getReading() + " m³");
        if (reading.getTimestamp() != null) {
            holder.readingDate.setText(dateFormat.format(reading.getTimestamp()));
        }
        
        // Set location if available
        if (reading.getAddress() != null && !reading.getAddress().isEmpty()) {
            holder.readingLocation.setVisibility(View.VISIBLE);
            holder.readingLocation.setText(reading.getAddress());
        } else {
            holder.readingLocation.setVisibility(View.GONE);
        }
        
        // Set notes if available
        if (reading.getNotes() != null && !reading.getNotes().isEmpty()) {
            holder.readingNotes.setVisibility(View.VISIBLE);
            holder.readingNotes.setText(reading.getNotes());
        } else {
            holder.readingNotes.setVisibility(View.GONE);
        }
        
        // Set timestamp if available
        if (reading.getTimestamp() != null) {
            holder.readingTimestamp.setVisibility(View.VISIBLE);
            holder.readingTimestamp.setText(timeFormat.format(reading.getTimestamp()));
        } else {
            holder.readingTimestamp.setVisibility(View.GONE);
        }
        
        // Set submission status
        holder.readingSubmitted.setText(reading.isSubmitted() ? "Beküldve" : "Nincs beküldve");
        holder.readingSubmitted.setTextColor(reading.isSubmitted() ? 
            holder.itemView.getContext().getColor(android.R.color.holo_green_dark) : 
            holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        
        // Handle photo
        if (reading.getPhotoUrl() != null && !reading.getPhotoUrl().isEmpty()) {
            holder.photoIcon.setVisibility(View.VISIBLE);
            holder.meterPhoto.setVisibility(View.VISIBLE);
            
            // Load image from Firebase Storage URL
            Glide.with(holder.itemView.getContext())
                .load(reading.getPhotoUrl())
                .into(holder.meterPhoto);
        } else {
            holder.photoIcon.setVisibility(View.GONE);
            holder.meterPhoto.setVisibility(View.GONE);
        }
        
        // Add fade-in animation
        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return readings.size();
    }

    static class ReadingViewHolder extends RecyclerView.ViewHolder {
        TextView readingValue;
        TextView readingDate;
        TextView readingLocation;
        TextView readingNotes;
        TextView readingTimestamp;
        TextView readingSubmitted;
        ImageView photoIcon;
        ImageView meterPhoto;

        ReadingViewHolder(View itemView) {
            super(itemView);
            readingValue = itemView.findViewById(R.id.textViewReading);
            readingDate = itemView.findViewById(R.id.textViewDate);
            readingLocation = itemView.findViewById(R.id.textViewLocation);
            readingNotes = itemView.findViewById(R.id.textViewNotes);
            readingTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            readingSubmitted = itemView.findViewById(R.id.textViewStatus);
            photoIcon = itemView.findViewById(R.id.imageViewPhotoIcon);
            meterPhoto = itemView.findViewById(R.id.imageViewPhoto);
        }
    }
} 