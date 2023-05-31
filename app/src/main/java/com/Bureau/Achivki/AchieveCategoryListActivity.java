package com.Bureau.Achivki;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.Build;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;


public class AchieveCategoryListActivity extends AppCompatActivity {
    private String userName;
    private ProgressBar progressBar;
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
        ImageView backgroundImage = findViewById(R.id.category_background);
//        Drawable drawableKalina = ContextCompat.getDrawable(this, R.drawable.kalina);



        switch (categoryName) {
            case "Красноярск":
                try {
                  InputStream ims = getAssets().open("category_background/city_krsk.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setBackground(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Еда и напитки":
                try {
                    InputStream ims = getAssets().open("food_and_drink.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setBackground(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Путешествия":
                try {
                    InputStream ims = getAssets().open("trips.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    topConstraintLayout.setBackground(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Кулинар":
                try {
                    InputStream ims = getAssets().open("chef.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setBackground(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Калининград":
                try {
                    InputStream ims = getAssets().open("category_background/city_kalina.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setBackground(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
        progressBar = findViewById(R.id.progressBar);
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

                                    boolean proof = document.getBoolean("proof");

                                    if (userAchievements.contains(achievementName)) {
                                        System.out.println("Достижение \"" + achievementName + "\" есть и у пользователя, и в категории " + categoryName);
                                        checkStatus(achievementName, categoryName, name, proof);
                                        achievedone++;
                                    }else{
                                        createAchieveBlock(achievementName, "black", categoryName, name, proof);
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

    private void checkStatus(String achievementName, String categoryName, String name, boolean proof){
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
                    if(Boolean.TRUE.equals(confirmed) == true){
                        System.out.println("confirmed");
                        createAchieveBlock(achievementName,"green", categoryName, name, proof);
                    }else if (Boolean.TRUE.equals(proofsended) == true) {
                        createAchieveBlock(achievementName,"yellow", categoryName, name, proof);
                        System.out.println("proofsended");
                    }else{
                        createAchieveBlock(achievementName,"black", categoryName, name, proof);
                        System.out.println("not ");
                    }
                }

            }
        });
    }


    private void createAchieveBlock(String achname, String color, String categoryName, String username, boolean proof){
        LinearLayout parentLayout = findViewById(R.id.scrollView1);

        boolean received;
        ConstraintLayout blockLayout;

        if (color == "green"){
            blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                    .inflate(R.layout.block_achieve_green, parentLayout, false);
            received = true;
        }else if(color == "yellow"){
            blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                    .inflate(R.layout.block_achieve_yellow, parentLayout, false);
            received = true;
        }else{
            blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                    .inflate(R.layout.block_achieve, parentLayout, false);
            received = false;
        }

        TextView AchieveNameTextView = blockLayout.findViewById(R.id.achieveName_blockTextView);

        AchieveNameTextView.setText(achname);

        parentLayout.addView(blockLayout);
        blockLayout.setOnClickListener(v -> {
            // Обработка нажатия кнопки
            Intent intent = new Intent(AchieveCategoryListActivity.this, AchievementDescriptionActivity.class);
            intent.putExtra("Achieve_key", achname);
            intent.putExtra("Category_key", categoryName);
            intent.putExtra("Is_Received", received);
            intent.putExtra("User_name", username);
            intent.putExtra("ProofNeeded", proof);
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