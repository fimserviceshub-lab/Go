package com.fsacts.go;

import android.app.backup.BackupAgent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomList extends AppCompatActivity {

    //Define variables
    ImageView img_location;
    ImageButton img_home_custom, img_list_custom;
    TextView lv_copyright, lv_no_location;
    RecyclerView recyclerView;
    DBHelper dbHelper;
    ArrayList<LocationModel> listItems;
    AlertDialog.Builder alertBuilder;

    LocationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_list);

        //Lock the screen to Portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Create reference to UI elements
        img_location = findViewById(R.id.img_location);
        lv_no_location = findViewById(R.id.lv_no_location);

        //------------------------------------------------------------------------------------------DBHelper, RecyclerView & LocationAdapter
        //Get data from DB and populate the listItems ArrayList
        dbHelper = new DBHelper(this);
        listItems = new ArrayList<>();
        listItems = (ArrayList<LocationModel>) dbHelper.getAllLocations();

        if(listItems.size() > 0){
            img_location.setVisibility(View.GONE);
            lv_no_location.setVisibility(View.GONE);

            //Connect recyclerView with UI element & configure
            recyclerView = findViewById(R.id.location_recyclear);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            //Finally initiate LocationAdapter and inject recyclerView into it
            adapter = new LocationAdapter(listItems, this);
            recyclerView.setAdapter(adapter);

            //All onClick activities on adapter
            adapter.setOnItemClickListener(new LocationAdapter.OnItemClickListener() {

                @Override
                public void onEditItem(int position, String shareTitle, String shareDate, String shareTime, String shareAddress, String shareNote) {
                    Intent intent = new Intent(CustomList.this, EditLocationForm.class);

                    //Get the current location details
                    LocationModel currentLocation = listItems.get(position);
                    //Log.i("CurrentLocation", currentLocation.toString());
                    intent.putExtra("location_id", currentLocation.getId());
                    intent.putExtra("location_title", currentLocation.getLocation_title());
                    intent.putExtra("location_date", currentLocation.getLocation_date());
                    intent.putExtra("location_time", currentLocation.getLocation_time());
                    intent.putExtra("location_address", currentLocation.getLocation_address());
                    intent.putExtra("location_note", currentLocation.getLocation_note());

                    startActivity(intent);
                }

                //Share specific location address with someone
                @Override
                public void onShareItem(int position, String shareDate, String shareTime, String shareAddress) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "I would like to share an address which I visited on " + shareDate
                            + " at (" + shareTime + "): " + shareAddress + "\n\n--Location is captured by Go@FSActs-Tech");

                    if(intent.resolveActivity(getPackageManager()) != null){
                        startActivity(Intent.createChooser(intent, "Share location: "));
                    }
                }

                //Delete a specific location information
                @Override
                public void onDeleteItem(int position, int itemId, int locationListSize) {
                    alertBuilder = new AlertDialog.Builder(CustomList.this);

                    alertBuilder.setTitle("Delete location:")
                            .setMessage("Do you want to delete this location?")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    listItems.remove(position); //Remove item from the list
                                    adapter.notifyItemRemoved(position); //Notify adapter to display changes
                                    dbHelper.deleteOne(itemId); //Remove item from DB
                                    Toast.makeText(CustomList.this, "Location is removed", Toast.LENGTH_SHORT).show();

                                    //If locationList has only one entry and that is deleted then show the default image and message
                                    if(locationListSize == 1){
                                        img_location.setVisibility(View.VISIBLE);
                                        lv_no_location.setVisibility(View.VISIBLE);
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }).show();
                }
            });
        }

        //------------------------------------------------------------------------------------------Bottom of the screen
        //Create reference to UI elements
        img_home_custom = findViewById(R.id.img_home_custom);
        img_list_custom = findViewById(R.id.img_list_custom);
        //lv_copyright = findViewById(R.id.lv_copyright);

        //Set Copyright dialog with current year
        Date today = new Date();
        SimpleDateFormat format_year = new SimpleDateFormat("yyyy");
        String yearToStr = format_year.format(today);
        //lv_copyright.setText("Copyright @ " + yearToStr + " FSActs-Tech");

        //OnClick event for Globe Image Button
        ImageButton globeImageButton = (ImageButton) img_home_custom.findViewById(R.id.img_home_custom);
        globeImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(CustomList.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }//End of onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Activate Menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //Custom Menu
        MenuItem item = menu.findItem(R.id.location_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setBackgroundColor(ContextCompat.getColor(this, R.color.Title_COLOR));


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}