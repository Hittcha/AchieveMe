package com.Bureau.Achivki;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity2 extends AppCompatActivity {

    private static final int REQUEST_CODE = 10;
    private static final int OTHER_ACTIVITY_REQUEST_CODE = 20;
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

    //private boolean received;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
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

        p(0,0);

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
                Intent intent = new Intent(MainActivity2.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();


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

    /*public void createAchieveList(String userId){


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


                    LinearLayout parentLayout = findViewById(R.id.scrollView1);
                    ConstraintLayout blockLayout;
                    try {
                        // Получение AssetManager
                        AssetManager manager = getAssets();

                        // Открытие JSON-файла
                        InputStream inputStream = manager.open("data.json");

                        // Чтение данных из файла
                        int size = inputStream.available();
                        byte[] buffer = new byte[size];
                        inputStream.read(buffer);
                        inputStream.close();

                        // Преобразование данных в строку
                        String jsonString = new String(buffer, StandardCharsets.UTF_8);

                        // Преобразование строки в объект JSON
                        JSONObject jsonObject = new JSONObject(jsonString);

                        // Обработка данных из JSON
                        JSONArray season1Array = jsonObject.getJSONArray("season1");
                        for (int i = 0; i < season1Array.length(); i++) {
                            boolean received;
                            JSONObject item = season1Array.getJSONObject(i);
                            String desc = item.getString("desc");
                            String achievementName = item.getString("name");
                            int price = item.getInt("price");
                            boolean proof = item.getBoolean("proof");

                            // Пример использования данных
                            Log.d("MainActivity", "Desc: " + desc);
                            Log.d("MainActivity", "Name: " + achievementName);
                            Log.d("MainActivity", "Price: " + price);
                            Log.d("MainActivity", "Proof: " + proof);

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

                            TextView AchieveNameTextView = blockLayout.findViewById(R.id.achieveName_blockTextView);
                            AchieveNameTextView.setText(achievementName);
                            parentLayout.addView(blockLayout);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }*/

    public void createAchieveList(String userId) {
        Intent intentMain = getIntent();
        // String categoryName = intentMain.getStringExtra("Category_key");
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

                    LinearLayout parentLayout = findViewById(R.id.scrollView1);
                    ConstraintLayout blockLayout;
                    try {
                        // Получение AssetManager
                        AssetManager manager = getAssets();

                        // Открытие JSON-файла
                        InputStream inputStream = manager.open("data.json");

                        // Чтение данных из файла
                        int size = inputStream.available();
                        byte[] buffer = new byte[size];
                        inputStream.read(buffer);
                        inputStream.close();

                        // Преобразование данных в строку
                        String jsonString = new String(buffer, StandardCharsets.UTF_8);

                        // Преобразование строки в объект JSON
                        JSONObject jsonObject = new JSONObject(jsonString);

                        // Обработка данных из JSON
                        JSONArray season1Array = jsonObject.getJSONArray(categoryName);
                        for (int i = 0; i < season1Array.length(); i++) {
                            count++;
                            boolean received;
                            //boolean proof = Boolean.TRUE.equals(document.getBoolean("proof"));
                            boolean collectable = false;
                            long achieveCount = 0;
                            long doneCount = 0;
                            String countDesc = "";
                            long dayLimit = 0;

                            JSONObject item = season1Array.getJSONObject(i);
                            String desc = item.getString("desc");
                            String achievementName = item.getString("name");
                            int price = item.getInt("price");
                            boolean proof = item.getBoolean("proof");

                            if (item.has("collectable")) {
                                collectable = item.getBoolean("collectable");
                                achieveCount = item.getLong("count");
                                dayLimit = item.getLong("dayLimit");
                                countDesc = item.getString("countDesc");
                                System.out.println("collectable " + collectable);
                            } else {
                                // Обработка ситуации, когда поле отсутствует
                                System.out.println("not collectable " + collectable);
                            }

                            long achievePrice = 0;
                            if (item.has("price")) {
                                achievePrice = item.getLong("price");
                                System.out.println("price " + achievePrice);
                            }


                            boolean isFavorites = false;

                            if (userFavorites.contains(achievementName)) {
                                isFavorites = true;
                            }
                            System.out.println("isFavorites " + isFavorites);

                            // Пример использования данных
                            Log.d("MainActivity", "Desc: " + desc);
                            Log.d("MainActivity", "Name: " + achievementName);
                            Log.d("MainActivity", "Price: " + price);
                            Log.d("MainActivity", "Proof: " + proof);

                            if (userAchievements.contains(achievementName)) {
                                System.out.println("Достижение \"" + achievementName + "\" есть и у пользователя, и в категории " + categoryName);
                                Map<String, Object> achievementMap = (Map<String, Object>) userAchieveMap.get(achievementName);

                                Boolean confirmed = (Boolean) achievementMap.get("confirmed");
                                Boolean proofsended = (Boolean) achievementMap.get("proofsended");
                                //long doneCount = 0;
                                if (achievementMap.containsKey("collectable")) {
                                    doneCount = (long) achievementMap.get("doneCount");
                                }
                                if (Boolean.TRUE.equals(confirmed)) {
                                    System.out.println("confirmed");
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                            .inflate(R.layout.block_achieve_green, parentLayout, false);
                                    received = true;
                                    achievedone++;
                                } else if (Boolean.TRUE.equals(proofsended)) {
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                            .inflate(R.layout.block_achieve_yellow, parentLayout, false);
                                    received = true;
                                    System.out.println("proofsended");
                                } else {
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                            .inflate(R.layout.block_achieve, parentLayout, false);
                                    received = false;
                                    System.out.println("not ");
                                }
                            } else {
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

                            int blockPosition = parentLayout.indexOfChild(blockLayout);

                            blockLayout.setTag(received);

                            ConstraintLayout finalBlockLayout = blockLayout;
                            blockLayout.setOnClickListener(v -> {
                                Intent intent;
                                // Обработка нажатия кнопки
                                if (finalCollectable) {
                                    intent = new Intent(this, AchievementWithProgressActivity.class);
                                } else {
                                    intent = new Intent(this, AchievementDescriptionActivity.class);
                                }

                                boolean currentReceived = (boolean) finalBlockLayout.getTag();
                                intent.putExtra("Is_Received", currentReceived);

                                intent.putExtra("Achieve_key", achievementName);
                                intent.putExtra("Category_key", categoryName);
                                //intent.putExtra("Is_Received", received);
                                intent.putExtra("ProofNeeded", proof);
                                intent.putExtra("collectable", finalCollectable);
                                intent.putExtra("achieveCount", finalAchieveCount);
                                intent.putExtra("dayLimit", finalDayLimit);
                                intent.putExtra("achievePrice", finalAchievePrice);
                                intent.putExtra("isFavorites", finalIsFavorites);
                                intent.putExtra("countDesc", finalCountDesc);

                                intent.putExtra("blockPosition", blockPosition);

                                startActivityForResult(intent, REQUEST_CODE);
                            });
                        }
                        p(achievedone , count);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LinearLayout parentLayout = findViewById(R.id.scrollView1);

        boolean received = false;

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Получение данных из возвращенного Intent
                boolean isAdded = data.getBooleanExtra("Is_Added", false);
                int blockPosition = data.getIntExtra("Block_Position", -1);

                long doneCount = data.getLongExtra("doneCount", -1);
                long achieveCount = data.getLongExtra("achieveCount", -1);
                String countDesc = data.getStringExtra("countDesc");

                boolean isCancelled = data.getBooleanExtra("Is_Cancelled", false);


                if (isAdded) {
                    // Действия при получении результата
                    if (blockPosition != -1) {
                        // Получение конкретного созданного блока по его позиции
                        ConstraintLayout blockLayout = (ConstraintLayout) parentLayout.getChildAt(blockPosition);

                        // Изменение фона блока на @drawable/achieve_proof
                        blockLayout.setBackgroundResource(R.drawable.achieve_proof);
                        received = true;

                        blockLayout.setTag(received);
                        blockLayout.setTag(R.id.countDesc, doneCount);

                        // Обновить текст progressDesc
                        ProgressBar progress = blockLayout.findViewById(R.id.achieveProgressBar);
                        TextView progressDesc = blockLayout.findViewById(R.id.countDesc);
                        progressDesc.setText(countDesc + ": " + doneCount + " из " + achieveCount);
                        progress.setMax((int)achieveCount);
                        progress.setProgress((int) doneCount);

                        if(achieveCount < 1) {
                            achievedone = achievedone + 1;
                        }else{
                            p(achievedone, count);
                        }
                        p(achievedone, count);
                    }
                }else if (isCancelled) {
                    // Действия при отмене

                    // Получение конкретного созданного блока по его позиции
                    ConstraintLayout blockLayout = (ConstraintLayout) parentLayout.getChildAt(blockPosition);

                    // Изменение фона блока на @drawable/achieve_unchecked
                    blockLayout.setBackgroundResource(R.drawable.achieve_unchecked);

                    received = false;
                    achievedone = achievedone - 1;
                    p(achievedone, count);

                    blockLayout.setTag(received);
                }

            }
        }
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
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}