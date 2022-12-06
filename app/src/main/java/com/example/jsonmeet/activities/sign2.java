package com.example.jsonmeet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jsonmeet.R;
import com.example.jsonmeet.utilities.Constants;
import com.example.jsonmeet.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class sign2 extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    private MaterialButton buttonSignIn;
    private ProgressBar pbar2;
    private PreferenceManager preferenceManager;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign2);
        auth = FirebaseAuth.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        findViewById(R.id.signup).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), sign.class)));
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonSignIn = findViewById(R.id.signinbtn);
        pbar2 = findViewById(R.id.signinbar);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputEmail.getText().toString().trim().isEmpty()) {
                    Toast.makeText(sign2.this, "Enter Email", Toast.LENGTH_SHORT).show();
                }else if (inputPassword.getText().toString().trim().isEmpty()) {
                    Toast.makeText(sign2.this, "Enter Password", Toast.LENGTH_SHORT).show();
                }else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {
                    Toast.makeText(sign2.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                }else {
                    signIn();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void signIn(){
        buttonSignIn.setVisibility(View.INVISIBLE);
        pbar2.setVisibility(View.VISIBLE);
        String e = inputEmail.getText().toString();
        String t = inputPassword.getText().toString();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        auth.signInWithEmailAndPassword(e, t).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .whereEqualTo(Constants.KEY_EMAIL, inputEmail.getText().toString())
                            .whereEqualTo(Constants.KEY_PASSWORD, inputPassword.getText().toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                        preferenceManager.putBoolean(Constants.KEY_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_ID, documentSnapshot.getId());
                                        preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                                        preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        pbar2.setVisibility(View.INVISIBLE);
                                        buttonSignIn.setVisibility(View.VISIBLE);
                                        Toast.makeText(sign2.this, "Unable To Sign In", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                } else {
                    pbar2.setVisibility(View.INVISIBLE);
                    buttonSignIn.setVisibility(View.VISIBLE);
                    Toast.makeText(sign2.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            });

    }
}