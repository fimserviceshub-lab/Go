package com.fsacts.go;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditLocationForm extends AppCompatActivity {

    FloatingActionButton btn_edit_close;
    EditText pt_edit_title, mt_edit_note;
    TextView lb_edit_date, lb_edit_time, lb_edit_address_display, lb_title_length, lb_note_length;
    Button btn_edit_save;
    DBHelper databaseHelper;
    String location_id, old_location_title, location_title, location_date, location_time, location_address, old_location_note, location_note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location_form);

        //Create reference to UI elements
        btn_edit_close = findViewById(R.id.btn_edit_close);
        pt_edit_title = findViewById(R.id.pt_edit_title);
        lb_title_length = findViewById(R.id.lb_title_length);

        lb_edit_date = findViewById(R.id.lb_edit_date);
        lb_edit_time = findViewById(R.id.lb_edit_time);
        lb_edit_address_display = findViewById(R.id.lb_edit_address_display);
        mt_edit_note = findViewById(R.id.mt_edit_note);
        lb_note_length = findViewById(R.id.lb_note_length);

        btn_edit_save = findViewById(R.id.btn_edit_save);

        btn_edit_save.setEnabled(false);

        //OnClick event on close action button
        ImageButton listImageButton = (ImageButton) btn_edit_close.findViewById(R.id.btn_edit_close);
        listImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(EditLocationForm.this, CustomList.class);
                startActivity(intent);
            }
        });

        //Listen from incoming messages from CustomList
        Bundle incomingMessages = getIntent().getExtras();

        if (incomingMessages != null) {
            //Capture incoming data
            location_id = incomingMessages.get("location_id").toString();
            location_title = incomingMessages.getString("location_title");
            location_date = incomingMessages.getString("location_date");
            location_time = incomingMessages.getString("location_time");
            location_address = incomingMessages.getString("location_address");
            location_note = incomingMessages.getString("location_note");

            //Fill in the form
            pt_edit_title.setText(location_title);
            lb_edit_date.setText(location_date);
            lb_edit_time.setText(location_time);
            lb_edit_address_display.setText(location_address);
            mt_edit_note.setText(location_note);

            //Set current length of EditText strings
            lb_title_length.setText(""+location_title.toString().length());
            lb_note_length.setText(""+location_note.toString().length());

        }else{
            Toast.makeText(EditLocationForm.this, "Error on editing location details!", Toast.LENGTH_SHORT).show();
        }

        //While changing location title
        pt_edit_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btn_edit_save.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = pt_edit_title.getText().toString();
                int num = s.length();
                lb_title_length.setText(""+(int)num);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                btn_edit_save.setEnabled(true);
            }
        });

        //While changing location note
        mt_edit_note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btn_edit_save.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = mt_edit_note.getText().toString();
                int num = s.length();
                lb_note_length.setText(""+(int)num);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                btn_edit_save.setEnabled(true);
            }
        });

        //Initializing database
        databaseHelper = new DBHelper(this);

        //OnClick event on Save changes
        btn_edit_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                boolean success = databaseHelper.updateLocation(Integer.parseInt(location_id), pt_edit_title.getText().toString(), mt_edit_note.getText().toString());

                if(success == true){
                    Toast toast= Toast.makeText(EditLocationForm.this, "Changes are saved successfully", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 190);
                    toast.show();

                    //Toast.makeText(EditLocationForm.this, "Changes are saved successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(EditLocationForm.this, "Error on saving changes!", Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(EditLocationForm.this, CustomList.class);
                startActivity(intent);
            }
        });

    }
}