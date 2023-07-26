package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
//        WindowCalculation windowCalculation = new WindowCalculation(this);
//        int screenHeight = windowCalculation.WindowCalculationHeight();
//
//            // меняем длинну основого layout
//        LinearLayout linearLayout = findViewById(R.id.activityMain_linearLayoutTop);
//        ViewGroup.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
//        layoutParams.height = screenHeight - 180;
//        Log.d("РАЗРЕШЕНИЕ", String.valueOf(screenHeight));


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
        if (userId.equals("oNJgYMcGVUg1vupBIWzT1fWdV372")){
            Button adminButton = findViewById(R.id.adminButton);
            adminButton.setVisibility(View.VISIBLE);
        }

        TextView welcomeMessage = findViewById(R.id.welcome_message);
        welcomeMessage.setText(savedName);


        TextView friendsListText = findViewById(R.id.friendsList);
        TextView subscriptionsListText = findViewById(R.id.subscriptionsList);
//        TextView scoreText = findViewById(R.id.scoreTextView);

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
            System.out.println(" fasdf sdf dsf dsgf sdgf dsg sdfg sd");
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

                listOfFavorites();
                //setImage(profileImageUrl);

                //Если аватар не сохранен локально - то грузим его с клауда и сохраняем
                if (!file.exists()) {
                    System.out.println("1 fasdf sdf dsf dsgf sdgf dsg sdfg sd");
                    setImage(profileImageUrl);
                }

            } else {
                //Toast.makeText(this, "Ошибка соеднинения", Toast.LENGTH_SHORT).show();
            }
        });

        // переключение между вкладками
        TextView textViewSeason = findViewById(R.id.textViewSeason);
        TextView textViewNewCategory = findViewById(R.id.textViewNewCategory);
        TextView textViewFavorites = findViewById(R.id.textViewFavorites);
        ScrollView scrollViewSeason = findViewById(R.id.scrollView_season);
        ScrollView scrollViewNewCategory = findViewById(R.id.scrollView_newCategory);
        ScrollView scrollViewFavorites = findViewById(R.id.scrollView_Favorites);

        textViewSeason.setOnClickListener(v -> {
            scrollViewSeason.setVisibility(View.VISIBLE);
            scrollViewNewCategory.setVisibility(View.GONE);
            scrollViewFavorites.setVisibility(View.GONE);
            textViewSeason.setTextColor(getResources().getColor(R.color.white));
            textViewNewCategory.setTextColor(getResources().getColor(R.color.inactive_button));
            textViewFavorites.setTextColor(getResources().getColor(R.color.inactive_button));
        });
        textViewNewCategory.setOnClickListener(v -> {
            scrollViewSeason.setVisibility(View.GONE);
            scrollViewNewCategory.setVisibility(View.VISIBLE);
            scrollViewFavorites.setVisibility(View.GONE);
            textViewSeason.setTextColor(getResources().getColor(R.color.inactive_button));
            textViewNewCategory.setTextColor(getResources().getColor(R.color.white));
            textViewFavorites.setTextColor(getResources().getColor(R.color.inactive_button));
        });
        textViewFavorites.setOnClickListener(v -> {
            scrollViewSeason.setVisibility(View.GONE);
            scrollViewNewCategory.setVisibility(View.GONE);
            scrollViewFavorites.setVisibility(View.VISIBLE);
            textViewSeason.setTextColor(getResources().getColor(R.color.inactive_button));
            textViewNewCategory.setTextColor(getResources().getColor(R.color.inactive_button));
            textViewFavorites.setTextColor(getResources().getColor(R.color.white));
        });

        friendsListText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyFriendsList.class);
            startActivity(intent);
        });

//        scoreText.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, MyCompletedAchievements.class);
//            startActivity(intent);
//        });

        subscriptionsListText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MySubscriptionsActivity.class);
            startActivity(intent);
        });


        //showStartAchieve();

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


        SVGImageView favoritesButton = findViewById(R.id.imageButtonFavorites);

        /*favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });*/

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewslineActivity.class);
            startActivity(intent);
        });

        ImageButton userButton = findViewById(R.id.userButton);
        SVGImageView leaderListButton = findViewById(R.id.imageButtonLeaderList);
        SVGImageView menuButton = findViewById(R.id.imageButtonMenu);
        SVGImageView achieveListButton = findViewById(R.id.imageButtonAchieveList);
//        ImageButton buttonSeasonAchieve = findViewById(R.id.imageButtonSeasonAchieve);
        SVGImageView usersListButton = findViewById(R.id.imageButtonUsersList);


        Button adminButton = findViewById(R.id.adminButton);

        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });

