package com.Bureau.Achivki;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Map;
import java.util.Set;

public class OtherUserAchievements extends AppCompatActivity {

    //private String userName;

    private static final int REQUEST_CODE = 10;


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

        createAchieveBlock();

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

    private void createAchieveBlock(){
        Intent intentOtherUser = getIntent();
        String userToken = intentOtherUser.getStringExtra("User_token");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference mAuthDocRef = db.collection("Users").document(userToken);


        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {

                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");

                for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                    Map<String, Object> achievement = (Map<String, Object>) entry.getValue();
                    String key = entry.getKey();

                    System.out.println("key: " + key);
                    String achievementName = (String) achievement.get("name");
                    String achievementCategory = (String) achievement.get("category");
                    Boolean confirmed = (Boolean) achievement.get("confirmed");
                    Boolean proofsended = (Boolean) achievement.get("proofsended");

                    System.out.println("confirmed: " + confirmed);
                    System.out.println("proofsended: " + proofsended);

                    boolean collectable = false;
                    long achieveCount = 0;
                    long doneCount = 0;
                    String countDesc = "";
                    long dayLimit = 0;

                    if (achievement.containsKey("collectable")) {
                        collectable = Boolean.TRUE.equals(achievement.get("collectable"));
                        achieveCount = (long) achievement.get("targetCount");
                        doneCount = (long) achievement.get("doneCount");
                        dayLimit = (long) achievement.get("dayLimit");
                        //countDesc = (String) achievement.get("countDesc");
                        System.out.println("collectable " + collectable);
                    } else {
                        // Обработка ситуации, когда поле отсутствует
                        System.out.println("not collectable " + collectable);
                    }

                    LinearLayout parentLayout = findViewById(R.id.scrollView);
                    ConstraintLayout blockLayout;

                    boolean received;
                    if(Boolean.TRUE.equals(confirmed)){
                        System.out.println("confirmed");
                        //createAchieveBlock(achievementName,"green", achievementCategory, true);
                        blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                .inflate(R.layout.block_achieve_green, parentLayout, false);
                        received = true;
                    }else if (Boolean.TRUE.equals(proofsended)) {
                        //createAchieveBlock(achievementName,"yellow", achievementCategory, true);
                        System.out.println("proofsended");
                        blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                .inflate(R.layout.block_achieve_yellow, parentLayout, false);
                        received = true;
                    }else{
                        //createAchieveBlock(achievementName,"black", achievementCategory, false);
                        System.out.println("not ");
                        blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                .inflate(R.layout.block_achieve, parentLayout, false);
                        received = false;
                    }

                    blockLayout.setOnClickListener(v -> {
                        checkAchieve(achievementName, achievementCategory);
                    });

                    if(collectable){
                        ProgressBar progress = blockLayout.findViewById(R.id.achieveProgressBar);
                        TextView progressDesc = blockLayout.findViewById(R.id.countDesc);
                        progress.setVisibility(View.VISIBLE);
                        progressDesc.setVisibility(View.VISIBLE);
                        progress.setMax((int)achieveCount);
                        progress.setProgress((int) doneCount);
                        progressDesc.setText("Выполненно: " + (int) doneCount + " из " + (int) achieveCount);
                    }

                    TextView AchieveNameTextView = blockLayout.findViewById(R.id.achieveName_blockTextView);
                    if (achievementCategory.equals("userAchieve")){
                        AchieveNameTextView.setText("Пользовательское достижение: " + achievementName);
                    }else{
                        AchieveNameTextView.setText(achievementName);
                    }

                    //AchieveNameTextView.setText(achievementName);
                    parentLayout.addView(blockLayout);
                }

            } else {
                // документ не найден
                Toast.makeText(this, "Документ не найден", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkAchieve(String achieveName, String category){

        // Получение ссылки на коллекцию достижений
        CollectionReference achievementsCollectionRef;

        if(category.equals("season1")){
            achievementsCollectionRef = FirebaseFirestore.getInstance().collection("SeasonAchievements");
        }else{
            achievementsCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");
        }

        CollectionReference usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Получение документа пользователя из коллекции Users
        DocumentReference userDocRef = usersCollectionRef.document(currentUser.getUid());
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot userDocSnapshot = task.getResult();
                if (userDocSnapshot.exists()) {
                    // Получение Map UserAchieve пользователя
                    Map<String, Object> userAchieveMap = (Map<String, Object>) userDocSnapshot.get("userAchievements");
                    // Получение достижений пользователя
                    Set<String> userAchievements = userAchieveMap.keySet();

                    // Получение Map UserAchieve пользователя
                    Map<String, Object> userFavoritesMap = (Map<String, Object>) userDocSnapshot.get("favorites");
                    // Получение достижений пользователя
                    Set<String> userFavorites = userFavoritesMap.keySet();

                    Query categoryQuery = achievementsCollectionRef.whereEqualTo("name", achieveName);
                    categoryQuery.get().addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Получаем имя достижения из документа
                            String achievementName = document.getString("name");

                            boolean proof = Boolean.TRUE.equals(document.getBoolean("proof"));
                            boolean collectable = false;
                            long achieveCount = 0;
                            long doneCount = 0;
                            String countDesc = "";
                            long dayLimit = 0;

                            if (document.contains("collectable")) {
                                collectable = Boolean.TRUE.equals(document.getBoolean("collectable"));
                                achieveCount = document.getLong("count");
                                dayLimit = document.getLong("dayLimit");
                                countDesc = document.getString("countDesc");
                                System.out.println("collectable " + collectable);
                            } else {
                                // Обработка ситуации, когда поле отсутствует
                                System.out.println("not collectable " + collectable);
                            }

                            long achievePrice = 0;
                            if (document.contains("price")) {
                                achievePrice = document.getLong("price");
                                System.out.println("price " + achievePrice);
                            }

                            String categoryName = document.getString("category");
                            boolean isUserAchieve = false;
                            if (categoryName.equals("userAchieve")) {
                                isUserAchieve = true;
                            }

                            boolean finalCollectable = collectable;
                            long finalAchievePrice = achievePrice;
                            long finalDayLimit = dayLimit;
                            long finalAchieveCount = achieveCount;


                            boolean isFavorites = false;
                            if (userFavorites.contains(achievementName)) {
                                isFavorites = true;
                            }
                            System.out.println("isFavorites " + isFavorites);

                            boolean received;
                            if (userAchievements.contains(achievementName)) {
                                System.out.println("Достижение \"" + achievementName + "\" есть и у пользователя, и в категории " + categoryName);
                                Map<String, Object> achievementMap = (Map<String, Object>) userAchieveMap.get(achievementName);

                                Boolean confirmed = (Boolean) achievementMap.get("confirmed");
                                Boolean proofsended = (Boolean) achievementMap.get("proofsended");

                                if (document.contains("collectable")) {
                                    doneCount = (long) achievementMap.get("doneCount");
                                }
                                if (Boolean.TRUE.equals(confirmed)) {
                                    System.out.println("confirmed");
                                    received = true;
                                } else if (Boolean.TRUE.equals(proofsended)) {
                                    received = true;
                                    System.out.println("proofsended");
                                } else {
                                    received = false;
                                    System.out.println("not ");
                                }
                            } else {
                                received = false;
                                System.out.println("Нет " + achievementName);
                            }


                            Intent intent;
                            // Обработка нажатия кнопки
                            if (finalCollectable) {
                                intent = new Intent(this, AchievementWithProgressActivity.class);
                            } else {
                                intent = new Intent(this, AchievementDescriptionActivity.class);
                            }
                            intent.putExtra("Achieve_key", achievementName);
                            intent.putExtra("Category_key", categoryName);
                            intent.putExtra("Is_Received", received);
                            intent.putExtra("ProofNeeded", proof);
                            intent.putExtra("collectable", finalCollectable);
                            intent.putExtra("achieveCount", finalAchieveCount);
                            intent.putExtra("dayLimit", finalDayLimit);
                            intent.putExtra("achievePrice", finalAchievePrice);
                            intent.putExtra("isFavorites", isFavorites);
                            intent.putExtra("isUserAchieve", isUserAchieve);
                            startActivityForResult(intent, REQUEST_CODE);
                        }
                    });
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            //перезагружаем список ачивок и обновляем счетсчик


            LinearLayout parentLayout = findViewById(R.id.scrollView);
            parentLayout.removeAllViews();

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            System.out.println("sfadfsd fsdf sdf sdf sdf sdf sd");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

            createAchieveBlock();

            mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    //createAchieveList(currentUser.getUid());
                } else {
                    // документ не найден
                }
            });
        }
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}