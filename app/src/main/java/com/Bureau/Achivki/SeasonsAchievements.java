package com.Bureau.Achivki;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeasonsAchievements extends AppCompatActivity {

    private ImageButton backButton;
    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    private String userName;

    private FirebaseFirestore db;

    private int count = 0;

    private int achievedone = 0;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seasons_achievements);

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        backButton = findViewById(R.id.imageButtonBack);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, MainActivity.class);
                startActivity(intent);
            }
        });

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeasonsAchievements.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();

        System.out.println("11111111111111111111111111111111111111111111111111111111 " + currentUser.getUid());

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    userName = documentSnapshot.getString("name");

                    //welcomeMessage.setText("Привет " + userName);
                    // использовать имя пользователя
                    createAchieveList(userName, currentUser.getUid());
                } else {
                    // документ не найден
                }
            }
        });
    }



    public void createAchieveList(String name, String userId) {

        //Intent intent = getIntent();
        //String categoryName = intent.getStringExtra("Category_key");

        String categoryName = "season1";
        boolean recived = true;

        // Получение ссылки на коллекцию пользователей
        CollectionReference usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

        // Получение ссылки на коллекцию достижений
        CollectionReference achievementsCollectionRef = FirebaseFirestore.getInstance().collection("SeasonAchievements");

        // Получение документа пользователя из коллекции Users
        DocumentReference userDocRef = usersCollectionRef.document(userId);
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot userDocSnapshot = task.getResult();
                if (userDocSnapshot.exists()) {
                    // Получение Map UserAchieve пользователя
                    Map<String, Object> userAchieveMap = (Map<String, Object>) userDocSnapshot.get("userAchievements");

                    //Map<String, Object> userAchieveMap1 = (Map<String, Object>) userDocSnapshot.get("userAchievements");

                    // Получение достижений пользователя
                    Set<String> userAchievements = userAchieveMap.keySet();
                    // List<String> userAchievementsList = new ArrayList<>(userAchievements);
                    // Получение документов достижений из коллекции achievements
                    Query categoryQuery = achievementsCollectionRef.whereEqualTo("category", categoryName);
                    categoryQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                count++;
                                // Получаем имя достижения из документа
                                String achievementName = document.getString("name");
                                boolean proof = document.getBoolean("proof");
                                // boolean confirm = document.getBoolean("confirmed");
                                System.out.println("proof " + proof);
                                //System.out.println("proof " + confirm);

                                // System.out.println("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww " +p);

                                // Сравниваем достижения пользователя и в категории "кулинария"
                                if (userAchievements.contains(achievementName)) {
                                    // Если достижение есть и там и там, выводим на экран
                                    //createButton(achievementName, "green", categoryName, name, proof);
                                    System.out.println("Достижение \"" + achievementName + "\" есть и у пользователя, и в категории " + categoryName);
                                    checkStatus(achievementName, categoryName, name, proof);
                                    achievedone++;
                                } else {
                                    createButton(achievementName, "black", categoryName, name, proof);
                                    //checkStatus(achievementName, categoryName, name, proof);
                                    System.out.println("Нет " + achievementName);
                                }
                            }
                            p(achievedone, count);
                        }
                    });
                }
            }
        });
    }

    private void checkStatus(String achievementName, String categoryName, String name, boolean proof){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();

        // String confirmed = "sfd";

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");

                for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                    Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                    if (achievement.get("name").equals(achievementName)) {
                        Boolean confirmed = (Boolean) achievement.get("confirmed");
                        Boolean proofsended = (Boolean) achievement.get("proofsended");
                        //System.out.println(confirmed);
                        if(confirmed == true){
                            System.out.println("confirmed");
                            createButton(achievementName,"green", categoryName, name, proof);
                        }else if (proofsended == true) {
                            createButton(achievementName,"yellow", categoryName, name, proof);
                            System.out.println("proofsended");
                            //createButton(achievementName, "green", categoryName, name, proof);
                        }else{
                            createButton(achievementName,"black", categoryName, name, proof);
                            System.out.println("not ");
                        }
                    }

                    //createAchieveButton(name, desc);
                }
                // Здесь можно продолжить работу с полученным Map достижений
            }
        });
    }



    private void createButton(String name, String color, String categoryName, String username, boolean proof) {
        LinearLayout layout = findViewById(R.id.scrollView1);
        Button button = new Button(SeasonsAchievements.this);
        button.setText(name);
        button.setBackgroundColor(Color.BLUE);

        boolean received;

        // Получаем ссылку на коллекцию "Users"
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        // Создаем запрос, который возвращает только документы, у которых есть достижение "красноярск"
        //Query query = usersRef.whereEqualTo("userAchievements.Путешествие в Скандинавию.name", name);

        //System.out.println(query);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();
        // String confirmed = "sfd";

       /* userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");

                for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                    Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                    if (achievement.get("name").equals(name)) {
                       Boolean confirmed = (Boolean) achievement.get("confirmed");
                        System.out.println(confirmed);

                    }

                    //createAchieveButton(name, desc);
                }
                // Здесь можно продолжить работу с полученным Map достижений
            }
        });*/

        // здесь можно добавить дополнительные параметры для кнопки, например, размеры, цвет, обработчик нажатия и т.д.

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(20, 20, 20, 20);
        button.setBackgroundColor(Color.GREEN);

        if (color == "green"){
            button.setBackgroundResource(R.drawable.achievebackgroundgreen);
            received = true;
        }else if(color == "yellow"){
            button.setBackgroundResource(R.drawable.achievebackgroundyellow);
            received = true;
        }else{
            button.setBackgroundResource(R.drawable.achievebackground);
            received = false;
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработка нажатия кнопки
                Intent intent = new Intent(SeasonsAchievements.this, SeasonAchievementsDescriptionActivity.class);
                intent.putExtra("Achieve_key", name);
                intent.putExtra("Category_key", categoryName);
                intent.putExtra("Is_Received", received);
                intent.putExtra("User_name", username);
                intent.putExtra("ProofNeeded", proof);
                startActivity(intent);
            }
        });


        button.setLayoutParams(layoutParams);

        button.setTag(name);
        //button.setBackgroundResource(R.drawable.achievebackground);

        LinearLayout scrollView = findViewById(R.id.scrollView1);
        scrollView.addView(button);

    }
    public void p(int a, int count){
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(count);
        progressBar.setProgress(a);
    }
}