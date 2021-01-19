package com.example.social_media_app_firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener{

    private ListView postsListView;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private ImageView imageViewSentPost;
    private TextView txtDescription;
    private ArrayList<DataSnapshot> dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        imageViewSentPost = findViewById(R.id.imageViewSentPost);
        txtDescription = findViewById(R.id.txtDescription);

        firebaseAuth = FirebaseAuth.getInstance();
        postsListView = findViewById(R.id.viewPosts);
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, usernames);
        postsListView.setAdapter(adapter);
        dataSnapshots = new ArrayList<>();

        FirebaseDatabase.getInstance("https://social-media-app-firebas-88f1a-default-rtdb.firebaseio.com/").getReference().child("my_users").child(firebaseAuth.getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                dataSnapshots.add(dataSnapshot);

                String fromWhomUsername = (String) dataSnapshot.child("fromWhom").getValue();
                usernames.add(fromWhomUsername);
                adapter.notifyDataSetChanged();
//                String link = FirebaseStorage.getInstance().getReference().child("my_images").child(dataSnapshot.child("imageIdentifier").toString()).getName();
//                Picasso.get().load(link).into(imageViewSentPost);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot: dataSnapshots
                ) {
                    if (snapshot.getKey().equals(dataSnapshot.getKey())){
                        dataSnapshots.remove(i);
                        usernames.remove(i);
                    }
                    i++;
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            txtDescription.setText((String) dataSnapshots.get(position).child("des").getValue());
            String link = (String) dataSnapshots.get(position).child("imageLink").getValue();
            Picasso.get().load(link).into(imageViewSentPost);
        }
    });



    postsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            AlertDialog alertDialog = new AlertDialog.Builder(ViewPostActivity.this)
//set icon
                    .setIcon(android.R.drawable.ic_dialog_alert)
//set title
                    .setTitle("Are you sure to delete this file")
//set message
                    .setMessage("Deleting will permanently remove image from database! ")
//set positive button
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what would happen when positive button is clicked
                            FirebaseStorage.getInstance().getReference().child("my_images").child((String) dataSnapshots.get(position).child("imageIdentifier").getValue()).delete();
                            FirebaseDatabase.getInstance("https://social-media-app-firebas-88f1a-default-rtdb.firebaseio.com/").getReference().child("my_users").child(firebaseAuth.getCurrentUser().getUid()).child("received_posts").child(dataSnapshots.get(position).getKey()).removeValue();
//                            usernames.remove(position);
                            txtDescription.setText("");

                            imageViewSentPost.setImageBitmap(null);

                        }
                    })
//set negative button
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what should happen when negative button is clicked
//                            Toast.makeText(getApplicationContext(),"Nothing Happened",Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();

            return false;
        }
    });
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

}
