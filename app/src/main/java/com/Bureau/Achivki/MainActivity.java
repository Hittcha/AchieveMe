package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String userName;
    private String profileImageUrl;
    private Long userScore;
    private Long userSubs;
    private Long userFriends;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);

        String savedName = sharedPreferences.getString("Name", "");
        userScore = sharedPreferences.getLong("Score", 0);
        userSubs = sharedPreferences.getLong("Subs", 0);
        userFriends = sharedPreferences.getLong("Friends", 0);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.appBackGround));

        db = FirebaseFirestore.getInstance();
        CollectionReference achievementsRef = db.collection("Achievements");

        List<String> categories = new ArrayList<>();


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String userId = currentUser.getUid();

        //Проверка айди админа
        if (userId.equals("S5S9nb7f0qheRWwlDu1CyCG4QxO2")){
            Button adminButton = findViewById(R.id.adminButton);
            adminButton.setVisibility(View.VISIBLE);
        }

        TextView welcomeMessage = findViewById(R.id.welcome_message);
        welcomeMessage.setText(savedName);


        TextView friendsListText = findViewById(R.id.friendsList);
        TextView subscriptionsListText = findViewById(R.id.subscriptionsList);
        TextView scoreText = findViewById(R.id.scoreTextView);

        TextView userScoreText = findViewById(R.id.userScore);
        TextView userSubsText = findViewById(R.id.subsCountTextView);
        TextView userFriendsText = findViewById(R.id.friendsCountTextView);

        userScoreText.setText("" + userScore);
        userFriendsText.setText("" + userFriends);
        userSubsText.setText("" + userSubs);

        //Грузим аватар из локальных файлов, а если нет... то нет хэх
        File file = new File(this.getFilesDir(), "UserAvatar");
                    if (file.exists()) {
                        loadAvatarFromLocalFiles();
                    }

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());


        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName = documentSnapshot.getString("name");
                userScore = documentSnapshot.getLong("score");
                userSubs = documentSnapshot.getLong("subs");
                userFriends = documentSnapshot.getLong("friendscount");
                profileImageUrl = documentSnapshot.getString("profileImageUrl");

                userScoreText.setText("" + userScore);
                userFriendsText.setText("" + userFriends);
                userSubsText.setText("" + userSubs);


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Name", userName);
                editor.putLong("Score", userScore);
                editor.putLong("Subs", userSubs);
                editor.putLong("Friends", userFriends);
                editor.apply();

                listOfFavorites(userName);
                setImage(profileImageUrl);

                //Если аватар не сохранен локально - то грузим его с клауда и сохраняем
                if (!file.exists()) {
                    setImage(profileImageUrl);
                }

            } else {
                //Toast.makeText(this, "Ошибка соеднинения", Toast.LENGTH_SHORT).show();
            }
        });

        friendsListText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyFriendsList.class);
            startActivity(intent);
        });

        scoreText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyCompletedAchievements.class);
            startActivity(intent);
        });

        subscriptionsListText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MySubscriptionsActivity.class);
            startActivity(intent);
        });

        achievementsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String category = document.getString("category");

                    if (category != null && !categories.contains(category)) {
                        categories.add(category);
                    }
                }
                createCategoryBlock(categories);
            } else {
                Log.d(TAG, "Error getting achievements: ", task.getException());
            }
        });

        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        ImageButton userButton = findViewById(R.id.userButton);
        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);
        ImageButton buttonSeasonAchieve = findViewById(R.id.imageButtonSeasonAchieve);
        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);

        Button adminButton = findViewById(R.id.adminButton);

        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        buttonSeasonAchieve.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SeasonsAchievements.class);
            startActivity(intent);
        });

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AchieveListActivity.class);
            startActivity(intent);
        });
        userButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserProfile.class);
            startActivity(intent);
        });

        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UsersListActivity.class);
            startActivity(intent);
        });

    }

    private void listOfFavorites(String name) {
        CollectionReference favoritesRef = db.collection("Users");
        favoritesRef.whereEqualTo("name", name).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);

                List<String> achievements = (List<String>) userDoc.get("favorites");

                // Создаем кнопки с именами ачивок
                for (String achievement : achievements) {

                    if (achievement.length() > 10) {
                        achievement = achievement.substring(0, 10) + "...";
                    }
                    Button button = new Button(MainActivity.this);
                    button.setText(achievement);
                    button.setTextSize(10);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    layoutParams.setMargins(20, 20, 20, 20);
                    button.setOnClickListener(v -> {

                        Intent intent = new Intent(MainActivity.this, ListOfFavoritesActivity.class);
                        startActivity(intent);

                    });
                    button.setBackgroundResource(R.drawable.favoritesachievebackground);
                    button.setLayoutParams(layoutParams);
                    button.setTag(achievement);

                    // Добавляем кнопку на экран
                    LinearLayout scrollView = findViewById(R.id.favoritesLinearLayout);
                    scrollView.addView(button);

                    // createButtons(achievement, 500, 500, "favoritesLinearLayout");
                }

                Button button = new Button(MainActivity.this);
                //button.setText(achievement);
                button.setTextSize(10);

                button.setOnClickListener(v -> {

                    Intent intent = new Intent(MainActivity.this, AchieveListActivity.class);
                    startActivity(intent);

                });

                //button.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);


                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(20, 20, 20, 20);
                button.setBackgroundResource(R.drawable.addfavoritesbutton);
                button.setLayoutParams(layoutParams);
                //button.setTag(achievement);

                // Добавляем кнопку на экран
                LinearLayout scrollView = findViewById(R.id.favoritesLinearLayout);
                scrollView.addView(button);

            } else {
                Log.d(TAG, "Error getting achievements: ", task.getException());
            }
        });
    }

    private void createCategoryBlock(List<String> achievementNames){
        LinearLayout parentLayout = findViewById(R.id.scrollView1);

        for (String name : achievementNames) {

            ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.block_category, parentLayout, false);

            TextView CategoryNameTextView = blockLayout.findViewById(R.id.categoryNameTextView);

            CategoryNameTextView.setText(name);

            parentLayout.addView(blockLayout);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            layoutParams.setMargins(20, 20, 20, 20);

            switch (name) {
                case "Красноярск":
                    blockLayout.setBackgroundResource(R.drawable.template_kras);
                    break;
                case "Еда и напитки":
                    blockLayout.setBackgroundResource(R.drawable.template_food);
                    break;
                case "Путешествия":
                    blockLayout.setBackgroundResource(R.drawable.template_travel);
                    break;
                case "Кулинар":
                    blockLayout.setBackgroundResource(R.drawable.template_cooking);
                    break;
                default:
                    blockLayout.setBackgroundResource(R.drawable.template);
                    break;
            }

            blockLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Обработка нажатия кнопки
                    System.out.println("Category_key  " + name);
                    Intent intent = new Intent(MainActivity.this, AchieveCategoryListActivity.class);
                    intent.putExtra("Category_key", name);
                    startActivity(intent);

                }
            });
        }
    }

    public void setImage(String imageRef) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference imageRef1 = storageRef.child(imageRef);

        System.out.println("URL " + imageRef1);

        ImageButton userButton = findViewById(R.id.userButton);
        imageRef1.getMetadata().addOnSuccessListener(storageMetadata -> {
            String mimeType = storageMetadata.getName();
            System.out.println("mimeType " + mimeType);
            if (mimeType != null && mimeType.startsWith("User")) {
                imageRef1.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Paint paint = new Paint();
                    paint.setShader(shader);

                    Canvas canvas = new Canvas(circleBitmap);
                    if (bitmap.getHeight() > bitmap.getWidth()){
                        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
                    }else{
                        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getHeight() / 2f, paint);
                    }
                    File file = new File(this.getFilesDir(), "UserAvatar");
                    if (!file.exists()) {
                        saveAvatarToLocalFiles(bitmap);
                    }
                    userButton.setImageBitmap(circleBitmap);
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                });
            }
        });
    }

    private void loadAvatarFromLocalFiles() {
        ImageButton userButton = findViewById(R.id.userButton);

        try {
            // Создание файла с указанным именем в локальной директории приложения
            File file = new File(this.getFilesDir(), "UserAvatar");

            // Чтение файла в виде Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            // Преобразование Bitmap в круговой вид
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);

            Canvas canvas = new Canvas(circleBitmap);
            if (bitmap.getHeight() > bitmap.getWidth()){
                canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
            }else{
                canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getHeight() / 2f, paint);
            }

            // Установка кругового Bitmap в качестве изображения для кнопки
            userButton.setImageBitmap(circleBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            //setImage(profileImageUrl);
        }
    }

    private void saveAvatarToLocalFiles(Bitmap bitmap){
        //final long MAX_DOWNLOAD_SIZE = 1024 * 1024; // Максимальный размер файла для загрузки
        try {
            // Создание локального файла для сохранения изображения
            File file = new File(this.getFilesDir(), "UserAvatar");

            // Сохранение Bitmap в файл
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void onBackPressed() {
        // Пустая реализация, чтобы ничего не происходило при нажатии кнопки "Назад"
    }
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }
}