package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private String userName;
    private String profileImageUrl;
    private Long userScore;
    private Long userSubs;
    private Long userFriends;

    private NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //обновление экрана при свайпе вниз
        /*SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            recreate();
            swipeRefreshLayout.setRefreshing(false);
        });*/

       /*nestedScrollView = findViewById(R.id.nestedScrollView);

        nestedScrollView.setOnTouchListener(new View.OnTouchListener() {
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endY = event.getY();
                        if (startY - endY < 200) {
                            // Свайп вниз с достаточной высотой
                            // Выполните операции обновления экрана здесь

                            // Например, обновите данные или перезагрузите фрагмент
                            // Или вызовите метод, который обновляет ваш интерфейс
                            recreate();
                        }
                        break;
                }
                return false;
            }
        });*/


        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);

        String savedName = sharedPreferences.getString("Name", "");
        userScore = sharedPreferences.getLong("Score", 0);
        userSubs = sharedPreferences.getLong("Subs", 0);
        userFriends = sharedPreferences.getLong("Friends", 0);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.appBackGround));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference achievementsRef = db.collection("Achievements");

        List<String> categories = new ArrayList<>();


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String userId = currentUser.getUid();

        //Проверка айди админа
        if (userId.equals("S5S9nb7f0qheRWwlDu1CyCG4QxO2")){
            Button adminButton = findViewById(R.id.adminButton);
            adminButton.setVisibility(View.VISIBLE);
        }

        TextView welcomeMessage = findViewById(R.id.welcome_message);
        welcomeMessage.setText(savedName);


        TextView friendsListText = findViewById(R.id.friendsList);
        TextView subscriptionsListText = findViewById(R.id.subscriptionsList);
        TextView scoreText = findViewById(R.id.scoreTextView);

        TextView userScoreText = findViewById(R.id.userScore);
        TextView userSubsText = findViewById(R.id.subsCountTextView);
        TextView userFriendsText = findViewById(R.id.friendsCountTextView);

        userScoreText.setText("" + userScore);
        userFriendsText.setText("" + userFriends);
        userSubsText.setText("" + userSubs);

        //Грузим аватар из локальных файлов, а если нет... то нет хэх
        File file = new File(this.getFilesDir(), "UserAvatar");
                    if (file.exists()) {
                        loadAvatarFromLocalFiles();
                    }

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());


        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName = documentSnapshot.getString("name");
                userScore = documentSnapshot.getLong("score");
                userSubs = documentSnapshot.getLong("subs");
                userFriends = documentSnapshot.getLong("friendscount");
                profileImageUrl = documentSnapshot.getString("profileImageUrl");

                userScoreText.setText("" + userScore);
                userFriendsText.setText("" + userFriends);
                userSubsText.setText("" + userSubs);


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Name", userName);
                editor.putLong("Score", userScore);
                editor.putLong("Subs", userSubs);
                editor.putLong("Friends", userFriends);
                editor.apply();

                //listOfFavorites();
                setImage(profileImageUrl);

                //Если аватар не сохранен локально - то грузим его с клауда и сохраняем
                if (!file.exists()) {
                    setImage(profileImageUrl);
                }

            } else {
                //Toast.makeText(this, "Ошибка соеднинения", Toast.LENGTH_SHORT).show();
            }
        });

        friendsListText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyFriendsList.class);
            startActivity(intent);
        });

        scoreText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyCompletedAchievements.class);
            startActivity(intent);
        });

        subscriptionsListText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MySubscriptionsActivity.class);
            startActivity(intent);
        });

        achievementsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String category = document.getString("category");

                    if (category != null && !categories.contains(category)) {
                        categories.add(category);
                    }
                }
                createCategoryBlock(categories);
            } else {
                Log.d(TAG, "Error getting achievements: ", task.getException());
            }
        });


        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        ImageButton userButton = findViewById(R.id.userButton);
        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);
        ImageButton buttonSeasonAchieve = findViewById(R.id.imageButtonSeasonAchieve);
        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);


        Button adminButton = findViewById(R.id.adminButton);

        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        buttonSeasonAchieve.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SeasonsAchievements.class);
            startActivity(intent);
        });

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AchieveListActivity.class);
            startActivity(intent);
        });
        userButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserProfile.class);
            startActivity(intent);
        });

        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UsersListActivity.class);
            startActivity(intent);
        });

    }

    private void listOfFavorites() {

        LinearLayout parentLayout = findViewById(R.id.favoritesLinearLayout);
        parentLayout.removeAllViews();

        AssetManager assetManager = getAssets();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userData = documentSnapshot.getData();
            Map<String, Object> fav = (Map<String, Object>) userData.get("favorites");

            Map<String, Object> userAchieveMap = (Map<String, Object>) userData.get("userAchievements");
            // Получение достижений пользователя
            Set<String> userAchievements = userAchieveMap.keySet();

            for (Map.Entry<String, Object> entry : fav.entrySet()) {
                Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                String achname = (String) achievement.get("name");
                String category = (String) achievement.get("category");

                ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.block_favorites_icon, parentLayout, false);

                TextView favorites_name_TextView = blockLayout.findViewById(R.id.favorites_name_TextView);

                Button favorites_icon_Button = blockLayout.findViewById(R.id.favorites_icon_Button);

                favorites_name_TextView.setText(achname);

                parentLayout.addView(blockLayout);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                switch (category) {
                    case "Красноярск":
                        try {
                            InputStream inputStream = getAssets().open("favorites/krasnoyarsk_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Еда и напитки":
                        try {
                            InputStream inputStream = assetManager.open("favorites/food_and_drink_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Путешествия":
                        try {
                            InputStream inputStream = assetManager.open("favorites/traveling_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Кулинар":
                        try {
                            InputStream inputStream = assetManager.open("favorites/cooking_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Калининград":
                        try {
                            InputStream inputStream = assetManager.open("favorites/kaliningrad_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        blockLayout.setBackgroundResource(R.drawable.template);
                        break;
                }
                favorites_icon_Button.setOnClickListener(v -> {
                    // Обработка нажатия кнопки

                    CollectionReference achievementsCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");

                   // CollectionReference usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

                    Query categoryQuery = achievementsCollectionRef.whereEqualTo("name", achname);
                    categoryQuery.get().addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            //count++;

                            // Получаем имя достижения из документа
                            String achievementName = document.getString("name");

                            boolean proof = Boolean.TRUE.equals(document.getBoolean("proof"));
                            boolean collectable = false;
                            long achieveCount = 0;
                            long doneCount = 0;
                            String countDesc = "";
                            long dayLimit = 0;

                            long achievePrice = 0;
                            if (document.contains("price")) {
                                achievePrice = document.getLong("price");
                                System.out.println("price " + achievePrice);
                            }

                            if (document.contains("collectable")) {
                                collectable = Boolean.TRUE.equals(document.getBoolean("collectable"));
                                achieveCount = document.getLong("count");
                                dayLimit = document.getLong("dayLimit");
                                countDesc = document.getString("countDesc");
                            } else {
                                // Обработка ситуации, когда поле отсутствует
                            }


                            Intent intent = new Intent(MainActivity.this, AchievementDescriptionActivity.class);


                            if (userAchievements.contains(achname)) {
                                System.out.println("Достижение \"" + achname + "\" есть и у пользователя, и в категории " + category);
                                Map<String, Object> achievementMap = (Map<String, Object>) userAchieveMap.get(achievementName);
                                if (document.contains("collectable")) {
                                    doneCount = (long) achievementMap.get("doneCount");

                                    intent.putExtra("collectable", collectable);
                                    intent.putExtra("dayLimit", dayLimit);
                                    intent.putExtra("achieveCount", achieveCount);
                                }

                                intent.putExtra("Category_key", category);
                                intent.putExtra("Achieve_key", achname);
                                intent.putExtra("achievePrice", achievePrice);
                                intent.putExtra("Is_Received", true);
                                intent.putExtra("ProofNeeded", proof);
                                startActivity(intent);

                                System.out.println("doneCount"+ doneCount);
                                //checkStatus(achievementName, categoryName, name, proof, collectable, achieveCount, doneCount, countDesc, dayLimit, achievePrice);
                                //achievedone++;
                            }else{
                                //createAchieveBlock(achievementName, "black", categoryName, name, proof, collectable, achieveCount, 0, countDesc, dayLimit, achievePrice);
                                System.out.println("Нет " + achievementName);

                                if (document.contains("collectable")) {

                                    intent.putExtra("collectable", collectable);
                                    intent.putExtra("dayLimit", dayLimit);
                                    intent.putExtra("achieveCount", achieveCount);
                                }

                                intent.putExtra("Category_key", category);
                                intent.putExtra("Achieve_key", achname);
                                intent.putExtra("achievePrice", achievePrice);
                                intent.putExtra("Is_Received", false);
                                intent.putExtra("ProofNeeded", proof);
                                startActivity(intent);
                            }

                        }
                    });
                });
            }

            ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.block_favorites_icon, parentLayout, false);

            TextView favorites_name_TextView = blockLayout.findViewById(R.id.favorites_name_TextView);

            Button favorites_icon_Button = blockLayout.findViewById(R.id.favorites_icon_Button);
            favorites_icon_Button.setBackgroundResource(R.drawable.addfavoritesbutton);

            favorites_name_TextView.setText("");

            favorites_icon_Button.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AchieveListActivity.class);
                startActivity(intent);

            });

            parentLayout.addView(blockLayout);
        });
    }

    private void createCategoryBlock(List<String> achievementNames){
        LinearLayout parentLayout = findViewById(R.id.scrollView1);

        AssetManager assetManager = getAssets();

        for (String name : achievementNames) {

            ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.block_category, parentLayout, false);

            TextView CategoryNameTextView = blockLayout.findViewById(R.id.categoryNameTextView);

            ImageView backGroundImageView = blockLayout.findViewById(R.id.block_category_imageview);

            CategoryNameTextView.setText(name);

            parentLayout.addView(blockLayout);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            layoutParams.setMargins(20, 20, 20, 20);

            switch (name) {
                case "Красноярск":
                    try {
                        InputStream inputStream = getAssets().open("category_small/krasnoyarsk.png");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                        backGroundImageView.setImageDrawable(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "Еда и напитки":
                    try {
                        InputStream inputStream = assetManager.open("category_small/food and drink.png");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                        backGroundImageView.setImageDrawable(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_food);
                    break;
                case "Путешествия":
                    try {
                        InputStream inputStream = assetManager.open("category_small/traveling.png");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                        backGroundImageView.setImageDrawable(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_travel);
                    break;
                case "Кулинар":
                    try {
                        InputStream inputStream = assetManager.open("category_small/cooking.png");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                        backGroundImageView.setImageDrawable(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                    break;
                case "Калининград":
                    try {
                        InputStream inputStream = assetManager.open("category_small/kaliningrad.png");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                        backGroundImageView.setImageDrawable(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                    break;
                default:
                    blockLayout.setBackgroundResource(R.drawable.template);
                    break;
            }

            blockLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Обработка нажатия кнопки
                    System.out.println("Category_key  " + name);
                    Intent intent = new Intent(MainActivity.this, AchieveCategoryListActivity.class);
                    intent.putExtra("Category_key", name);
                    startActivity(intent);

                }
            });
        }
    }

    public void setImage(String imageRef) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference imageRef1 = storageRef.child(imageRef);

        System.out.println("URL " + imageRef1);

        ImageButton userButton = findViewById(R.id.userButton);
        imageRef1.getMetadata().addOnSuccessListener(storageMetadata -> {
            String mimeType = storageMetadata.getName();
            System.out.println("mimeType " + mimeType);
            if (mimeType != null && mimeType.startsWith("User")) {
                imageRef1.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Paint paint = new Paint();
                    paint.setShader(shader);

                    Canvas canvas = new Canvas(circleBitmap);
                    if (bitmap.getHeight() > bitmap.getWidth()){
                        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
                    }else{
                        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getHeight() / 2f, paint);
                    }
                    File file = new File(this.getFilesDir(), "UserAvatar");
                    if (!file.exists()) {
                        saveAvatarToLocalFiles(bitmap);
                    }
                    userButton.setImageBitmap(circleBitmap);
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                });
            }
        });
    }

    private void loadAvatarFromLocalFiles() {
        ImageButton userButton = findViewById(R.id.userButton);

        try {
            // Создание файла с указанным именем в локальной директории приложения
            File file = new File(this.getFilesDir(), "UserAvatar");

            // Чтение файла в виде Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            // Преобразование Bitmap в круговой вид
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);

            Canvas canvas = new Canvas(circleBitmap);
            if (bitmap.getHeight() > bitmap.getWidth()){
                canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
            }else{
                canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getHeight() / 2f, paint);
            }

            // Установка кругового Bitmap в качестве изображения для кнопки
            userButton.setImageBitmap(circleBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            //setImage(profileImageUrl);
        }
    }

    private void saveAvatarToLocalFiles(Bitmap bitmap){
        //final long MAX_DOWNLOAD_SIZE = 1024 * 1024; // Максимальный размер файла для загрузки
        try {
            // Создание локального файла для сохранения изображения
            File file = new File(this.getFilesDir(), "UserAvatar");

            // Сохранение Bitmap в файл
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void onBackPressed() {
        // Пустая реализация, чтобы ничего не происходило при нажатии кнопки "Назад"
    }
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);

        listOfFavorites();
    }
}