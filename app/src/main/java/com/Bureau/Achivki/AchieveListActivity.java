package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AchieveListActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private ImageButton backButton;

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    private int buttonCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve_list);

        db = FirebaseFirestore.getInstance();

        List<String> categories = new ArrayList<>();

        CollectionReference achievementsRef = db.collection("Achievements");


        TableLayout tableLayout = findViewById(R.id.tableLayout);

        Set<String> uniqueNames = new HashSet<>();

        backButton = findViewById(R.id.imageButtonBack);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveListActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveListActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveListActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveListActivity.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        achievementsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
                                    Intent intent = new Intent(AchieveListActivity.this, MainActivity2.class);
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
            }
        });


       /* achievementsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int buttonCount = 0;
                if (task.isSuccessful()) {
                    List<String> achievementNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String category = document.getString("category");

                        System.out.println("category " + category);

                        if (category != null && !categories.contains(category)) {
                            categories.add(category);
                        }

                        String achievementName = document.getId();
                        achievementNames.add(achievementName);
                       // buttonCount++;
                    }
                    createButtons(categories, buttonCount,500, "scrollView1");
                    buttonCount++;
                } else {
                    Log.d(TAG, "Error getting achievements: ", task.getException());
                }
            }
        });*/

    }

    private void createButtons(List<String> achievementNames, int count, int h, String layoutid) {
        LinearLayout layout = findViewById(R.id.scrollView1);
        for (String name : achievementNames) {
           /* Button button = new Button(this);
            button.setText(name);
            layout.addView(button);*/
            TableLayout tableLayout = findViewById(R.id.tableLayout);

            Button button = new Button(AchieveListActivity.this);
            button.setText(name);
            button.setBackgroundColor(Color.BLUE);


            /*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(w, h);*/
            /*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );*/

            //layoutParams.setMargins(20, 20, 20, 20);
            button.setBackgroundResource(R.drawable.template);
           // button.setLayoutParams(layoutParams);
            button.setTag(name);


            if (count % 2 == 0) {
                // Создать новую строку в таблице
                TableRow row = new TableRow(getApplicationContext());
                tableLayout.addView(row);
            }

            // Найти последнюю созданную строку и добавить кнопку в нее
            TableRow lastRow = (TableRow) tableLayout.getChildAt(tableLayout.getChildCount() - 1);
            lastRow.addView(button);

            // Увеличить счетчик кнопок



            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Обработка нажатия кнопки
                    System.out.println("Category_key  " + name);
                    Intent intent = new Intent(AchieveListActivity.this, MainActivity2.class);
                    intent.putExtra("Category_key", name);
                    startActivity(intent);
                }
            });

           /* if(layoutid == "scrollView1"){
                LinearLayout scrollView = findViewById(R.id.scrollView1);
                scrollView.addView(button);
            }
            if(layoutid == "favoritesLinearLayout"){
                LinearLayout scrollView = findViewById(R.id.favoritesLinearLayout);
                scrollView.addView(button);
            }*/
            //LinearLayout scrollView = findViewById(R.id.scrollView1);
            //scrollView.addView(button);

        }
    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}