package com.Bureau.Achivki;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderBoardActivity extends AppCompatActivity {

    private String userName;
    private String profileImageUrl;
    private Long userScore;

    private TextView userNameText;
    private TextView userScoreText;

    private final List<TextView> textviewListOfLeaders = new ArrayList<>();
    private final List<TextView> textviewListOfScore = new ArrayList<>();
    private final List<ImageButton> imageUserButtons = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        textviewListOfLeaders.add(findViewById(R.id.textPlace1));
        textviewListOfLeaders.add(findViewById(R.id.textPlace2));
        textviewListOfLeaders.add(findViewById(R.id.textPlace3));
        textviewListOfLeaders.add(findViewById(R.id.textPlace4));
        textviewListOfLeaders.add(findViewById(R.id.textPlace5));
        textviewListOfLeaders.add(findViewById(R.id.textPlace6));

        textviewListOfScore.add(findViewById(R.id.Score1));
        textviewListOfScore.add(findViewById(R.id.Score2));
        textviewListOfScore.add(findViewById(R.id.Score3));
        textviewListOfScore.add(findViewById(R.id.Score4));
        textviewListOfScore.add(findViewById(R.id.Score5));
        textviewListOfScore.add(findViewById(R.id.Score6));

        imageUserButtons.add(findViewById(R.id.imageUserButton1));
        imageUserButtons.add(findViewById(R.id.imageUserButton2));
        imageUserButtons.add(findViewById(R.id.imageUserButton3));
        imageUserButtons.add(findViewById(R.id.imageUserButton4));
        imageUserButtons.add(findViewById(R.id.imageUserButton5));
        imageUserButtons.add(findViewById(R.id.imageUserButton6));


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db;

        userNameText = findViewById(R.id.userName);
        userScoreText = findViewById(R.id.userScore);

        ImageButton userAvatarView = findViewById(R.id.userAvatar);

        userAvatarView.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderBoardActivity.this, UserProfile.class);
            startActivity(intent);
        });

        File file = new File(this.getFilesDir(), "UserAvatar");
        if (file.exists()) {
            loadAvatarFromLocalFiles("UserAvatar");
        }

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());
        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {

                userName = documentSnapshot.getString("name");
                userScore = documentSnapshot.getLong("score");
                profileImageUrl = documentSnapshot.getString("profileImageUrl");

                //profile = documentSnapshot.getId();
                userNameText.setText(userName);
                userScoreText.setText("Счет: " + userScore);

                if (!file.exists()) {
                    setUserImage(profileImageUrl);
                }

                //setUserImage(profileImageUrl);
            } else {
                // документ не найден
            }
        });

        String collectionName = "Leaders";
        String documentId = "IiSkYx3cqYvapeN6W8wc";

        db.collection(collectionName)
                .document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Получение map "global" с лидерами
                            Map<String, Object> leaders = (Map<String, Object>) document.getData().get("globalLeaders");

                            // Преобразование map в список элементов для сортировки
                            List<Map.Entry<String, Object>> leaderList = new ArrayList<>(leaders.entrySet());

                            // Сортировка списка лидеров по score
                            Collections.sort(leaderList, (o1, o2) -> {
                                HashMap<String, Object> leader1 = (HashMap<String, Object>) o1.getValue();
                                HashMap<String, Object> leader2 = (HashMap<String, Object>) o2.getValue();

                                Long score1 = (Long) leader1.get("score");
                                Long score2 = (Long) leader2.get("score");

                                return score2.compareTo(score1); // В порядке убывания
                                // Используйте score1.compareTo(score2) для порядка возрастания
                            });

                            int count = 0;
                            // Вывод отсортированных лидеров
                            for (Map.Entry<String, Object> leader : leaderList) {

                                String name = leader.getKey();

                                HashMap<String, Object> leaderData = (HashMap<String, Object>) leader.getValue();

                                Long score = (Long) leaderData.get("score");
                                String profileImageUrl = (String) leaderData.get("profileImageUrl");
                                String token = (String) leaderData.get("token");

                                if (count == 0) {
                                    setImage(profileImageUrl, count, token);
                                    textviewListOfLeaders.get(count).setText(name);
                                    textviewListOfScore.get(count).setText("Счет: " + score);

                                } else if (count == 1) {
                                    setImage(profileImageUrl, count, token);
                                    textviewListOfLeaders.get(count).setText(name);
                                    textviewListOfScore.get(count).setText("Счет: " + score);
                                } else if (count == 2) {
                                    setImage(profileImageUrl, count, token);
                                    textviewListOfLeaders.get(count).setText(name);
                                    textviewListOfScore.get(count).setText("Счет: " + score);
                                }else if (count == 3) {
                                    setImage(profileImageUrl, count, token);
                                    textviewListOfLeaders.get(count).setText(name);
                                    textviewListOfScore.get(count).setText("Счет: " + score);
                                } else if (count == 4) {
                                    setImage(profileImageUrl, count, token);
                                    textviewListOfLeaders.get(count).setText(name);
                                    textviewListOfScore.get(count).setText("Счет: " + score);
                                }else if (count == 5) {
                                    setImage(profileImageUrl, count, token);
                                    textviewListOfLeaders.get(count).setText(name);
                                    textviewListOfScore.get(count).setText("Счет: " + score);
                                } else if (count == 6) {
                                    setImage(profileImageUrl, count, token);
                                    textviewListOfLeaders.get(count).setText(name);
                                    textviewListOfScore.get(count).setText("Счет: " + score);
                                }
                                count++;
                            }
                        } else {
                            System.out.println("Документ не найден");
                        }
                    } else {
                        System.out.println("Ошибка получения документа: " + task.getException());
                    }
                });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderBoardActivity.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderBoardActivity.this, MainActivity.class);
            startActivity(intent);
        });

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderBoardActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderBoardActivity.this, AchieveListActivity.class);
            startActivity(intent);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderBoardActivity.this, UsersListActivity.class);
            startActivity(intent);
        });

    }
    public void setImage(String a, int count, String token){

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef1 = storageRef.child(a);

        imageRef1.getMetadata().addOnSuccessListener(storageMetadata -> {
            String mimeType = storageMetadata.getName();
            System.out.println("mimeType " + mimeType);

            imageUserButtons.get(count).setOnClickListener(v -> {
                Intent intent = new Intent(LeaderBoardActivity.this, OtherUserActivity.class);
                intent.putExtra("User_token", token);
                startActivity(intent);
            });
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
                    imageUserButtons.get(count).setImageBitmap(circleBitmap);
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                });
            }
        });
    }

    private void loadAvatarFromLocalFiles(String fileName) {
        ImageButton userButton = findViewById(R.id.userAvatar);

        try {
            // Создание файла с указанным именем в локальной директории приложения
            File file = new File(this.getFilesDir(), fileName);

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

    public void setUserImage(String imageRef) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef1 = storageRef.child(imageRef);

        ImageButton userButton = findViewById(R.id.userAvatar);
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
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }
}