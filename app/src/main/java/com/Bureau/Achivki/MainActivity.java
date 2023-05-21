package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String userName;
    private String profileImageUrl;
    private TextView welcomeMessage;
    private TextView userScoreText;
    private TextView userSubsText;
    private TextView userFriendsText;
    private Long userScore;
    private Long userSubs;
    private Long userFriends;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.appBackGround));
        }

        db = FirebaseFirestore.getInstance();
        CollectionReference achievementsRef = db.collection("Achievements");

        List<String> categories = new ArrayList<>();


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        assert currentUser != null;
        welcomeMessage = findViewById(R.id.welcome_message);

        TextView friendsListText = findViewById(R.id.friendsList);
        TextView subscriptionsListText = findViewById(R.id.subscriptionsList);
        TextView scoreText = findViewById(R.id.scoreTextView);

        userScoreText = findViewById(R.id.userScore);
        userSubsText = findViewById(R.id.subsCountTextView);
        userFriendsText = findViewById(R.id.friendsCountTextView);

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName = documentSnapshot.getString("name");

                userScore = documentSnapshot.getLong("score");

                userSubs = documentSnapshot.getLong("subs");

                userFriends = documentSnapshot.getLong("friendscount");

                profileImageUrl = documentSnapshot.getString("profileImageUrl");

                welcomeMessage.setText(userName);

                userFriendsText.setText("" + userFriends);

                userScoreText.setText("" + userScore);

                userSubsText.setText("" + userSubs);

                listoffavorites(userName);
                setImage(profileImageUrl);


            } else {
                // документ не найден
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
                List<String> achievementNames = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String category = document.getString("category");

                    System.out.println("category " + category);

                    if (category != null && !categories.contains(category)) {
                        categories.add(category);
                    }

                    String achievementName = document.getId();
                    achievementNames.add(achievementName);
                }
                createButtons(categories, 500, 500, "scrollView1");
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

    private void createButtons(List<String> achievementNames, int w, int h, String layoutid) {
        for (String name : achievementNames) {

            Button button = new Button(MainActivity.this);
            button.setText(name);
            button.setBackgroundColor(Color.BLUE);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            layoutParams.setMargins(20, 20, 20, 20);
            button.setBackgroundResource(R.drawable.template);
            button.setLayoutParams(layoutParams);
            button.setTag(name);


            button.setOnClickListener(v -> {
                // Обработка нажатия кнопки
                System.out.println("Category_key  " + name);
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("Category_key", name);
                startActivity(intent);
            });

            if (layoutid == "scrollView1") {
                LinearLayout scrollView = findViewById(R.id.scrollView1);
                scrollView.addView(button);
            }
            if (layoutid == "favoritesLinearLayout") {
                LinearLayout scrollView = findViewById(R.id.favoritesLinearLayout);
                scrollView.addView(button);
            }
        }
    }

    private void listoffavorites(String name) {
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
                    //button.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);


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
                    userButton.setImageBitmap(circleBitmap);
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                });
            }
        });
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