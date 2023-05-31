package com.Bureau.Achivki;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import java.util.Objects;
import java.util.Set;


public class AchieveCategoryListActivity extends AppCompatActivity {
    private String userName;
    private int count = 0;
    private int achievedone = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve_category_list);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentMain = getIntent();
        String categoryName = intentMain.getStringExtra("Category_key");


        TextView categoryNameTextView = findViewById(R.id.categoryNameTextView);
        categoryNameTextView.setText(categoryName);
        ConstraintLayout topConstraintLayout = findViewById(R.id.top_constraint_layout);
        Drawable drawableKalina = ContextCompat.getDrawable(this, R.drawable.kalina);

        switch (categoryName) {
            case "Красноярск":
                topConstraintLayout.setBackground(null);
                break;
            case "Еда и напитки":
                topConstraintLayout.setBackground(null);
                break;
            case "Путешествия":
                topConstraintLayout.setBackground(null);
                break;
            case "Кулинар":
                topConstraintLayout.setBackground(null);
                break;
            case "Калининград":
                topConstraintLayout.setBackground(drawableKalina);
                break;
            default:
                topConstraintLayout.setBackground(null);
                break;
        }
        //topConstraintLayout.setBackground(drawableKalina);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle(categoryName);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName = documentSnapshot.getString("name");

                createAchieveList(userName, currentUser.getUid());
            } else {
                // документ не найден
            }
        });
    }

    public void p(int a, int count){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(count);
        progressBar.setProgress(a);
    }

    public void createAchieveList(String name, String userId){

        Intent intentMain = getIntent();
        String categoryName = intentMain.getStringExtra("Category_key");

        // Получение ссылки на коллекцию пользователей
        CollectionReference usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

        // Получение ссылки на коллекцию достижений
        CollectionReference achievementsCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");

        // Получение документа пользователя из коллекции Users
        DocumentReference userDocRef = usersCollectionRef.document(userId);
        userDocRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot userDocSnapshot = task.getResult();
                        if (userDocSnapshot.exists()) {
                            // Получение Map UserAchieve пользователя
                            Map<String, Object> userAchieveMap = (Map<String, Object>) userDocSnapshot.get("userAchievements");
                            // Получение достижений пользователя
                            Set<String> userAchievements = userAchieveMap.keySet();
                            // Получение документов достижений из коллекции achievements
                            Query categoryQuery = achievementsCollectionRef.whereEqualTo("category", categoryName);
                            categoryQuery.get().addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    count++;

                                    // Получаем имя достижения из документа
                                    String achievementName = document.getString("name");

                                    boolean proof = Boolean.TRUE.equals(document.getBoolean("proof"));
                                    boolean collectable = false;
                                    long achieveCount = 0L;
                                    long doneCount = 0;

                                    if (document.contains("collectable")) {
                                        collectable = Boolean.TRUE.equals(document.getBoolean("collectable"));
                                        achieveCount = document.getLong("count");

                                    } else {
                                        // Обработка ситуации, когда поле отсутствует
                                    }

                                    if (userAchievements.contains(achievementName)) {
                                        System.out.println("Достижение \"" + achievementName + "\" есть и у пользователя, и в категории " + categoryName);
                                        Map<String, Object> achievementMap = (Map<String, Object>) userAchieveMap.get(achievementName);
                                        if (document.contains("collectable")) {
                                            doneCount = (long) achievementMap.get("doneCount");
                                        }
                                        System.out.println("doneCount"+ doneCount);
                                        checkStatus(achievementName, categoryName, name, proof, collectable, achieveCount, doneCount);
                                        achievedone++;
                                    }else{
                                        createAchieveBlock(achievementName, "black", categoryName, name, proof, collectable, achieveCount, 0);
                                        System.out.println("Нет " + achievementName);
                                    }
                                }
                                p(achievedone , count);
                            });
                        }
                    }
                });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);
        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveCategoryListActivity.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveCategoryListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveCategoryListActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveCategoryListActivity.this, AchieveListActivity.class);
            startActivity(intent);
        });

        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveCategoryListActivity.this, UsersListActivity.class);
            startActivity(intent);
        });

    }

    private void checkStatus(String achievementName, String categoryName, String name, boolean proof, boolean collectable, long achieveCount, long doneCount){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        //List<String> achievementNames = new ArrayList<>();

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userData = documentSnapshot.getData();
            Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");

            for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                if (achievement.get("name").equals(achievementName)) {
                    Boolean confirmed = (Boolean) achievement.get("confirmed");
                    Boolean proofsended = (Boolean) achievement.get("proofsended");
                    if(Boolean.TRUE.equals(confirmed)){
                        System.out.println("confirmed");
                        createAchieveBlock(achievementName,"green", categoryName, name, proof, collectable, achieveCount, doneCount);
                    }else if (Boolean.TRUE.equals(proofsended)) {
                        createAchieveBlock(achievementName,"yellow", categoryName, name, proof, collectable, achieveCount, doneCount);
                        System.out.println("proofsended");
                    }else{
                        createAchieveBlock(achievementName,"black", categoryName, name, proof, collectable, achieveCount, doneCount);
                        System.out.println("not ");
                    }
                }
            }
        });
    }

    private void createAchieveBlock(String achieveName, String color, String categoryName, String username, boolean proof, boolean collectable, long achieveCount, long doneCount){
        LinearLayout parentLayout = findViewById(R.id.scrollView1);

        boolean received;
        ConstraintLayout blockLayout;

        if (Objects.equals(color, "green")){
            blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                    .inflate(R.layout.block_achieve_green, parentLayout, false);
            received = true;
        }else if(Objects.equals(color, "yellow")){
            blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                    .inflate(R.layout.block_achieve_yellow, parentLayout, false);
            received = true;
        }else{
            blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                    .inflate(R.layout.block_achieve, parentLayout, false);
            received = false;
        }

        if(collectable){
            ProgressBar progess = blockLayout.findViewById(R.id.achieveProgressBar);
            progess.setVisibility(View.VISIBLE);
            progess.setMax((int)achieveCount);
            progess.setProgress((int) doneCount);
        }

        TextView AchieveNameTextView = blockLayout.findViewById(R.id.achieveName_blockTextView);

        AchieveNameTextView.setText(achieveName);

        parentLayout.addView(blockLayout);
        blockLayout.setOnClickListener(v -> {
            Intent intent;
            // Обработка нажатия кнопки
            if(collectable){
                intent = new Intent(AchieveCategoryListActivity.this, AchievementWithProgressActivity.class);
            }else{
                intent = new Intent(AchieveCategoryListActivity.this, AchievementDescriptionActivity.class);
            }
            //Intent intent = new Intent(AchieveCategoryListActivity.this, AchievementDescriptionActivity.class);
            intent.putExtra("Achieve_key", achieveName);
            intent.putExtra("Category_key", categoryName);
            intent.putExtra("Is_Received", received);
            intent.putExtra("User_name", username);
            intent.putExtra("ProofNeeded", proof);
            intent.putExtra("collectable", collectable);
            intent.putExtra("achieveCount", achieveCount);
            startActivity(intent);
        });

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
            Intent intent = new Intent(AchieveCategoryListActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}