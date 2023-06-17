package com.Bureau.Achivki;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AchieveConfirmation extends AppCompatActivity {
    private Button setPublic;
    private Button selectImageButton;

    private ImageView mImageView;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;


    private FirebaseAuth mAuth;

    private static final int REQUEST_CODE_SELECT_IMAGE = 100;

    private ImageView profileImageView;

    private FirebaseStorage storage;
    private String achieveName;
    long dayLimit;


    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve_confirmation);

        mAuth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();

        Intent intentFromMain = getIntent();
        achieveName = intentFromMain.getStringExtra("Achieve_key");

        String achieveName = intentFromMain.getStringExtra("Achieve_key");
        String categoryName = intentFromMain.getStringExtra("Category_key");
        String userName = intentFromMain.getStringExtra("User_name");
        long achieveCount = intentFromMain.getLongExtra("achieveCount", 0);
        dayLimit = intentFromMain.getLongExtra("dayLimit", 0);
        boolean collectable = getIntent().getBooleanExtra("collectable", false);
        Long achievePrice = intentFromMain.getLongExtra("achievePrice", 0);


        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("users").child(mAuth.getCurrentUser().getUid()).child("UserAvatar");

        mImageView = findViewById(R.id.image_view);
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        profileImageView = findViewById(R.id.image_view);
        selectImageButton = findViewById(R.id.button_choose_image);


        selectImageButton.setOnClickListener(v -> {

            mAuth = FirebaseAuth.getInstance();
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

                    //long doneCount = 0;
                    // Увеличиваем значение doneCount на 1

                    if (collectable) {
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
                                    //selectImageFromLibrary();
                                    askPermission();
                                }else{
                                    Toast.makeText(AchieveConfirmation.this, "Превышен дневной лимит", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                System.out.println("Дни не совпадают.");
                                //selectImageFromLibrary();
                                askPermission();
                            }
                            //existingAchieveMap.put("doneCount", doneCount + 1);
                            //existingAchieveMap.put("time", currentTime);
                        }
                    }else{
                        //selectImageFromLibrary();
                        askPermission();
                    }
                } else {
                    // Если мап не существует, создаем новый Map с информацией о новом достижении
                    System.out.println("мап не существует, создаем новый Map с информацией о новом достижении");
                    if (collectable) {
                    }
                    //selectImageFromLibrary();
                    askPermission();
                }

                // Сохраняем обновленный Map achieve в Firestore
                userAchievements.put("userAchievements", achieveMap);
                usersRef.set(userAchievements);
            });
        });



        mImageView = findViewById(R.id.image_view);
        db = FirebaseFirestore.getInstance();


        setPublic = findViewById(R.id.button2);

        setPublic.setOnClickListener(v -> setImagePublic(achieveName));

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);

        ImageButton menuButton = findViewById(R.id.imageButtonMenu);

        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);

        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

        StorageReference imageProofRef = storageRef.child("users").child(mAuth.getCurrentUser().getUid()).child("UserAvatar");


        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveConfirmation.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveConfirmation.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveConfirmation.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });
        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveConfirmation.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchieveConfirmation.this, UsersListActivity.class);
                startActivity(intent);
            }
        });
    }

    public void askPermission() {
        selectImageFromLibrary();
        if (ContextCompat.checkSelfPermission(AchieveConfirmation.this,
                Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {

            // Если разрешения нет, запрашиваем его у пользователя
            ActivityCompat.requestPermissions(AchieveConfirmation.this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);

            //ActivityCompat.requestPermissions(UserProfile.this,
            //       new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            //       PERMISSION_REQUEST_CODE);

        } else {
            // Если разрешение есть, вызываем окно выбора фотографий
            selectImageFromLibrary();
            //loadPhotosFromGallery();
        }
        //selectImageFromLibrary();*/
    }

    public void selectImageFromLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_CODE_SELECT_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        boolean collectable = getIntent().getBooleanExtra("collectable", false);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db1.collection("Users").document(currentUser.getUid());

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    if (inputStream != null) {
                        long fileSizeInBytes = inputStream.available();
                        long fileSizeInMegabytes = fileSizeInBytes / (1024 * 1024);
                        //inputStream.close();

                        System.out.println("fileSizeInMegabytes: " + fileSizeInMegabytes);

                        if (fileSizeInMegabytes > 5) {
                            // Файл превышает 5 мегабайт, выдаем ошибку
                            Toast.makeText(this, "Выбранное изображение слишком большое. Максимальный размер - 5 МБ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    profileImageView.setImageBitmap(bitmap);

                    if(collectable){
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
                                long doneCount = (long) existingAchieveMap.get("doneCount") + 1;
                                uploadImageToStorage(bitmap, achieveName + doneCount);
                            } else {
                                // Если мап не существует, создаем новый Map с информацией о новом достижении
                                System.out.println("мап не существует, создаем новый Map с информацией о новом достижении");
                                uploadImageToStorage(bitmap, achieveName + 1);
                            }
                            // Сохраняем обновленный Map achieve в Firestore
                            userAchievements.put("userAchievements", achieveMap);
                            usersRef.set(userAchievements);
                            //addScore(currentUser.getUid());

                            //Toast.makeText(AchievementWithProgressActivity.this, "Достижение добавлено", Toast.LENGTH_SHORT).show();
                        });

                    }else{
                        uploadImageToStorage(bitmap, achieveName);
                    }

                    //uploadImageToStorage(bitmap, achieveName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public void uploadImageToStorage(Bitmap bitmap, String name) {

        Intent intentFromMain = getIntent();
        achieveName = intentFromMain.getStringExtra("Achieve_key");

        String achieveName = intentFromMain.getStringExtra("Achieve_key");
        String categoryName = intentFromMain.getStringExtra("Category_key");
        String userName = intentFromMain.getStringExtra("User_name");
        long achieveCount = intentFromMain.getLongExtra("achieveCount", 0);
        dayLimit = intentFromMain.getLongExtra("dayLimit", 0);
        boolean collectable = getIntent().getBooleanExtra("collectable", false);

        Long achievePrice = intentFromMain.getLongExtra("achievePrice", 0);

        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(AchieveConfirmation.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads
                Toast.makeText(AchieveConfirmation.this, "Изображение успешно загружено", Toast.LENGTH_SHORT).show();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());


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
            // Проверяем, существует ли уже мап с именем achieveName
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(calendar.getTime());

            if (achieveMap.containsKey(achieveName)) {
                // Если мап существует, получаем его
                Map<String, Object> existingAchieveMap = (Map<String, Object>) achieveMap.get(achieveName);

                if (collectable) {
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
                                existingAchieveMap.put("url" + (doneCount + 1), "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);
                            }else{
                                Toast.makeText(AchieveConfirmation.this, "Превышен дневной лимит", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            System.out.println("Дни не совпадают.");
                            existingAchieveMap.put("dayDone", 1);
                            existingAchieveMap.put("doneCount", doneCount + 1);
                            existingAchieveMap.put("time", currentTime);
                            existingAchieveMap.put("url"  + (doneCount + 1), "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);
                        }
                        //existingAchieveMap.put("doneCount", doneCount + 1);
                        //existingAchieveMap.put("time", currentTime);
                    }
                }
            } else {
                // Если мап не существует, создаем новый Map с информацией о новом достижении
                System.out.println("мап не существует, создаем новый Map с информацией о новом достижении");
                Map<String, Object> newAchieveMap = new HashMap<>();
                newAchieveMap.put("name", achieveName);
                newAchieveMap.put("confirmed", false);
                newAchieveMap.put("proofsended", true);
                newAchieveMap.put("time", currentTime);
                newAchieveMap.put("url1", "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);
                if (collectable) {
                    newAchieveMap.put("collectable", collectable);
                    newAchieveMap.put("targetCount", achieveCount);
                    newAchieveMap.put("doneCount", 1);
                    newAchieveMap.put("dayDone", 1);
                    newAchieveMap.put("dayLimit", dayLimit);
                }

                // Добавляем новое достижение в Map achieve пользователя
                achieveMap.put(achieveName, newAchieveMap);
            }

            // Сохраняем обновленный Map achieve в Firestore
            userAchievements.put("userAchievements", achieveMap);
            usersRef.set(userAchievements);
            //addScore(currentUser.getUid());

            //Toast.makeText(AchievementWithProgressActivity.this, "Достижение добавлено", Toast.LENGTH_SHORT).show();
        });

        //Добавляем достижение в UsersLogs ProofList для модерации

            DocumentReference usersLogsRef = db.collection("UsersLogs").document("ProofList");
        usersLogsRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userAchievements = documentSnapshot.getData();
            if (userAchievements == null) {
                // Если не существует, создаем новый документ
                userAchievements = new HashMap<>();
                userAchievements.put(userID, new HashMap<>());
            } else if (!userAchievements.containsKey(userID)) {
                // Если Map achieve не существует, создаем его
                userAchievements.put(userID, new HashMap<>());
            }

            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get(userID);

            // Создаем новый Map с информацией о новом достижении
            Map<String, Object> newAchieveMap = new HashMap<>();
            newAchieveMap.put("name", achieveName);
            newAchieveMap.put("url", "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            String time = sdf.format(calendar.getTime());
            newAchieveMap.put("time", time);
            newAchieveMap.put("token", userID);

            if(achievePrice != 0){
                newAchieveMap.put("achievePrice", achievePrice);
            }else{
                newAchieveMap.put("achievePrice", 10);
            }

            if(collectable){
                newAchieveMap.put("collectable", collectable);
            }

            // Добавляем новое достижение в Map achieve пользователя
            achieveMap.put(name, newAchieveMap);

            // Сохраняем обновленный Map achieve в Firestore
            userAchievements.put(userID, achieveMap);
            usersLogsRef.set(userAchievements);
            //Toast.makeText(AchieveConfirmation.this, "test", Toast.LENGTH_SHORT).show();
            showPublicButton();
        });
    }

    private void setImage(String imageRef){
        // Получаем ссылку на изображение

        //StorageReference imageRef1 = storageRef.child(imageRef);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("/users/mC32I9D6xPSnQfd65wzdskqVTph2/UserAvatar");

        // Создаем URL из ссылки на изображение
        Task<Uri> urlTask = storageRef.getDownloadUrl();
        while (!urlTask.isSuccessful());
        Uri downloadUrl = urlTask.getResult();

        // Загружаем изображение в ImageView с помощью HttpURLConnection
        try {
            URL url = new URL(downloadUrl.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(myBitmap);
        } catch (IOException e) {
            // Обработка ошибок
        }

    }

    private void setImagePublic(String name){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());


        usersRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userAchievements = documentSnapshot.getData();

            Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userAchievements");
            // Проверяем, существует ли уже мап с именем achieveName
            long doneCount = 0;
            if (achieveMap.containsKey(achieveName)) {
                // Если мап существует, получаем его
                Map<String, Object> existingAchieveMap = (Map<String, Object>) achieveMap.get(achieveName);

                // Увеличиваем значение doneCount на 1
                if(existingAchieveMap.containsKey("doneCount")){
                    doneCount = (long) existingAchieveMap.get("doneCount");
                    //System.out.println(" if doneCount" + doneCount);
                }
                //ystem.out.println("doneCount" + doneCount);
                //doneCount = (long) existingAchieveMap.get("doneCount");
                System.out.println(" if doneCount" + doneCount);
            }

            if (userAchievements == null) {
                // Если пользователь не существует, создаем новый документ
                userAchievements = new HashMap<>();
                userAchievements.put("userPhotos", new HashMap<>());
            } else if (!userAchievements.containsKey("userPhotos")) {
                // Если Map achieve не существует, создаем его
                userAchievements.put("userPhotos", new HashMap<>());
            }

            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> achieveMap1 = (Map<String, Object>) userAchievements.get("userPhotos");

            ArrayList<String> people = new ArrayList<>();

            // Создаем новый Map с информацией о новом достижении
            Map<String, Object> newAchieveMap = new HashMap<>();
            newAchieveMap.put("name", achieveName);
            newAchieveMap.put("like", people);
            newAchieveMap.put("likes", 0);
            //newAchieveMap.put("url", "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name + doneCount);
            if(doneCount != 0){
                newAchieveMap.put("url", "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name + doneCount);
                System.out.println("! 0");
            }else{
                newAchieveMap.put("url", "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);
                System.out.println(" 0");
            }
            newAchieveMap.put("status", "yellow");

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            String time = sdf.format(calendar.getTime());
            newAchieveMap.put("time", time);

            // Добавляем новое достижение в Map achieve пользователя
            if(doneCount != 0){
                achieveMap1.put(achieveName + doneCount, newAchieveMap);
                System.out.println("! 0");
            }else{
                achieveMap1.put(achieveName, newAchieveMap);
                System.out.println(" 0");
            }
            //achieveMap1.put(achieveName + doneCount, newAchieveMap);

            // Сохраняем обновленный Map achieve в Firestore
            userAchievements.put("userPhotos", achieveMap1);
            usersRef.set(userAchievements);
            Toast.makeText(AchieveConfirmation.this, "Достижение добавлено в профиль", Toast.LENGTH_SHORT).show();
        });
    }
    public void showPublicButton(){
        setPublic.setVisibility(View.VISIBLE); // отображаем кнопку
    }

    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    private void hideButtonAdd(){selectImageButton.setVisibility(View.GONE); // отображаем кнопку
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
}