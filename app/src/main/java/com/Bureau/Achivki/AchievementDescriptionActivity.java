package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AchievementDescriptionActivity extends AppCompatActivity {

    private TextView descMessage;
    private ImageButton addButton;
    private ImageButton delButton;
    private ImageButton confirmButton;
    private FirebaseAuth mAuth;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_description);

        Intent intentFromMain = getIntent();
        String achieveName = intentFromMain.getStringExtra("Achieve_key");
        String categoryName = intentFromMain.getStringExtra("Category_key");
        String userName = intentFromMain.getStringExtra("User_name");

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(achieveName);

        addButton = findViewById(R.id.submit_button);
        delButton = findViewById(R.id.delete_button);
        ImageButton backButton = findViewById(R.id.BackButton);
        ImageButton addFavorites = findViewById(R.id.addFavorites);
        descMessage = findViewById(R.id.desc_message);
        confirmButton = findViewById(R.id.confirmButton);


        boolean received = getIntent().getBooleanExtra("Is_Received", false);
        boolean proof = getIntent().getBooleanExtra("ProofNeeded", false);


        if (received) {
            addButton.setVisibility(View.GONE); // скрываем кнопку
            delButton.setVisibility(View.VISIBLE); // отображаем кнопку
        } else {
            delButton.setVisibility(View.GONE); // скрываем кнопку
            addButton.setVisibility(View.VISIBLE); // отображаем кнопку
        }

        if (proof) {
            showProofButton();
        }

        System.out.println(achieveName);
        System.out.println(categoryName);
        System.out.println(received);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference achievementsRef = db.collection("Achievements");

        achievementsRef.whereEqualTo("name", achieveName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String description = document.getString("desc");

                            descMessage.setText(description);
                        }
                    } else {
                        Log.d(TAG, "Ошибка получения достижений из Firestorm: ", task.getException());
                    }
                });
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchievementDescriptionActivity.this, AchieveCategoryListActivity.class);
            intent.putExtra("Category_key", categoryName);
            startActivity(intent);
        });

        confirmButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchievementDescriptionActivity.this, AchieveConfirmation.class);
            intent.putExtra("Achieve_key", achieveName);
            startActivity(intent);
        });


        addButton.setOnClickListener(v -> {

            if (proof) {
                showProofButton();

            }else {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                DocumentReference usersRef = db1.collection("Users").document(currentUser.getUid());

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                String time = sdf.format(calendar.getTime());

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
        });
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());

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

        addFavorites.setOnClickListener(new View.OnClickListener() {
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
    }

    private void showButtonDel(){
        addButton.setVisibility(View.GONE); // скрываем кнопку
        delButton.setVisibility(View.VISIBLE); // отображаем кнопку
    }

    private void showButtonAdd(){
        delButton.setVisibility(View.GONE); // скрываем кнопку
        addButton.setVisibility(View.VISIBLE); // отображаем кнопку
    }
    public void addScore(String userName){

        // Получаем ссылку на коллекцию пользователей
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        System.out.println("userName " + userName);
        usersRef.whereEqualTo("name", userName).get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        // Получить ID пользователя
                        String userId = documentSnapshot.getId();
                        DocumentReference userDocRef = usersRef.document(userId);
                        if (task.isSuccessful()) {
                            userDocRef.update("score", FieldValue.increment(10));
                        } else {
                            Log.d(TAG, "Ошибка получения достижений из Firestorm: ", task.getException());
                        }
                    }
                });

    }
    public void delScore(String userName){

        // Получаем ссылку на коллекцию пользователей
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        System.out.println("userName " + userName);
        usersRef.whereEqualTo("name", userName).get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        // Получить ID пользователя
                        String userId = documentSnapshot.getId();
                        DocumentReference userDocRef = usersRef.document(userId);
                        if (task.isSuccessful()) {
                            userDocRef.update("score", FieldValue.increment(-10));
                        } else {
                            Log.d(TAG, "Ошибка получения достижений из Firestorm: ", task.getException());
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