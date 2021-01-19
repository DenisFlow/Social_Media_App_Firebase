package com.example.social_media_app_firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private EditText edtUserName, edtEmail, edtPassword;
    private Button btnSignUp, btnSignIn;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            // transit to next activity
            transitionToSocialMediaActivity();
        }

    }

    private void signUp() {
        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Sign up is successful.",
                                    Toast.LENGTH_SHORT).show();
//                            DatabaseRefernce mdatabaseref=FirebaseDatabase.getInstance().getReferenceFromUrl(url);.
//                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://social-media-app-firebas-88f1a-default-rtdb.firebaseio.com/");
//                            databaseReference.child("my_users").child(task.getResult().getUser().getUid()).child("username").setValue(edtUserName.getText().toString());
//                            FirebaseDatabase.getInstance().getReference().child("my_users").child(task.getResult().getUser().getUid()).child("username").setValue(edtUserName.getText().toString());
                            FirebaseDatabase.getInstance("https://social-media-app-firebas-88f1a-default-rtdb.firebaseio.com/").getReference().child("my_users").child(task.getResult().getUser().getUid()).child("username").setValue(edtUserName.getText().toString());

                            transitionToSocialMediaActivity();
                        } else {
                            Toast.makeText(MainActivity.this, "Sign up failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Sign is successful.",
                            Toast.LENGTH_SHORT).show();
//                    FirebaseDatabase.getInstance().getReference().child("my_users").child(task.getResult().getUser().getUid()).child("username").setValue(edtUserName.getText().toString());
                    transitionToSocialMediaActivity();

                } else {
                    Toast.makeText(MainActivity.this, "Sign is  failed.",
                            Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void transitionToSocialMediaActivity() {
        Intent intent = new Intent(this, SocialMediaActivity.class);
        startActivity(intent);
    }
}