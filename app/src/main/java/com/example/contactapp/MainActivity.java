package com.example.contactapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // Vue
    private FloatingActionButton fab;
    private RecyclerView contactRv;

    // Base de données
    private DbHelper dbHelper;

    // Adaptateur
    private AdapterContact adapterContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser la base de données
        dbHelper = new DbHelper(this);

        // Initialisation
        fab = findViewById(R.id.fab);
        contactRv = findViewById(R.id.contactRv);

        contactRv.setHasFixedSize(true);

        // Ajouter un auditeur
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Passer à une nouvelle activité pour ajouter un contact
                Intent intent = new Intent(MainActivity.this, AddEditContact.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });

        loadData();
    }

    private void loadData() {
        adapterContact = new AdapterContact(this, dbHelper.getAllData());
        contactRv.setAdapter(adapterContact);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Rafraîchir les données
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_top_menu, menu);
        // Obtenir l'élément de recherche dans le menu
        MenuItem item = menu.findItem(R.id.searchContact);
        // Zone de recherche
        SearchView searchView = (SearchView) item.getActionView();
        // Définir la largeur maximale
        // ...

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String name) {
                searchContact(name);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchContact(newText);
                return true;
            }
        });

        return true;
    }

    private void searchContact(String name) {
        adapterContact = new AdapterContact(this,
                dbHelper.getSearchContact(name));
        contactRv.setAdapter(adapterContact);
    }

    @Override
    public boolean onOptionsItemSelected
            (@NonNull MenuItem item) {
        if (item.getItemId() ==
                R.id.deleteAllContact) {
            showDeleteConfirmationDialog();
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Supprimer tous les contacts");
        builder.setMessage("Êtes-vous sûr de vouloir supprimer tous les contacts ?");
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Supprimer tous les contacts
                dbHelper.deleteAllContact();
                onResume();
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Annuler la suppression
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
