package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AchievementDescriptionActivity extends AppCompatActivity {

    private TextView descMessage;

    private ImageButton addButton;

    private ImageButton delButton;

    private ImageButton backButton;
    private ImageButton confirmButton;

    private DatabaseReference ref;
    private DatabaseReference ref1;
    private FirebaseAuth mAuth;

    private String received;

    private ImageButton addFavorites;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_description);

        Intent intent = getIntent();
        String achieveName = intent.getStringExtra("Achieve_key");
        String categoryName = intent.getStringExtra("Category_key");
        String userName = intent.getStringExtra("User_name");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle(achieveName);

        addButton = findViewById(R.id.submit_button);

        delButton = findViewById(R.id.delete_button);

        backButton = findViewById(R.id.BackButton);

        addFavorites = findViewById(R.id.addFavorites);

        descMessage = findViewById(R.id.desc_message);

        confirmButton = findViewById(R.id.confirmButton);



        boolean received = getIntent().getBooleanExtra("Is_Received", false);

        boolean proof = getIntent().getBooleanExtra("ProofNeeded", false);


        if (received == true) {
            System.out.println("sdadasd " + received);
            addButton.setVisibility(View.GONE); // скрываем кнопку
            delButton.setVisibility(View.VISIBLE); // отображаем кнопку
        } else {
            System.out.println("else");
            delButton.setVisibility(View.GONE); // скрываем кнопку
            addButton.setVisibility(View.VISIBLE); // отображаем кнопку
        }

        if (proof == true) {
            System.out.println("Proooooooooooooooooooooof");
            showProofButton();
        }


        System.out.println(achieveName);
        System.out.println(categoryName);
        System.out.println(received);

        //showButtons(received);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference achievementsRef = db.collection("Achievements");

        achievementsRef.whereEqualTo("name", achieveName).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                String description = document.getString("desc");

                                descMessage.setText(description);
                            }
                        } else {
                            Log.d(TAG, "Ошибка получения достижений из Firestore: ", task.getException());
                        }
                    }
                });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onBackPressed();
                Intent intent = new Intent(AchievementDescriptionActivity.this, MainActivity2.class);
                intent.putExtra("Category_key", categoryName);
                startActivity(intent);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchievementDescriptionActivity.this, AchieveConfirmation.class);
                intent.putExtra("Achieve_key", achieveName);
                startActivity(intent);
            }
        });


        addButton.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             if (proof == true) {
                                                 System.out.println("Proooooooooooooooooooooof");
                                                 showProofButton();

                                             }else {


                                                 mAuth = FirebaseAuth.getInstance();
                                                 FirebaseUser currentUser = mAuth.getCurrentUser();
                                                 FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                 DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());

                                                 Calendar calendar = Calendar.getInstance();
                                                 SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                                                 String time = sdf.format(calendar.getTime());


                                                 // Map<String, Object> userAchievements = new HashMap<>();

                                                 usersRef.get().addOnSuccessListener(documentSnapshot -> {
                                                     Map<String, Object> userAchievements = documentSnapshot.getData();
                                                     if (userAchievements == null) {
                                                         // Если пользователь не существует, создаем новый документ
                                                         userAchievements = new HashMap<>();
                                                         userAchievements.put("userAchievements", new HashMap<>());
                                                     } else if (!userAchievements.containsKey("userAchievements")) {
                                                         // Если Map achieve не существует, создаем его
                                                         userAchievements.put("userAchievements", new HashMap<>());
                                                     }

                                                     // Получаем текущий Map achieve из документа пользователя
                                                     Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userAchievements");

                                                     // Создаем новый Map с информацией о новом достижении
                                                     Map<String, Object> newAchieveMap = new HashMap<>();
                                                     newAchieveMap.put("name", achieveName);
                                                     newAchieveMap.put("confirmed", true);
                                                     newAchieveMap.put("proofsended", true);
                                                     newAchieveMap.put("time", time);

                                                     // Добавляем новое достижение в Map achieve пользователя
                                                     achieveMap.put(achieveName, newAchieveMap);

                                                     // Сохраняем обновленный Map achieve в Firestore
                                                     userAchievements.put("userAchievements", achieveMap);
                                                     usersRef.set(userAchievements);
                                                     addScore(userName);
                                                     Toast.makeText(AchievementDescriptionActivity.this, "Достижение добавлено", Toast.LENGTH_SHORT).show();
                                                 });

                                                 showButtonDel();
                                             }
                                         }
                                     });



        /*addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference usersRef = db.collection("Users");

                // Найти пользователя с именем
                Query query = usersRef.whereEqualTo("name", userName);
                query.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                            // Получить ID пользователя
                            String userId = documentSnapshot.getId();
                            // Обновить массив ачивок пользователя
                            usersRef.document(userId).update(categoryName, FieldValue.arrayUnion(achieveName))
                                    .addOnSuccessListener(aVoid -> {
                                        // Ачивка добавлена успешно
                                        addScore(userName);
                                        Log.d(TAG, "Ачивка добавлена пользователю Gena");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Обработка ошибок
                                        Log.w(TAG, "Ошибка при добавлении ачивки пользователю Gena", e);
                                    });
                        }
                    } else {
                        // Обработка ошибок
                        Log.w(TAG, "Ошибка при поиске пользователя Gena", task.getException());
                    }
                });
                showButtonDel();
            }
        });*/

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());


                // Map<String, Object> userAchievements = new HashMap<>();

                usersRef.get().addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> userAchievements = documentSnapshot.getData();

                    Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userAchievements");

                    achieveMap.remove(achieveName);

                    userAchievements.put("userAchievements", achieveMap);

                    usersRef.set(userAchievements);

                    delScore(userName);

                    Toast.makeText(AchievementDescriptionActivity.this, "Достижение удалено", Toast.LENGTH_SHORT).show();
                });
                showButtonAdd();
            }
        });



        /*delButton.setOnClickListener(new View.OnClickListener() {
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
                            usersRef.document(userId).update(categoryName, FieldValue.arrayRemove(achieveName))
                                    .addOnSuccessListener(aVoid -> {
                                        // Ачивка добавлена успешно
                                        Log.d(TAG, "Ачивка добавлена пользователю " + userName);
                                        delScore(userName);
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

                showButtonAdd();
            }
        });*/

        addFavorites.setOnClickListener(new View.OnClickListener() {
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
                            usersRef.document(userId).update("favorites", FieldValue.arrayUnion(achieveName))
                                    .addOnSuccessListener(aVoid -> {
                                        // Ачивка добавлена успешно
                                        Log.d(TAG, "Ачивка добавлена пользователю: " + userName);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Обработка ошибок
                                        Log.w(TAG, "Ошибка при добавлении ачивки пользователю: " + userName, e);
                                    });
                        }
                    } else {
                        // Обработка ошибок
                        Log.w(TAG, "Ошибка при поиске пользователя: " + userName, task.getException());
                    }
                });
            }
        });
        /*

        // Подключаемся к базе данных Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Получаем ссылку на узел с текстом
        DatabaseReference myRef = database.getReference("Achievements/" + categoryName + "/AchieveName/" + achieveName);


        mAuth = FirebaseAuth.getInstance();

        ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("Achiev").child(categoryName);

        ref1 = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());

        addButton = findViewById(R.id.submit_button);

        delButton = findViewById(R.id.delete_button);

        backButton = findViewById(R.id.BackButton);

        addFavorites = findViewById(R.id.addFavorites);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean booleanValue = (Boolean) dataSnapshot.child(achieveName).getValue();
                //Button myButton = findViewById(R.id.submit_button); // замените R.id.my_button на id своей кнопки
                if (booleanValue) {
                    addButton.setVisibility(View.GONE); // скрываем кнопку
                    delButton.setVisibility(View.VISIBLE); // отображаем кнопку
                } else {
                    delButton.setVisibility(View.GONE); // скрываем кнопку
                    addButton.setVisibility(View.VISIBLE); // отображаем кнопку
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок чтения из Firebase
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child(achieveName).setValue(true);
                //ref1.child("Score").setValue(10);
                addScore(categoryName, achieveName);
            }
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child(achieveName).setValue(false);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchievementDescriptionActivity.this, MainActivity2.class);
                intent.putExtra("Category_key", categoryName);
                startActivity(intent);
            }
        });

        addFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref1.child("Favorites").child(achieveName).setValue(categoryName);
            }
        });
    }


    public void addScore(String categoryName, String achieveName){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("Achiev");

       // FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference scoreRef = database.getReference("Users").child(mAuth.getCurrentUser().getUid()).child("Score");
        //DatabaseReference achieveScoreRef = database.getReference();

        DatabaseReference refAchieveScore = database.getReference("Achievements/" + categoryName + "/AchieveName/" + achieveName + "/score");
        //DatabaseReference refAchieveScore = database.getReference();
        System.out.println(refAchieveScore);



        scoreRef.get().addOnSuccessListener(snapshot -> {
            Long currentScore = (Long) snapshot.getValue();
            System.out.println("currentScore = " + currentScore);

            refAchieveScore.get().addOnSuccessListener(asnapshot -> {
                Long achieveScore = (Long) asnapshot.getValue();
               // Long currentScore = (Long) snapshot.getValue();

                Long updatedScore = currentScore + achieveScore;
                System.out.println("achieveScore = " + achieveScore);
                scoreRef.setValue(updatedScore);
            });

            // Отправляем обновленный счет обратно в Firebase
           // scoreRef.setValue(updatedScore);
        });
*/

    }

    private void showButtonDel(){
        addButton.setVisibility(View.GONE); // скрываем кнопку
        delButton.setVisibility(View.VISIBLE); // отображаем кнопку
    }

    private void showButtonAdd(){
        delButton.setVisibility(View.GONE); // скрываем кнопку
        addButton.setVisibility(View.VISIBLE); // отображаем кнопку
    }

    private void showButtons(String a) {
        System.out.println("dgfsfsdfsdf " + a);
        if (a == "green") {
            System.out.println("sdadasd " + a);
            addButton.setVisibility(View.GONE); // скрываем кнопку
            delButton.setVisibility(View.VISIBLE); // отображаем кнопку
        } else {
            System.out.println("else");
            delButton.setVisibility(View.GONE); // скрываем кнопку
            addButton.setVisibility(View.VISIBLE); // отображаем кнопку
        }
    }
    public void addScore(String userName){

        // Получаем ссылку на коллекцию пользователей
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        System.out.println("userName " + userName);
        usersRef.whereEqualTo("name", userName).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                            // Получить ID пользователя
                            String userId = documentSnapshot.getId();
                            DocumentReference userDocRef = usersRef.document(userId);
                            if (task.isSuccessful()) {
                                userDocRef.update("score", FieldValue.increment(10));
                            } else {
                                Log.d(TAG, "Ошибка получения достижений из Firestore: ", task.getException());
                            }
                        }
                    }
                });

    }
    public void delScore(String userName){

        // Получаем ссылку на коллекцию пользователей
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        System.out.println("userName " + userName);
        usersRef.whereEqualTo("name", userName).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                            // Получить ID пользователя
                            String userId = documentSnapshot.getId();
                            DocumentReference userDocRef = usersRef.document(userId);
                            if (task.isSuccessful()) {
                                userDocRef.update("score", FieldValue.increment(-10));
                            } else {
                                Log.d(TAG, "Ошибка получения достижений из Firestore: ", task.getException());
                            }
                        }
                    }
                });

    }
    public void showProofButton(){
        addButton.setVisibility(View.GONE); // скрываем кнопку
        confirmButton.setVisibility(View.VISIBLE); // отображаем кнопку
        delButton.setVisibility(View.GONE);
    }

}