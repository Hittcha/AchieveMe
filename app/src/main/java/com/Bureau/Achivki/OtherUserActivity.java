package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;


import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherUserActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    private FirebaseFirestore firestore;
    private String userName;
    private String otherUserName;

    private long userScore;

    private long userFriends;

    private long userSubs;

    private String myprofileImageUrl;
    private String profileImageUrl;

    private TextView welcomeMessage;

    private TextView userScoreText;

    public TextView friendsCountText;

    private TextView subsCountText;

    private boolean liked;

    private FirebaseAuth mAuth;

    private ToggleButton subscribeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.appBackGround));
        }

        ImageButton backButton = findViewById(R.id.imageButtonBack);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.pulse);
        backButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(anim);

            }

            onBackPressed();
            return false;
        });


        Intent intent = getIntent();
        String userToken = intent.getStringExtra("User_token");

        System.out.println(userToken);

        db = FirebaseFirestore.getInstance();
        DocumentReference mAuthDocRefOther = db.collection("Users").document(userToken);


        welcomeMessage = findViewById(R.id.welcome_message);
        userScoreText = findViewById(R.id.scoreText);
        friendsCountText = findViewById(R.id.friendsCount);
        subsCountText = findViewById(R.id.subsTextView);
        TextView subscriptionsListTextView = findViewById(R.id.subscriptionsList2);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String myID = currentUser.getUid();
        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName = documentSnapshot.getString("name");
            }

            myprofileImageUrl = documentSnapshot.getString("profileImageUrl");


            mAuthDocRefOther.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        otherUserName = documentSnapshot.getString("name");

                        profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        userScore = documentSnapshot.getLong("score");

                        userSubs = documentSnapshot.getLong("subs");

                        String userKey = mAuthDocRefOther.getId();

                        userFriends = documentSnapshot.getLong("friendscount");

                        friendsCountText.setText("" + userFriends);

                        subsCountText.setText("" + userSubs);

                        setSubscribeButton(otherUserName, myID, userKey, profileImageUrl, mAuthDocRef, userName, mAuthDocRefOther, myprofileImageUrl);

                        welcomeMessage.setText(otherUserName);
                        userScoreText.setText("" + userScore);
                        //userScore.
                        // использовать имя пользователя
                        listoffavorites(otherUserName, userToken);
                        setImage(profileImageUrl);


                        Map<String, Object> userData = documentSnapshot.getData();
                        Map<String, Object> achievements = (Map<String, Object>) userData.get("userPhotos");

                        List<Map.Entry<String, Object>> sortedAchievements = new ArrayList<>(achievements.entrySet());

                        // Sort the achievements by time
                        Collections.sort(sortedAchievements, (entry1, entry2) -> {
                            Map<String, Object> achievement1 = (Map<String, Object>) entry1.getValue();
                            Map<String, Object> achievement2 = (Map<String, Object>) entry2.getValue();
                            String time1 = (String) achievement1.get("time");
                            String time2 = (String) achievement2.get("time");
                            if (time1 == null && time2 == null) {
                                return 0; // Both times are null, consider them equal
                            } else if (time1 == null) {
                                return -1; // time1 is null, consider it smaller than time2
                            } else if (time2 == null) {
                                return 1; // time2 is null, consider it smaller than time1
                            } else {
                                return time2.compareTo(time1);
                            }
                        });

                        for (Map.Entry<String, Object> entry : sortedAchievements) {
                            Map<String, Object> achievement = (Map<String, Object>) entry.getValue();
                            String key = entry.getKey();
                            System.out.println("key: " + key);

                            Long likes = (Long) achievement.get("likes");
                            String url = (String) achievement.get("url");
                            String achname = (String) achievement.get("name");
                            String time = (String) achievement.get("time");

                            String status = (String) achievement.get("status");

                            ArrayList<String> people = (ArrayList<String>) achievement.get("like");

                            // Выводим данные достижения на экран
                            System.out.println("likes: " + likes);
                            System.out.println("url: " + url);

                            createImageBlock(url, likes, people, userToken, key ,userName, time, achname, status);
                        }

                    } else {
                        // документ не найден
                    }
                }
            });
        });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);
        TextView achieveListTextView = findViewById(R.id.scoreTextView2);

        achieveListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, OtherUserAchievements.class);
                intent.putExtra("User_token", userToken);
                startActivity(intent);
            }
        });

        TextView friendsListTextView = findViewById(R.id.friendsList2);

        friendsListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, OtherUserFriends.class);
                intent.putExtra("User_token", userToken);
                startActivity(intent);
            }
        });

        subscriptionsListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, OtherUserSubs.class);
                intent.putExtra("User_token", userToken);
                startActivity(intent);
            }
        });
        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });

    }

    private void setSubscribeButton(String nameOtherUser, String myID, String otherID, String avatar, DocumentReference mAuthDocRef, String userName, DocumentReference mAuthDocRefOther, String profileImageUrl){
        subscribeButton = findViewById(R.id.subscribeButton);

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> friends = documentSnapshot.getData();


            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> friendMap = (Map<String, Object>) friends.get("friends");



            subscribeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        //subscribeButton.setBackgroundResource(R.drawable.likeimageclicked);

                        subscribeButton.setBackgroundResource(R.drawable.subbuttonclicked);
                        System.out.println("Подписан " + otherID);
                        //addLike(userName, key);

                        Map<String, Object> newfriendMap = new HashMap<>();
                        newfriendMap.put("name", nameOtherUser);
                        newfriendMap.put("avatar", avatar);

                        // Добавляем новое достижение в Map achieve пользователя
                        friendMap.put(otherID, newfriendMap);

                        // Сохраняем обновленный Map achieve в Firestore
                        friends.put("friends", friendMap);
                        mAuthDocRef.set(friends);

                        friends.put("friendscount", Long.valueOf(friendMap.size()));

                        mAuthDocRef.set(friends);

                        addFollower(userName, myID, profileImageUrl, mAuthDocRefOther);

                    } else {

                        subscribeButton.setBackgroundResource(R.drawable.subbutton);

                        friendMap.remove(otherID);

                        friends.put("friends", friendMap);

                        friends.put("friendscount", Long.valueOf(friendMap.size()));

                        mAuthDocRef.set(friends);

                        delFollower(userName, myID, profileImageUrl, mAuthDocRefOther);
                    }
                }
            });

            // Создаем новый Map с информацией о новом достижении

            if (friendMap.containsKey(otherID)){
                System.out.println("Подписан " + otherID);
                subscribeButton.setChecked(true);
            }

        });
    }

    private void addFollower(String userName, String key, String avatar, DocumentReference mAuthDocRefOther){
        DocumentReference mAuthDocRef1 = db.collection("Users").document(key);

        mAuthDocRefOther.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> friends = documentSnapshot.getData();


            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> friendMap = (Map<String, Object>) friends.get("subscribers");

            Map<String, Object> newSubMap = new HashMap<>();
            newSubMap.put("name", userName);
            newSubMap.put("avatar", avatar);

            // Добавляем новое достижение в Map achieve пользователя
            friendMap.put(key, newSubMap);

            // Сохраняем обновленный Map achieve в Firestore
            friends.put("subscribers", friendMap);
            mAuthDocRefOther.set(friends);

            friends.put("subs", Long.valueOf(friendMap.size()));

            mAuthDocRefOther.set(friends);


        });
    }

    private void delFollower(String userName, String key, String avatar, DocumentReference mAuthDocRefOther){
        DocumentReference mAuthDocRef1 = db.collection("Users").document(key);

        mAuthDocRefOther.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> friends = documentSnapshot.getData();


            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> friendMap = (Map<String, Object>) friends.get("subscribers");

            friendMap.remove(key);

            friends.put("subscribers", friendMap);

            friends.put("subs", Long.valueOf(friendMap.size()));

            mAuthDocRefOther.set(friends);

        });
    }

    public void setImage(String imageRef) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

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

    private void listoffavorites(String name, String id) {
        CollectionReference favoritesRef = db.collection("Users");

        LinearLayout parentLayout = findViewById(R.id.favoritesLinearLayout);

        AssetManager assetManager = getAssets();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(id);

        //List<String> achievementNames = new ArrayList<>();

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userData = documentSnapshot.getData();
            Map<String, Object> fav = (Map<String, Object>) userData.get("favorites");

            for (Map.Entry<String, Object> entry : fav.entrySet()) {
                Map<String, Object> achievement = (Map<String, Object>) entry.getValue();

                String achname = (String) achievement.get("name");
                String category = (String) achievement.get("category");

                ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(OtherUserActivity.this)
                        .inflate(R.layout.block_favorites_icon, parentLayout, false);

                TextView favorites_name_TextView = blockLayout.findViewById(R.id.favorites_name_TextView);

                Button favorites_icon_Button = blockLayout.findViewById(R.id.favorites_icon_Button);

                favorites_name_TextView.setText(achname);

                parentLayout.addView(blockLayout);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                HorizontalScrollView scroll = findViewById(R.id.favoritesScrollView);
                if (achievement.isEmpty()) {
                    scroll.setVisibility(View.GONE);
                }else{
                    scroll.setVisibility(View.VISIBLE);
                }

                layoutParams.setMargins(20, 20, 20, 20);

                switch (category) {
                    case "Красноярск":
                        try {
                            InputStream inputStream = getAssets().open("favorites/krasnoyarsk_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Еда и напитки":
                        try {
                            InputStream inputStream = assetManager.open("favorites/food_and_drink_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //blockLayout.setBackgroundResource(R.drawable.template_food);
                        break;
                    case "Путешествия":
                        try {
                            InputStream inputStream = assetManager.open("favorites/traveling_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //blockLayout.setBackgroundResource(R.drawable.template_travel);
                        break;
                    case "Кулинар":
                        try {
                            InputStream inputStream = assetManager.open("favorites/cooking_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                        break;
                    case "Калининград":
                        try {
                            InputStream inputStream = assetManager.open("favorites/kaliningrad_favorites.png");
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCornerRadius(20); // Здесь можно указать радиус закругления
                            favorites_icon_Button.setBackground(roundedBitmapDrawable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //blockLayout.setBackgroundResource(R.drawable.template_cooking);
                        break;
                    default:
                        blockLayout.setBackgroundResource(R.drawable.template);
                        break;
                }
                favorites_icon_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Обработка нажатия кнопки
                        System.out.println("Category_key  " + category);
                        Intent intent = new Intent(OtherUserActivity.this, AchieveCategoryListActivity.class);
                        intent.putExtra("Category_key", category);
                        startActivity(intent);

                    }
                });
            }
        });
    }

    private void createImageBlock(String url, Long likes, ArrayList people, String otherUserName, String key, String userToken, String time, String achname, String status){
        LinearLayout parentLayout = findViewById(R.id.scrollView);

        //Button btnAdd = findViewById(R.id.btn_add);

        // CollectionReference achievementCollectionRef = FirebaseFirestore.getInstance().collection("Achievements");

        ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(OtherUserActivity.this)
                .inflate(R.layout.block_images, parentLayout, false);

        TextView AchieveNameTextView = blockLayout.findViewById(R.id.achname);
        TextView DateTextView = blockLayout.findViewById(R.id.date);
        TextView likesTextView = blockLayout.findViewById(R.id.likesCount);

        likesTextView.setText(likes.toString());
        DateTextView.setText(time);
        AchieveNameTextView.setText(achname);

        parentLayout.addView(blockLayout);
        liked = false;

        ToggleButton likeButton = blockLayout.findViewById(R.id.toggleButton2);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );


        likeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    likeButton.setBackgroundResource(R.drawable.likeimageclicked);
                    System.out.println("url " + url);
                    addLike(userName, key, otherUserName);

                    int score = Integer.parseInt(likesTextView.getText().toString());
                    score++;
                    likesTextView.setText(Integer.toString(score));
                } else {
                    likeButton.setBackgroundResource(R.drawable.likeimage);
                    System.out.println("url2 " + url);
                    delLike(userName, key, otherUserName);

                    int score = Integer.parseInt(likesTextView.getText().toString());
                    score--;
                    likesTextView.setText(Integer.toString(score));
                }
            }
        });

        if (people.contains(userName)) {
            liked = true;
            likeButton.setChecked(true);
            likesTextView.setText(likes.toString());
        }else{
            liked = false;
        }

        ImageView imageView = blockLayout.findViewById(R.id.imageView3);

        System.out.println("status " + status);

        ImageView statusImageView = blockLayout.findViewById(R.id.statusImageView);

        if(status == null){
            status = "grey";
        }

        switch (status) {
            case "yellow":
                statusImageView.setImageResource(R.drawable.galka_yellow);
                break;
            case "green":
                statusImageView.setImageResource(R.drawable.galka_green);
                break;
            case "red":
                statusImageView.setImageResource(R.drawable.galka_red);
                break;
            default:
                statusImageView.setImageResource(R.drawable.galka_grey);
                break;
        }

        firestore = FirebaseFirestore.getInstance();

        // Загрузка изображения в Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(url);
        // Получение URL изображения
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // URL изображения
                String imageUrl = uri.toString();

                // Отображение изображения с использованием Picasso
                //ImageView imageView = findViewById(R.id.imageView3);
                Picasso.get()
                        .load(imageUrl)
                        .into(imageView);
                imageView.setAdjustViewBounds(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Обработка ошибки загрузки изображения
            }
        });
    }

    public void addLike(String userName, String key, String userToken){

        // Получаем ссылку на коллекцию пользователей
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(userToken);

        System.out.println("userName " + userName);
        usersRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userAchievements = documentSnapshot.getData();

            Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userPhotos");

            Map<String, Object> achieveMap1 = (Map<String, Object>) achieveMap.get(key);

            ArrayList<String> people = (ArrayList<String>) achieveMap1.get("like");

            Long likes = (Long) achieveMap1.get("likes");

            if (people.contains(userName)) {
                // Если Map achieve не существует, создаем его
            }else{
                people.add(userName);

                achieveMap1.put("like", people);
                likes = Long.valueOf(people.size());
            }

            achieveMap1.put("likes", likes);

            usersRef.set(userAchievements);

        });

    }

    public void delLike(String userName, String key, String userToken) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(userToken);

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
            } else {

            }

            achieveMap1.put("likes", likes);

            usersRef.set(userAchievements);

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
