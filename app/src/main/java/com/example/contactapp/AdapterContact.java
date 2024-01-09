package com.example.contactapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;

import java.util.ArrayList;

public class AdapterContact extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final ArrayList<ModelContact> contactList;
    private final DbHelper dbHelper;

    public AdapterContact(Context context, ArrayList<ModelContact> contactList) {
        this.context = context;
        this.contactList = contactList;
        dbHelper = new DbHelper(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = createContactView(parent);
        return new RecyclerView.ViewHolder(view) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ModelContact modelContact = contactList.get(position);
        bindContactData(holder.itemView, modelContact);
        setupClickListeners(holder.itemView, modelContact);
    }

    private View createContactView(ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.row_contact_item, parent, false);
    }

    private void bindContactData(View itemView, ModelContact modelContact) {
        // Obtenir les données
        String name = modelContact.getName();
        String phone = modelContact.getPhone();
        String image = modelContact.getImage();

        // Définir les données dans la vue
        TextView contactName = itemView.findViewById(R.id.contact_name);
        TextView contactPhone = itemView.findViewById(R.id.contact_phone);
        ImageView contactImage = itemView.findViewById(R.id.contact_image);

        contactName.setText(name);
        contactPhone.setText(phone);
        if (image.equals("null")) {
            contactImage.setImageResource(R.drawable.ic_baseline_person_24);
        } else {
            contactImage.setImageURI(Uri.parse(image));
        }
    }

    private void setupClickListeners(View itemView, ModelContact modelContact) {
        // Gérer le clic pour lancer l'application d'appel
        itemView.findViewById(R.id.contact_number_dial).setOnClickListener(view -> dialContact(modelContact.getPhone()));

        // Gérer le clic pour lancer l'application de messagerie
        itemView.findViewById(R.id.contact_message_dial).setOnClickListener(view -> messageContact(modelContact.getPhone()));

        // Gérer le clic sur l'élément pour afficher les détails du contact
        itemView.findViewById(R.id.mainLayout).setOnClickListener(view -> openContactDetails(modelContact.getId()));

        // Gérer le clic sur le bouton d'édition
        itemView.findViewById(R.id.contact_edit).setOnClickListener(view -> editContact(modelContact));

        // Gérer le clic sur le bouton de suppression
        itemView.findViewById(R.id.contact_delete).setOnClickListener(view -> deleteContact(modelContact.getId()));
    }

    private void dialContact(String phoneNumber) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(dialIntent);
    }

    private void messageContact(String phoneNumber) {
        Intent messageIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
        context.startActivity(messageIntent);
    }

    private void openContactDetails(String contactId) {
        Intent intent = new Intent(context, ContactDetails.class);
        intent.putExtra("contactId", contactId);
        context.startActivity(intent);
    }

    private void editContact(ModelContact modelContact) {
        Intent intent = new Intent(context, AddEditContact.class);
        intent.putExtra("ID", modelContact.getId());
        intent.putExtra("NAME", modelContact.getName());
        intent.putExtra("PHONE", modelContact.getPhone());
        intent.putExtra("EMAIL", modelContact.getEmail());
        intent.putExtra("NOTE", modelContact.getNote());
        intent.putExtra("IMAGE", modelContact.getImage());
        intent.putExtra("isEditMode", true);
        context.startActivity(intent);
    }

    private void deleteContact(String contactId) {
        dbHelper.deleteContact(contactId);
        ((MainActivity) context).onResume();
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }
}