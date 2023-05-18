package com.Bureau.Achivki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.provider.MediaStore;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;


public class UserProfile extends AppCompatActivity {
    private Button suggestAchieveButton;

    private Button myAchievementsButton;

    private String userName;
    private String profileImageUrl;

    private Long userScore;

    private Long userSubs;

    private Long userFriends;

    private boolean liked;


    private TextView userNameText;
    private TextView userScoreText;

    private TextView userFriendsText;

    private TextView userSubsText;

    private TextView friendsListText;

    private TextView subscriptionsListText;

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


    private static final int PERMISSION_REQUEST_CODE = 100;


    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();

        userSubsText = findViewById(R.id.subsCountTextView);

        userNameText = findViewById(R.id.userName);

        userFriendsText = findViewById(R.id.friendsCountTextView);

        userScoreText = findViewById(R.id.userScore);

        friendsListText = findViewById(R.id.friendsList);

        subscriptionsListText = findViewById(R.id.subscriptionsList);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("users").child(mAuth.getCurrentUser().getUid()).child("UserAvatar");

        mImageView = findViewById(R.id.image_view);
        db = FirebaseFirestore.getInstance();
        //FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();


        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());
        mAuthDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    userName = documentSnapshot.getString("name");

                    userScore = documentSnapshot.getLong("score");

                    userSubs = documentSnapshot.getLong("subs");

                    userFriends = documentSnapshot.getLong("friendscount");

                    System.out.println("score " + userScore);

                    profileImageUrl = documentSnapshot.getString("profileImageUrl");

                    userNameText.setText(userName);

                    userFriendsText.setText("" + userFriends);

                    userScoreText.setText("" + userScore);

                    userSubsText.setText("" + userSubs);
                    // использовать имя пользователя
                    setImage(profileImageUrl);


                    Map<String, Object> userData = documentSnapshot.getData();
                    Map<String, Object> achievements = (Map<String, Object>) userData.get("userPhotos");
                    for (Map.Entry<String, Object> entry : achievements.entrySet()) {
                        Map<String, Object> achievement = (Map<String, Object>) entry.getValue();
                        String key = entry.getKey();
                        System.out.println("key: " + key);
                        Long likes = (Long) achievement.get("likes");
                        String url = (String) achievement.get("url");
                        String achname = (String) achievement.get("name");
                        String time = (String) achievement.get("time");

                        ArrayList<String> people = (ArrayList<String>) achievement.get("like");

                        // Выводим данные достижения на экран
                        System.out.println("likes: " + likes);
                        System.out.println("url: " + url);

                        createImageBlock(url, likes, people, userName, key, achname, time);
                    }


                } else {
                    // документ не найден
                }
            }
        });

        suggestAchieveButton = findViewById(R.id.SuggestAchieve);
        suggestAchieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, SuggestAchieveActivity.class);
                startActivity(intent);
            }
        });

        myAchievementsButton = findViewById(R.id.myAchievementsButton);
        myAchievementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, MyAchievementsActivity.class);
                startActivity(intent);
            }
        });
        mStorageRef = FirebaseStorage.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());

        System.out.println("value = " + value);

        System.out.println("value = " + value);


        profileImageView = findViewById(R.id.image_view);
        ImageButton selectImageButton = findViewById(R.id.button_choose_image);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImageFromLibrary();

                //loadPhotosFromGallery();
                //pickImages();
                /*if (ContextCompat.checkSelfPermission(UserProfile.this,
                        Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Если разрешения нет, запрашиваем его у пользователя
                    ActivityCompat.requestPermissions(UserProfile.this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);

                    ActivityCompat.requestPermissions(UserProfile.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);

                } else {
                    // Если разрешение есть, вызываем окно выбора фотографий
                    selectImageFromLibrary();

                }*/
            }
        });

        scoreText = findViewById(R.id.scoreTextView);

        friendsListText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, MyFriendsList.class);
                startActivity(intent);
            }
        });

        scoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, MyCompletedAchievements.class);
                startActivity(intent);
            }
        });

        subscriptionsListText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, MySubscriptionsActivity.class);
                startActivity(intent);
            }
        });

        leaderListButton = findViewById(R.id.imageButtonLeaderList);

        menuButton = findViewById(R.id.imageButtonMenu);

        favoritesButton = findViewById(R.id.imageButtonFavorites);

        achieveListButton = findViewById(R.id.imageButtonAchieveList);

        leaderListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                startActivity(intent);
            }
        });

        achieveListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, AchieveListActivity.class);
                startActivity(intent);
            }
        });
        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, ListOfFavoritesActivity.class);
                startActivity(intent);
            }
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, UsersListActivity.class);
                //User user = new User("Имя пользователя", 1);
                //intent.putExtra("user", user);
                startActivity(intent);
            }
        });
    }

    public void selectImageFromLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Bitmap circleBitmap = getCircleBitmap(bitmap);
                    profileImageView.setImageBitmap(circleBitmap);

                    //uploadImageToStorage(circleBitmap, "UserAvatarCircle");
                    uploadImageToStorage(bitmap, "UserAvatar");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        if (bitmap.getHeight() > bitmap.getWidth()){
            canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
        }else{
            canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getHeight() / 2f, paint);
        }
        //canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getHeight() / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    public void uploadImageToStorage(Bitmap bitmap, String name) {
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + name);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        usersRef.document(currentUser.getUid()).update("profileImageUrl", "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(UserProfile.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads
                Toast.makeText(UserProfile.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setImage(String imageRef) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference imageRef1 = storageRef.child(imageRef);

        System.out.println("URL " + imageRef1);

        ImageView userButton = findViewById(R.id.image_view);
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

    private void pickImages() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Создаем ссылку на папку, в которую будем загружать фотографии
        StorageReference photosRef = storageRef.child("photos");

        // Получаем доступ к файлам на телефоне
        //File photosDirectory = new File(Environment.getExternalStorageDirectory() + "/Pictures");
        File photosDirectory = new File(Environment.getExternalStorageDirectory() + "/DCIM");
        File[] photos = photosDirectory.listFiles();

        // Загружаем последние 5 фотографий
        int photosToUpload = Math.min(photos.length, 2);
        for (int i = 0; i < photosToUpload; i++) {
            // Создаем ссылку на файл, который будем загружать
            File photo = photos[photos.length - i - 1];
            Uri fileUri = Uri.fromFile(photo);
            StorageReference photoRef = photosRef.child(photo.getName());

            // Загружаем файл в Firebase Storage
            UploadTask uploadTask = photoRef.putFile(fileUri);
        }

    }

    private void loadPhotosFromGallery() {
        // Запрос фотографий из галереи
        String[] projection = {MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);

        // Сохранение последних 5 фотографий в Firebase Storage
        int count = 0;
        while (cursor.moveToNext() && count < 1) {
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            Uri imageUri = Uri.fromFile(new File(imagePath));
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("photos/" + imageUri.getLastPathSegment());
            UploadTask uploadTask = storageRef.putFile(imageUri);
            count++;
        }
        cursor.close();
    }

    private void createImageBlock(String url, Long likes, ArrayList people, String userName, String key, String achname, String time){
        LinearLayout parentLayout = findViewById(R.id.scrollView);


        ConstraintLayout blockLayout = (ConstraintLayout) LayoutInflater.from(UserProfile.this)
                .inflate(R.layout.block_images, parentLayout, false);

        TextView AchieveNameTextView = blockLayout.findViewById(R.id.achname);
        TextView DateTextView = blockLayout.findViewById(R.id.date);
        TextView likesTextView = blockLayout.findViewById(R.id.likesCount);

        likesTextView.setText(likes.toString());

        DateTextView.setText(time);

        parentLayout.addView(blockLayout);
        liked = false;

        AchieveNameTextView.setText(achname);

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
                    addLike(userName, key);

                    int score = Integer.parseInt(likesTextView.getText().toString());
                    score++;
                    likesTextView.setText(Integer.toString(score));
                } else {
                    likeButton.setBackgroundResource(R.drawable.likeimage);
                    System.out.println("url2 " + url);
                    delLike(userName, key);

                    int score = Integer.parseInt(likesTextView.getText().toString());
                    score--;
                    likesTextView.setText(Integer.toString(score));
                }
            }
        });

        if (people.contains(userName)) {
            // Если Map achieve не существует, создаем его
            liked = true;
            //likeButton.setBackgroundResource(R.drawable.likeimageclicked);
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

                    // определяем размеры экрана
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    Display display = ContextCompat.getSystemService(UserProfile.this, DisplayManager.class).getDisplay(Display.DEFAULT_DISPLAY);


                    int targetWidth = getResources().getDisplayMetrics().widthPixels / 3;


                    int targetHeight = targetWidth;
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);


                    imageView.setImageBitmap(bitmap);

                    imageView.setAdjustViewBounds(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addLike(String userName, String key){

        // Получаем ссылку на коллекцию пользователей
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());

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
    public void delLike(String userName, String key) {

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());

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

}
