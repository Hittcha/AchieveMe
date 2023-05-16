package com.Bureau.Achivki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AchieveConfirmation extends AppCompatActivity {
    private Button setPublic;

    private ImageView mImageView;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;


    private FirebaseAuth mAuth;

    private static final int REQUEST_CODE_SELECT_IMAGE = 100;

    private ImageView profileImageView;

    private FirebaseStorage storage;

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;
    private String achieveName;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve_confirmation);

        mAuth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();

        Intent intent = getIntent();
        achieveName = intent.getStringExtra("Achieve_key");


        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("users").child(mAuth.getCurrentUser().getUid()).child("UserAvatar");

        mImageView = findViewById(R.id.image_view);
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        profileImageView = findViewById(R.id.image_view);
        Button selectImageButton = findViewById(R.id.button_choose_image);


        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(AchieveConfirmation.this,
                        android.Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Если разрешения нет, запрашиваем его у пользователя
                    ActivityCompat.requestPermissions(AchieveConfirmation.this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            PERMISSION_REQUEST_CODE);

                   /* ActivityCompat.requestPermissions(UserProfile.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);*/

                } else {
                    // Если разрешение есть, вызываем окно выбора фотографий
                    selectImageFromLibrary();
                }
            }
        });



        mImageView = findViewById(R.id.image_view);
        db = FirebaseFirestore.getInstance();


        setPublic = findViewById(R.id.button2);

        setPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImagePublic(achieveName);
            }
        });

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

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




    }
    public void selectImageFromLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_CODE_SELECT_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    profileImageView.setImageBitmap(bitmap);

                    uploadImageToStorage(bitmap, achieveName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void uploadImageToStorage(Bitmap bitmap, String name) {
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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

            // Создаем новый Map с информацией о новом достижении
            Map<String, Object> newAchieveMap = new HashMap<>();
            newAchieveMap.put("name", achieveName);
            newAchieveMap.put("confirmed", false);
            newAchieveMap.put("proofsended", true);
            newAchieveMap.put("url", "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);

            // Добавляем новое достижение в Map achieve пользователя
            achieveMap.put(achieveName, newAchieveMap);

            // Сохраняем обновленный Map achieve в Firestore
            userAchievements.put("userAchievements", achieveMap);
            usersRef.set(userAchievements);
            Toast.makeText(AchieveConfirmation.this, "Достижение добавлено на проверку", Toast.LENGTH_SHORT).show();
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
            if (userAchievements == null) {
                // Если пользователь не существует, создаем новый документ
                userAchievements = new HashMap<>();
                userAchievements.put("userPhotos", new HashMap<>());
            } else if (!userAchievements.containsKey("userPhotos")) {
                // Если Map achieve не существует, создаем его
                userAchievements.put("userPhotos", new HashMap<>());
            }

            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userPhotos");

            ArrayList<String> people = new ArrayList<>();

            // Создаем новый Map с информацией о новом достижении
            Map<String, Object> newAchieveMap = new HashMap<>();
            newAchieveMap.put("name", achieveName);
            newAchieveMap.put("like", people);
            newAchieveMap.put("likes", 0);
            newAchieveMap.put("url", "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/proof/" + name);

            // Добавляем новое достижение в Map achieve пользователя
            achieveMap.put(achieveName, newAchieveMap);

            // Сохраняем обновленный Map achieve в Firestore
            userAchievements.put("userPhotos", achieveMap);
            usersRef.set(userAchievements);
            Toast.makeText(AchieveConfirmation.this, "Достижение добавлено на проверку", Toast.LENGTH_SHORT).show();
        });
    }
    public void showPublicButton(){
        setPublic.setVisibility(View.VISIBLE); // отображаем кнопку
    }


}