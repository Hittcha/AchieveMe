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
import android.widget.TextView;

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
import java.util.List;

public class ListOfFavoritesActivity extends AppCompatActivity {

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_favorites);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfFavoritesActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfFavoritesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfFavoritesActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfFavoritesActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfFavoritesActivity.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        ArrayList<String> achievements = (ArrayList<String>) document.get("favorites");

                        String userName = (String) document.get("name");


                        System.out.println("name " + userName);

                        createAchieveButton(achievements, userName);

                        // теперь у вас есть имя пользователя и список его достижений
                    } else {
                        // обработка ошибки - документ не существует
                    }
                } else {
                    // обработка ошибки - загрузка данных не удалась
                }
            }
        });
    }

    private void createAchieveButton(ArrayList<String> achieveName, String userName) {

        for (String name : achieveName) {

            LinearLayout parentLayout = findViewById(R.id.scrollView);

            //Button btnAdd = findViewById(R.id.btn_add);

            CollectionReference achievementCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");

            ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(ListOfFavoritesActivity.this)
                    .inflate(R.layout.block_layout, parentLayout, false);

            TextView usernameTextView = blockLayout.findViewById(R.id.username);
            TextView balanceTextView = blockLayout.findViewById(R.id.balance);
            //String achievementDesc;

            // Задайте текст для TextView в соответствии с данными пользователя

            Query query = achievementCollectionRef.whereEqualTo("name", name);

            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    String achievementDesc = null;
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        achievementDesc = documentSnapshot.getString("desc");
                    }

                    balanceTextView.setText(achievementDesc);
                }
            });

            ImageButton deleteAch = blockLayout.findViewById(R.id.deleteAch);

            deleteAch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference usersRef = db.collection("Users");

                    // Найти пользователя с именем "Олег"
                    Query query = usersRef.whereEqualTo("name", userName);
                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                // Получить ID пользователя
                                String userId = documentSnapshot.getId();
                                // Обновить массив ачивок пользователя
                                usersRef.document(userId).update("favorites", FieldValue.arrayRemove(name))
                                        .addOnSuccessListener(aVoid -> {
                                            // Ачивка добавлена успешно
                                            Log.d(TAG, "удалено у пользователя " + userName);
                                            recreate();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Обработка ошибок
                                            Log.w(TAG, "Ошибка при добавлении ачивки пользователю: " + userName, e);
                                        });
                            }
                        } else {
                            // Обработка ошибок
                            Log.w(TAG, "Ошибка при поиске пользователя Gena", task.getException());
                        }
                    });
                }
            });

            usernameTextView.setText(name);

            parentLayout.addView(blockLayout);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }
    }
}