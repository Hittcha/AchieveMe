package com.Bureau.Achivki;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OtherUserActivity extends AppCompatActivity {
    private FirebaseFirestore db;
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
                        //listoffavorites(otherUserName, userToken);
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

        friendsListTextView.setOnClickListener(v -> {
            Intent intent1 = new Intent(OtherUserActivity.this, OtherUserFriends.class);
            intent1.putExtra("User_token", userToken);
            startActivity(intent1);
        });

        subscriptionsListTextView.setOnClickListener(v -> {
            Intent intent12 = new Intent(OtherUserActivity.this, OtherUserSubs.class);
            intent12.putExtra("User_token", userToken);
            startActivity(intent12);
        });
        leaderListButton.setOnClickListener(v -> {
            Intent intent13 = new Intent(OtherUserActivity.this, LeaderBoardActivity.class);
            startActivity(intent13);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent14 = new Intent(OtherUserActivity.this, MainActivity.class);
            startActivity(intent14);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent15 = new Intent(OtherUserActivity.this, AchieveListActivity.class);
            startActivity(intent15);
        });

        favoritesButton.setOnClickListener(v -> {
            Intent intent16 = new Intent(OtherUserActivity.this, ListOfFavoritesActivity.class);
            startActivity(intent16);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent17 = new Intent(OtherUserActivity.this, UsersListActivity.class);
            //User user = new User("Имя пользователя", 1);
            //intent.putExtra("user", user);
            startActivity(intent17);
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

                        delFollower(myID, mAuthDocRefOther);
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

    private void delFollower(String key, DocumentReference mAuthDocRefOther){
        //DocumentReference mAuthDocRef1 = db.collection("Users").document(key);

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

        statusImageView.setOnClickListener(v -> {

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();

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
        });

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

        //FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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

        Button button = blockLayout.findViewById(R.id.button3);
        NavigationView navigationView = blockLayout.findViewById(R.id.navigation_view);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_item1); // Replace 'menu_item_id' with the actual ID of the menu item you want to hide
        menuItem.setVisible(false);

        button.setOnClickListener(v -> {
            toggleNavigationView(navigationView);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            // Обработка выбранного пункта меню
            Intent intent = null;
            switch (item.getItemId()) {
                case R.id.nav_item1:
                    // Действие при выборе настройки 1
                    //parentLayout.removeView(blockLayout);
                    //deleteUserPost(key);
                    intent = new Intent(this, SuggestAchieveActivity.class);
                    break;
                case R.id.nav_item2:
                    // Действие при выборе настройки 2
                    intent = new Intent(this, ReportActivity.class);
                    intent.putExtra("userToken", otherUserName);
                    intent.putExtra("Achieve_key", achname);
                    intent.putExtra("url", url);
                    break;
            }
            // Закрытие меню после выбора пункта
            //toggleNavigationView(navigationView);
            navigationView.setVisibility(View.INVISIBLE);
            startActivity(intent);
            return true;
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

    private void showNavigationView(NavigationView navigationView) {
        navigationView.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", navigationView.getWidth(), 0);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void hideNavigationView(NavigationView navigationView) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", 0, navigationView.getWidth());
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                navigationView.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    private void toggleNavigationView(NavigationView navigationView) {
        if (navigationView.getVisibility() == View.VISIBLE) {
            hideNavigationView(navigationView);
        } else {
            showNavigationView(navigationView);
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
}
