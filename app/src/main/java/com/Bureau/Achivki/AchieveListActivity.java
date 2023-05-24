package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AchieveListActivity extends AppCompatActivity {

    private int buttonCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Категории");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> categories = new ArrayList<>();

        CollectionReference achievementsRef = db.collection("Achievements");


        TableLayout tableLayout = findViewById(R.id.tableLayout);

        Set<String> uniqueNames = new HashSet<>();

        ImageButton backButton = findViewById(R.id.imageButtonBack);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);

        ImageButton menuButton = findViewById(R.id.imageButtonMenu);

        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);

        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, AchieveListActivity.class);
            startActivity(intent);
        });

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchieveListActivity.this, UsersListActivity.class);
            startActivity(intent);
        });

        achievementsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Обработка полученных документов
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Получить название достижения из документа
                    String categoryName = document.getString("category");

                    // Проверить, является ли это уникальным именем
                    if (!uniqueNames.contains(categoryName)) {
                        // Создать новую кнопку с названием достижения
                        Button button = new Button(getApplicationContext());
                        button.setText(categoryName);

                        button.setBackgroundResource(R.drawable.template);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Обработка нажатия кнопки
                                System.out.println("Category_key  " + categoryName);
                                Intent intent = new Intent(AchieveListActivity.this, AchieveCategoryListActivity.class);
                                intent.putExtra("Category_key", categoryName);
                                startActivity(intent);
                            }
                        });

                        // Добавить имя в HashSet уникальных имен
                        uniqueNames.add(categoryName);

                        // Проверить, должны ли мы создать новую строку в таблице
                        if (buttonCount % 2 == 0) {
                            // Создать новую строку в таблице
                            TableRow row = new TableRow(getApplicationContext());
                            row.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT,
                                    1.0f
                            ));
                            tableLayout.addView(row);
                        }

                        // Найти последнюю созданную строку и добавить кнопку в нее
                        TableRow lastRow = (TableRow) tableLayout.getChildAt(tableLayout.getChildCount() - 1);
                        TableRow.LayoutParams params = new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );
                        params.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
                        button.setLayoutParams(params);
                        lastRow.addView(button);

                        // Увеличить счетчик кнопок
                        buttonCount++;
                    }
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
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