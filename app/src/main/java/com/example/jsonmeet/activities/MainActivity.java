package com.example.jsonmeet.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.jsonmeet.R;
import com.example.jsonmeet.adapters.userAdapter;
import com.example.jsonmeet.listners.userListener;
import com.example.jsonmeet.models.User;
import com.example.jsonmeet.utilities.Constants;
import com.example.jsonmeet.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements userListener {

    private PreferenceManager preferenceManager;
    private userAdapter adapter;
    private TextView textError;
    private SwipeRefreshLayout refreshLayout;
    private List<User> users;

    private final int OPTIMIZATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = new PreferenceManager(getApplicationContext());
        TextView textTitle = findViewById(R.id.titletext);
        textTitle.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        findViewById(R.id.signout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                Intent intent = new Intent(getApplicationContext(), sign2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    sendToken(task.getResult().getToken());
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recycler);

        textError = findViewById(R.id.textinput);
        refreshLayout = findViewById(R.id.swipe);
        refreshLayout.setOnRefreshListener(this::getUsers);

        users = new ArrayList<>();
        adapter = new userAdapter(users, this );
        //  adapter.setUsers(users);
        recyclerView.setAdapter(adapter);
        getUsers();
        optimizations();
    }

    public void getUsers() {
        refreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        refreshLayout.setRefreshing(false);
                        String userId = preferenceManager.getString(Constants.KEY_ID);
                        if (task.isSuccessful() && task.getResult() != null) {
                            users.clear();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if (userId.equals(documentSnapshot.getId())) {
                                    continue;
                                }
                                User user = new User();
                                user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                                user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                                user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                                user.token = documentSnapshot.getString(Constants.KEY_TOKEN);
                                users.add(user);
                            }
                            if (users.size() > 0) {
                                adapter.notifyDataSetChanged();
                            } else {
                                textError.setText(String.format("%s", "No User Available"));
                                textError.setVisibility(View.VISIBLE);
                            }
                        } else {
                            textError.setText(String.format("%s", "No User Available"));
                            textError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void sendToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_ID)

                );
        documentReference.update(Constants.KEY_TOKEN, token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //       Toast.makeText(MainActivity.this, "Token Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //           Toast.makeText(MainActivity.this, "Unable To Send Token", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        preferenceManager.clearPreferences();
                        startActivity(new Intent(getApplicationContext(), sign2.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Unable To Sign Out", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void initialVideoMeet(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, user.firstName + " " + user.lastName + " Is Not Available For Meeting", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutGoingInvitation.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    @Override
    public void initialAudioMeet(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, user.firstName + " " + user.lastName + " Is Not Available For Meeting", Toast.LENGTH_SHORT).show();
        } else {
            // Toast.makeText(this, "Audio Meeting With " + user.firstName + " " + user.lastName, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), OutGoingInvitation.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }
     private void optimizations() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("WARNING");
                builder.setMessage("Battery optimization is enabled. It can interrupt background service");
                builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivityForResult(intent, OPTIMIZATION);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
            }
        }
     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPTIMIZATION) {
            optimizations();
        }
    }
}
