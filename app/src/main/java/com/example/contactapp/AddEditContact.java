package com.example.contactapp;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddEditContact extends AppCompatActivity {

    private ImageView profileIv;
    private EditText nameEt, phoneEt, emailEt, noteEt;
    private Button fab;

    // Variables pour les données du contact
    private String id, image, name, phone, email, note;
    private Boolean isEditMode;

    // ActionBar
    private ActionBar actionBar;

    // Constantes de permission
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final int IMAGE_FROM_GALLERY_CODE = 300;
    private static final int IMAGE_FROM_CAMERA_CODE = 400;

    // Tableaux de chaînes pour les autorisations
    private String[] cameraPermission;
    private String[] storagePermission;

    // Variable d'URI pour l'image
    private Uri imageUri;

    // Assistant de base de données
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);

        // Initialiser la base de données
        dbHelper = new DbHelper(this);

        // Initialiser les autorisations
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Initialiser l'ActionBar
        actionBar = getSupportActionBar();

        // Bouton de retour
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Initialiser les vues
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        noteEt = findViewById(R.id.noteEt);
        fab = findViewById(R.id.fab);

        // Obtenir les données d'intention
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);

        if (isEditMode) {
            // Titre de la barre d'outils pour la modification
            actionBar.setTitle("Update the Contact");

            // Obtenir les autres valeurs de l'intention
            id = intent.getStringExtra("ID");
            name = intent.getStringExtra("NAME");
            phone = intent.getStringExtra("PHONE");
            email = intent.getStringExtra("EMAIL");
            note = intent.getStringExtra("NOTE");
            image = intent.getStringExtra("IMAGE");

            // Définir les valeurs dans les champs d'édition
            nameEt.setText(name);
            phoneEt.setText(phone);
            emailEt.setText(email);
            noteEt.setText(note);

            imageUri = Uri.parse(image);

            if (image.equals("null")) {
                profileIv.setImageResource(R.drawable.ic_baseline_person_24);
            } else {
                profileIv.setImageURI(imageUri);
            }

        } else {
            // Mode ajout
            actionBar.setTitle("Ajouter un contact");
        }

        // Ajouter un gestionnaire d'événements pour le bouton flottant
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        // Gestionnaire d'événements pour l'image de profil
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });
    }

    private void showImagePickerDialog() {

        // Options pour la boîte de dialogue
        String options[] = {"Caméra", "Galerie"};

        // Constructeur de boîte de dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Titre
        builder.setTitle("Choisir une option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Gérer le clic sur l'élément
                if (which == 0) { // Commence à partir de l'index 0
                    // Caméra sélectionnée
                    if (!checkCameraPermission()) {
                        // Demander la permission de la caméra
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (which == 1) {
                    // Galerie sélectionnée
                    if (!checkStoragePermission()) {
                        // Demander la permission de stockage
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }

                }
            }
        }).create().show();
    }

    private void pickFromGallery() {
        // Intent pour choisir une image depuis la galerie
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*"); // uniquement des images

        startActivityForResult(galleryIntent, IMAGE_FROM_GALLERY_CODE);
    }

    private void pickFromCamera() {
        // ContentValues pour les informations sur l'image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "IMAGE_TITLE");
        values.put(MediaStore.Images.Media.DESCRIPTION, "IMAGE_DETAIL");

        // Enregistrer l'URI de l'image
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent pour ouvrir la caméra
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(cameraIntent, IMAGE_FROM_CAMERA_CODE);
    }

    private void saveData() {

        // Obtenir les données fournies par l'utilisateur
        name = nameEt.getText().toString();
        phone = phoneEt.getText().toString();
        email = emailEt.getText().toString();
        note = noteEt.getText().toString();

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern patternemail = Pattern.compile(emailPattern);
        Matcher matcheremail = patternemail.matcher(email);

        String phonePattern = "^[0-9]{10}$";
        Pattern patternphone = Pattern.compile(phonePattern);
        Matcher matcherphone = patternphone.matcher(phone);

        // Vérifier les données du champ
        if (!name.isEmpty() && !phone.isEmpty() && !email.isEmpty() && !note.isEmpty() && matcheremail.matches() && matcherphone.matches()) {
            // Sauvegarder les données si l'utilisateur n'a qu'une seule donnée

            // Vérifier le mode d'édition ou d'ajout pour enregistrer les données dans SQLite
            if (isEditMode) {
                // Mode d'édition
                dbHelper.updateContact(
                        "" + id,
                        "" + imageUri,
                        "" + name,
                        "" + phone,
                        "" + email,
                        "" + note
                );

                Toast.makeText(getApplicationContext(), "Updated successfully...", Toast.LENGTH_SHORT).show();

            } else {
                // Mode ajout
                long id = dbHelper.insertContact(
                        "" + imageUri,
                        "" + name,
                        "" + phone,
                        "" + email,
                        "" + note
                );
                // Vérifier si l'insertion des données a réussi, afficher un message Toast
                Toast.makeText(getApplicationContext(), "Inserted successfully... " + id, Toast.LENGTH_SHORT).show();
            }

        }
        else if(name.isEmpty() || phone.isEmpty() || email.isEmpty() || note.isEmpty())
            // Afficher un message Toast
            Toast.makeText(getApplicationContext(), "empty inputs!", Toast.LENGTH_SHORT).show();
        else if(!matcheremail.matches())
            // Afficher un message Toast
            Toast.makeText(getApplicationContext(), "the email is not valid", Toast.LENGTH_SHORT).show();
        else if (!matcherphone.matches())
            // Afficher un message Toast
            Toast.makeText(getApplicationContext(), "the phone number is not valid", Toast.LENGTH_SHORT).show();

    }



    // Surcharge de la touche de retour
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    // Vérifier la permission de la caméra
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result & result1;
    }

    // Demander la permission de la caméra
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_PERMISSION_CODE);
    }

    // Vérifier la permission de stockage
    private boolean checkStoragePermission() {
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result1;
    }

    // Demander la permission de stockage
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_PERMISSION_CODE);
    }

    // Gérer la demande de permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0) {

                    // Si toutes les autorisations sont accordées, retourne vrai, sinon faux
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        // Les deux autorisations sont accordées
                        pickFromCamera();
                    } else {
                        // Permission non accordée
                        Toast.makeText(getApplicationContext(), "Autorisations de la caméra et du stockage nécessaires..", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_PERMISSION_CODE:
                if (grantResults.length > 0) {

                    // Si toutes les autorisations sont accordées, retourne vrai, sinon faux
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted) {
                        // Autorisation accordée
                        pickFromGallery();
                    } else {
                        // Permission non accordée
                        Toast.makeText(getApplicationContext(), "Autorisation de stockage nécessaire..", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    // Gérer le résultat de l'activité
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_FROM_GALLERY_CODE) {
                // Image choisie depuis la galerie
                // Recadrer l'image
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(AddEditContact.this);

            } else if (requestCode == IMAGE_FROM_CAMERA_CODE) {
                // Image choisie depuis la caméra
                // Recadrer l'image
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(AddEditContact.this);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                // Image recadrée reçue
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageUri = result.getUri();
                profileIv.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // Gestion des erreurs
                Toast.makeText(getApplicationContext(), "Quelque chose s'est mal passé", Toast.LENGTH_SHORT).show();
            }
        }
    }
}