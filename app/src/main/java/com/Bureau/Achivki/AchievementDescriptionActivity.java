package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AchievementDescriptionActivity extends AppCompatActivity {

    private TextView descMessage;
    private Button addButton;
    private Button delButton;
    private Button confirmButton;
    private SVGImageView confirmIcon;
    private FirebaseAuth mAuth;

    private boolean isAdded = false;
    private boolean isDeleted = false;



    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //запрещаем закрывать окно нажав вне окна

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        setContentView(R.layout.activity_achievement_description);

        Intent intentFromMain = getIntent();
        String achieveName = intentFromMain.getStringExtra("Achieve_key");
        String categoryName = intentFromMain.getStringExtra("Category_key");

        int blockPosition = intentFromMain.getIntExtra("blockPosition", 0);

        Long achievePrice = intentFromMain.getLongExtra("achievePrice", 0);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);



        addButton = findViewById(R.id.submit_button);
        delButton = findViewById(R.id.delete_button);
        ImageButton backButton = findViewById(R.id.BackButton);
        descMessage = findViewById(R.id.desc_message);
        confirmButton = findViewById(R.id.confirmButton);
        TextView achieveText = findViewById(R.id.AchieveName);
        confirmIcon = findViewById(R.id.iconConfirm_imageView);

        WindowCalculation windowCalculation = new WindowCalculation(this);
        double textWeight = windowCalculation.WindowCalculationWeight() * 0.8;
        ViewGroup.LayoutParams textViewLayoutParams = achieveText.getLayoutParams();
        textViewLayoutParams.width = (int) textWeight;
        achieveText.setLayoutParams(textViewLayoutParams);
        achieveText.requestLayout();
        achieveText.setText(achieveName);//изменям размер контейнера названия достижения

        boolean received = getIntent().getBooleanExtra("Is_Received", false);
        boolean proof = getIntent().getBooleanExtra("ProofNeeded", false);
        boolean favorite = getIntent().getBooleanExtra("isFavorites", false);

        boolean isUserAchieve = getIntent().getBooleanExtra("isUserAchieve", false);
        String desc = intentFromMain.getStringExtra("desc");

        //boolean collectable = getIntent().getBooleanExtra("collectable", false);

        System.out.println("isUserAchieve" + isUserAchieve);
        System.out.println("desc" + desc);

        System.out.println("achievePrice " + achievePrice);
        System.out.println("-------isFavorites " + favorite);

        // выставляем иконки
        try {
            InputStream inputStream = getAssets().open("interface_icon/photo.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            confirmIcon.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            throw new RuntimeException(e);
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/BigBan.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.achieveIcon_imageView);
            svgImageView.setSVG(svg);
        } catch (SVGParseException | IOException e) {
            throw new RuntimeException(e);
        }


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
                                    descMessage.setText(description);
                                }else {
                                    descMessage.setText(description + " (+" + achievePrice + " ОС).");
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
                                    //String name = document.getString("name");
                                    String description = document.getString("desc");
                                    //String description = document.getString("desc");
                                    System.out.println("description" + desc);
                                    descMessage.setText(description);
                                }
                            } else {
                                Log.d(TAG, "Ошибка получения достижений из Firestorm: ", task.getException());
                            }
                        });
            }

        backButton.setOnClickListener(v -> {

            /*Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();*/
            /*Intent resultIntent = new Intent();
            resultIntent.putExtra("Is_Received", true);
            resultIntent.putExtra("Block_Position", blockPosition);
            setResult(RESULT_OK, resultIntent);
            finish();*/

            Intent resultIntent = new Intent();

            if (isAdded) {
                resultIntent.putExtra("Is_Added", true);
                resultIntent.putExtra("Block_Position", blockPosition);
            } else if (isDeleted) {
                resultIntent.putExtra("Is_Cancelled", true);
                resultIntent.putExtra("Block_Position", blockPosition);
            }

            setResult(RESULT_OK, resultIntent);
            finish();

        });

        confirmButton.setOnClickListener(v -> {
            Intent intent = new Intent(AchievementDescriptionActivity.this, AchieveConfirmation.class);
            intent.putExtra("Achieve_key", achieveName);
            intent.putExtra("achievePrice", achievePrice);
            intent.putExtra("Category_key", categoryName);
            startActivity(intent);
        });


        addButton.setOnClickListener(v -> {

            //isProofSended = true;
            isAdded = true;
            isDeleted = false;

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
                    newAchieveMap.put("category", categoryName);
                    newAchieveMap.put("proofsended", true);
                    newAchieveMap.put("time", time);

                    // Добавляем новое достижение в Map achieve пользователя
                    achieveMap.put(achieveName, newAchieveMap);

                    // Сохраняем обновленный Map achieve в Firestore

                    if (achievePrice != 0){
                        newAchieveMap.put("price", achievePrice);
                    }
                    userAchievements.put("userAchievements", achieveMap);
                    usersRef.set(userAchievements);
                    addScore(currentUser.getUid(), achievePrice);
                    Toast.makeText(AchievementDescriptionActivity.this, "Достижение добавлено", Toast.LENGTH_SHORT).show();
                });
                showButtonDel();
            }
        });
        delButton.setOnClickListener(v -> {

            isDeleted = true;
            isAdded = false;

            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            FirebaseFirestore db12 = FirebaseFirestore.getInstance();
            DocumentReference usersRef = db12.collection("Users").document(currentUser.getUid());

            usersRef.get().addOnSuccessListener(documentSnapshot -> {

                Map<String, Object> userAchievements = documentSnapshot.getData();
                Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userAchievements");

                achieveMap.remove(achieveName);
                userAchievements.put("userAchievements", achieveMap);
                usersRef.set(userAchievements);
                delScore(currentUser.getUid(), achievePrice);

                Toast.makeText(AchievementDescriptionActivity.this, "Достижение удалено", Toast.LENGTH_SHORT).show();
            });
            showButtonAdd();
        });

        descMessage.setOnTouchListener(new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(AchievementDescriptionActivity.this, new GestureDetector.SimpleOnGestureListener(){
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

    private void showButtonDel(){
        addButton.setVisibility(View.GONE); // скрываем кнопку
        delButton.setVisibility(View.VISIBLE); // отображаем кнопку
    }

    private void showButtonAdd(){
        delButton.setVisibility(View.GONE); // скрываем кнопку
        addButton.setVisibility(View.VISIBLE); // отображаем кнопку
    }
    private void addScore(String uid, long achievePrice) {
        // Получаем ссылку на коллекцию пользователей
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        long standardPrice = 1;
        if(achievePrice > 0){
            standardPrice = achievePrice;
        }

        DocumentReference userDocRef = usersRef.document(uid);
        userDocRef.update("score", FieldValue.increment(standardPrice))
                .addOnSuccessListener(aVoid -> {
                    // Успешное обновление
                    System.out.println("Успешное обновление счета");
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки обновления
                    System.out.println("Ошибка обновления счета: " + e.getMessage());
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
        confirmIcon.setVisibility(View.VISIBLE);
        delButton.setVisibility(View.GONE);
    }

    @SuppressLint("ResourceAsColor")
    private void addFavorites(){
        Intent intentFromMain = getIntent();
        String achieveName = intentFromMain.getStringExtra("Achieve_key");
        String categoryName = intentFromMain.getStringExtra("Category_key");
        Long achievePrice = intentFromMain.getLongExtra("achievePrice", 0);

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

                // Сохраняем обновленный Map achieve в Firestore
                achieveMap.put(achieveName, newFav);
                userAchievements.put("favorites", achieveMap);
                usersRef.set(userAchievements);
                Toast.makeText(this, "Достижение добавлено в избранное", Toast.LENGTH_SHORT).show();
                changeStrokeColor();

            });
        }
    }
    private void changeStrokeColor() {
        // изменения цвета рамки, при добавление в избранное
        View mainConstraintLayout = findViewById(R.id.main_constraintLayout_description);
        @SuppressLint("UseCompatLoadingForDrawables")
        GradientDrawable drawable = (GradientDrawable) getDrawable(R.drawable.achievedescriptionbackground);
        int color = ContextCompat.getColor(this,R.color.button);
        drawable.setStroke(3, color);
        mainConstraintLayout.setBackground(drawable);
    }
    private void changeStrokeColorBack() {
        // изменения цвета рамки, при добавление в избранное
        View mainConstraintLayout = findViewById(R.id.main_constraintLayout_description);
        @SuppressLint("UseCompatLoadingForDrawables")
        GradientDrawable drawable = (GradientDrawable) getDrawable(R.drawable.achievedescriptionbackground);
        int color = ContextCompat.getColor(this,R.color.black);
        drawable.setStroke(3, color);
        mainConstraintLayout.setBackground(drawable);
    }
}