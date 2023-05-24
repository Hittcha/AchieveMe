package com.Bureau.Achivki;

import android.content.Intent;
import android.os.Build;
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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class OtherUserAchievements extends AppCompatActivity {

    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_achievements);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Достижения ");

        Intent intentOtherUser = getIntent();
        String userToken = intentOtherUser.getStringExtra("User_token");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference mAuthDocRef = db.collection("Users").document(userToken);


        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName = documentSnapshot.getString("name");
                getSupportActionBar().setTitle("Достижения " + userName);

                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");
                for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                    Map<String, Object> achievement = (Map<String, Object>) entry.getValue();
                    String key = entry.getKey();
                    System.out.println("key: " + key);
                    String achievementName = (String) achievement.get("name");
                    Boolean confirmed = (Boolean) achievement.get("confirmed");
                    Boolean proofSended = (Boolean) achievement.get("proofsended");


                    if(confirmed == true){
                        createAchieveBlock(achievementName, "green");
                    }else if (proofSended == true) {
                        createAchieveBlock(achievementName, "yellow");
                    }else{
                        createAchieveBlock(achievementName, "black");
                    }
                }
            } else {
                // документ не найден
                Toast.makeText(OtherUserAchievements.this, "Документ не найден", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUserAchievements.this, LeaderBoardActivity.class);
            startActivity(intent);
        });
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUserAchievements.this, MainActivity.class);
            startActivity(intent);
        });
        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUserAchievements.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);
        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUserAchievements.this, AchieveListActivity.class);
            startActivity(intent);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUserAchievements.this, UsersListActivity.class);
            startActivity(intent);
        });
    }

    private void createAchieveBlock(String achievementName, String color) {
        LinearLayout parentLayout = findViewById(R.id.scrollView);
        ConstraintLayout blockLayout;

        if (color == "green") {
            blockLayout = (ConstraintLayout) LayoutInflater.from(OtherUserAchievements.this)
                    .inflate(R.layout.block_achieve_green, parentLayout, false);
        } else if (color == "yellow") {
            blockLayout = (ConstraintLayout) LayoutInflater.from(OtherUserAchievements.this)
                    .inflate(R.layout.block_achieve_yellow, parentLayout, false);
        } else {
            blockLayout = (ConstraintLayout) LayoutInflater.from(OtherUserAchievements.this)
                    .inflate(R.layout.block_achieve, parentLayout, false);
        }

        TextView AchieveNameTextView = blockLayout.findViewById(R.id.achieveName_blockTextView);
        AchieveNameTextView.setText(achievementName);
        parentLayout.addView(blockLayout);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}