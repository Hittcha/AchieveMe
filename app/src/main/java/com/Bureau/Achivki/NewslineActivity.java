package com.Bureau.Achivki;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class NewslineActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsline);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Тестовая лента");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);

        String savedName = sharedPreferences.getString("Name", "");


        //DocumentReference docRef = firestore.collection("UsersLogs").document("UsersPosts");

        DocumentReference mAuthDocRef = db.collection("UsersLogs").document("UsersPosts");

        mAuthDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> postData = document.getData();
                    if (postData != null) {
                        Collection<Object> postMaps = postData.values();
                        for (Object postObj : postMaps) {
                            if (postObj instanceof Map) {
                                Map<String, Object> postMap = (Map<String, Object>) postObj;

                                Long likes = (Long) postMap.get("likes");
                                String url = (String) postMap.get("url");
                                String achname = (String) postMap.get("name");
                                String time = (String) postMap.get("time");
                                String status = (String) postMap.get("status");
                                String otherUserToken = (String) postMap.get("token");
                                ArrayList<String> people = (ArrayList<String>) postMap.get("like");

                                // Вызов функции createImageBlock() с полученными данными
                                System.out.println(achname);
                                createImageBlock(url, likes, people, savedName, achname, time, status, otherUserToken);
                            }
                        }
                    }
                } else {
                    // Документ не найден
                }
            } else {
                // Ошибка при выполнении запроса
            }
        });




    }

    private void createImageBlock(String url, Long likes, ArrayList people, String userName, String achname, String time, String status, String otherUserToken){

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String userId = currentUser.getUid();

        LinearLayout parentLayout = findViewById(R.id.ScrollView1);

        System.out.println(userName);
        ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(this)
                .inflate(R.layout.block_images, parentLayout, false);

        TextView AchieveNameTextView = blockLayout.findViewById(R.id.achname);
        TextView DateTextView = blockLayout.findViewById(R.id.date);
        TextView likesTextView = blockLayout.findViewById(R.id.likesCount);
        SVGImageView achieveIcon = blockLayout.findViewById(R.id.imageView_achieveIcon);

        likesTextView.setText(likes.toString());

        DateTextView.setText(time);

        // изменение размера textview названия ачивки
        WindowCalculation windowCalculation = new WindowCalculation(this);
        double textWeight = windowCalculation.WindowCalculationWeight() * 0.55;
        ViewGroup.LayoutParams textViewLayoutParams = AchieveNameTextView.getLayoutParams();
        textViewLayoutParams.width = (int) textWeight;
        AchieveNameTextView.setLayoutParams(textViewLayoutParams);
        AchieveNameTextView.requestLayout();

        try {
            InputStream inputStream = getAssets().open("interface_icon/home.svg");
            SVG svg = SVG.getFromInputStream(inputStream);
            achieveIcon.setSVG(svg);
        } catch (IOException | SVGParseException e) {
            e.printStackTrace();
        }

        parentLayout.addView(blockLayout);
        boolean liked = false;

        AchieveNameTextView.setText(achname);

        ToggleButton likeButton = blockLayout.findViewById(R.id.toggleButton2);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );


        //Проверка был ли лайкнут пост пользователем

        likeButton.setOnCheckedChangeListener(null);

        if (people.contains(userName)) {
            likeButton.setChecked(true);
            likeButton.setBackgroundResource(R.drawable.likeimageclicked); // Установка фона для выделенного состояния
            likesTextView.setText(likes.toString());
        } else {
            likeButton.setChecked(false);
            likeButton.setBackgroundResource(R.drawable.likeimage); // Установка фона для невыделенного состояния
        }

       /* if(checkIfLikedLocally(userName, otherUserToken+"_"+achname)==true){
            likeButton.setChecked(true);
            int score = Integer.parseInt(likesTextView.getText().toString());
            score++;
            likesTextView.setText(Integer.toString(score));
            likeButton.setBackgroundResource(R.drawable.likeimageclicked);
        }else{
            // likeButton.setChecked(false);
        }*/

        likeButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                likeButton.setBackgroundResource(R.drawable.likeimageclicked);
                System.out.println("url " + url);
                addLike(userName, achname, otherUserToken);

                int score = Integer.parseInt(likesTextView.getText().toString());
                score++;
                likesTextView.setText(Integer.toString(score));
            } else {
                likeButton.setBackgroundResource(R.drawable.likeimage);
                System.out.println("url2 " + url);
                delLike(userName, achname, otherUserToken);

                int score = Integer.parseInt(likesTextView.getText().toString());
                score--;
                likesTextView.setText(Integer.toString(score));
            }
        });

        ImageView imageView = blockLayout.findViewById(R.id.imageView3);
//        ImageView blurImage = blockLayout.findViewById(R.id.imageView_Blur);

        System.out.println("status " + status);

