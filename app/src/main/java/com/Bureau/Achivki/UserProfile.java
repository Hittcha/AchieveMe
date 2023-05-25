package com.Bureau.Achivki;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class UserProfile extends AppCompatActivity {
    private String userName;
    private String profileImageUrl;

    private Long userScore;

    private Long userSubs;

    private Long userFriends;

    private boolean liked;

    private int value = 0;

    private ImageView mImageView;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final int REQUEST_CODE_SELECT_IMAGE = 100;

    private ImageView profileImageView;
    private FirebaseStorage storage;
    private DrawerLayout drawerLayout;

    Intent intent;


    private static final int PERMISSION_REQUEST_CODE = 100;


    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

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


        drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton btnOpenMenu = findViewById(R.id.btn_open_menu);
        btnOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Обработка выбранного пункта меню
                switch (item.getItemId()) {
                    case R.id.nav_item1:
                        // Действие при выборе настройки 1
                        intent = new Intent(UserProfile.this, SuggestAchieveActivity.class);
                        break;
                    case R.id.nav_item2:
                        // Действие при выборе настройки 2
                        intent = new Intent(UserProfile.this, MyAchievementsActivity.class);
                        break;
                }

                // Закрытие меню после выбора пункта
                drawerLayout.closeDrawer(GravityCompat.END);
                startActivity(intent);
                return true;
            }
        });

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        TextView userSubsText = findViewById(R.id.subsCountTextView);
        TextView userNameText = findViewById(R.id.userName);
        TextView userFriendsText = findViewById(R.id.friendsCountTextView);
        TextView userScoreText = findViewById(R.id.userScore);
        TextView friendsListText = findViewById(R.id.friendsList);
        TextView subscriptionsListText = findViewById(R.id.subscriptionsList);

        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", this.MODE_PRIVATE);

        String savedName = sharedPreferences.getString("Name", "");
        userScore = sharedPreferences.getLong("Score", 0);
        userSubs = sharedPreferences.getLong("Subs", 0);
        userFriends = sharedPreferences.getLong("Friends", 0);

        userNameText.setText(savedName);
        userFriendsText.setText("" + userFriends);
        userScoreText.setText("" + userScore);
        userSubsText.setText("" + userSubs);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        //StorageReference imageRef = storageRef.child("users").child(mAuth.getCurrentUser().getUid()).child("UserAvatar");

        //mImageView = findViewById(R.id.image_view);
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //String userID = currentUser.getUid();

        //Грузим аватар из локальных файлов, если нет то стандартный
        File file = new File(this.getFilesDir(), "UserAvatar");
        if (file.exists()) {
            loadAvatarFromLocalFiles("UserAvatar", "/users/StandartUser/UserAvatar.png");
        }


        DocumentReference mAuthDocRef = db.collection("Users").document(currentUser.getUid());

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {

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
                        return time1.compareTo(time1);
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

                    ArrayList<String> people = (ArrayList<String>) achievement.get("like");

                    // Выводим данные достижения на экран
                    System.out.println("likes: " + likes);
                    System.out.println("url: " + url);

                    createImageBlock(url, likes, people, userName, key, achname, time);
                }

            } else {
                // документ не найден
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());

        System.out.println("value = " + value);

        System.out.println("value = " + value);


        profileImageView = findViewById(R.id.image_view);
        ImageButton selectImageButton = findViewById(R.id.button_choose_image);

        selectImageButton.setOnClickListener(v -> {

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
        });

        TextView scoreText = findViewById(R.id.scoreTextView);

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

        //ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        //ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        //ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        //ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);


        ImageButton leaderListButton = findViewById(R.id.imageButtonLeaderList);
        leaderListButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, LeaderBoardActivity.class);
            startActivity(intent);
        });

        ImageButton menuButton = findViewById(R.id.imageButtonMenu);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton achieveListButton = findViewById(R.id.imageButtonAchieveList);
        achieveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, AchieveListActivity.class);
            startActivity(intent);
        });

        ImageButton favoritesButton = findViewById(R.id.imageButtonFavorites);
        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, ListOfFavoritesActivity.class);
            startActivity(intent);
        });

        ImageButton usersListButton = findViewById(R.id.imageButtonUsersList);
        usersListButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, UsersListActivity.class);
            startActivity(intent);
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

                    uploadImageToStorage(bitmap, "UserAvatar");
                    saveAvatarToLocalFiles(bitmap, "UserAvatar");
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

    private void saveAvatarToLocalFiles(Bitmap bitmap, String name){
        //final long MAX_DOWNLOAD_SIZE = 1024 * 1024; // Максимальный размер файла для загрузки
        try {
            // Создание локального файла для сохранения изображения
            File file = new File(UserProfile.this.getFilesDir(), name);

            // Сохранение Bitmap в файл
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            // Обновление информации о пути к локальному файлу в приложении (если необходимо)

            // Уведомление об успешном сохранении
            Toast.makeText(UserProfile.this, "Аватар сохранен локально.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setImage(String imageRef) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef1 = storageRef.child(imageRef);

        ImageView userButton = findViewById(R.id.image_view);
        imageRef1.getMetadata().addOnSuccessListener(storageMetadata -> {
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
        });
    }

    private void loadAvatarFromLocalFiles(String fileName, String profileImageUrl) {
        ImageView userButton = findViewById(R.id.image_view);

        try {
            // Создание файла с указанным именем в локальной директории приложения
            File file = new File(this.getFilesDir(), fileName);

            // Чтение файла в виде Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            // Преобразование Bitmap в круговой вид (если необходимо)
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

            // Установка кругового Bitmap в качестве изображения для кнопки
            userButton.setImageBitmap(circleBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            setImage(profileImageUrl);
        }
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
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }
}
