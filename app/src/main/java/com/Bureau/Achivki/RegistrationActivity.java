package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private TextView emailEditText;
    private TextView passwordEditText;
    private TextView userNameEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Button registrationButton = findViewById(R.id.ActivityLogin_register_button);
        emailEditText = findViewById(R.id.ActivityLogin_email_edit_text);
        passwordEditText = findViewById(R.id.ActivityLogin_password_edit_text);
        userNameEditText = findViewById(R.id.ActivityReg_userName_edit_text);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();


        registrationButton.setOnClickListener(v -> {
            if (emailEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty() || userNameEditText.getText().toString().isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Одно из полей регистрации пустое", Toast.LENGTH_SHORT).show();
            }else if(passwordEditText.getText().toString().length() < 6) {
                Toast.makeText(RegistrationActivity.this, "Пароль должен быть длинной минимум 6 символов", Toast.LENGTH_SHORT).show();
            }else{

                mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                CollectionReference usersRef = db.collection("Users");

                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                FirebaseUser currentUser = mAuth.getCurrentUser();

                                String name = userNameEditText.getText().toString();
                                String email = emailEditText.getText().toString();
                                String pass = passwordEditText.getText().toString();

                                Map<String, Object> userAchieveMap = new HashMap<>();
                                Map<String, Object> userCreatedAchieveMap = new HashMap<>();
                                Map<String, Object> userPhotoMap = new HashMap<>();
                                Map<String, Object> subscribers = new HashMap<>();
                                Map<String, Object> friends = new HashMap<>();
                                ArrayList<String> favorites = new ArrayList<>();


                                if (currentUser != null) {
                                   // Map<String, Object> user = new HashMap<>();
                                }
                                db.collection("Achievements").get().addOnCompleteListener(task1 -> {
                                    Map<String, Object> user = new HashMap<>();
                                    if (task1.isSuccessful()) {
                                        user.put("name", name);
                                        user.put("email", email);
                                        user.put("favorites", favorites);
                                        user.put("pass", pass);
                                        user.put("score", 0L);
                                        user.put("friendscount", 0L);
                                        user.put("subs", 0L);
                                        user.put("profileImageUrl", "/users/StandartUser/UserAvatar.png");
                                        user.put("userAchievements", userAchieveMap);
                                        user.put("achieve", userCreatedAchieveMap);
                                        user.put("userPhotos", userPhotoMap);
                                        user.put("subscribers", subscribers);
                                        user.put("friends", friends);
                                        usersRef.document(currentUser.getUid()).set(user);
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task1.getException());
                                    }
                                });

                                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(RegistrationActivity.this, "Такой пользователь уже существует дюдя", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    public void backButtonClick(View view) { finish(); }
}