//        buttonSeasonAchieve.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, SeasonsAchievements.class);
//            startActivity(intent);
//        });

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

        // выставляем иконки
        try {
            InputStream inputStream = getAssets().open("interface_icon/kubok.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.score_cup);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/mesto.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.location_icon);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/home.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonMenu);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/rate.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonLeaderList);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/chel.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonUsersList);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/kubok niz.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonAchieveList);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/Star 1.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonFavorites);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }



    }

    private void showStartAchieve(){
        LinearLayout parentLayout = findViewById(R.id.scrollView1);
        parentLayout.removeAllViews();

        String achname = "name";
        String category = "category";

        ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.block_category, parentLayout, false);

        TextView CategoryNameTextView = blockLayout.findViewById(R.id.categoryNameTextView);

        CategoryNameTextView.setText(achname);

        parentLayout.addView(blockLayout);

        blockLayout.setBackgroundResource(R.drawable.template);
        parentLayout.addView(blockLayout);

    }

    private void listOfFavorites() {
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

            TableLayout tableLayout = new TableLayout(MainActivity.this);
            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT
            );
            tableLayout.setLayoutParams(tableLayoutParams);

            WindowCalculation windowCalculation = new WindowCalculation(this);
            int screenWeight = windowCalculation.WindowCalculationWeight();

            // Рассчитываем ширину блока на основе размера экрана
            int blockWidth = (screenWeight / 3) - 25;
            TableRow currentRow = null; // Текущая строка для добавления blockLayout
            int blockCount = 0; // Счетчик для отслеживания количества blockLayout в текущей строке

            for (Map.Entry<String, Object> entry : fav.entrySet()) {
                Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                String achname = (String) achievement.get("name");
                String category = (String) achievement.get("category");

                View blockLayout = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.block_achieve_category_list, currentRow, false);

                TextView favorites_name_TextView = blockLayout.findViewById(R.id.categoryNameTextView);

                ImageView favorites_icon_Button = blockLayout.findViewById(R.id.block_category_imageview);

                int maxTextLength = 13;

                assert achname != null;
                if (achname.length() > maxTextLength) {
                    String truncatedText = achname.substring(0, maxTextLength) + "…";
                    favorites_name_TextView.setText(truncatedText);
                    favorites_name_TextView.setEllipsize(TextUtils.TruncateAt.END);
                } else {
                    favorites_name_TextView.setText(achname);
                    favorites_name_TextView.setEllipsize(null);
                }


                if (blockCount == 0) {
                    // Создать новую строку, если текущая строка пустая
                    currentRow = new TableRow(MainActivity.this);
                    tableLayout.addView(currentRow);
                }

                // Создать новый blockLayout с названием достижения и установить ширину и высоту

                TableRow.LayoutParams blockLayoutParams = new TableRow.LayoutParams(
                        blockWidth,
                        blockWidth
                );
                blockLayoutParams.setMargins(10, 10, 10, 10); // Устанавливаем отступы между блоками
                blockLayout.setLayoutParams(blockLayoutParams);

                currentRow.addView(blockLayout);
                blockCount++;

                // Создать новую строку, если в текущей строке уже есть два blockLayout
                if (blockCount == 3) {
                    blockCount = 0;
                    currentRow = null;
                }

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                layoutParams.setMargins(10, 10, 10, 10);

                switch (category) {
                    case "Красноярск":
                        try {
                            InputStream inputStream = getAssets().open("favorites/krasnoyarsk_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
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
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
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
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
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
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
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
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "season1":
                        try {
                            InputStream inputStream = assetManager.open("favorites/season1_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Книги":
                        try {
                            InputStream inputStream = assetManager.open("favorites/books_favorites.jpg");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Москва":
                        try {
                            InputStream inputStream = assetManager.open("favorites/moscow_favorites.jpg");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Фильмы":
                        try {
                            InputStream inputStream = assetManager.open("favorites/films_favorites.jpg");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Санкт Петербург":
                        try {
                            InputStream inputStream = assetManager.open("favorites/sankt_petersburg_favorites.jpg");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(30); // Здесь можно указать радиус закругления
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
                    CollectionReference achievementsCollectionRef;
                    if (category.equals("season1")) {
                        achievementsCollectionRef = FirebaseFirestore.getInstance().collection("SeasonAchievements");
                    } else {
                        achievementsCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");
                    }
                    Query categoryQuery = achievementsCollectionRef.whereEqualTo("name", achname);
                    categoryQuery.get().addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {

                            boolean proof = Boolean.TRUE.equals(document.getBoolean("proof"));
                            boolean collectable = false;
                            long achieveCount = 0;
                            String countDesc = "";
                            long dayLimit = 0;

                            long achievePrice = 0;
                            if (document.contains("price")) {
                                achievePrice = document.getLong("price");
                                System.out.println("price " + achievePrice);
                            }

                            Intent intent = new Intent(MainActivity.this, AchievementDescriptionActivity.class);

                            if (document.contains("collectable")) {
                                System.out.println("collectable");
                                collectable = Boolean.TRUE.equals(document.getBoolean("collectable"));
                                achieveCount = document.getLong("count");
                                dayLimit = document.getLong("dayLimit");
                                countDesc = document.getString("countDesc");
                                intent = new Intent(MainActivity.this, AchievementWithProgressActivity.class);
                            }

                            if (userAchievements.contains(achname)) {
                                System.out.println("Достижение \"" + achname + "\" есть и у пользователя, и в категории " + category);
                                intent.putExtra("dayLimit", dayLimit);
                                intent.putExtra("achieveCount", achieveCount);
                                intent.putExtra("collectable", collectable);
                                intent.putExtra("Category_key", category);
                                intent.putExtra("Achieve_key", achname);
                                intent.putExtra("achievePrice", achievePrice);
                                intent.putExtra("Is_Received", true);
                                intent.putExtra("ProofNeeded", proof);
                                intent.putExtra("isFavorites", true);
                                startActivity(intent);
                            } else {
                                intent.putExtra("dayLimit", dayLimit);
                                intent.putExtra("achieveCount", achieveCount);
                                intent.putExtra("collectable", collectable);
                                intent.putExtra("Category_key", category);
                                intent.putExtra("Achieve_key", achname);
                                intent.putExtra("achievePrice", achievePrice);
                                intent.putExtra("Is_Received", false);
                                intent.putExtra("ProofNeeded", proof);
                                intent.putExtra("isFavorites", true);
                                startActivity(intent);
                            }
                        }
                    });
                });
            }
            ScrollView parentLayout = findViewById(R.id.scrollView_Favorites);

            parentLayout.addView(tableLayout);
//            ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(MainActivity.this)
//                    .inflate(R.layout.block_favorites_icon, parentLayout, false);
//
//            TextView favorites_name_TextView = blockLayout.findViewById(R.id.favorites_name_TextView);
//
//            Button favorites_icon_Button = blockLayout.findViewById(R.id.favorites_icon_Button);
//            favorites_icon_Button.setBackgroundResource(R.drawable.addfavoritesbutton);
//
//            favorites_name_TextView.setText("");
//
//            favorites_icon_Button.setOnClickListener(v -> {
//                Intent intent = new Intent(MainActivity.this, AchieveListActivity.class);
//                startActivity(intent);
//
//            });

//            parentLayout.addView(blockLayout);
        });
    }

    private void createCategoryBlock(List<String> achievementNames){


        AssetManager assetManager = getAssets();
        // Создание таблицы
        TableLayout tableLayout = new TableLayout(MainActivity.this);
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT
        );
        tableLayout.setLayoutParams(tableLayoutParams);

        // Получение размера экрана
        WindowCalculation windowCalculation = new WindowCalculation(this);
        int screenWeight = windowCalculation.WindowCalculationWeight();

        // Рассчитываем ширину блока на основе размера экрана
        int blockWidth = (screenWeight / 3)-25; // Делим ширину экрана на 3
        TableRow currentRow = null; // Текущая строка для добавления blockLayout
        int blockCount = 0; // Счетчик для отслеживания количества blockLayout в текущей строке

        for (String name : achievementNames) {

//            ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(MainActivity.this)
//                    .inflate(R.layout.block_category, parentLayout, false);
            View blockLayout = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.block_achieve_category_list, currentRow, false);
            TextView CategoryNameTextView = blockLayout.findViewById(R.id.categoryNameTextView);

            ImageView backGroundImageView = blockLayout.findViewById(R.id.block_category_imageview);

            // Обрезаем название категории, если оно слишком большое
            int maxTextLength = 9;

            if (name.length() > maxTextLength) {
                String truncatedText = name.substring(0, maxTextLength) + "…";
                CategoryNameTextView.setText(truncatedText);
                CategoryNameTextView.setEllipsize(TextUtils.TruncateAt.END);
            } else {
                CategoryNameTextView.setText(name);
                CategoryNameTextView.setEllipsize(null);
            }

            if (blockCount == 0) {
                // Создать новую строку, если текущая строка пустая
                currentRow = new TableRow(MainActivity.this);
                tableLayout.addView(currentRow);
            }

            // Создать новый blockLayout с названием достижения и установить ширину и высоту

            TableRow.LayoutParams blockLayoutParams = new TableRow.LayoutParams(
                    //TableRow.LayoutParams.MATCH_PARENT,
                    blockWidth,
                    blockWidth
            );
            blockLayoutParams.setMargins(7, 7, 7, 7); // Устанавливаем отступы между блоками
            blockLayout.setLayoutParams(blockLayoutParams);

            TextView favorites_name_TextView = blockLayout.findViewById(R.id.categoryNameTextView);
            ImageView favorites_icon_Button = blockLayout.findViewById(R.id.block_category_imageview);


            TextView categoryCount_TextView = blockLayout.findViewById(R.id.categoryCountText);
            SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);

            long achieveCategoryUserDoneScore = sharedPreferences.getLong(name+"UserDoneScore", 0);
            long achieveCategoryMaxScore = sharedPreferences.getLong(name+"MaxScore", 0);

            if(achieveCategoryUserDoneScore != 0 && achieveCategoryMaxScore !=0) {
                categoryCount_TextView.setText(achieveCategoryUserDoneScore + "/" + achieveCategoryMaxScore);
            }else{
                categoryCount_TextView.setText("");
            }