//        ImageView statusImageView = blockLayout.findViewById(R.id.statusImageView);

        /*achieveIcon.setOnClickListener(v -> {

            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> fav = (Map<String, Object>) userData.get("favorites");

                Map<String, Object> userAchieveMap = (Map<String, Object>) userData.get("userAchievements");
                // Получение достижений пользователя
                Set<String> userAchievements = userAchieveMap.keySet();

                for (Map.Entry<String, Object> entry : fav.entrySet()) {
                    Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                    //String achname = (String) achievement.get("name");
                    String category = (String) achievement.get("category");

                    CollectionReference achievementsCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");

                    Query categoryQuery = achievementsCollectionRef.whereEqualTo("name", achname);
                    categoryQuery.get().addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {

                            boolean proof = Boolean.TRUE.equals(document.getBoolean("proof"));
                            boolean collectable = false;
                            long achieveCount = 0;
                            String countDesc = "";
                            long dayLimit = 0;

                            long achievePrice = 0;
                            if (document.contains("price")) {
                                achievePrice = document.getLong("price");
                                System.out.println("price " + achievePrice);
                            }

                            Intent intent = new Intent(this, AchievementDescriptionActivity.class);

                            if (document.contains("collectable")) {
                                System.out.println("collectable");
                                collectable = Boolean.TRUE.equals(document.getBoolean("collectable"));
                                achieveCount = document.getLong("count");
                                dayLimit = document.getLong("dayLimit");
                                countDesc = document.getString("countDesc");
                                intent = new Intent(this, AchievementWithProgressActivity.class);
                            }

                            if (userAchievements.contains(achname)) {
                                System.out.println("Достижение \"" + achname + "\" есть и у пользователя, и в категории " + category);
                                intent.putExtra("dayLimit", dayLimit);
                                intent.putExtra("achieveCount", achieveCount);
                                intent.putExtra("collectable", collectable);
                                intent.putExtra("Category_key", category);
                                intent.putExtra("Achieve_key", achname);
                                intent.putExtra("achievePrice", achievePrice);
                                intent.putExtra("Is_Received", true);
                                intent.putExtra("ProofNeeded", proof);
                                intent.putExtra("isFavorites", true);
                                startActivity(intent);
                            }else{
                                intent.putExtra("dayLimit", dayLimit);
                                intent.putExtra("achieveCount", achieveCount);
                                intent.putExtra("collectable", collectable);
                                intent.putExtra("Category_key", category);
                                intent.putExtra("Achieve_key", achname);
                                intent.putExtra("achievePrice", achievePrice);
                                intent.putExtra("Is_Received", false);
                                intent.putExtra("ProofNeeded", proof);
                                intent.putExtra("isFavorites", true);
                                startActivity(intent);
                            }
                        }
                    });
                }
            });
        });*/

        if(status == null){
            status = "grey";
        }

        switch (status) {
            case "yellow":
                achieveIcon.setBackgroundResource(R.drawable.galka_yellow);
                break;
            case "green":
                achieveIcon.setBackgroundResource(R.drawable.galka_green);
                break;
            case "red":
                achieveIcon.setBackgroundResource(R.drawable.galka_red);
                break;
            default:
                achieveIcon.setBackgroundResource(R.drawable.galka_grey);
                break;
        }

        firestore = FirebaseFirestore.getInstance();

        // Загрузка изображения в Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(url);
        // Получение URL изображения
        /*imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // URL изображения
            String imageUrl = uri.toString();

            // Отображение изображения с использованием Picasso
            Picasso.get()
                    .load(imageUrl)
                    .into(imageView);
            imageView.setAdjustViewBounds(true);
        }).addOnFailureListener(e -> {
            // Обработка ошибки загрузки изображения
        });*/

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // URL изображения
            String imageUrl = uri.toString();

