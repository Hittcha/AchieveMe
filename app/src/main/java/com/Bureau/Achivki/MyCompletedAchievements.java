package com.Bureau.Achivki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyCompletedAchievements extends AppCompatActivity {

    private Button suggestAchieveButton;

    private Button myAchievementsButton;

    private String userName;
    private String profileImageUrl;

    private Long userScore;

    private boolean liked;


    private TextView userNameText;
    private TextView userScoreText;

    private int value = 0;

    private ImageView mImageView;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final int REQUEST_CODE_SELECT_IMAGE = 100;

    private ImageView profileImageView;

    private FirebaseStorage storage;

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    private TextView scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_completed_achievements);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());


        mAuthDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    userName = documentSnapshot.getString("name");

                    userScore = documentSnapshot.getLong("score");

                    System.out.println("score " + userScore);

                    profileImageUrl = documentSnapshot.getString("profileImageUrl");

                    //userNameText.setText(userName);

                    //userScoreText.setText("" + userScore);
                    // использовать имя пользователя
                    //setImage(profileImageUrl);


                    Map<String, Object> userData = documentSnapshot.getData();
                    Map<String, Object> achievements = (Map<String, Object>) userData.get("userAchievements");
                    for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                        Map<String, Object> achievement = (Map<String, Object>) entry.getValue();
                        String key = entry.getKey();
                        System.out.println("key: " + key);
                        //String desc = (String) achievement.get("desc");
                        String achievementName = (String) achievement.get("name");
                        Boolean confirmed = (Boolean) achievement.get("confirmed");
                        Boolean proofsended = (Boolean) achievement.get("proofsended");

                        System.out.println("confirmed: " + confirmed);
                        System.out.println("proofsended: " + proofsended);

                        if(confirmed == true){
                            System.out.println("confirmed");
                            createButton(achievementName,"green", userName);
                        }else if (proofsended == true) {
                            createButton(achievementName,"yellow", userName);
                            System.out.println("proofsended");
                            //createButton(achievementName, "green", categoryName, name, proof);
                        }else{
                            createButton(achievementName,"black", userName);
                            System.out.println("not ");
                        }

                        //ArrayList<String> people = (ArrayList<String>) achievement.get("like");

                        // Выводим данные достижения на экран
                        System.out.println("name: " + achievementName);
                        //System.out.println("desc: " + desc);
                        //createImageButton(t, url);

                        //createImageBlock(url, likes, people, userName, key, achname);
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
                Intent intent = new Intent(MyCompletedAchievements.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyCompletedAchievements.this, MainActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyCompletedAchievements.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyCompletedAchievements.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyCompletedAchievements.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });


    }

    private void createButton(String name, String color, String username) {
        LinearLayout layout = findViewById(R.id.scrollView1);
        Button button = new Button(MyCompletedAchievements.this);
        button.setText(name);
        button.setBackgroundColor(Color.BLUE);

        boolean received;

        // Получаем ссылку на коллекцию "Users"
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

        // Создаем запрос, который возвращает только документы, у которых есть достижение "красноярск"
        Query query = usersRef.whereEqualTo("userAchievements.Путешествие в Скандинавию.name", name);

        System.out.println(query);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        List<String> achievementNames = new ArrayList<>();
        // String confirmed = "sfd";

        // здесь можно добавить дополнительные параметры для кнопки, например, размеры, цвет, обработчик нажатия и т.д.

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(20, 20, 20, 20);
        button.setBackgroundColor(Color.GREEN);

        if (color == "green"){
            button.setBackgroundResource(R.drawable.achievebackgroundgreen);
            received = true;
        }else if(color == "yellow"){
            button.setBackgroundResource(R.drawable.achievebackgroundyellow);
            received = true;
        }else{
            button.setBackgroundResource(R.drawable.achievebackground);
            received = false;
        }

        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработка нажатия кнопки
                Intent intent = new Intent(MyCompletedAchievements.this, AchievementDescriptionActivity.class);
                intent.putExtra("Achieve_key", name);
                //intent.putExtra("Category_key", categoryName);
                intent.putExtra("Is_Received", received);
                intent.putExtra("User_name", username);
                //intent.putExtra("ProofNeeded", proof);
                startActivity(intent);
            }
        });*/


        button.setLayoutParams(layoutParams);

        button.setTag(name);
        //button.setBackgroundResource(R.drawable.achievebackground);

        LinearLayout scrollView = findViewById(R.id.scrollView1);
        scrollView.addView(button);

    }
}