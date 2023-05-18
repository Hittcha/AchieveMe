package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String userName;
    private String profileImageUrl;

    private ImageButton ButtonSeasonAchieve;
    private TextView welcomeMessage;
    private TextView userScoreText;
    private Long userScore;
    private FirebaseFirestore db;

    private ImageButton userbutton;

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    private ImageButton UsersListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = FirebaseFirestore.getInstance();

        CollectionReference achievementsRef = db.collection("Achievements");

        List<String> categories = new ArrayList<>();

        List<String> favorites = new ArrayList<>();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());
        welcomeMessage = findViewById(R.id.welcome_message);

        userScoreText = findViewById(R.id.userScore);

        //User user = new User();

        mAuthDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    userName = documentSnapshot.getString("name");

                    userScore = documentSnapshot.getLong("score");

                    profileImageUrl = documentSnapshot.getString("profileImageUrl");

                    welcomeMessage.setText(userName);

                    userScoreText.setText("Счет " + userScore);
                    // использовать имя пользователя
                    listoffavorites(userName);
                    setImage(profileImageUrl);

                    //user.setName(userName);
                    //user.setScore(Math.toIntExact(userScore));


                } else {
                    // документ не найден
                }
            }
        });


        achievementsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
            }
        });

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        userbutton = findViewById(R.id.userButton);

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        ButtonSeasonAchieve = findViewById(R.id.imageButtonSeasonAchieve);

        UsersListButton = findViewById(R.id.imageButtonUsersList);

        ButtonSeasonAchieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SeasonsAchievements.class);
                startActivity(intent);
            }
        });

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });
        userbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserProfile.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        UsersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });

    }

    private void createButtons(List<String> achievementNames, int w, int h, String layoutid) {
        LinearLayout layout = findViewById(R.id.scrollView1);
        for (String name : achievementNames) {
           /* Button button = new Button(this);
            button.setText(name);
            layout.addView(button);*/

            Button button = new Button(MainActivity.this);
            button.setText(name);
            button.setBackgroundColor(Color.BLUE);


            /*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(w, h);*/
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            layoutParams.setMargins(20, 20, 20, 20);
            button.setBackgroundResource(R.drawable.template);
            button.setLayoutParams(layoutParams);
            button.setTag(name);


            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Обработка нажатия кнопки
                    System.out.println("Category_key  " + name);
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    intent.putExtra("Category_key", name);
                    startActivity(intent);
                }
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
        favoritesRef.whereEqualTo("name", name).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //List<String> achievementNames = new ArrayList<>();
                    // Получаем документ пользователя Олег
                    DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);

                    // Получаем массив ачивок пользователя Олег
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
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(MainActivity.this, ListOfFavoritesActivity.class);
                                startActivity(intent);

                            }
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

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(MainActivity.this, AchieveListActivity.class);
                            // intent.putExtra("Category_key", name);
                            startActivity(intent);

                        }
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
            }
        });
    }

    public void setImage(String imageRef) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        //StorageReference imageRef1 = FirebaseStorage.getInstance().getReferenceFromUrl(a);

        //StorageReference imageRef1 = storageRef.child(a + "/UserAvatar");

        StorageReference imageRef1 = storageRef.child(imageRef);

        System.out.println("URL " + imageRef1);

        ImageButton userButton = findViewById(R.id.userButton);
        imageRef1.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                String mimeType = storageMetadata.getName();
                System.out.println("mimeType " + mimeType);
                if (mimeType != null && mimeType.startsWith("User")) {
                    imageRef1.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
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
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }
            }
        });
    }
    public void onBackPressed() {
        // Пустая реализация, чтобы ничего не происходило при нажатии кнопки "Назад"
    }
}