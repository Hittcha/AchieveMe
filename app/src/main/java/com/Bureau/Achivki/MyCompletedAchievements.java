package com.Bureau.Achivki;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

public class MyCompletedAchievements extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_completed_achievements);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Мои выполненные достижения");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());


        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {

                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");

                for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                    Map<String, Object> achievement = (Map<String, Object>) entry.getValue();
                    String key = entry.getKey();

                    System.out.println("key: " + key);
                    String achievementName = (String) achievement.get("name");
                    Boolean confirmed = (Boolean) achievement.get("confirmed");
                    Boolean proofsended = (Boolean) achievement.get("proofsended");

                    System.out.println("confirmed: " + confirmed);
                    System.out.println("proofsended: " + proofsended);

                    if(Boolean.TRUE.equals(confirmed)){
                        System.out.println("confirmed");
                        createAchieveBlock(achievementName,"green");
                    }else if (Boolean.TRUE.equals(proofsended)) {
                        createAchieveBlock(achievementName,"yellow");
                        System.out.println("proofsended");
                    }else{
                        createAchieveBlock(achievementName,"black");
                        System.out.println("not ");
                    }
                }

            } else {
                // документ не найден
                Toast.makeText(MyCompletedAchievements.this, "Документ не найден", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyCompletedAchievements.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyCompletedAchievements.this, MainActivity.class);
            startActivity(intent);
        });

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyCompletedAchievements.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyCompletedAchievements.this, AchieveListActivity.class);
            startActivity(intent);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyCompletedAchievements.this, UsersListActivity.class);
            startActivity(intent);
        });


    }

    private void createAchieveBlock(String achievementName, String color) {
        LinearLayout parentLayout = findViewById(R.id.scrollViewLayout);
        ConstraintLayout blockLayout;

        if (Objects.equals(color, "green")) {
            blockLayout = (ConstraintLayout) LayoutInflater.from(MyCompletedAchievements.this)
                    .inflate(R.layout.block_achieve_green, parentLayout, false);
        } else if (Objects.equals(color, "yellow")) {
            blockLayout = (ConstraintLayout) LayoutInflater.from(MyCompletedAchievements.this)
                    .inflate(R.layout.block_achieve_yellow, parentLayout, false);
        } else {
            blockLayout = (ConstraintLayout) LayoutInflater.from(MyCompletedAchievements.this)
                    .inflate(R.layout.block_achieve, parentLayout, false);
        }

        TextView AchieveNameTextView = blockLayout.findViewById(R.id.achieveName_blockTextView);
        AchieveNameTextView.setText(achievementName);
        parentLayout.addView(blockLayout);
    }
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}