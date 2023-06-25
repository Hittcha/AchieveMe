package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AchievementWithProgressActivity extends AppCompatActivity {
    private TextView descMessage;
    private Button delButton;
    private Button confirmButton;
    private Button addButton;
    private FirebaseAuth mAuth;
    long dayLimit;
    long doneCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //запрещаем закрывать окно нажав вне окна

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        setContentView(R.layout.activity_achievement_with_progress);
        Intent intentFromMain = getIntent();
        String achieveName = intentFromMain.getStringExtra("Achieve_key");
        String categoryName = intentFromMain.getStringExtra("Category_key");
        String userName = intentFromMain.getStringExtra("User_name");
        long achieveCount = intentFromMain.getLongExtra("achieveCount", 0);
        dayLimit = intentFromMain.getLongExtra("dayLimit", 0);

        doneCount = intentFromMain.getLongExtra("doneCount", 0);

        Long achievePrice = intentFromMain.getLongExtra("achievePrice", 0);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        ScrollView scrollView = findViewById(R.id.description_scrollview);

        /*androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("fdsf");*/

        addButton = findViewById(R.id.submit_button);
        delButton = findViewById(R.id.delete_button);
        ImageButton backButton = findViewById(R.id.BackButton);
        //ImageButton addFavorites = findViewById(R.id.addFavorites);
        descMessage = findViewById(R.id.desc_message);
        confirmButton = findViewById(R.id.confirmButton);
        TextView achieveText = findViewById(R.id.AchieveName);

        achieveText.setText(achieveName);


        boolean received = getIntent().getBooleanExtra("Is_Received", false);
        boolean proof = getIntent().getBooleanExtra("ProofNeeded", false);
        boolean collectable = getIntent().getBooleanExtra("collectable", false);

        boolean favorite = getIntent().getBooleanExtra("isFavorites", false);

        boolean isUserAchieve = getIntent().getBooleanExtra("isUserAchieve", false);
        String desc = intentFromMain.getStringExtra("desc");


        if (proof) {
            showProofButton();
        }

        if (favorite) {
            changeStrokeColor();
        }else{
            changeStrokeColorBack();
        }

        System.out.println(achieveName);
        System.out.println(categoryName);
        System.out.println(received);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference achievementsRef = db.collection("Achievements");

        if(isUserAchieve){
            descMessage.setText(desc);
        }else if(categoryName.equals("season1")) {
            achievementsRef = db.collection("SeasonAchievements");
            achievementsRef.whereEqualTo("name", achieveName).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //String name = document.getString("name");
                                String description = document.getString("desc");
                                //String description = document.getString("desc");
                                System.out.println("description" + desc);
                                if(achievePrice < 1){
                                    descMessage.setText(description + "\nВыполнено " + doneCount + " из " + achieveCount);
                                }else {
                                    descMessage.setText(description + " (+" + achievePrice + " ОС)." + "\nВыполнено " + doneCount + " из " + achieveCount);
                                }
                            }
                        } else {
                            Log.d(TAG, "Ошибка получения достижений из Firestorm: ", task.getException());
                        }
                    });
        }else {
            achievementsRef.whereEqualTo("name", achieveName).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String description = document.getString("desc");

                                if(achievePrice < 1){
                                    descMessage.setText(description + "\nВыполнено " + doneCount + " из " + achieveCount);
                                }else {
                                    descMessage.setText(description + " (+" + achievePrice + " ОС)." + "\nВыполнено " + doneCount + " из " + achieveCount);
                                }
                            }
                        } else {
                            Log.d(TAG, "Ошибка получения достижений из Firestorm: ", task.getException());
                        }
                    });
        }
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        System.out.println("isUserAchieve" + isUserAchieve);
        System.out.println("desc" + desc);

        System.out.println("achievePrice " + achievePrice);
        System.out.println("-------isFavorites " + favorite);

        confirmButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchievementWithProgressActivity.this, AchieveConfirmation.class);
            intent.putExtra("Achieve_key", achieveName);
            intent.putExtra("Category_key", categoryName);
            intent.putExtra("User_name", userName);
            intent.putExtra("achieveCount", achieveCount);
            intent.putExtra("dayLimit", dayLimit);
            intent.putExtra("collectable", collectable);
            intent.putExtra("achievePrice", achievePrice);
            startActivity(intent);
        });


        addButton.setOnClickListener(v -> {

            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            FirebaseFirestore db1 = FirebaseFirestore.getInstance();
            DocumentReference usersRef = db1.collection("Users").document(currentUser.getUid());

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(calendar.getTime());

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
                // Проверяем, существует ли уже мап с именем achieveName
                if (achieveMap.containsKey(achieveName)) {
                    // Если мап существует, получаем его
                    Map<String, Object> existingAchieveMap = (Map<String, Object>) achieveMap.get(achieveName);

                    // Увеличиваем значение doneCount на 1
                    long doneCount = (long) existingAchieveMap.get("doneCount");
                    dayLimit = (long) existingAchieveMap.get("dayLimit");
                    long dayDone = (long) existingAchieveMap.get("dayDone");
                    String achieveTime = (String) existingAchieveMap.get("time");
                    if(doneCount == achieveCount){
                        hideButtonAdd();
                    }else{
                        boolean isSameDay = compareDay(currentTime, achieveTime);
                        if (isSameDay) {
                            System.out.println("Дни совпадают!");
                            if(dayDone < dayLimit){
                                existingAchieveMap.put("dayDone", dayDone + 1);
                                existingAchieveMap.put("doneCount", doneCount + 1);
                                existingAchieveMap.put("time", currentTime);
                                //addScore(currentUser.getUid());
                                addScore(currentUser.getUid(), achievePrice);
                            }else{
                                //usersRef.update("score", FieldValue.increment(10));
                                System.out.println("currentUser.getUid() " + currentUser.getUid());
                                Toast.makeText(AchievementWithProgressActivity.this, "Превышен дневной лимит", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            System.out.println("Дни не совпадают.");
                            existingAchieveMap.put("dayDone", 1);
                            existingAchieveMap.put("doneCount", doneCount + 1);
                            existingAchieveMap.put("time", currentTime);
                            //addScore(currentUser.getUid());
                            addScore(currentUser.getUid(), achievePrice);
                        }
                    }
                } else {
                    // Если мап не существует, создаем новый Map с информацией о новом достижении
                    Map<String, Object> newAchieveMap = new HashMap<>();
                    newAchieveMap.put("name", achieveName);
                    newAchieveMap.put("confirmed", true);
                    newAchieveMap.put("proofsended", true);
                    newAchieveMap.put("time", currentTime);
                    newAchieveMap.put("category", categoryName);
                    if (collectable) {
                        newAchieveMap.put("collectable", collectable);
                        newAchieveMap.put("targetCount", achieveCount);
                        newAchieveMap.put("doneCount", 1);
                        newAchieveMap.put("dayDone", 1);
                        newAchieveMap.put("dayLimit", dayLimit);
                    }

                    // Добавляем новое достижение в Map achieve пользователя
                    achieveMap.put(achieveName, newAchieveMap);
                    System.out.println("currentUser.getUid() " + currentUser.getUid());
                    //addScore(currentUser.getUid());
                    addScore(currentUser.getUid(), achievePrice);
                }

                // Сохраняем обновленный Map achieve в Firestore
                userAchievements.put("userAchievements", achieveMap);
                usersRef.set(userAchievements);

            });
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

                    //delScore(currentUser.getUid());

                    delScore(currentUser.getUid(), achievePrice);

                    Toast.makeText(AchievementWithProgressActivity.this, "Достижение удалено", Toast.LENGTH_SHORT).show();
                });
                //showButtonAdd();
            }
        });

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(AchievementWithProgressActivity.this, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    addFavorites();
                    return super.onDoubleTap(e);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    public boolean compareDay(String time1, String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

        try {
            // Преобразование времени в объекты Date
            Date date1 = sdf.parse(time1);
            Date date2 = sdf.parse(time2);

            // Создание Calendar и установка дней для сравнения
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(date1);

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(date2);

            // Сравнение дней
            return calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH) &&
                    calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                    calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }



    private void hideButtonAdd(){
        addButton.setVisibility(View.GONE); // отображаем кнопку
    }

    /*public void addScore(String uid) {
        // Get a reference to the user document
        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("Users").document(uid);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve the current score value from the document
                int currentScore = documentSnapshot.getLong("score").intValue();

                // Increment the score by 10
                int newScore = currentScore + 10;

                // Update the score in the document
                userDocRef.update("score", newScore)
                        .addOnSuccessListener(aVoid -> {
                            // Score updated successfully
                            System.out.println("Score updated successfully. New score: " + newScore);
                        })
                        .addOnFailureListener(e -> {
                            // Failed to update the score
                            System.out.println("Failed to update score: " + e.getMessage());
                        });
            } else {
                // User document does not exist
                System.out.println("User document does not exist for UID: " + uid);
            }
        }).addOnFailureListener(e -> {
            // Failed to retrieve the user document
            System.out.println("Failed to retrieve user document: " + e.getMessage());
        });
    }

    public void delScore(String uid){

        // Получаем ссылку на коллекцию пользователей
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        DocumentReference userDocRef = usersRef.document(uid);
        userDocRef.update("score", FieldValue.increment(-10))
                .addOnSuccessListener(aVoid -> {
                    // Успешное обновление
                    System.out.println("Успешное обновление счета");
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки обновления
                    System.out.println("Ошибка обновления счета: " + e.getMessage());
                });
    }*/

    private void addScore(String uid, long achievePrice) {
        // Получаем ссылку на коллекцию пользователей

        long standardPrice = 1;
        if(achievePrice > 0){
            standardPrice = achievePrice;
        }



        // Get a reference to the user document
        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("Users").document(uid);

        long finalStandardPrice = standardPrice;
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve the current score value from the document
                int currentScore = documentSnapshot.getLong("score").intValue();

                // Increment the score by 10
                int newScore = (int) (currentScore + finalStandardPrice);

                // Update the score in the document
                userDocRef.update("score", newScore)
                        .addOnSuccessListener(aVoid -> {
                            // Score updated successfully
                            System.out.println("Score updated successfully. New score: " + newScore);
                        })
                        .addOnFailureListener(e -> {
                            // Failed to update the score
                            System.out.println("Failed to update score: " + e.getMessage());
                        });
            } else {
                // User document does not exist
                System.out.println("User document does not exist for UID: " + uid);
            }
        }).addOnFailureListener(e -> {
            // Failed to retrieve the user document
            System.out.println("Failed to retrieve user document: " + e.getMessage());
        });

    }
    public void delScore(String uid, long achievePrice){

        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        long standardPrice = 1;
        if(achievePrice > 0){
            standardPrice = achievePrice;
        }

        DocumentReference userDocRef = usersRef.document(uid);
        userDocRef.update("score", FieldValue.increment(-standardPrice))
                .addOnSuccessListener(aVoid -> {
                    // Успешное обновление
                    System.out.println("Успешное обновление счета");
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки обновления
                    System.out.println("Ошибка обновления счета: " + e.getMessage());
                });
    }

    public void showProofButton(){
        addButton.setVisibility(View.GONE); // скрываем кнопку
        confirmButton.setVisibility(View.VISIBLE); // отображаем кнопку
        delButton.setVisibility(View.GONE);
    }
    private void addFavorites(){
        Intent intentFromMain = getIntent();
        String achieveName = intentFromMain.getStringExtra("Achieve_key");
        String categoryName = intentFromMain.getStringExtra("Category_key");
        Long achievePrice = intentFromMain.getLongExtra("achievePrice", 0);
        boolean collectable = getIntent().getBooleanExtra("collectable", false);
        long achieveCount = intentFromMain.getLongExtra("achieveCount", 0);
        String countDesc = intentFromMain.getStringExtra("countDesc");

        if(!categoryName.equals("userAchieve")) {

            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());


            usersRef.get().addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> userAchievements = documentSnapshot.getData();

                Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("favorites");

                // Создаем новый Map с информацией о новом достижении
                Map<String, Object> newFav = new HashMap<>();
                newFav.put("name", achieveName);
                newFav.put("category", categoryName);
                newFav.put("price", achievePrice);
                newFav.put("collectable", collectable);
                newFav.put("achieveCount", achieveCount);
                newFav.put("countDesc", countDesc);


                // Сохраняем обновленный Map achieve в Firestore
                achieveMap.put(achieveName, newFav);
                userAchievements.put("favorites", achieveMap);
                usersRef.set(userAchievements);
                Toast.makeText(this, "Достижение добавлено в профиль", Toast.LENGTH_SHORT).show();
                changeStrokeColor();
            });
        }
    }

    private void changeStrokeColor() {
        // изменения цвета рамки, при добавление в избранное
        View mainConstraintLayout = findViewById(R.id.main_constraintLayout_description);
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable drawable = getDrawable(R.drawable.achievedescriptionbackground);
        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        int layerIndex = 0;
        Drawable layer = layerDrawable.getDrawable(layerIndex);
        GradientDrawable gradientDrawable = (GradientDrawable) layer;
        int color = ContextCompat.getColor(this,R.color.button);
        gradientDrawable.setStroke(3, color);
        layerDrawable.setDrawable(layerIndex, gradientDrawable);
        mainConstraintLayout.setBackground(layerDrawable);
    }
    private void changeStrokeColorBack() {
        // изменения цвета рамки, при добавление в избранное
        View mainConstraintLayout = findViewById(R.id.main_constraintLayout_description);
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable drawable = getDrawable(R.drawable.achievedescriptionbackground);
        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        int layerIndex = 0;
        Drawable layer = layerDrawable.getDrawable(layerIndex);
        GradientDrawable gradientDrawable = (GradientDrawable) layer;
        int color = ContextCompat.getColor(this,R.color.black);
        gradientDrawable.setStroke(3, color);
        layerDrawable.setDrawable(layerIndex, gradientDrawable);
        mainConstraintLayout.setBackground(layerDrawable);
    }
}