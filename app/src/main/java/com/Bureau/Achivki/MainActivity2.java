package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
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
import java.util.List;


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

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();

        System.out.println("11111111111111111111111111111111111111111111111111111111 " + currentUser.getUid());

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());




       /* Intent intent = getIntent();
        String categoryName = intent.getStringExtra("Category_key");

        System.out.println(categoryName);*/

        backButton = findViewById(R.id.imageButtonBack);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mAuthDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    userName = documentSnapshot.getString("name");

                    //welcomeMessage.setText("Привет " + userName);
                    // использовать имя пользователя
                    createAchieveList(userName);
                } else {
                    // документ не найден
                }
            }
        });

        //createAchieveList();

      /*  FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Achiv");

        //путь до списка ачивок
        DatabaseReference buttonsRef = database.getReference("Achievements").child(categoryName);

        //путь до списка ачивок пользователя
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("Achiev").child(categoryName);

        //создаем кнопки ачивок
        buttonsRef.addValueEventListener(new ValueEventListener() {
           // private int a = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot buttonSnapshot : dataSnapshot.getChildren()) {

                    count++;

                    //SetCa(10);
                    setMyValue(100);

                    String buttonName = buttonSnapshot.getKey().toString();

                    System.out.println(dataSnapshot.child(buttonName+"/id").getValue());

                    Button button = new Button(MainActivity2.this);
                    button.setText(buttonName);
                    button.setBackgroundColor(Color.BLUE);

                    // здесь можно добавить дополнительные параметры для кнопки, например, размеры, цвет, обработчик нажатия и т.д.

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutParams.setMargins(20, 20, 20, 20);
                    button.setBackgroundColor(Color.GREEN);

                    button.setLayoutParams(layoutParams);

                    button.setTag(buttonName);
                    //int a = 0;
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String ac = snapshot.child(buttonName).getValue().toString();

                            boolean test = (boolean) snapshot.child(buttonName).getValue();
                            int testo = 10;

                            //Ca = testo;

                            SetCa(testo);

                            if (test == false){
                                button.setBackgroundColor(Color.RED);
                                //a--;

                            }
                            else{
                                button.setBackgroundColor(Color.GREEN);
                                a++;
                            }
                            System.out.println("A  "+ a);

                           // Ca = a;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Обработка нажатия кнопки
                            Intent intent = new Intent(MainActivity2.this, AchievementDescriptionActivity.class);
                            intent.putExtra("Achieve_key", buttonName);
                            intent.putExtra("Category_key", categoryName);
                            startActivity(intent);
                        }
                    });

                    LinearLayout scrollView = findViewById(R.id.scrollView1);
                    scrollView.addView(button);

                }
                System.out.println("Count  "+ count);


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        System.out.println("myValue =  "+ myValue);
*/
    }

    public void countUser(){

    }

    public void p(int a, int count){
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(count);
        progressBar.setProgress(a);
    }

    public void createAchieveList(String name){

        Intent intent = getIntent();
        String categoryName = intent.getStringExtra("Category_key");
        boolean recived = true;

        // Получаем ссылки на коллекции в Firestore


        CollectionReference achievementCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");

        // Создаем запрос для получения документов только с категорией "кулинар"
        Query query = achievementCollectionRef.whereEqualTo("category", categoryName);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<String> achievements = new ArrayList<>();

                // Проходимся по всем документам и получаем значения поля "achieve"
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    String achievement = documentSnapshot.getString("name");

                    count++;

                    CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

                    // Получаем список достижений пользователя с именем Олег
                    usersRef.whereEqualTo("name", name).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult().getDocuments()) {

                                //count++;
                                // Получаем список достижений пользователя
                                ArrayList<String> userAchievements = (ArrayList<String>) document.get(categoryName);
                                if (userAchievements.contains(achievement)) {

                                    createButton(achievement, "green", categoryName, name);

                                    achievedone++;

                                    //button.setBackgroundColor(Color.GREEN);
                                    System.out.println("Есть " + achievement);
                                } else {
                                    createButton(achievement, "black", categoryName, name);
                                    //button.setBackgroundColor(Color.RED);
                                    System.out.println("Нет " + achievement);
                                }
                                // Здесь нужно добавить код для добавления кнопки на экран.
                            }
                            //System.out.println("category " + achievement);
                            if (achievement != null && !achievements.contains(achievement)) {
                                achievements.add(achievement);
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

        // backButton.setBackgroundResource(R.drawable.achievebackgroundgreen);


        /*FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Achiv");*

        //путь до списка ачивок
        DatabaseReference buttonsRef = database.getReference("Achievements").child(categoryName).child("AchieveName");

        //путь до списка ачивок пользователя
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());

        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();*/


        //создаем кнопки ачивок
        /*buttonsRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot buttonSnapshot : dataSnapshot.getChildren()) {

                    //count++;

                    int count = (int) dataSnapshot.getChildrenCount();

                    System.out.println("COUNT = " + count);


                    String buttonName = buttonSnapshot.getKey().toString();

                   // long a = (long) dataSnapshot.child(buttonName + "/score").getValue();

                    //System.out.println("Score " + a);

                    System.out.println(dataSnapshot.child(buttonName+"/id").getValue());

                    Button button = new Button(MainActivity2.this);
                    button.setText(buttonName);
                    button.setBackgroundColor(Color.BLUE);

                    // здесь можно добавить дополнительные параметры для кнопки, например, размеры, цвет, обработчик нажатия и т.д.

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    layoutParams.setMargins(20, 20, 20, 20);
                    button.setBackgroundColor(Color.GREEN);

                    //button.setLayoutParams(new LinearLayout.LayoutParams(500, 600));


                   // LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(932, 128);
                    //layoutParams.setMargins(20, 20, 20, 20);

                    button.setLayoutParams(layoutParams);

                    button.setTag(buttonName);
                    //button.setBackgroundResource(R.drawable.achievebackground);

                    ref.addValueEventListener(new ValueEventListener() {
                    //ref1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //String ac = snapshot.child(buttonName).getValue().toString();

                            boolean test = (boolean) snapshot.child("Achiev").child(categoryName).child(buttonName).getValue();
                            //System.out.println("score1 " + a);

                            //int userScore = (int) snapshot.child("Achiev").child("Score").getValue();

                            if (test == false){
                                button.setBackgroundColor(Color.RED);
                                button.setBackgroundResource(R.drawable.achievebackground);

                            }
                            else{
                                button.setBackgroundColor(Color.GREEN);
                                button.setBackgroundResource(R.drawable.achievebackgroundgreen);
                                achievedone++;
                                //setAchievedone(achievedone);

                            }
                           // totalScore = count*count
                           p(achievedone , count);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });

                    //p(achievedone , count);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Обработка нажатия кнопки
                            Intent intent = new Intent(MainActivity2.this, AchievementDescriptionActivity.class);
                            intent.putExtra("Achieve_key", buttonName);
                            intent.putExtra("Category_key", categoryName);
                            startActivity(intent);
                        }
                    });

                    LinearLayout scrollView = findViewById(R.id.scrollView1);
                    scrollView.addView(button);

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });*/

    }



    private void createButton(String name, String color, String categoryName, String username) {
        LinearLayout layout = findViewById(R.id.scrollView1);
        Button button = new Button(MainActivity2.this);
        button.setText(name);
        button.setBackgroundColor(Color.BLUE);

        boolean received;

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
                startActivity(intent);
            }
        });


        button.setLayoutParams(layoutParams);

        button.setTag(name);
        //button.setBackgroundResource(R.drawable.achievebackground);

        LinearLayout scrollView = findViewById(R.id.scrollView1);
        scrollView.addView(button);

    }

}