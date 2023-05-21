package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    //private Button subscribeButton;

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

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String myID = currentUser.getUid();
        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                   @Override
                                                   public void onSuccess(DocumentSnapshot documentSnapshot) {
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



                                                                   //System.out.println("keykeykeykeykey: " + userKey);

                                                                   setSubscribeButton(otherUserName, myID, userKey, profileImageUrl, mAuthDocRef, userName, mAuthDocRefOther, myprofileImageUrl);

                                                                   welcomeMessage.setText(otherUserName);
                                                                   userScoreText.setText("" + userScore);
                                                                   //userScore.
                                                                   // использовать имя пользователя
                                                                   listoffavorites(otherUserName, userToken);
                                                                   setImage(profileImageUrl);



                                                                   Map<String, Object> userData = documentSnapshot.getData();
                                                                   Map<String, Object> achievements = (Map<String, Object>) userData.get("userPhotos");
                                                                   for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                                                                       Map<String, Object> achievement = (Map<String, Object>) entry.getValue();
                                                                       String key = entry.getKey();
                                                                       System.out.println("key: " + key);
                                                                       Long likes = (Long) achievement.get("likes");
                                                                       String url = (String) achievement.get("url");
                                                                       String time = (String) achievement.get("time");
                                                                       String achname = (String) achievement.get("name");

                                                                       ArrayList<String> people = (ArrayList<String>) achievement.get("like");

                                                                       // Выводим данные достижения на экран
                                                                       System.out.println("likes: " + likes);
                                                                       System.out.println("url: " + url);

                                                                       createImageBlock(url, likes, people, userToken, key ,userName, time, achname);
                                                                   }


                                                               } else {
                                                                   // документ не найден
                                                               }
                                                           }
                                                       });
                                                   }
        });

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);
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
                        Button button = new Button(OtherUserActivity.this);
                        button.setText(achievement);
                        button.setTextSize(10);
                        //button.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);


                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                        );
                        layoutParams.setMargins(20, 8, 20, 8);

                        button.setBackgroundResource(R.drawable.favoritesachievebackground);
                        button.setLayoutParams(layoutParams);
                        button.setTag(achievement);

                        // Добавляем кнопку на экран
                        LinearLayout scrollView = findViewById(R.id.favoritesLinearLayout);
                        scrollView.addView(button);

                        // createButtons(achievement, 500, 500, "favoritesLinearLayout");
                    }

                    Button button = new Button(OtherUserActivity.this);
                    //button.setText(achievement);
                    button.setTextSize(10);




                    // Добавляем кнопку на экран
                    LinearLayout scrollView = findViewById(R.id.favoritesLinearLayout);
                    HorizontalScrollView scroll = findViewById(R.id.favoritesScrollView);
                   // scrollView.addView(button);

                    if (achievements.isEmpty()) {
                        scroll.setVisibility(View.GONE);
                    }else{
                    }

                } else {
                    Log.d(TAG, "Error getting achievements: ", task.getException());
                }
            }
        });
    }

    private void createImageBlock(String url, Long likes, ArrayList people, String otherUserName, String key, String userToken, String time, String achname){
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

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child(url);
        try {
            final File localFile = File.createTempFile("images", "jpg");
            userImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());


                    imageView.setImageBitmap(bitmap);

                    imageView.setAdjustViewBounds(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
