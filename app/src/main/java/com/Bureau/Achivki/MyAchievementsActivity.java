package com.Bureau.Achivki;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyAchievementsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_achievements);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Созданные достижения");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();


        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userData = documentSnapshot.getData();
            Map<String, Object> achievements = (Map<String, Object>) userData.get("achieve");
            for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                Map<String, String> achievement = (Map<String, String>) entry.getValue();
                String name = achievement.get("name");
                String desc = achievement.get("desc");
                // Выводим данные достижения на экран
                System.out.println("Achievement name: " + name);
                System.out.println("Achievement description: " + desc);
                createAchieveButton(name, desc);
            }
            // Здесь можно продолжить работу с полученным Map достижений
        });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);

        ImageButton menuButton = findViewById(R.id.imageButtonMenu);

        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);

        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyAchievementsActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyAchievementsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyAchievementsActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyAchievementsActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyAchievementsActivity.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });


        /*userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Получаем map достижений пользователя
                        Map<String, Object> achievements = (Map<String, Object>) document.get("achieve");
                        // Обрабатываем каждое достижение
                        for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                            Map<String, String> achievement = (Map<String, String>) entry.getValue();
                            String name = achievement.get("name");
                            String desc = achievement.get("desc");
                            // Выводим данные достижения на экран
                            System.out.println("Achievement name: " + name);
                            System.out.println("Achievement description: " + desc);
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });*/
    }

    private void createAchieveButton(String achieveName, String achievementDesc) {

        LinearLayout parentLayout = findViewById(R.id.scrollView);

        //Button btnAdd = findViewById(R.id.btn_add);

        ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(MyAchievementsActivity.this)
                .inflate(R.layout.block_layout, parentLayout, false);

        TextView usernameTextView = blockLayout.findViewById(R.id.username);
        TextView balanceTextView = blockLayout.findViewById(R.id.balance);
        //String achievementDesc;

        // Задайте текст для TextView в соответствии с данными пользователя



        balanceTextView.setText(achievementDesc);


        ImageButton deleteAch = blockLayout.findViewById(R.id.deleteAch);

        deleteAch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference usersCollection = db.collection("Users");
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                DocumentReference userDocRef = usersCollection.document(currentUser.getUid());

                userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> userData = documentSnapshot.getData();
                        Map<String, Object> achievements = (Map<String, Object>) userData.get("achieve");
                        // Здесь можно продолжить работу с полученным Map достижений
                        String achievementNameToRemove = achieveName;
                        achievements.remove(achievementNameToRemove);

                        userDocRef.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Достижение успешно удалено
                                //Context context;
                                Toast.makeText(MyAchievementsActivity.this, "Достижение успешно удалено", Toast.LENGTH_SHORT).show();
                                recreate();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Обработка ошибки при удалении достижения
                                Toast.makeText(MyAchievementsActivity.this, "Ошибка при удалении достижения", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        usernameTextView.setText(achieveName);

        parentLayout.addView(blockLayout);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
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