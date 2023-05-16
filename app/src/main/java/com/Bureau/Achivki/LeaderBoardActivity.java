package com.Bureau.Achivki;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LeaderBoardActivity extends AppCompatActivity {

    private String userName;
    private String profileImageUrl;
    private Long userScore;

    private TextView userNameText;
    private TextView userScoreText;

    private ImageButton userAvatarView;

    private FirebaseAuth mAuth;

    private ImageButton favoritesButton;

    private ImageButton achieveListButton;

    private ImageButton leaderListButton;

    private ImageButton menuButton;

    private int a = 0;

    private List<TextView> textviewListOfLeaders = new ArrayList<>();
    private List<TextView> textviewListOfScore = new ArrayList<>();
    private List<ImageButton> imageUserButtons = new ArrayList<>();

    private List<String> imageRefArray = new ArrayList<>();

    private String profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        textviewListOfLeaders.add((TextView) findViewById(R.id.textPlace1));
        textviewListOfLeaders.add((TextView) findViewById(R.id.textPlace2));
        textviewListOfLeaders.add((TextView) findViewById(R.id.textPlace3));
        textviewListOfLeaders.add((TextView) findViewById(R.id.textPlace4));
        textviewListOfLeaders.add((TextView) findViewById(R.id.textPlace5));
        textviewListOfLeaders.add((TextView) findViewById(R.id.textPlace6));

        textviewListOfScore.add((TextView) findViewById(R.id.Score1));
        textviewListOfScore.add((TextView) findViewById(R.id.Score2));
        textviewListOfScore.add((TextView) findViewById(R.id.Score3));
        textviewListOfScore.add((TextView) findViewById(R.id.Score4));
        textviewListOfScore.add((TextView) findViewById(R.id.Score5));
        textviewListOfScore.add((TextView) findViewById(R.id.Score6));

        imageUserButtons.add((ImageButton) findViewById(R.id.imageUserButton1));
        imageUserButtons.add((ImageButton) findViewById(R.id.imageUserButton2));
        imageUserButtons.add((ImageButton) findViewById(R.id.imageUserButton3));
        imageUserButtons.add((ImageButton) findViewById(R.id.imageUserButton4));
        imageUserButtons.add((ImageButton) findViewById(R.id.imageUserButton5));
        imageUserButtons.add((ImageButton) findViewById(R.id.imageUserButton6));


        mAuth = FirebaseAuth.getInstance();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference leadersRef = db.collection("Leaders");


       // storage = FirebaseStorage.getInstance();

        userNameText = findViewById(R.id.userName);

        userScoreText = findViewById(R.id.userScore);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("users").child(mAuth.getCurrentUser().getUid()).child("UserAvatar");

        userAvatarView = findViewById(R.id.userAvatar);




        db = FirebaseFirestore.getInstance();
        //FirebaseAuth mAuth = FirebaseAuth.getInstance();
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

                    profile = documentSnapshot.getId();

                    System.out.println("profile " + profile);

                    userNameText.setText(userName);

                    userScoreText.setText("Счет: " + userScore);
                    // использовать имя пользователя
                    setUserImage(profileImageUrl);
                } else {
                    // документ не найден
                }
            }
        });


        Query query = leadersRef.orderBy("score", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Leader> leadersList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Leader leader = document.toObject(Leader.class);
                    leadersList.add(leader);
                }
                // Делайте что-то с вашим списком лидеров здесь
                int count = 0;
                for (Leader leader : leadersList) {
                    System.out.println("Name: " + leader.getName() + ", Score: " + leader.getScore());
                    if (count >= 6) {
                        break;
                    }
                    if (count == 0) {
                        //String s = s.setText
                        setImage(leader.getProfileImageUrl(), count, leader.getToken());
                        textviewListOfLeaders.get(count).setText(leader.getName());
                        textviewListOfScore.get(count).setText("Счет: " + String.valueOf(leader.getScore()));

                    } else if (count == 1) {
                        setImage(leader.getProfileImageUrl(), count, leader.getToken());
                        textviewListOfLeaders.get(count).setText(leader.getName());
                        textviewListOfScore.get(count).setText(String.valueOf("Счет: " +leader.getScore()));
                    } else if (count == 2) {
                        setImage(leader.getProfileImageUrl(), count, leader.getToken());
                        textviewListOfLeaders.get(count).setText(leader.getName());
                        textviewListOfScore.get(count).setText(String.valueOf("Счет: " +leader.getScore()));
                    }else if (count == 3) {
                        setImage(leader.getProfileImageUrl(), count, leader.getToken());
                        textviewListOfLeaders.get(count).setText(leader.getName());
                        textviewListOfScore.get(count).setText(String.valueOf("Счет: " +leader.getScore()));
                    } else if (count == 4) {
                        setImage(leader.getProfileImageUrl(), count, leader.getToken());
                        textviewListOfLeaders.get(count).setText(leader.getName());
                        textviewListOfScore.get(count).setText(String.valueOf("Счет: " +leader.getScore()));
                    }else if (count == 5) {
                        setImage(leader.getProfileImageUrl(), count, leader.getToken());
                        textviewListOfLeaders.get(count).setText(leader.getName());
                        textviewListOfScore.get(count).setText(String.valueOf("Счет: " +leader.getScore()));
                    } else if (count == 6) {
                        setImage(leader.getProfileImageUrl(), count, leader.getToken());
                        textviewListOfLeaders.get(count).setText(leader.getName());
                        textviewListOfScore.get(count).setText(String.valueOf("Счет: " +leader.getScore()));
                    }
                    count++;
                }
            } else {
                Log.d(TAG, "Error getting leaders: ", task.getException());
            }
        });

       /* FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference leadersRef = db.collection("Leaders");
        DocumentReference documentRef = leadersRef.document("LeadersList");
        documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> leadersList = (List<String>) document.get("LeadersArray");
                        if (leadersList != null && !leadersList.isEmpty()) {
                            int numOfLeaders = leadersList.size();
                            for (int i = 0; i < numOfLeaders; i++) {
                                if (i < 6) {
                                    // Заполнить textview именем лидера
                                    textviewListOfLeaders.get(i).setText(leadersList.get(i));

                                } else {
                                    // Если имен в массиве меньше чем кнопок, то написать в кнопке "лидера еще нет"
                                    //textviewListOfLeaders.get(i).setText("Место еще не занято");
                                    //break;
                                }
                            }
                        } else {
                            // Обработать случай, когда массив LeadersArray пустой
                        }
                    } else {
                        // Обработать случай, когда документ не найден
                    }
                } else {
                    // Обработать случай ошибки при получении документа
                }
            }
        });

*/
        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeaderBoardActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeaderBoardActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeaderBoardActivity.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeaderBoardActivity.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });

    }
    public void setImage(String a, int count, String token){

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference imageRef1 = storageRef.child(a);

        System.out.println("URL " + imageRef1);


        imageRef1.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                String mimeType = storageMetadata.getName();
                System.out.println("mimeType " + mimeType);

                imageUserButtons.get(count).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(LeaderBoardActivity.this, OtherUserActivity.class);
                        intent.putExtra("User_token", token);
                        startActivity(intent);
                    }
                });
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
                            imageUserButtons.get(count).setImageBitmap(circleBitmap);
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


        /*imageRef1.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Paint paint = new Paint();
                paint.setShader(shader);

                Canvas canvas = new Canvas(circleBitmap);
                canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);

                imageUserButtons.get(count).setImageBitmap(circleBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });*/


        /*imageRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                //Picasso.get().load(uri).into(imageUserButtons.get(a));
                System.out.println("A " + a );
               // int c = a;
                Picasso.get().load(uri).into(imageUserButtons.get(count));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Обработка ошибки
            }
        });*/
    }

    public void setUserImage(String imageRef) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        //StorageReference imageRef1 = FirebaseStorage.getInstance().getReferenceFromUrl(a);

        //StorageReference imageRef1 = storageRef.child(a + "/UserAvatar");

        StorageReference imageRef1 = storageRef.child(imageRef);

        System.out.println("URL " + imageRef1);

        ImageButton userButton = findViewById(R.id.userAvatar);
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

}