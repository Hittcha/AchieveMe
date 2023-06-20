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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Set;

public class ListOfFavoritesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_favorites);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Список избранного");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);

        ImageButton menuButton = findViewById(R.id.imageButtonMenu);

        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);

        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

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
                        //ArrayList<String> achievements = (ArrayList<String>) document.get("favorites");

                        String userName = (String) document.get("name");


                        System.out.println("name " + userName);

                        createAchieveButton();

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

    private void createAchieveButton() {

        LinearLayout parentLayout = findViewById(R.id.scrollView);

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
                //Map<String, Object> achievementMap = (Map<String, Object>) userAchieveMap.get(achieveName);


                String achieveName = (String) achievement.get("name");
                String category = (String) achievement.get("category");
                String desc = (String) achievement.get("desc");

                System.out.println("desc " + desc);
                System.out.println("achieveName " + achieveName);

                boolean collectable = false;
                long achieveCount = 0;
                long dayLimit = 0;
                long doneCount = 0;
                String countDesc = "";


                if (achievement.containsKey("collectable")) {
                    collectable = (boolean) achievement.get("collectable");
                    achieveCount = (long) achievement.get("achieveCount");
                   //dayLimit = (long) achievement.get("dayLimit");

                    countDesc = (String) achievement.get("countDesc");
                    System.out.println("collectable " + collectable);
                } else {
                    // Обработка ситуации, когда поле отсутствует
                    System.out.println("not collectable " + collectable);
                }

                boolean proof = Boolean.TRUE.equals(achievement.get("proof"));
                if (proof) {

                } else {

                }

                long achievePrice = 0;
                if (achievement.containsKey("price")) {
                    achievePrice = (long) achievement.get("price");
                    System.out.println("price " + achievePrice);
                }


                ConstraintLayout blockLayout;
                boolean received;

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
                        if (achievementMap.containsKey("doneCount")) {
                            doneCount = (long) achievementMap.get("doneCount");
                        }
                    }else if(Boolean.TRUE.equals(proofsended)){
                        blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                                .inflate(R.layout.block_achieve_yellow, parentLayout, false);
                        received = true;
                        if (achievementMap.containsKey("doneCount")) {
                            doneCount = (long) achievementMap.get("doneCount");
                        }
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

                AchieveNameTextView.setText(achieveName);

                blockLayout.setOnClickListener(v -> {
                    checkAchieve(achieveName, received);
                });

                ImageButton deleteAch = blockLayout.findViewById(R.id.delete_achieve_button);

                deleteAch.setVisibility(View.VISIBLE);
                deleteAch.setOnClickListener(v -> {

                    FirebaseAuth mAuth1 = FirebaseAuth.getInstance();
                    FirebaseUser currentUser1 = mAuth1.getCurrentUser();
                    FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                    DocumentReference usersRef = db1.collection("Users").document(currentUser1.getUid());

                    usersRef.get().addOnSuccessListener(documentSnapshot1 -> {
                        Map<String, Object> userAchievements1 = documentSnapshot1.getData();

                        Map<String, Object> achieveMap = (Map<String, Object>) userAchievements1.get("favorites");

                        achieveMap.remove(achieveName);

                        userAchievements1.put("favorites", achieveMap);

                        usersRef.set(userAchievements1);

                        //recreate();

                        parentLayout.removeView(blockLayout);

                        Toast.makeText(ListOfFavoritesActivity.this, "Достижение удалено", Toast.LENGTH_SHORT).show();
                    });
                });

                parentLayout.addView(blockLayout);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
            }
        });
    }

    public void checkAchieve(String achieveName, boolean received){

        Intent intentMain = getIntent();
        String categoryName = intentMain.getStringExtra("Category_key");

        // Получение ссылки на коллекцию пользователей
        //CollectionReference usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

        // Получение ссылки на коллекцию достижений
        CollectionReference achievementsCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");

        Query categoryQuery = achievementsCollectionRef.whereEqualTo("name", achieveName);
        categoryQuery.get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                // Получаем имя достижения из документа
                String achievementName = document.getString("name");

                boolean proof = Boolean.TRUE.equals(document.getBoolean("proof"));
                boolean collectable = false;
                long achieveCount = 0;
                long doneCount = 0;
                String countDesc = "";
                long dayLimit = 0;

                if (document.contains("collectable")) {
                    collectable = Boolean.TRUE.equals(document.getBoolean("collectable"));
                    achieveCount = document.getLong("count");
                    dayLimit = document.getLong("dayLimit");
                    countDesc = document.getString("countDesc");
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

                boolean finalCollectable = collectable;
                long finalAchievePrice = achievePrice;
                long finalDayLimit = dayLimit;
                long finalAchieveCount = achieveCount;

                Intent intent;
                // Обработка нажатия кнопки
                if (finalCollectable) {
                    intent = new Intent(this, AchievementWithProgressActivity.class);
                } else {
                    intent = new Intent(this, AchievementDescriptionActivity.class);
                }
                intent.putExtra("Achieve_key", achievementName);
                intent.putExtra("Category_key", categoryName);
                intent.putExtra("Is_Received", received);
                intent.putExtra("ProofNeeded", proof);
                intent.putExtra("collectable", finalCollectable);
                intent.putExtra("achieveCount", finalAchieveCount);
                intent.putExtra("dayLimit", finalDayLimit);
                intent.putExtra("achievePrice", finalAchievePrice);
                intent.putExtra("isFavorites", true);
                intent.putExtra("isUserAchieve", false);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

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
                    createAchieveButton();
                } else {
                    // документ не найден
                }
            });
        }
    }
}