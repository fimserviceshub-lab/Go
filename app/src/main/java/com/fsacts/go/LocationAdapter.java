package com.fsacts.go;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> implements Filterable {
    ArrayList<LocationModel> locationList;
    ArrayList<LocationModel> locationListAll;
    Context context;
    OnItemClickListener listener;

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        //Run on background thread
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<LocationModel> filteredList = new ArrayList<>();

            if(charSequence.toString().isEmpty()){
                filteredList.addAll(locationListAll);
            }else{
                for (LocationModel location: locationListAll){
                    if(location.getLocation_title().toLowerCase().contains(charSequence.toString().toLowerCase()) || location.getLocation_date().toLowerCase().contains(charSequence.toString().toLowerCase()) || location.getLocation_time().toLowerCase().contains(charSequence.toString().toLowerCase()) || location.getLocation_address().toLowerCase().contains(charSequence.toString().toLowerCase())||location.getLocation_note().toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filteredList.add(location);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        //Run on UI thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            locationList.clear();
            locationList.addAll((Collection<? extends LocationModel>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public interface OnItemClickListener{
        void onEditItem(int position, String shareTitle, String shareDate, String shareTime, String shareAddress, String shareNote);
        void onShareItem(int position, String shareDate, String shareTime, String shareAddress);
        void onDeleteItem(int position, int itemId, int locationListSize);
    }

    public LocationAdapter(ArrayList<LocationModel> locationList, Context context) {
        this.locationList = locationList;
        this.locationListAll = new ArrayList<>(locationList);
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener){
        listener = clickListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.locationentry, parent, false);
        return new LocationViewHolder(v, listener, locationList);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationModel locationModel = locationList.get(position);
        holder.cv_title.setText(locationModel.getLocation_title());
        holder.cv_date.setText(locationModel.getLocation_date());
        holder.cv_time.setText(locationModel.getLocation_time());

        //Open any selected address with Google Map
        holder.cv_address.setText(Html.fromHtml("<a href=\"http://maps.google.com/maps?q=" +  locationModel.getLocation_address().toString()   +"\">" + locationModel.getLocation_address() + "</a>"));
        holder.cv_address.setMovementMethod(LinkMovementMethod.getInstance());

        holder.cv_note.setText(locationModel.getLocation_note());
    }

    @Override
    public int getItemCount() { return locationList.size(); }

    public class LocationViewHolder extends RecyclerView.ViewHolder {

        TextView cv_title, cv_date, cv_time, cv_address, cv_note;
        ImageView btn_edit, btn_share, btn_delete;

        public LocationViewHolder(@NonNull View itemView, OnItemClickListener listener, ArrayList<LocationModel> locationList) {
            super(itemView);
            cv_title = itemView.findViewById(R.id.cv_title);
            cv_date = itemView.findViewById(R.id.cv_date);
            cv_time = itemView.findViewById(R.id.cv_time);
            cv_address = itemView.findViewById(R.id.cv_address);
            cv_note = itemView.findViewById(R.id.cv_note);

            btn_edit = itemView.findViewById(R.id.btn_edit);
            btn_share = itemView.findViewById(R.id.btn_share);
            btn_delete = itemView.findViewById(R.id.btn_delete);

            //To edit a specific location title & note
            btn_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LocationModel locationModel = locationList.get(getAdapterPosition());
                    listener.onEditItem(getAdapterPosition(), locationModel.getLocation_title(), locationModel.getLocation_date(), locationModel.getLocation_time(), locationModel.getLocation_address(), locationModel.getLocation_note());
                }
            });

            //To share a specific location address
            btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LocationModel locationModel = locationList.get(getAdapterPosition());
                    listener.onShareItem(getAdapterPosition(), locationModel.getLocation_date(), locationModel.getLocation_time(), locationModel.getLocation_address());
                }
            });

            //To delete a specific location information
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LocationModel locationModel = locationList.get(getAdapterPosition());
                    listener.onDeleteItem(getAdapterPosition(), locationModel.getId(), locationList.size());
                }
            });
        }
    }
}
