package com.fsacts.go;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> implements Filterable {

    public interface OnItemClickListener {
        void onEditItem(@NonNull LocationModel locationModel);

        void onShareItem(@NonNull LocationModel locationModel);

        void onDeleteItem(@NonNull LocationModel locationModel);
    }

    private final Context context;
    private final ArrayList<LocationModel> visibleItems = new ArrayList<>();
    private final ArrayList<LocationModel> allItems = new ArrayList<>();
    private OnItemClickListener listener;

    public LocationAdapter(@NonNull Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitItems(@NonNull List<LocationModel> items) {
        allItems.clear();
        allItems.addAll(items);
        visibleItems.clear();
        visibleItems.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.locationentry, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationModel locationModel = visibleItems.get(position);
        holder.cvTitle.setText(locationModel.getTitle());
        holder.cvDate.setText(locationModel.getLocationDate());
        holder.cvTime.setText(locationModel.getLocationTime());
        holder.cvAddress.setText(locationModel.getAddress());
        holder.cvNote.setText(locationModel.getNote().isEmpty() ? "No note added" : locationModel.getNote());

        holder.cvAddress.setOnClickListener(view -> {
            String uri = "google.navigation:q=" + locationModel.getLatitude() + "," + locationModel.getLongitude();
            Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            googleIntent.setPackage("com.google.android.apps.maps");

            Intent fallbackIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=" + locationModel.getLatitude() + "," + locationModel.getLongitude())
            );

            if (googleIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(googleIntent);
            } else if (fallbackIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(fallbackIntent);
            }
        });

        holder.btnEdit.setOnClickListener(view -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onEditItem(visibleItems.get(adapterPosition));
            }
        });

        holder.btnShare.setOnClickListener(view -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onShareItem(visibleItems.get(adapterPosition));
            }
        });

        holder.btnDelete.setOnClickListener(view -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteItem(visibleItems.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<LocationModel> filteredItems = new ArrayList<>();
            String query = constraint == null ? "" : constraint.toString().trim().toLowerCase(Locale.US);

            if (query.isEmpty()) {
                filteredItems.addAll(allItems);
            } else {
                for (LocationModel location : allItems) {
                    if (location.getTitle().toLowerCase(Locale.US).contains(query)
                            || location.getLocationDate().toLowerCase(Locale.US).contains(query)
                            || location.getLocationTime().toLowerCase(Locale.US).contains(query)
                            || location.getAddress().toLowerCase(Locale.US).contains(query)
                            || location.getNote().toLowerCase(Locale.US).contains(query)) {
                        filteredItems.add(location);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredItems;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            visibleItems.clear();
            visibleItems.addAll((List<LocationModel>) results.values);
            notifyDataSetChanged();
        }
    };

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        final TextView cvTitle;
        final TextView cvDate;
        final TextView cvTime;
        final TextView cvAddress;
        final TextView cvNote;
        final FloatingActionButton btnEdit;
        final FloatingActionButton btnShare;
        final FloatingActionButton btnDelete;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            cvTitle = itemView.findViewById(R.id.cv_title);
            cvDate = itemView.findViewById(R.id.cv_date);
            cvTime = itemView.findViewById(R.id.cv_time);
            cvAddress = itemView.findViewById(R.id.cv_address);
            cvNote = itemView.findViewById(R.id.cv_note);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
