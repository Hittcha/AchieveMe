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
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MyAchievementsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 10;
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

        createAchieveList(currentUser.getUid());


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
                //createAchieveButton(name, desc);
            }
            // Здесь можно продолжить работу с полученным Map достижений
        });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);

        ImageButton menuButton = findViewById(R.id.imageButtonMenu);

        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);

        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyAchievementsActivity.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyAchievementsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyAchievementsActivity.this, AchieveListActivity.class);
            startActivity(intent);
        });

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyAchievementsActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyAchievementsActivity.this, UsersListActivity.class);
            //User user = new User("Имя пользователя", 1);
            //intent.putExtra("user", user);
            startActivity(intent);
        });
    }

    public void createAchieveList(String userId) {

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
                    Map<String, Object> userFavoritesMap = (Map<String, Object>) userDocSnapshot.get("achieve");
                    // Получение достижений пользователя
                    //Set<String> userFavorites = userFavoritesMap.keySet();

                    for (Map.Entry<String, Object> entry : userFavoritesMap.entrySet()) {
                        Map<String, Object> achievement = (Map<String, Object>) entry.getValue();
                        String achieveName = (String) achievement.get("name");
                        String desc = (String) achievement.get("desc");

                        boolean collectable = false;
                        long achieveCount = 0;
                        long dayLimit = 0;

                        if (achievement.containsKey("collectable")) {
                            collectable = (boolean) achievement.get("collectable");

                            achieveCount = (long) achievement.get("count");
                            dayLimit = (long) achievement.get("dayLimit");
                        }

                        boolean proof = Boolean.TRUE.equals(achievement.get("proof"));
                        if (proof) {

                        } else {

                        }


                        LinearLayout parentLayout = findViewById(R.id.scrollView);

                        boolean received;
                        ConstraintLayout blockLayout;

                        if (userAchievements.contains(achieveName)) {

                            Map<String, Object> achievementMap = (Map<String, Object>) userAchieveMap.get(achieveName);

                            System.out.println("Достижение \"" + achieveName + "\" есть и у пользователя, и в категории ");
                            Boolean confirmed = (Boolean) achievementMap.get("confirmed");
                            Boolean proofsended = (Boolean) achievementMap.get("proofsended");
                            System.out.println("proofsended " + proofsended);

                            if(Boolean.TRUE.equals(confirmed)){
                                blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                        .inflate(R.layout.block_achieve_green, parentLayout, false);
                                received = true;
                            }else if(Boolean.TRUE.equals(proofsended)){
                                blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                        .inflate(R.layout.block_achieve_yellow, parentLayout, false);
                                received = true;
                            }else{
                                blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                        .inflate(R.layout.block_achieve, parentLayout, false);
                                received = false;
                            }

                        }else{
                            System.out.println("Нет " + achieveName);
                            blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                    .inflate(R.layout.block_achieve, parentLayout, false);
                            received = false;
                        }

                        TextView AchieveNameTextView = blockLayout.findViewById(R.id.achieveName_blockTextView);

                        AchieveNameTextView.setText(achieveName);

                        ImageButton deleteAchieveButton = blockLayout.findViewById(R.id.delete_achieve_button);
                        deleteAchieveButton.setVisibility(View.VISIBLE);

                        deleteAchieveButton.setOnClickListener(v -> {
                            userFavoritesMap.remove(achieveName);

                            parentLayout.removeView(blockLayout);

                            userDocRef.update("achieve", userFavoritesMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Достижение успешно удалено и обновлено в базе данных
                                        // Выполните дополнительные действия при необходимости
                                    })
                                    .addOnFailureListener(e -> {
                                        // Произошла ошибка при обновлении достижения в базе данных
                                        // Обработайте ошибку или выведите сообщение об ошибке
                                    });

                        });

                        parentLayout.addView(blockLayout);

                        boolean finalCollectable = collectable;
                        long finalAchieveCount = achieveCount;
                        long finalDayLimit = dayLimit;
                        blockLayout.setOnClickListener(v -> {
                            Intent intent;
                            // Обработка нажатия кнопки
                            if (finalCollectable) {
                                intent = new Intent(this, AchievementWithProgressActivity.class);
                            } else {
                                intent = new Intent(this, AchievementDescriptionActivity.class);
                            }
                            intent.putExtra("Achieve_key", achieveName);
                            intent.putExtra("Category_key", "userAchieve");
                            intent.putExtra("Is_Received", received);
                            intent.putExtra("desc", desc);
                            intent.putExtra("ProofNeeded", proof);
                            intent.putExtra("collectable", finalCollectable);
                            intent.putExtra("achieveCount", finalAchieveCount);
                            intent.putExtra("dayLimit", finalDayLimit);
                            intent.putExtra("achievePrice", 1L);
                            intent.putExtra("isFavorites", false);
                            intent.putExtra("isUserAchieve", true);
                            //parentLayout.removeView(blockLayout);
                            startActivityForResult(intent, REQUEST_CODE);

                        });

                    }

                }
            }
        });
    }

    /*private void createAchieveButton(String achieveName, String achievementDesc) {

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
    }*/

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            //перезагружаем список ачивок и обновляем счетсчик


            LinearLayout parentLayout = findViewById(R.id.scrollView);
            parentLayout.removeAllViews();

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