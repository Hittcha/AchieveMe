package com.Bureau.Achivki;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MyFriendsList extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends_list);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Мои подписки");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        String userID = currentUser.getUid();

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName = documentSnapshot.getString("name");

                Map<String, Object> userData = documentSnapshot.getData();
                Map<String, Object> friends = (Map<String, Object>) userData.get("friends");
                for (Map.Entry<String, Object> entry : friends.entrySet()) {
                    Map<String, Object> friend = (Map<String, Object>) entry.getValue();
                    String key = entry.getKey();
                    System.out.println("key: " + key);

                    String friendName = (String) friend.get("name");
                    String friendAvatar = (String) friend.get("avatar");

                    System.out.println("friendName: " + friendName);
                    System.out.println("friendAvatar: " + friendAvatar);

                    createFriendBlock(friendName, friendAvatar, mAuthDocRef, key, userID);

                }
            } else {
                // документ не найден
            }
        });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyFriendsList.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyFriendsList.this, MainActivity.class);
            startActivity(intent);
        });

        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyFriendsList.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyFriendsList.this, AchieveListActivity.class);
            startActivity(intent);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyFriendsList.this, UsersListActivity.class);
            startActivity(intent);
        });
    }

    public void createFriendBlock(String name, String avatarUrl, DocumentReference mAuthDocRef, String  key, String userID){
        LinearLayout parentLayout = findViewById(R.id.scrollView);


        ConstraintLayout blockFriends = (ConstraintLayout) LayoutInflater.from(MyFriendsList.this)
                .inflate(R.layout.block_friends, parentLayout, false);

        TextView usernameTextView = blockFriends.findViewById(R.id.userName);

        usernameTextView.setText(name);

        parentLayout.addView(blockFriends);

        Button delButton = blockFriends.findViewById(R.id.delete_button);

        delButton.setOnClickListener(v -> {
            System.out.println("del " + name);
            mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> userFriends = documentSnapshot.getData();

                Map<String, Object> friendMap = (Map<String, Object>) userFriends.get("friends");

                friendMap.remove(key);

                userFriends.put("friends", friendMap);

                userFriends.put("friendscount", Long.valueOf(friendMap.size()));

                mAuthDocRef.set(userFriends);

                parentLayout.removeView(blockFriends);

                delFollower(key, userID);

                Toast.makeText(MyFriendsList.this, "Пользователь удален из подписок", Toast.LENGTH_SHORT).show();
            });

        });

        ImageButton imageUserButton = blockFriends.findViewById(R.id.imageUserButton);

        imageUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyFriendsList.this, OtherUserActivity.class);
            intent.putExtra("User_token", key);
            startActivity(intent);
        });

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child(avatarUrl);
        try {
            final File localFile = File.createTempFile("images", "jpg");
            userImageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
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
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void delFollower(String key, String myKey){
        DocumentReference mAuthDocRefOther = db.collection("Users").document(key);

        mAuthDocRefOther.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> friends = documentSnapshot.getData();

            System.out.println("key "+ key);
            System.out.println("myKey "+ myKey);
            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> friendMap = (Map<String, Object>) friends.get("subscribers");

            friendMap.remove(myKey);

            friends.put("subscribers", friendMap);

            friends.put("subs", Long.valueOf(friendMap.size()));

            mAuthDocRefOther.set(friends);

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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}