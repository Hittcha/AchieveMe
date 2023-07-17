package com.Bureau.Achivki;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;


public class AchieveCategoryListActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 10;
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

        p(0,0);


        TextView categoryNameTextView = findViewById(R.id.categoryNameTextView);
        categoryNameTextView.setText(categoryName);
        ConstraintLayout topConstraintLayout = findViewById(R.id.top_constraint_layout);
        ImageView backgroundImage = findViewById(R.id.category_background);
//        Drawable drawableKalina = ContextCompat.getDrawable(this, R.drawable.kalina);

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



        switch (categoryName) {
            case "Красноярск":
                try {
                    InputStream ims = getAssets().open("category_background/city_krsk.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Еда и напитки":
                try {
                    InputStream ims = getAssets().open("food_and_drink.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Путешествия":
                try {
                    InputStream ims = getAssets().open("trips.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Кулинар":
                try {
                    InputStream ims = getAssets().open("chef.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Калининград":
                try {
                    InputStream ims = getAssets().open("category_background/city_kalina.png");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Фильмы":
                try {
                    InputStream ims = getAssets().open("category_small/films2.jpg");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Книги":
                try {
                    InputStream ims = getAssets().open("category_small/books2.jpg");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Москва":
                try {
                    InputStream ims = getAssets().open("category_small/moscow2.jpg");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Санкт Петербург":
                try {
                    InputStream ims = getAssets().open("category_small/sankt_petersburg2.jpg");
                    Drawable drawableBackground = Drawable.createFromStream(ims, null);
                    backgroundImage.setImageDrawable(drawableBackground);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                topConstraintLayout.setBackground(null);
                break;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                createAchieveList(currentUser.getUid());
            } else {
                // документ не найден
            }
        });
    }

    private void p(int a, int count){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(count);
        progressBar.setProgress(a);

        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);

        Intent intentMain = getIntent();
        String categoryName = intentMain.getStringExtra("Category_key");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(categoryName+"UserDoneScore", a);
        editor.putLong(categoryName+"MaxScore", count);
        editor.apply();
    }

    public void createAchieveList(String userId){


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
                            String countDesc = "Выполнено";
                            long dayLimit = 0;

                            if (document.contains("collectable")) {
                                collectable = Boolean.TRUE.equals(document.getBoolean("collectable"));
                                achieveCount = document.getLong("count");
                                dayLimit = document.getLong("dayLimit");
                                if (document.contains("countDesc")) {
                                    countDesc = document.getString("countDesc");
                                }
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
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                                            .inflate(R.layout.block_achieve_green, parentLayout, false);
                                    received = true;
                                    achievedone++;
                                }else if (Boolean.TRUE.equals(proofsended)) {
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                                            .inflate(R.layout.block_achieve_yellow, parentLayout, false);
                                    received = true;
                                    System.out.println("proofsended");
                                }else{
                                    blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
                                            .inflate(R.layout.block_achieve, parentLayout, false);
                                    received = false;
                                    System.out.println("not ");
                                }
                            }else{
                                blockLayout = (ConstraintLayout) LayoutInflater.from(AchieveCategoryListActivity.this)
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
                                if(countDesc.equals(null)){
                                    progressDesc.setText("\nВыполнено " + doneCount + " из " + achieveCount);
                                }else {
                                    progressDesc.setText(countDesc + ": " + (int) doneCount + " из " + (int) achieveCount);
                                }
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
                            long finalDoneCount = doneCount;

                            int blockPosition = parentLayout.indexOfChild(blockLayout);

                            blockLayout.setTag(received);

                            ConstraintLayout finalBlockLayout = blockLayout;

                            blockLayout.setOnClickListener(v -> {
                                Intent intent;
                                // Обработка нажатия кнопки
                                if (finalCollectable) {
                                    intent = new Intent(AchieveCategoryListActivity.this, AchievementWithProgressActivity.class);
                                } else {
                                    intent = new Intent(AchieveCategoryListActivity.this, AchievementDescriptionActivity.class);
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
                                intent.putExtra("doneCount", finalDoneCount);

                                intent.putExtra("blockPosition", blockPosition);

                                startActivityForResult(intent, REQUEST_CODE);
                            });
                        }
                        p(achievedone , count);
                    });
                }
            }
        });
    }
    private void ShowAchievementDescription() {
        PopupWindow popupWindow = new PopupWindow(this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.activity_achievement_description, null);
        popupWindow.setContentView(popupView);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
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

   /* protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Получение данных из возвращенного Intent
                boolean isReceived = data.getBooleanExtra("Is_Received", false);
                int blockPosition = data.getIntExtra("Block_Position", -1);

                if (blockPosition != -1) {
                    // Получение ссылки на LinearLayout, содержащий блоки
                    LinearLayout parentLayout = findViewById(R.id.scrollView1);

                    // Получение конкретного созданного блока по его позиции
                    ConstraintLayout blockLayout = (ConstraintLayout) parentLayout.getChildAt(blockPosition);

                    // Изменение фона блока на @drawable/achieve_unproof
                    blockLayout.setBackgroundResource(R.drawable.achieve_unproof);
                }
            }
        }
    }*/


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