//            Picasso.get()
//                    .load(imageUrl)
//                    .transform(new PhotoBlur(this, 25))
//                    .into(blurImage);
//            blurImage.setAdjustViewBounds(true);

            // Отображение уменьшенного изображения с использованием Picasso
            Picasso.get()
                    .load(imageUrl)
                    .resize(500, 500) // Указываем желаемый размер
                    .centerCrop() // Обрезаем изображение по центру
                    .into(imageView);
            imageView.setAdjustViewBounds(true);

        }).addOnFailureListener(e -> {
            // Обработка ошибки загрузки изображения
        });

        Button button = blockLayout.findViewById(R.id.button3);
        NavigationView navigationView = blockLayout.findViewById(R.id.navigation_view);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_item2); // Replace 'menu_item_id' with the actual ID of the menu item you want to hide
        menuItem.setVisible(false);

        /*button.setOnClickListener(v -> {
            toggleNavigationView(navigationView);
        });*/

        // Add touch listener to the parent layout
        /*blockLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Check if the touch event is outside the navigationView bounds
                if (event.getAction() == MotionEvent.ACTION_DOWN && isTouchOutsideView(event, navigationView)) {
                    hideNavigationView(navigationView);
                    return true;
                }
                return false;
            }
        });*/

       /* navigationView.setNavigationItemSelectedListener(item -> {
            // Обработка выбранного пункта меню
            switch (item.getItemId()) {
                case R.id.nav_item1:
                    // Действие при выборе настройки 1
                    parentLayout.removeView(blockLayout);
                    deleteUserPost(key);
                    //intent = new Intent(UserProfile.this, SuggestAchieveActivity.class);
                    break;
                case R.id.nav_item2:
                    // Действие при выборе настройки 2
                    intent = new Intent(UserProfile.this, MyAchievementsActivity.class);
                    break;
            }
            // Закрытие меню после выбора пункта
            //toggleNavigationView(navigationView);
            navigationView.setVisibility(View.INVISIBLE);
            //startActivity(intent);
            return true;
        });*/

    }

    public void addLike(String userName, String key, String userToken) {
        // Получаем ссылку на коллекцию пользователей
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(userToken);

        DocumentReference lentaRef = db.collection("UsersLogs").document("UsersPosts");

        System.out.println("-------------------------------------------------- ");
        System.out.println("userName " + userName);
        System.out.println("key " + key);
        System.out.println("userToken " + userToken);

        usersRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userAchievements = documentSnapshot.getData();
            Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userPhotos");
            Map<String, Object> achieveMap1 = (Map<String, Object>) achieveMap.get(key);
            ArrayList<String> people = (ArrayList<String>) achieveMap1.get("like");
            Long likes = (Long) achieveMap1.get("likes");

            if (people.contains(userName)) {
                // Если лайк уже существует, не делаем ничего
            } else {
                people.add(userName);

                achieveMap1.put("like", people);
                likes = Long.valueOf(people.size());

                // Сохраняем информацию о лайке в SharedPreferences
                //saveLikeLocally(userName, userToken+"_"+key, true);
            }

            achieveMap1.put("likes", likes);

            usersRef.set(userAchievements);

        });

        lentaRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Получаем Map из документа с именем "dasda_123"
                    Map<String, Object> achieveMap1 = (Map<String, Object>) documentSnapshot.get(userToken + "_" + key);

                    if (achieveMap1 != null) {
                        // Извлекаем ArrayList<String> по ключу "like"
                        ArrayList<String> people = (ArrayList<String>) achieveMap1.get("like");
                        Long likes = (Long) achieveMap1.get("likes");

                        // Используем полученный список
                        if (people != null) {
                            // Делаем что-то с people ArrayList<String>
                            people.add(userName);
                            likes = Long.valueOf(people.size());
                            achieveMap1.put("likes", likes);

                            // Обновляем Map с измененным списком в документе
                            lentaRef.update(userToken + "_" + key, achieveMap1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Документ успешно обновлен
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Обработка ошибки при обновлении документа
                                        }
                                    });
                        }
                    }
                } else {
                    // Документ не существует
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Обработка ошибки
            }
        });
    }
    public void delLike(String userName, String key, String userToken) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(userToken);

        DocumentReference lentaRef = db.collection("UsersLogs").document("UsersPosts");

        System.out.println("userName " + userName);
        usersRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userAchievements = documentSnapshot.getData();
            Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userPhotos");
            Map<String, Object> achieveMap1 = (Map<String, Object>) achieveMap.get(key);

            ArrayList<String> people = (ArrayList<String>) achieveMap1.get("like");
            Long likes = (Long) achieveMap1.get("likes");

            if (people.contains(userName)) {
                people.remove(userName);
                likes = Long.valueOf(people.size());

                // Сохраняем информацию о удаленном лайке в SharedPreferences
                //saveLikeLocally(userName, userToken+"_"+key, false);
            }

            achieveMap1.put("likes", likes);

            usersRef.set(userAchievements);
        });
        lentaRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Получаем Map из документа с именем "dasda_123"
                    Map<String, Object> achieveMap1 = (Map<String, Object>) documentSnapshot.get(userToken + "_" + key);

                    if (achieveMap1 != null) {
                        // Извлекаем ArrayList<String> по ключу "like"
                        ArrayList<String> people = (ArrayList<String>) achieveMap1.get("like");
                        Long likes = (Long) achieveMap1.get("likes");

                        // Используем полученный список
                        if (people != null) {
                            // Делаем что-то с people ArrayList<String>
                            people.remove(userName);
                            likes = Long.valueOf(people.size());
                            achieveMap1.put("likes", likes);

                            // Обновляем Map с измененным списком в документе
                            lentaRef.update(userToken + "_" + key, achieveMap1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Документ успешно обновлен
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Обработка ошибки при обновлении документа
                                        }
                                    });
                        }
                    }
                } else {
                    // Документ не существует
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Обработка ошибки
            }
        });
    }

    // Сохранение информации о лайке в SharedPreferences
    private void saveLikeLocally(String userName, String key, boolean liked) {
        SharedPreferences sharedPreferences = getSharedPreferences("Likes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(userName + "_" + key, liked);
        editor.apply();
    }

    // Проверка, был ли поставлен лайк в локальном хранилище
    private boolean checkIfLikedLocally(String userName, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences("Likes", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(userName + "_" + key, false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}