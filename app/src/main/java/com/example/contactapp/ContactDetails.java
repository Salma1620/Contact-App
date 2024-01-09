package com.example.contactapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Format;
import java.util.Calendar;
import java.util.Locale;

public class ContactDetails extends AppCompatActivity {

    // Vue
    private TextView nameTv, phoneTv, emailTv, noteTv;
    private ImageView profileIv;

    private String id;

    // Assistant de base de données
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        // Initialiser la base de données
        dbHelper = new DbHelper(this);

        // Obtenir les données de l'intent
        Intent intent = getIntent();
        id = intent.getStringExtra("contactId");

        // Initialiser la vue
        nameTv = findViewById(R.id.nameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        noteTv = findViewById(R.id.noteTv);

        profileIv = findViewById(R.id.profileIv);

        loadDataById();

    }

    private void loadDataById() {

        // Obtenir les données de la base de données
        // Requête pour trouver les données par ID
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.C_ID + " =\"" + id + "\"";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                // Obtenir les données
                String name = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_NAME));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_PHONE));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_EMAIL));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_NOTE));

                // Définir les données
                nameTv.setText(name);
                phoneTv.setText(phone);
                emailTv.setText(email);
                noteTv.setText(note);

                if (image.equals("null")) {
                    profileIv.setImageResource(R.drawable.ic_baseline_person_24);
                } else {
                    profileIv.setImageURI(Uri.parse(image));
                }

            } while (cursor.moveToNext());
        }

        db.close();

    }
}
