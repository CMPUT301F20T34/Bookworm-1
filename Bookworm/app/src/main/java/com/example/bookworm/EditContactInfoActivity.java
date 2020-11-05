package com.example.bookworm;

<<<<<<< HEAD
public class EditContactInfoActivity {
=======
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

import java.io.IOException;

import javax.annotation.Nullable;

public class EditContactInfoActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;

    private String username = "";
    private TextView usernameView;
    private TextView phoneEditView;
    private TextView emailEditView;
    private ImageView contactImage;

    private final int PICK_IMAGE_REQUEST = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact_info);

        fAuth = FirebaseAuth.getInstance();

        if(getIntent().getExtras() != null){
            username = getIntent().getStringExtra("username");
        }

        usernameView = (TextView) findViewById(R.id.usernameView);
        phoneEditView = (TextView) findViewById(R.id.editPhoneNumber);
        emailEditView = (TextView) findViewById(R.id.editEmail);
        contactImage = (ImageView) findViewById(R.id.contactImage);

        if(username != ""){
            usernameView.setText(username);
            phoneEditView.setText("Loading phone number...");
            emailEditView.setText("Loading email...");
            Database.getUser(username).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        phoneEditView.setText(task.getResult().get("phoneNumber").toString());
                        emailEditView.setText(task.getResult().get("email").toString());
                    }
                }});
        }
        //contactImage.setImageResource(); Need more info on how we are handling images
    }

    public void editImageButton(View view){
        ImageView contactImage = (ImageView) findViewById(R.id.contactImage);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
    }

    public void saveContactInfo(View view){
        // Implement save to firebase

        User userUpdate = new User(username, "", emailEditView.getText().toString(), phoneEditView.getText().toString());

        Database.updateUser(userUpdate);

        AlertDialog inputAlert = new AlertDialog.Builder(this).create();
        inputAlert.setTitle("Contact info saved for user:");
        inputAlert.setMessage(username);
        inputAlert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){

            Uri imageFilePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageFilePath);
                contactImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

>>>>>>> Fixed saving contact info and added my functionality to profile view.
}
