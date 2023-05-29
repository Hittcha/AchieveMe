package com.Bureau.Achivki;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SuggestAchieveActivity extends AppCompatActivity {

    private TextView editTextTextAchieveName;
    private TextView editTextTextAchieveDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_achieve);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Создать свое достижение");

        editTextTextAchieveName = findViewById(R.id.editTextTextAchieveName);
        editTextTextAchieveDesc = findViewById(R.id.editTextTextAchieveDesc);

        Button suggestButton = findViewById(R.id.suggestButton);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);

        ImageButton menuButton = findViewById(R.id.imageButtonMenu);

        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);

        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestAchieveActivity.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestAchieveActivity.this, MainActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestAchieveActivity.this, AchieveListActivity.class);
            startActivity(intent);
        });
        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestAchieveActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestAchieveActivity.this, UsersListActivity.class);
            startActivity(intent);
        });


        suggestButton.setOnClickListener(v -> {
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
        });
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}