//            favorites_name_TextView.setText(categoryName);
            currentRow.addView(blockLayout);
            blockCount++;

            // Создать новую строку, если в текущей строке уже есть два blockLayout
            if (blockCount == 3) {
                blockCount = 0;
                currentRow = null;
            }

//            parentLayout.addView(blockLayout);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            layoutParams.setMargins(7, 7, 7, 7);

            switch (name) {
                case "Красноярск":
                    try {
                        InputStream inputStream = getAssets().open("category_small/krasnoyarsk2.jpg");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "Еда и напитки":
                    try {
                        InputStream inputStream = assetManager.open("category_small/food and drink.png");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
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
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
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
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
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
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                    break;
                case "Фильмы":
                    try {
                        InputStream inputStream = assetManager.open("category_small/films2.jpg");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                    break;
                case "Книги":
                    try {
                        InputStream inputStream = assetManager.open("category_small/books2.jpg");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                    break;
                case "Москва":
                    try {
                        InputStream inputStream = assetManager.open("category_small/moscow2.jpg");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                    break;
                case "Санкт Петербург":
                    try {
                        InputStream inputStream = assetManager.open("category_small/sankt_petersburg2.jpg");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(40); // Здесь можно указать радиус закругления
                        backGroundImageView.setBackground(roundedBitmapDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                    break;
                default:
                    blockLayout.setBackgroundResource(R.drawable.template);
                    break;
            }

            backGroundImageView.setOnClickListener(v -> {
                // Обработка нажатия кнопки
                System.out.println("Category_key  " + name);
                Intent intent = new Intent(MainActivity.this, AchieveCategoryListActivity.class);
                intent.putExtra("Category_key", name);
                startActivity(intent);

            });

        }
        ViewGroup parentLayout = findViewById(R.id.scrollView_newCategory);
        parentLayout.addView(tableLayout);
    }

    private void CreateSeasonBlock() {


        // Запрос на добавлние достижений сезонного челленджа


    }
    /*public void setImage(String imageRef) {

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
    }*/

    public void setImage(String imageRef) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef1 = storageRef.child(imageRef);

        ImageButton userButton = findViewById(R.id.userButton);
        imageRef1.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(userButton);

            // Сохранение изображения в локальные файлы, если оно еще не сохранено
            File file = new File(this.getFilesDir(), "UserAvatar");
            if (!file.exists()) {
                imageRef1.getMetadata().addOnSuccessListener(storageMetadata -> {
                    String mimeType = storageMetadata.getName();
                    System.out.println("mimeType " + mimeType);
                    if (mimeType != null && mimeType.startsWith("User")) {
                        imageRef1.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            saveAvatarToLocalFiles(bitmap);
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                    }
                });
            }
        }).addOnFailureListener(exception -> {
            // Обработка ошибки загрузки изображения
        });
    }


    /*private void loadAvatarFromLocalFiles() {
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
    }*/

    /*private void loadAvatarFromLocalFiles() {
        ImageButton userButton = findViewById(R.id.userButton);

        try {
            // Создание файла с указанным именем в локальной директории приложения
            File file = new File(this.getFilesDir(), "UserAvatar");

            // Загрузка изображения из файла с использованием Picasso
            Picasso.get()
                    .load(file)
                    .fit()
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(userButton);
        } catch (Exception e) {
            e.printStackTrace();
            // Обработка ошибки загрузки изображения из локальных файлов
            // setImage(profileImageUrl);
        }
    }*/

    private void loadAvatarFromLocalFiles() {
        ImageView userButton = findViewById(R.id.userButton);

        try {
            // Создание файла с указанным именем в локальной директории приложения
            File file = new File(this.getFilesDir(), "UserAvatar");

            // Чтение файла в виде Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            // Преобразование Bitmap в круговой вид (если необходимо)
            CircleTransform circleTransform = new CircleTransform();
            Bitmap circleBitmap = circleTransform.transform(bitmap);

            // Установка кругового Bitmap в качестве изображения для ImageView
            userButton.setImageBitmap(circleBitmap);
        } catch (Exception e) {
            e.printStackTrace();
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

//        listOfFavorites();
    }
}