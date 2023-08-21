package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class AchieveListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve_list);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Категории");*/

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.appBackGround));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference achievementsRef = db.collection("Achievements");


        ImageButton backButton = findViewById(R.id.imageButtonBack);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        SVGImageView leaderListButton = findViewById(R.id.imageButtonLeaderList);
        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        SVGImageView menuButton = findViewById(R.id.imageButtonMenu);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        SVGImageView achieveListButton = findViewById(R.id.imageButtonAchieveList);
        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, AchieveListActivity.class);
            startActivity(intent);
        });

        SVGImageView favoritesButton = findViewById(R.id.imageButtonFavorites);
        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        SVGImageView usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, UsersListActivity.class);
            startActivity(intent);
        });

        // Создание таблицы
        TableLayout tableLayout = new TableLayout(AchieveListActivity.this);
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT
        );
        tableLayout.setLayoutParams(tableLayoutParams);

        // Получение размера экрана
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Рассчитываем ширину блока на основе размера экрана
        int blockWidth = (screenWidth / 2)-25; // Делим ширину экрана пополам
        int blockHeight = screenHeight / 4;

        // Получение данных из базы данных и добавление blockLayout в таблицу
        achievementsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Обработка полученных документов
                AssetManager assetManager = getAssets();
                TableRow currentRow = null; // Текущая строка для добавления blockLayout
                int blockCount = 0; // Счетчик для отслеживания количества blockLayout в текущей строке
                Set<String> uniqueNames = new HashSet<>(); // Множество для отслеживания уникальных названий

                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Получить название достижения из документа
                    String categoryName = document.getString("category");

                    // Проверить, является ли это уникальным именем
                    if (!uniqueNames.contains(categoryName)) {
                        uniqueNames.add(categoryName); // Добавить название в множество

                        if (blockCount == 0) {
                            // Создать новую строку, если текущая строка пустая
                            currentRow = new TableRow(AchieveListActivity.this);
                            tableLayout.addView(currentRow);
                        }

                        // Создать новый blockLayout с названием достижения и установить ширину и высоту
                        View blockLayout = LayoutInflater.from(AchieveListActivity.this)
                                .inflate(R.layout.block_achieve_category_list, currentRow, false);
                        TableRow.LayoutParams blockLayoutParams = new TableRow.LayoutParams(
                                //TableRow.LayoutParams.MATCH_PARENT,
                                blockWidth,
                                blockHeight
                        );
                        blockLayoutParams.setMargins(12, 12, 12, 12); // Устанавливаем отступы между блоками
                        blockLayout.setLayoutParams(blockLayoutParams);

                        TextView favorites_name_TextView = blockLayout.findViewById(R.id.categoryNameTextView);
                        ImageView favorites_icon_Button = blockLayout.findViewById(R.id.block_category_imageview);

                        favorites_name_TextView.setText(categoryName);
                        currentRow.addView(blockLayout);
                        blockCount++;

                        // Создать новую строку, если в текущей строке уже есть два blockLayout
                        if (blockCount == 2) {
                            blockCount = 0;
                            currentRow = null;
                        }

                        switch (categoryName) {
                            case "Красноярск":
                                try {
                                    InputStream inputStream = getAssets().open("category_small/krasnoyarsk2.jpg");
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
                                    InputStream inputStream = assetManager.open("category_small/food and drink.png");
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
                                    InputStream inputStream = assetManager.open("category_small/traveling.png");
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
                                    InputStream inputStream = assetManager.open("category_small/cooking.png");
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
                                    InputStream inputStream = assetManager.open("category_small/kaliningrad.png");
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                    roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                                    favorites_icon_Button.setBackground(roundedBitmapDrawable);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "Фильмы":
                                try {
                                    InputStream inputStream = assetManager.open("category_small/films2.jpg");
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                    roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                                    favorites_icon_Button.setBackground(roundedBitmapDrawable);
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
                                    roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                                    favorites_icon_Button.setBackground(roundedBitmapDrawable);
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
                                    roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                                    favorites_icon_Button.setBackground(roundedBitmapDrawable);
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
                                    roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                                    favorites_icon_Button.setBackground(roundedBitmapDrawable);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                                break;
                            default:
                                blockLayout.setBackgroundResource(R.drawable.template);
                                break;
                        }

                        favorites_icon_Button.setOnClickListener(v -> {
                            // Обработка нажатия кнопки
                            Intent intent = new Intent(AchieveListActivity.this, AchieveCategoryListActivity.class);
                            intent.putExtra("Category_key", categoryName);
                            startActivity(intent);
                        });
                    }
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });

        // Добавление таблицы в родительский контейнер
        ViewGroup parentLayout = findViewById(R.id.scrollView2);
        parentLayout.addView(tableLayout);

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

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
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