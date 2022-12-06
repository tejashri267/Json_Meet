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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class sign extends AppCompatActivity {
    private EditText inputFirst, inputLast, inputEmail, inputPass, inputConfirm;
    private MaterialButton buttonSignUp;
    private ProgressBar pbar1;
    private PreferenceManager preferenceManager;
    //private FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        //db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
         preferenceManager = new PreferenceManager(getApplicationContext());

        findViewById(R.id.imgback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        inputFirst = findViewById(R.id.InputFirstName);
        inputLast = findViewById(R.id.InputLastName);
        inputEmail = findViewById(R.id.InputEmail);
        inputPass = findViewById(R.id.InputPassword);
        inputConfirm = findViewById(R.id.InputConfirmPassword);
        buttonSignUp = findViewById(R.id.signupbtn);
        pbar1 = findViewById(R.id.signupbar);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputFirst.getText().toString().trim().isEmpty()) {
                    Toast.makeText(sign.this, "Enter First Name", Toast.LENGTH_SHORT).show();
                } else if (inputLast.getText().toString().trim().isEmpty()) {
                    Toast.makeText(sign.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
                } else if (inputEmail.getText().toString().trim().isEmpty()) {
                    Toast.makeText(sign.this, "Enter Email Address", Toast.LENGTH_SHORT).show();
                } else if (inputPass.getText().toString().trim().isEmpty()) {
                    Toast.makeText(sign.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else if (!inputPass.getText().toString().equals(inputConfirm.getText().toString())) {
                    Toast.makeText(sign.this, "Enter Valid Password", Toast.LENGTH_SHORT).show();
                } else if (inputConfirm.getText().toString().trim().isEmpty()) {
                    Toast.makeText(sign.this, "Confirm Your Password", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {
                    Toast.makeText(sign.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                } else {
                    signUp();
                }
            }

            private void signUp() {
                buttonSignUp.setVisibility(View.INVISIBLE);
                pbar1.setVisibility(View.VISIBLE);
                String email , pass;
                email = inputEmail.getText().toString();
                pass = inputPass.getText().toString();

                FirebaseFirestore database = FirebaseFirestore.getInstance();
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_FIRST_NAME, inputFirst.getText().toString());
                user.put(Constants.KEY_LAST_NAME, inputLast.getText().toString());
                user.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
                user.put(Constants.KEY_PASSWORD, inputPass.getText().toString());
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .add(user)
                                    .addOnSuccessListener (new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            preferenceManager.putBoolean(Constants.KEY_SIGNED_IN, true);
                                            preferenceManager.putString(Constants.KEY_ID, documentReference.getId());
                                            preferenceManager.putString(Constants.KEY_FIRST_NAME, inputFirst.getText().toString());
                                            preferenceManager.putString(Constants.KEY_LAST_NAME, inputLast.getText().toString());
                                            preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.getText().toString());
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pbar1.setVisibility(View.INVISIBLE);
                                            buttonSignUp.setVisibility(View.VISIBLE);
                                            Toast.makeText(sign.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            pbar1.setVisibility(View.INVISIBLE);
                            buttonSignUp.setVisibility(View.VISIBLE);
                            Toast.makeText(sign.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
    }
}

