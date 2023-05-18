package com.Bureau.Achivki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MySubscriptionsActivity extends AppCompatActivity {

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_subscriptions);
        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    userName = documentSnapshot.getString("name");

                    //userScore = documentSnapshot.getLong("score");

                    //System.out.println("score " + userScore);

                    //profileImageUrl = documentSnapshot.getString("profileImageUrl");

                    //userNameText.setText(userName);

                    //userScoreText.setText("" + userScore);
                    // использовать имя пользователя
                    //setImage(profileImageUrl);


                    Map<String, Object> userData = documentSnapshot.getData();
                    Map<String, Object> friends = (Map<String, Object>) userData.get("subscribers");
                    for (Map.Entry<String, Object> entry : friends.entrySet()) {
                        Map<String, Object> friend = (Map<String, Object>) entry.getValue();
                        String key = entry.getKey();
                        System.out.println("key: " + key);

                        String friendName = (String) friend.get("name");
                        String friendAvatar = (String) friend.get("avatar");

                        System.out.println("friendName: " + friendName);
                        System.out.println("friendAvatar: " + friendAvatar);

                        createFriendBlock(friendName, friendAvatar, mAuthDocRef, key);

                    }

                } else {
                    // документ не найден
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
                Intent intent = new Intent(MySubscriptionsActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MySubscriptionsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MySubscriptionsActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MySubscriptionsActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MySubscriptionsActivity.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });
    }

    public void createFriendBlock(String name, String avatarUrl, DocumentReference mAuthDocRef, String  key){
        LinearLayout parentLayout = findViewById(R.id.scrollView);


        ConstraintLayout blockFriends = (ConstraintLayout) LayoutInflater.from(MySubscriptionsActivity.this)
                .inflate(R.layout.block_friends, parentLayout, false);

        TextView usernameTextView = blockFriends.findViewById(R.id.userName);


        usernameTextView.setText(name);

        parentLayout.addView(blockFriends);

        Button delButton = blockFriends.findViewById(R.id.delete_button);
        delButton.setVisibility(View.GONE);


        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("del " + name);
                mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> userFriends = documentSnapshot.getData();

                    Map<String, Object> friendMap = (Map<String, Object>) userFriends.get("subscribers");

                    friendMap.remove(key);

                    userFriends.put("subscribers", friendMap);

                    userFriends.put("subs", Long.valueOf(friendMap.size()));

                    mAuthDocRef.set(userFriends);

                    parentLayout.removeView(blockFriends);

                    //delScore(userName);

                    Toast.makeText(MySubscriptionsActivity.this, "Пользователь удален из подписок", Toast.LENGTH_SHORT).show();
                });

            }
        });

        ImageView imageView = blockFriends.findViewById(R.id.imageUserButton);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child(avatarUrl);
        try {
            final File localFile = File.createTempFile("images", "jpg");
            userImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());

                    // определяем размеры экрана
                    /*DisplayMetrics displayMetrics = new DisplayMetrics();
                    Display display = ContextCompat.getSystemService(MyFriendsList.this, DisplayManager.class).getDisplay(Display.DEFAULT_DISPLAY);


                    int targetWidth = getResources().getDisplayMetrics().widthPixels / 3;


                    int targetHeight = targetWidth;
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
*/

                    // Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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


                    imageView.setImageBitmap(circleBitmap);

                    //imageView.setAdjustViewBounds(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}