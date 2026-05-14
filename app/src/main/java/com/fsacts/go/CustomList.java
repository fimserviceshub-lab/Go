package com.fsacts.go;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CustomList extends AppCompatActivity {

    private ImageView imgLocation;
    private TextView lvNoLocation;
    private RecyclerView recyclerView;
    private DBHelper dbHelper;
    private LocationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_list);

        imgLocation = findViewById(R.id.img_location);
        lvNoLocation = findViewById(R.id.lv_no_location);
        recyclerView = findViewById(R.id.location_recyclear);
        FloatingActionButton imgHomeCustom = findViewById(R.id.img_home_custom);

        dbHelper = new DBHelper(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new LocationAdapter.OnItemClickListener() {
            @Override
            public void onEditItem(@NonNull LocationModel locationModel) {
                Intent intent = new Intent(CustomList.this, EditLocationForm.class);
                intent.putExtra("location_id", locationModel.getId());
                intent.putExtra("location_title", locationModel.getTitle());
                intent.putExtra("location_date", locationModel.getLocationDate());
                intent.putExtra("location_time", locationModel.getLocationTime());
                intent.putExtra("location_address", locationModel.getAddress());
                intent.putExtra("location_note", locationModel.getNote());
                startActivity(intent);
            }

            @Override
            public void onShareItem(@NonNull LocationModel locationModel) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(
                        Intent.EXTRA_TEXT,
                        "I would like to share a place I visited on "
                                + locationModel.getLocationDate()
                                + " at "
                                + locationModel.getLocationTime()
                                + ":\n"
                                + locationModel.getAddress()
                                + "\n\nhttps://www.google.com/maps/search/?api=1&query="
                                + locationModel.getLatitude()
                                + ","
                                + locationModel.getLongitude()
                );
                startActivity(Intent.createChooser(intent, "Share location"));
            }

            @Override
            public void onDeleteItem(@NonNull LocationModel locationModel) {
                new AlertDialog.Builder(CustomList.this)
                        .setTitle("Delete location")
                        .setMessage("Do you want to delete this saved location?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            boolean deleted = dbHelper.deleteOne(locationModel.getId());
                            if (deleted) {
                                Toast.makeText(CustomList.this, "Location removed.", Toast.LENGTH_SHORT).show();
                                loadLocations();
                            } else {
                                Toast.makeText(CustomList.this, "Unable to delete that location.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        imgHomeCustom.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLocations();
    }

    private void loadLocations() {
        List<LocationModel> locations = dbHelper.getAllLocations();
        adapter.submitItems(locations);

        boolean hasLocations = !locations.isEmpty();
        imgLocation.setVisibility(hasLocations ? View.GONE : View.VISIBLE);
        lvNoLocation.setVisibility(hasLocations ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.location_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setBackgroundColor(ContextCompat.getColor(this, R.color.Title_COLOR));
        searchView.setQueryHint("Search saved locations");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }
}
