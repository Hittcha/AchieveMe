package com.Bureau.Achivki;

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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class OtherUserFriends extends AppCompatActivity {

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_friends);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Подписки");

        Intent intent = getIntent();
        String userToken = intent.getStringExtra("User_token");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference mAuthDocRef = db.collection("Users").document(userToken);

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName = documentSnapshot.getString("name");
                getSupportActionBar().setTitle("Подписки " + userName);

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

                    createFriendBlock(friendName, friendAvatar, key);

                }

            } else {
                // документ не найден
                Toast.makeText(OtherUserFriends.this, "Документ не найден", Toast.LENGTH_SHORT).show();

            }
        });

        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);
        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);

        leaderListButton.setOnClickListener(v -> {
            Intent intent15 = new Intent(OtherUserFriends.this, LeaderBoardActivity.class);
            startActivity(intent15);
        });

        menuButton.setOnClickListener(v -> {
            Intent intent14 = new Intent(OtherUserFriends.this, MainActivity.class);
            startActivity(intent14);
        });

        favoritesButton.setOnClickListener(v -> {
            Intent intent13 = new Intent(OtherUserFriends.this, ListOfFavoritesActivity.class);
            startActivity(intent13);
        });

        achieveListButton.setOnClickListener(v -> {
            Intent intent12 = new Intent(OtherUserFriends.this, AchieveListActivity.class);
            startActivity(intent12);
        });

        usersListButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(OtherUserFriends.this, UsersListActivity.class);
            startActivity(intent1);
        });
    }

    public void createFriendBlock(String name, String avatarUrl, String  key){
        LinearLayout parentLayout = findViewById(R.id.scrollView);


        ConstraintLayout blockFriends = (ConstraintLayout) LayoutInflater.from(OtherUserFriends.this)
                .inflate(R.layout.block_friends, parentLayout, false);

        TextView usernameTextView = blockFriends.findViewById(R.id.userName);


        usernameTextView.setText(name);

        parentLayout.addView(blockFriends);

        Button delButton = blockFriends.findViewById(R.id.delete_button);
        delButton.setVisibility(View.GONE);


        ImageButton imageUserButton = blockFriends.findViewById(R.id.imageUserButton);

        imageUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUserFriends.this, OtherUserActivity.class);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}