package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SuggestAchieveActivity extends AppCompatActivity {

    private Button suggestButton;

    private TextView editTextTextAchieveName;
    private TextView editTextTextAchieveDesc;
    private FirebaseAuth mAuth;

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_achieve);

        editTextTextAchieveName = findViewById(R.id.editTextTextAchieveName);
        editTextTextAchieveDesc = findViewById(R.id.editTextTextAchieveDesc);

        suggestButton = findViewById(R.id.suggestButton);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuggestAchieveActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuggestAchieveActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuggestAchieveActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });
        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuggestAchieveActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuggestAchieveActivity.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });


       // Map<String, Object> userAchievements = new HashMap<>();

        suggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextTextAchieveName.getText().toString().isEmpty() || editTextTextAchieveDesc.getText().toString().isEmpty()){
                    Toast.makeText(SuggestAchieveActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                }else{

                    //TextView nameTextView = findViewById(R.id.nameTextView);
                    String name = editTextTextAchieveName.getText().toString();

                    //TextView emailTextView = findViewById(R.id.emailEditText);
                    String desc = editTextTextAchieveDesc.getText().toString();
                    usersRef.get().addOnSuccessListener(documentSnapshot -> {
                        Map<String, Object> userAchievements = documentSnapshot.getData();
                        if (userAchievements == null) {
                            // Если пользователь не существует, создаем новый документ
                            userAchievements = new HashMap<>();
                            userAchievements.put("achieve", new HashMap<>());
                        } else if (!userAchievements.containsKey("achieve")) {
                            // Если Map achieve не существует, создаем его
                            userAchievements.put("achieve", new HashMap<>());
                        }

                        // Получаем текущий Map achieve из документа пользователя
                        Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("achieve");

                        // Создаем новый Map с информацией о новом достижении
                        Map<String, Object> newAchieveMap = new HashMap<>();
                        newAchieveMap.put("name", name);
                        newAchieveMap.put("desc", desc);

                        // Добавляем новое достижение в Map achieve пользователя
                        achieveMap.put(name, newAchieveMap);

                        // Сохраняем обновленный Map achieve в Firestore
                        userAchievements.put("achieve", achieveMap);
                        usersRef.set(userAchievements);
                        Toast.makeText(SuggestAchieveActivity.this, "Достижение добавлено", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });


    }
    /*protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }*/
}