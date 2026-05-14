package com.fsacts.go;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditLocationForm extends AppCompatActivity {

    private FloatingActionButton btnEditClose;
    private EditText ptEditTitle;
    private EditText mtEditNote;
    private TextView lbEditDate;
    private TextView lbEditTime;
    private TextView lbEditAddressDisplay;
    private TextView lbTitleLength;
    private TextView lbNoteLength;
    private Button btnEditSave;
    private DBHelper databaseHelper;

    private long locationId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location_form);

        btnEditClose = findViewById(R.id.btn_edit_close);
        ptEditTitle = findViewById(R.id.pt_edit_title);
        mtEditNote = findViewById(R.id.mt_edit_note);
        lbEditDate = findViewById(R.id.lb_edit_date);
        lbEditTime = findViewById(R.id.lb_edit_time);
        lbEditAddressDisplay = findViewById(R.id.lb_edit_address_display);
        lbTitleLength = findViewById(R.id.lb_title_length);
        lbNoteLength = findViewById(R.id.lb_note_length);
        btnEditSave = findViewById(R.id.btn_edit_save);

        databaseHelper = new DBHelper(this);
        btnEditSave.setEnabled(false);

        btnEditClose.setOnClickListener(view -> finish());

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Toast.makeText(this, "Unable to load the location details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        locationId = extras.getLong("location_id", -1L);
        String locationTitle = extras.getString("location_title", "");
        String locationDate = extras.getString("location_date", "");
        String locationTime = extras.getString("location_time", "");
        String locationAddress = extras.getString("location_address", "");
        String locationNote = extras.getString("location_note", "");

        ptEditTitle.setText(locationTitle);
        mtEditNote.setText(locationNote);
        lbEditDate.setText(locationDate);
        lbEditTime.setText(locationTime);
        lbEditAddressDisplay.setText(locationAddress);
        lbTitleLength.setText(String.valueOf(locationTitle.length()));
        lbNoteLength.setText(String.valueOf(locationNote.length()));

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lbTitleLength.setText(String.valueOf(ptEditTitle.getText().length()));
                lbNoteLength.setText(String.valueOf(mtEditNote.getText().length()));
                btnEditSave.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        ptEditTitle.addTextChangedListener(textWatcher);
        mtEditNote.addTextChangedListener(textWatcher);

        btnEditSave.setOnClickListener(view -> saveChanges());
    }

    private void saveChanges() {
        boolean success = databaseHelper.updateLocation(
                locationId,
                ptEditTitle.getText().toString(),
                mtEditNote.getText().toString()
        );

        if (success) {
            Toast toast = Toast.makeText(this, "Changes saved successfully.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 190);
            toast.show();
            finish();
        } else {
            Toast.makeText(this, "Unable to save changes.", Toast.LENGTH_SHORT).show();
        }
    }
}
