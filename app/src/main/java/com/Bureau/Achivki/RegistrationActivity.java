package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private Button registrationButton;

    private TextView emailEditText;
    private TextView passwordEditText;
    private TextView userNameEditText;

    private FirebaseDatabase database;
    private DatabaseReference ref;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registrationButton = findViewById(R.id.ActivityLogin_register_button);
        emailEditText = findViewById(R.id.ActivityLogin_email_edit_text);
        passwordEditText = findViewById(R.id.ActivityLogin_password_edit_text);
        userNameEditText = findViewById(R.id.userName_edit_text);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        DatabaseReference achivRef = database.getReference("Achievements");

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty() || userNameEditText.getText().toString().isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        CollectionReference usersRef = db.collection("Users");

                                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                        FirebaseUser currentUser = mAuth.getCurrentUser();

                                        ArrayList<String> favorites = new ArrayList<>();

                                        //TextView nameTextView = findViewById(R.id.nameTextView);
                                        String name = userNameEditText.getText().toString();

                                        //TextView emailTextView = findViewById(R.id.emailEditText);
                                        String email = emailEditText.getText().toString();

                                        String pass = passwordEditText.getText().toString();

                                        Map<String, Object> userAchieveMap = new HashMap<>();
                                        Map<String, Object> userCreatedAchieveMap = new HashMap<>();
                                        Map<String, Object> userPhotoMap = new HashMap<>();
                                        Map<String, Object> subscribers = new HashMap<>();
                                        Map<String, Object> friends = new HashMap<>();

                                        String profileImageUrl = "/users/StandartUser/";

                                        if (currentUser != null) {
                                           // Map<String, Object> user = new HashMap<>();

                                        }

                                        db.collection("Achievements").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                Map<String, Object> user = new HashMap<>();
                                                //user.put("achievements", categoriesArray);
                                                if (task.isSuccessful()) {
                                                    /*ArrayList<String> categories = new ArrayList<>();
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        String category = document.getString("category");
                                                        //categories.add(category);
                                                        user.put(category, categories);
                                                       // usersRef.document(currentUser.getUid()).set(user);
                                                    }*/
                                                    //String[] categoriesArray = categories.toArray(new String[categories.size()]);
                                                    // categoriesArray теперь содержит список категорий в виде массива String
                                                    // Можно передать его в каталог Users для документа пользователя
                                                   // Map<String, Object> user = new HashMap<>();
                                                    //user.put("achievements", categoriesArray);
                                                    // usersRef.document(currentUser.getUid()).set(user);
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
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
                                            }
                                        });

                                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(RegistrationActivity.this, "Такой пользователь уже существует дюдя", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}