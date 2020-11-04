package com.example.bookworm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import android.text.TextUtils;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.ArrayList;

import static com.example.bookworm.ViewPhotoFragment.newInstance;

public class AddBookActivity extends AppCompatActivity {
    private Book book;
    private FirebaseAuth fAuth;
    private EditText titleEditText;
    private EditText authorEditText;
    private EditText isbnEditText;
    private EditText descriptionEditText;
    private TextView ownerNameText;
    private Button viewPhotoButton;
    private Button addPhotoButton;
    private Button deletePhotoButton;
    private Button addButton;
    private ImageView profilePhoto;
    private Uri photoUri;
    private StorageReference storageReference;
    private final int RESULT_LOAD_IMAGE = 100;
    private String sPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        // Define views
        titleEditText = findViewById(R.id.keywordSearchBar);
        authorEditText = findViewById(R.id.editTextTextPersonName2);
        isbnEditText = findViewById(R.id.editTextNumber);
        descriptionEditText = findViewById(R.id.editTextTextPersonName4);
        ownerNameText = findViewById(R.id.textView8);
        viewPhotoButton = findViewById(R.id.button3);
        addPhotoButton = findViewById(R.id.button4);
        deletePhotoButton = findViewById(R.id.button5);
        addButton = findViewById(R.id.button6);
        profilePhoto = findViewById(R.id.profile_photo);
        profilePhoto.setTag(R.drawable.ic_book);

        //BookPhoto.setImageResource(R.drawable.ic_book);


        // Define authentication and storage references

        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        final String email = fAuth.getCurrentUser().getEmail();
        final String uid = fAuth.getCurrentUser().getUid();

//        checkFilePermissions();

        CollectionReference users = FirebaseFirestore.getInstance().collection("Libraries").document("Main_Library").collection("users");
        Query query = users.whereEqualTo("email", email);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        ownerNameText.setText(document.getId());
                        book = new Book(document.getId(), uid);
                    }
                }
            }
        });


        viewPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BitmapDrawable drawable = (BitmapDrawable) profilePhoto.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    ViewPhotoFragment fragment = newInstance(bitmap);
                    fragment.show(getSupportFragmentManager(), "VIEW_PHOTO");
                } catch (Exception e) {
                    Toast.makeText(AddBookActivity.this, "No book photo available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddImage();
            }
        });

        deletePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DelImage();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the fields from the UI
                String title = titleEditText.getText().toString();
                String author = authorEditText.getText().toString();
                String isbn = isbnEditText.getText().toString();
                ArrayList<String> descriptions = new ArrayList<String>();
                String[] ss = descriptionEditText.getText().toString().split(" ");
                for (String s : ss) {
                    if (!TextUtils.isEmpty(s)) {
                        descriptions.add(s);
                    }
                }

                // Define needed information
                String userID = FirebaseAuth.getInstance().getUid();
                String path = "images/users/" + userID + "/books/" + isbn + ".jpg";
                String owner = ownerNameText.getText().toString();

                // If the form is missing information
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) || TextUtils.isEmpty(isbn)) {
                    Toast.makeText(AddBookActivity.this, "Title, author, and ISBN are required", Toast.LENGTH_SHORT).show();
                } else {
                    // Attempt to upload the image.
                    Database.writeProfilePhoto(storageReference.child(path), userID, photoUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // The form does not have missing information
                                book.setTitle(title);
                                book.setAuthor(author);
                                book.setIsbn(isbn);
                                book.setPhotograph(sPhoto);
                                book.setDescription(descriptions);

                                // Write the book to the database
                                final ArrayList<Integer> returnValue = new ArrayList<Integer>();
                                returnValue.add(0);
                                Database.writeBook(book);
                                Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    if (Database.getListenerSignal() == 1) {
                                        Toast.makeText(AddBookActivity.this,
                                            "Your book is successfully saved",
                                            Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else if (Database.getListenerSignal() == -1){
                                        Toast.makeText(AddBookActivity.this,
                                            "Something went wrong while adding your book",
                                            Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AddBookActivity.this,
                                            "Something went wrong while adding your book, please try again",
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }, 1000);
                            }
                        })
                            .addOnFailureListener(e -> Toast.makeText(AddBookActivity.this,
                                    "Image could not be written to database",
                                    Toast.LENGTH_SHORT)
                                    .show()
                            );
                }
            }
        });
    }



    private void AddImage() {
        if ((int) profilePhoto.getTag() == R.drawable.ic_book) {
            // Defining Implicit Intent to mobile gallery
//            Intent intent = new Intent();
//            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), ADD_IMAGE_REQUEST);
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
        }
        else{
            Toast.makeText(AddBookActivity.this, "Book Photo already exists! please try to remove then add a new one.", Toast.LENGTH_SHORT).show();
        }
    }

    private void DelImage() {
        if ((int) profilePhoto.getTag() != R.drawable.ic_book) {
            sPhoto = null;
            profilePhoto.setImageResource(R.drawable.ic_book);
            profilePhoto.setTag(R.drawable.ic_book);
        }
        else{
            Toast.makeText(AddBookActivity.this, "Book Photo is empty.", Toast.LENGTH_SHORT).show();
        }
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }


    // Override onActivityResult method

    /**
     * Get images from user's phone
     * https://stackoverflow.com/questions/38352148/get-image-from-the-gallery-and-show-in-imageview#38352844
     * User: Atul Mavani
     * Accessed Oct. 31, 2020
     *
     * @param reqCode the request from the activity
     * @param resultCode  the result of the activity (OK / failure)
     * @param data        any data that was returned
     */
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                this.photoUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(this.photoUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                sPhoto = BitMapToString(selectedImage);
                profilePhoto.setImageBitmap(selectedImage);
                profilePhoto.setTag(0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(AddBookActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(AddBookActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }
                                              }




//    /**
//     * Runs when the application first begins, ensures that we have permission to access the user's data.
//     * https://github.com/mitchtabian/Firebase-Save-Images/blob/master/FirebaseUploadImage/app/src/main/java/com/tabian/firebaseuploadimage/AddBookActivity.java
//     * Accessed October 31, 2020
//     */
//    private void checkFilePermissions() {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
//            int permissionCheck = AddBookActivity.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
//            permissionCheck += AddBookActivity.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
//            if (permissionCheck != 0) {
//                this.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1001); //Any number
//            }
//        } else {
//            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
//        }
//    }