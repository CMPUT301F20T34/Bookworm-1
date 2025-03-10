package com.example.bookworm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

/**
 * Shows the profile of the current user in a static form.
 */
public class ProfileActivity extends AppCompatActivity {
    private FirebaseUser fUser;
    private String authEmail;
    private Context context = this;
    private TextView phoneNumber;
    private TextView email;
    private TextView username;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        phoneNumber = findViewById(R.id.phone_profile);
        email = findViewById(R.id.email_profile);
        username = findViewById(R.id.username_profile);
        profilePhoto = findViewById(R.id.profile_view_image);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        /* Get the email of the user. If they don't
            exist, redirect user to sign-up screen */
        if (fUser != null) {
            authEmail = fUser.getEmail();
        } else {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        }

        getUserInformation();
    }

    /**
     * Moves to the activity for editing the current user's profile
     * @param view
     */
    public void editProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), EditContactInfoActivity.class);
        TextView usernameView = findViewById(R.id.username_profile);
        String username = usernameView.getText().toString();
        intent.putExtra("username", username);
        startActivity(intent);
    }

    /**
     * Logs the current user out, and returns to the login activity.
     * @param view the view that was clicked on.
     */
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    /**
     * Gets the user information from the database, either on activity create
     * or activity resume
     */
    private void getUserInformation() {
        Database.getProfilePhoto(FirebaseAuth.getInstance().getUid())
            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Glide.with(context).load(task.getResult()).into(profilePhoto);
                    } else {
                        profilePhoto.setImageResource(R.drawable.ic_book);
                    }
                }
            });

        Database.getUserFromEmail(authEmail)
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> data = doc.getData();
                        Database.getProfilePhoto(doc.getId())
                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Glide.with(context).load(task.getResult()).into(profilePhoto);
                                    } else {
                                        profilePhoto.setImageResource(R.drawable.ic_book);
                                    }
                                }
                            });
                        phoneNumber.setText(data.get("phoneNumber").toString());
                        email.setText(data.get("email").toString());
                        username.setText(doc.getId());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this,
                    "Can't retrieve user information from database",
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInformation();
    }
}