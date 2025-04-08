package com.example.mobilprojekt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReadingHistoryAdapter extends RecyclerView.Adapter<ReadingHistoryAdapter.ReadingViewHolder> {

    private List<MeterReading> readings;

    public ReadingHistoryAdapter(List<MeterReading> readings) {
        this.readings = readings;
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading_history, parent, false);
        return new ReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        MeterReading reading = readings.get(position);
        holder.textViewReadingValue.setText("Óraállás: " + reading.getReading());
        holder.textViewReadingDate.setText("Dátum: " + reading.getDate());
        
        setFadeInAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return readings != null ? readings.size() : 0;
    }

    private void setFadeInAnimation(View view) {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        animation.setStartOffset(200);
        view.startAnimation(animation);
    }

    static class ReadingViewHolder extends RecyclerView.ViewHolder {
        TextView textViewReadingValue;
        TextView textViewReadingDate;

        ReadingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewReadingValue = itemView.findViewById(R.id.textViewReadingValue);
            textViewReadingDate = itemView.findViewById(R.id.textViewReadingDate);
        }
    }
} 