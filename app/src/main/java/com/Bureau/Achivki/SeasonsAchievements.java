package com.Bureau.Achivki;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeasonsAchievements extends AppCompatActivity {

    private static final int REQUEST_CODE = 10;
    private ImageButton backButton;
    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    private String userName;

    private FirebaseFirestore db;

    private int count = 0;

    private int achievedone = 0;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seasons_achievements);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Сезонный челендж");

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);
        ImageView backgroundImage = findViewById(R.id.season_category_background);

        try {
            InputStream ims = getAssets().open("season_challenge/summer_challenge_background.png");
            Drawable drawableBackground = Drawable.createFromStream(ims, null);
            backgroundImage.setImageDrawable(drawableBackground);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();

        //System.out.println("11111111111111111111111111111111111111111111111111111111 " + currentUser.getUid());

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                createAchieveList(currentUser.getUid());
                System.out.println("Создаю список");
            } else {
                // документ не найден
                System.out.println("документ не найден");
            }
        });


    }

    public void createAchieveList(String userId){


        Intent intentMain = getIntent();
        //String categoryName = intentMain.getStringExtra("Category_key");
        String categoryName = "season1";

        // Получение ссылки на коллекцию пользователей
        CollectionReference usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

        // Получение ссылки на коллекцию достижений
        CollectionReference achievementsCollectionRef = FirebaseFirestore.getInstance().collection("SeasonAchievements");

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

                    // Получение Map UserAchieve пользователя
                    Map<String, Object> userFavoritesMap = (Map<String, Object>) userDocSnapshot.get("favorites");
                    // Получение достижений пользователя
                    Set<String> userFavorites = userFavoritesMap.keySet();


                    // Получение документов достижений из коллекции achievements
                    Query categoryQuery = achievementsCollectionRef.whereEqualTo("category", categoryName);
                    categoryQuery.get().addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            count++;

                            boolean received;
                            LinearLayout parentLayout = findViewById(R.id.scrollView1);
                            ConstraintLayout blockLayout;

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


                            boolean isFavorites = false;

                            if (userFavorites.contains(achievementName)) {
                                isFavorites = true;
                            }
                            System.out.println("isFavorites " + isFavorites);


                            if (userAchievements.contains(achievementName)) {
                                System.out.println("Достижение \"" + achievementName + "\" есть и у пользователя, и в категории " + categoryName);
                                Map<String, Object> achievementMap = (Map<String, Object>) userAchieveMap.get(achievementName);

                                Boolean confirmed = (Boolean) achievementMap.get("confirmed");
                                Boolean proofsended = (Boolean) achievementMap.get("proofsended");

                                if (document.contains("collectable")) {
                                    doneCount = (long) achievementMap.get("doneCount");
                                }
                                if(Boolean.TRUE.equals(confirmed)){
                                    System.out.println("confirmed");
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                            .inflate(R.layout.block_achieve_green, parentLayout, false);
                                    received = true;
                                    achievedone++;
                                }else if (Boolean.TRUE.equals(proofsended)) {
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                            .inflate(R.layout.block_achieve_yellow, parentLayout, false);
                                    received = true;
                                    System.out.println("proofsended");
                                }else{
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                            .inflate(R.layout.block_achieve, parentLayout, false);
                                    received = false;
                                    System.out.println("not ");
                                }
                            }else{
                                blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                        .inflate(R.layout.block_achieve, parentLayout, false);
                                received = false;
                                System.out.println("Нет " + achievementName);
                            }

                            if(collectable){
                                ProgressBar progress = blockLayout.findViewById(R.id.achieveProgressBar);
                                TextView progressDesc = blockLayout.findViewById(R.id.countDesc);
                                progress.setVisibility(View.VISIBLE);
                                progressDesc.setVisibility(View.VISIBLE);
                                progress.setMax((int)achieveCount);
                                progress.setProgress((int) doneCount);
                                progressDesc.setText(countDesc + ": " + (int) doneCount + " из " + (int) achieveCount);
                            }

                            TextView AchieveNameTextView = blockLayout.findViewById(R.id.achieveName_blockTextView);
                            AchieveNameTextView.setText(achievementName);
                            parentLayout.addView(blockLayout);

                            boolean finalCollectable = collectable;
                            boolean finalIsFavorites = isFavorites;
                            long finalAchievePrice = achievePrice;
                            long finalDayLimit = dayLimit;
                            long finalAchieveCount = achieveCount;
                            String finalCountDesc = countDesc;
                            blockLayout.setOnClickListener(v -> {
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
                                intent.putExtra("isFavorites", finalIsFavorites);
                                intent.putExtra("countDesc", finalCountDesc);
                                startActivityForResult(intent, REQUEST_CODE);
                            });
                        }
                        p(achievedone , count);
                    });
                }
            }
        });
    }

    private void checkStatus(String achievementName, String categoryName, String name, boolean proof){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();

        // String confirmed = "sfd";

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");

                for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                    Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                    if (achievement.get("name").equals(achievementName)) {
                        Boolean confirmed = (Boolean) achievement.get("confirmed");
                        Boolean proofsended = (Boolean) achievement.get("proofsended");
                        //System.out.println(confirmed);
                        if(confirmed == true){
                            System.out.println("confirmed");
                            createButton(achievementName,"green", categoryName, name, proof);
                        }else if (proofsended == true) {
                            createButton(achievementName,"yellow", categoryName, name, proof);
                            System.out.println("proofsended");
                            //createButton(achievementName, "green", categoryName, name, proof);
                        }else{
                            createButton(achievementName,"black", categoryName, name, proof);
                            System.out.println("not ");
                        }
                    }

                    //createAchieveButton(name, desc);
                }
                // Здесь можно продолжить работу с полученным Map достижений
            }
        });
    }



    private void createButton(String name, String color, String categoryName, String username, boolean proof) {
        LinearLayout layout = findViewById(R.id.scrollView1);
        Button button = new Button(SeasonsAchievements.this);
        button.setText(name);
        button.setBackgroundColor(Color.BLUE);

        boolean received;

        // Получаем ссылку на коллекцию "Users"
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        // Создаем запрос, который возвращает только документы, у которых есть достижение "красноярск"
        //Query query = usersRef.whereEqualTo("userAchievements.Путешествие в Скандинавию.name", name);

        //System.out.println(query);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();
        // String confirmed = "sfd";

       /* userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");

                for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                    Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                    if (achievement.get("name").equals(name)) {
                       Boolean confirmed = (Boolean) achievement.get("confirmed");
                        System.out.println(confirmed);

                    }

                    //createAchieveButton(name, desc);
                }
                // Здесь можно продолжить работу с полученным Map достижений
            }
        });*/

        // здесь можно добавить дополнительные параметры для кнопки, например, размеры, цвет, обработчик нажатия и т.д.

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(20, 20, 20, 20);
        button.setBackgroundColor(Color.GREEN);

        if (color == "green"){
            button.setBackgroundResource(R.drawable.achievebackgroundgreen);
            received = true;
        }else if(color == "yellow"){
            button.setBackgroundResource(R.drawable.achievebackgroundyellow);
            received = true;
        }else{
            button.setBackgroundResource(R.drawable.achievebackground);
            received = false;
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработка нажатия кнопки
                Intent intent = new Intent(SeasonsAchievements.this, SeasonAchievementsDescriptionActivity.class);
                intent.putExtra("Achieve_key", name);
                intent.putExtra("Category_key", categoryName);
                intent.putExtra("Is_Received", received);
                intent.putExtra("User_name", username);
                intent.putExtra("ProofNeeded", proof);
                startActivity(intent);
            }
        });


        button.setLayoutParams(layoutParams);

        button.setTag(name);
        //button.setBackgroundResource(R.drawable.achievebackground);

        LinearLayout scrollView = findViewById(R.id.scrollView1);
        scrollView.addView(button);

    }
    public void p(int a, int count){
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(count);
        progressBar.setProgress(a);
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
            Intent intent = new Intent(SeasonsAchievements.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}