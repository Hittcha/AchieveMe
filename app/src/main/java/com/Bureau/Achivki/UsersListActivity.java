package com.Bureau.Achivki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class UsersListActivity extends AppCompatActivity {

    private SVGImageView favoritesButton;

    private SVGImageView achieveListButton;

    private SVGImageView leaderListButton;

    private SVGImageView menuButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));*/
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.appBackGround));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Список пользователей");


        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        String userID = currentUser.getUid();

        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Получаем результат запроса
                    QuerySnapshot querySnapshot = task.getResult();

                    // Перебираем каждый документ
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        // Получаем значение поля "name" для каждого документа
                        String name = document.getString("name");
                        int score = Math.toIntExact(document.getLong("score"));
                        String avatar = document.getString("profileImageUrl");
                        String id = document.getId();

                        createUsersBlock(name, score, avatar, id);

                        // Выводим имя на экран
                        System.out.println("Имя пользователя: " + name);
                    }
                } else {
                    // Обработка ошибок
                    System.out.println("Ошибка получения данных из Firestore: " + task.getException());
                }
            }
        });

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersListActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersListActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersListActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        SVGImageView usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersListActivity.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });

       /* leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyFriendsList.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyFriendsList.this, MainActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyFriendsList.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyFriendsList.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });*/
        try {
            InputStream inputStream = getAssets().open("interface_icon/home.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonMenu);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/rate.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonLeaderList);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/chel.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonUsersList);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/kubok niz.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonAchieveList);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = getAssets().open("interface_icon/Star 1.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            SVGImageView svgImageView = findViewById(R.id.imageButtonFavorites);
            svgImageView.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }
    }
/*
    public void createUsersBlock(String name, int score, String avatarUrl, String userID){
        LinearLayout parentLayout = findViewById(R.id.scrollView);

        ConstraintLayout blockFriends = (ConstraintLayout) LayoutInflater.from(UsersListActivity.this)
                .inflate(R.layout.block_userslist, parentLayout, false);

        TextView usernameTextView = blockFriends.findViewById(R.id.userName);
        TextView scoreTextView = blockFriends.findViewById(R.id.userScore);

        usernameTextView.setText(name);

        scoreTextView.setText("Счет: " + score);

        parentLayout.addView(blockFriends);

        ImageButton imageUserButton = blockFriends.findViewById(R.id.imageUserButton);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child(avatarUrl);
        try {
            final File localFile = File.createTempFile("images", "jpg");
            userImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());

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


                    imageUserButton.setImageBitmap(circleBitmap);

                    //imageView.setAdjustViewBounds(true);
                    String s = "UsersListActivity";

                    imageUserButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(UsersListActivity.this, OtherUserActivity.class);
                            intent.putExtra("User_token", userID);
                            startActivity(intent);
                        }
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
/*public void createUsersBlock(String name, int score, String avatarUrl, String userID) {
    LinearLayout parentLayout = findViewById(R.id.scrollView);

    ConstraintLayout blockFriends = (ConstraintLayout) LayoutInflater.from(UsersListActivity.this)
            .inflate(R.layout.block_userslist, parentLayout, false);

    TextView usernameTextView = blockFriends.findViewById(R.id.userName);
    TextView scoreTextView = blockFriends.findViewById(R.id.userScore);

    usernameTextView.setText(name);
    scoreTextView.setText("Счет: " + score);

    parentLayout.addView(blockFriends);

    ImageButton imageUserButton = blockFriends.findViewById(R.id.imageUserButton);

    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    StorageReference userImageRef = storageRef.child(avatarUrl);
    try {
        final File localFile = File.createTempFile("images", "jpg");
        userImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                if (bitmap != null) {
                    int desiredWidth = imageUserButton.getWidth(); // Задайте требуемую ширину
                    int desiredHeight = imageUserButton.getHeight(); // Задайте требуемую высоту

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, false);

                    Bitmap circleBitmap = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
                    BitmapShader shader = new BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Paint paint = new Paint();
                    paint.setShader(shader);

                    Canvas canvas = new Canvas(circleBitmap);
                    float radius = Math.min(desiredWidth, desiredHeight) / 2f;
                    canvas.drawCircle(desiredWidth / 2f, desiredHeight / 2f, radius, paint);

                    imageUserButton.setImageBitmap(circleBitmap);

                    String s = "UsersListActivity";

                    imageUserButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(UsersListActivity.this, OtherUserActivity.class);
                            intent.putExtra("User_token", userID);
                            startActivity(intent);
                        }
                    });
                } else {
                    // Обработка случая, когда загруженное изображение не удалось декодировать
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Обработка ошибки загрузки изображения
            }
        });
    } catch (IOException e) {
        e.printStackTrace();
        // Обработка ошибки создания временного файла
    }
}*/

    public void createUsersBlock(String name, int score, String avatarUrl, String userID) {
        LinearLayout parentLayout = findViewById(R.id.scrollView);

        ConstraintLayout blockFriends = (ConstraintLayout) LayoutInflater.from(UsersListActivity.this)
                .inflate(R.layout.block_userslist, parentLayout, false);

        TextView usernameTextView = blockFriends.findViewById(R.id.userName);
        TextView scoreTextView = blockFriends.findViewById(R.id.userScore);

        usernameTextView.setText(name);
        scoreTextView.setText("Счет: " + score);

        parentLayout.addView(blockFriends);

        ImageButton imageUserButton = blockFriends.findViewById(R.id.imageUserButton);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child(avatarUrl);
        try {
            final File localFile = File.createTempFile("images", "jpg");
            userImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    if (bitmap != null) {

                        CircleTransform circleTransform = new CircleTransform();
                        Bitmap circleBitmap = circleTransform.transform(bitmap);

                        imageUserButton.setImageBitmap(circleBitmap);

                        imageUserButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(UsersListActivity.this, OtherUserActivity.class);
                                intent.putExtra("User_token", userID);
                                startActivity(intent);
                            }
                        });
                    } else {
                        // Обработка случая, когда загруженное изображение не удалось декодировать
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Обработка ошибки загрузки изображения
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            // Обработка ошибки создания временного файла
        }
    }




    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}