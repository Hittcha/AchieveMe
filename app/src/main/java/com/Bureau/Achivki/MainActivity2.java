package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity2 extends AppCompatActivity {

    private FirebaseAuth mAuth;
    //private Button backButton;

    private String userName;

    private ImageButton backButton;

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    private ProgressBar progressBar;

    private FirebaseFirestore db;

    private String confirmed = "sfd";

    private int count = 0;
    private int totalScore = 0;
    //private int a = 0;

    private int achievedone = 0;

    //private boolean received;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String categoryName = intent.getStringExtra("Category_key");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle(categoryName);

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

    public void countUser(){

    }

    public void p(int a, int count){
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(count);
        progressBar.setProgress(a);
    }

    public void createAchieveList(String name, String userId){

        Intent intent = getIntent();
        String categoryName = intent.getStringExtra("Category_key");
        boolean recived = true;

        TextView categoryTextView = findViewById(R.id.categoryNameTextView);

        categoryTextView.setText(categoryName);

        // Получаем ссылки на коллекции в Firestore


        CollectionReference achievementCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");


        // Получение ссылки на коллекцию пользователей
        CollectionReference usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

        // Получение ссылки на коллекцию достижений
        CollectionReference achievementsCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");

        // Получение идентификатора пользователя
        //String userId = "2skonTHGvld6H74APIfSuc7vxZJ2";

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
                                        }else{
                                            createButton(achievementName, "black", categoryName, name, proof);
                                            //checkStatus(achievementName, categoryName, name, proof);
                                            System.out.println("Нет " + achievementName);
                                        }
                                    }
                                    p(achievedone , count);
                                }
                            });
                        }
                    }
                });

        //System.out.println(categoryName);
        backButton = findViewById(R.id.imageButtonBack);

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
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
        Button button = new Button(MainActivity2.this);
        button.setText(name);
        button.setBackgroundColor(Color.BLUE);

        boolean received;

        // Получаем ссылку на коллекцию "Users"
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        // Создаем запрос, который возвращает только документы, у которых есть достижение "красноярск"
        Query query = usersRef.whereEqualTo("userAchievements.Путешествие в Скандинавию.name", name);

        System.out.println(query);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();
       // String confirmed = "sfd";

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
                Intent intent = new Intent(MainActivity2.this, AchievementDescriptionActivity.class);
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
            //onBackPressed();
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}