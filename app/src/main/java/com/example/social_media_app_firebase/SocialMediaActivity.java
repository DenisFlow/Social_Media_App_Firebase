package com.example.social_media_app_firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseAuth mAuth;

    private Button btnCreatePost;
    private EditText edtDescription;
    private ListView listViewUsers;
    private ImageView imageViewPost;
    private String imageIdentifier;
    private String uploadedImageLink;

    private ArrayList<String> usernames;
    private ArrayAdapter adapter;

    private ArrayList<String> uids;

    Bitmap receivedImageBitmap;
    private String imageDownloadLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_social_media);
        mAuth = FirebaseAuth.getInstance();

        btnCreatePost = findViewById(R.id.btnCreatePost);
        edtDescription = findViewById(R.id.edtDescription);
        listViewUsers = findViewById(R.id.listViewUsers);
        imageViewPost = findViewById(R.id.imageViewPost);
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, usernames);
        uids = new ArrayList<>();
        listViewUsers.setOnItemClickListener(this);


        listViewUsers.setAdapter(adapter);

        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageToServer();
            }
        });

        imageViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectImage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000){
            if (resultCode == Activity.RESULT_OK && data != null){
                Uri chosenImageData = data.getData();

                try {
                    receivedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageData);
                    imageViewPost.setImageBitmap(receivedImageBitmap);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutItem:
                logout();
                break;
            case R.id.viewPostsItem:
                Intent intent = new Intent(this, ViewPostActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logout();
    }

    private void logout(){
        mAuth.signOut();
        finish();
    }

    public void selectImage() {
        if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(SocialMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            }

        } else {
            getChosenImage();

        }

    }


    private void getChosenImage() {
//        FancyToast.makeText(getContext(), "Now we can access the image", Toast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1000);
    }


    private void uploadImageToServer() {

        if (receivedImageBitmap != null) {
            imageViewPost.setDrawingCacheEnabled(true);
            imageViewPost.buildDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            receivedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            imageIdentifier = UUID.randomUUID().toString() + ".png";
            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SocialMediaActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(SocialMediaActivity.this, "Uploading process is successful", Toast.LENGTH_SHORT).show();

                    edtDescription.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance("https://social-media-app-firebas-88f1a-default-rtdb.firebaseio.com/").getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            uids.add(dataSnapshot.getKey());
                            String username = (String) dataSnapshot.child("username").getValue();
                            usernames.add(username);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                imageDownloadLink = task.getResult().toString();
                            }
                        }
                    });

                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("fromWhom", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        dataMap.put("imageIdentifier", imageIdentifier);
        dataMap.put("imageLink", imageDownloadLink);
        dataMap.put("des", edtDescription.getText().toString());
        FirebaseDatabase.getInstance("https://social-media-app-firebas-88f1a-default-rtdb.firebaseio.com/").getReference().child("my_users").child(uids.get(position)).child("received_posts").push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SocialMediaActivity.this, "Data sent", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}

