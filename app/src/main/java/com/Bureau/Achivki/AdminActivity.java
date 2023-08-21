package com.Bureau.Achivki;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class AdminActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private static final String SOURCE_COLLECTION = "Users";
    private static final String TARGET_COLLECTION = "UsersLogs";
    private static final String TARGET_DOCUMENT = "UsersPosts";
    private static final String TARGET_MAP = "posts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.StatusBarColor));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setTitle("Админка");

        Button leaderListUpdate = findViewById(R.id.leaderListUpdate);
        Button loadImageButton = findViewById(R.id.load_image_button);

        Button confirmUserAchieveButton = findViewById(R.id.confirm_user_achieve_button);
        Button disproveUserAchieveButton = findViewById(R.id.disprove_user_achieve_button);

        Button updateUsersPostsButton = findViewById(R.id.updateUsersPostsButton);

        updateUsersPostsButton.setOnClickListener(v -> {
            try {
                updateUsersPosts();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        leaderListUpdate.setOnClickListener(v -> updateLeaderList());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersLogsRef = db.collection("UsersLogs");

        /*loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference mAuthDocRef = db.collection("UsersLogs").document("ProofList");

                mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();

                        for (Map.Entry<String, Object> entry : userData.entrySet()) {
                            String userId = entry.getKey();
                            Map<String, Object> achievements = (Map<String, Object>) entry.getValue();

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

                            for (Map.Entry<String, Object> achievementEntry : sortedAchievements) {
                                Map<String, Object> achievement = (Map<String, Object>) achievementEntry.getValue();
                                String key = achievementEntry.getKey();
                                System.out.println("key: " + key);

                                String url = (String) achievement.get("url");
                                String achname = (String) achievement.get("name");
                                String time = (String) achievement.get("time");

                                System.out.println("url: " + url);

                                loadProof(url);

                            }
                        }

                    } else {
                        // document does not exist
                    }
                });
            }
        });*/

        loadImageButton.setOnClickListener(new View.OnClickListener() {
            private List<Map.Entry<String, Object>> sortedAchievements;
            private int currentIndex = 0;

            @Override
            public void onClick(View v) {

                confirmUserAchieveButton.setVisibility(View.VISIBLE);
                disproveUserAchieveButton.setVisibility(View.VISIBLE);

                DocumentReference mAuthDocRef = db.collection("UsersLogs").document("ProofList");

                mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();

                        for (Map.Entry<String, Object> entry : userData.entrySet()) {
                            String userId = entry.getKey();
                            Map<String, Object> achievements = (Map<String, Object>) entry.getValue();

                            sortedAchievements = new ArrayList<>(achievements.entrySet());

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

                            if (currentIndex < sortedAchievements.size()) {
                                Map.Entry<String, Object> achievementEntry = sortedAchievements.get(currentIndex);
                                Map<String, Object> achievement = (Map<String, Object>) achievementEntry.getValue();
                                String key = achievementEntry.getKey();
                                System.out.println("key: " + key);

                                String url = (String) achievement.get("url");
                                String achname = (String) achievement.get("name");
                                long achievePrice = (long) achievement.get("achievePrice");
                                String time = (String) achievement.get("time");
                                String token = (String) achievement.get("token");

                                Boolean collectable = (Boolean) achievement.get("collectable");
                                if(collectable==null){
                                    collectable = false;
                                }

                                /*if (achievement.contains("collectable")) {
                                    collectable = Boolean.TRUE.equals(achievement.getBoolean("collectable"));
                                    System.out.println("price " + achievePrice);
                                }*/

                                System.out.println("collectable: " + collectable);

                                System.out.println("url: " + url);

                                loadProof(url);

                                //currentIndex++;

                                confirmUserAchieveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        confirmUserAchieve(token, achname, achievePrice, key);

                                        //sortedAchievements.remove(currentIndex);
                                        achievements.remove(key);
                                        userData.put(userId, achievements);
                                        mAuthDocRef.set(userData);

                                        confirmUserAchieveButton.setVisibility(View.GONE);
                                        disproveUserAchieveButton.setVisibility(View.GONE);
                                    }
                                });

                                Boolean finalCollectable = collectable;
                                disproveUserAchieveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        disproofUserAchieve(token, achname, finalCollectable, key);
                                        //sortedAchievements.remove(currentIndex);
                                        achievements.remove(key);
                                        userData.put(userId, achievements);
                                        mAuthDocRef.set(userData);

                                        confirmUserAchieveButton.setVisibility(View.GONE);
                                        disproveUserAchieveButton.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }else{
                        // document does not exist
                    }
                });
            }
        });
    }

    private void disproofUserAchieve(String token, String achieveName, boolean collectable, String key){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(token);


        usersRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userAchievements = documentSnapshot.getData();
            if (userAchievements == null) {
                // Если не существует, создаем новый документ
                userAchievements = new HashMap<>();
                userAchievements.put("userAchievements", new HashMap<>());
            } else if (!userAchievements.containsKey("userAchievements")) {
                // Если Map achieve не существует, создаем его
                userAchievements.put("userAchievements", new HashMap<>());
            }

            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userAchievements");
            Map<String, Object> photosMap = (Map<String, Object>) userAchievements.get("userPhotos");

            System.out.println("achieveName " + key);

            if (photosMap.containsKey(key)) {
                Map<String, Object> photosMap1 = (Map<String, Object>) photosMap.get(key);
                System.out.println("status ");
                String status = (String) photosMap1.get("status");
                photosMap1.put("status", "red");
                System.out.println("status " + status);
            }

            if (collectable){
                if (achieveMap.containsKey(achieveName)) {
                    Map<String, Object> existingAchieveMap = (Map<String, Object>) achieveMap.get(achieveName);
                    long doneCount = (long) existingAchieveMap.get("doneCount");
                    long dayDone = (long) existingAchieveMap.get("dayDone");
                    existingAchieveMap.put("doneCount", doneCount - 1);
                    existingAchieveMap.put("dayDone", dayDone - 1);
                    existingAchieveMap.remove("url" + doneCount);
                }
                /*long doneCount = (long) achieveMap.get("doneCount");
                achieveMap.put("doneCount", doneCount - 1);*/
            }else{
                achieveMap.remove(achieveName);
            }

            //achieveMap.remove(achieveName);

            // Сохраняем обновленный Map achieve в Firestore
            userAchievements.put("userAchievements", achieveMap);
            usersRef.set(userAchievements);
            Toast.makeText(AdminActivity.this, "Достижение удалено", Toast.LENGTH_SHORT).show();
            //showPublicButton();
        });
    }

    private void confirmUserAchieve(String token, String achieveName, long achievePrice, String key){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(token);

        usersRef.update("score", FieldValue.increment(achievePrice));


        usersRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userAchievements = documentSnapshot.getData();
            if (userAchievements == null) {
                // Если не существует, создаем новый документ
                userAchievements = new HashMap<>();
                userAchievements.put("userAchievements", new HashMap<>());
            } else if (!userAchievements.containsKey("userAchievements")) {
                // Если Map achieve не существует, создаем его
                userAchievements.put("userAchievements", new HashMap<>());
            }

            // Получаем текущий Map achieve из документа пользователя
            Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get("userAchievements");

            Map<String, Object> photosMap = (Map<String, Object>) userAchievements.get("userPhotos");

            System.out.println("achieveName " + key);

            if (photosMap.containsKey(key)) {
                Map<String, Object> photosMap1 = (Map<String, Object>) photosMap.get(key);
                System.out.println("status ");
                String status = (String) photosMap1.get("status");
                photosMap1.put("status", "green");
                System.out.println("status " + status);
            }

            // Изменяем значение confirmed для достижения с определенным именем
            for (Map.Entry<String, Object> entry : achieveMap.entrySet()) {
                String achieveKey = entry.getKey();
                Map<String, Object> achieveData = (Map<String, Object>) entry.getValue();
                if (achieveData.containsKey("name") && achieveData.get("name").equals(achieveName)) {
                    achieveData.put("confirmed", true);
                    break;  // Если вы хотите остановиться после первого соответствия
                }
            }
            //Long userScore = documentSnapshot.getLong("score");
            // Сохраняем обновленный Map achieve в Firestore
            userAchievements.put("userAchievements", achieveMap);
            usersRef.set(userAchievements);
            Toast.makeText(AdminActivity.this, "Достижение добавлено на проверку", Toast.LENGTH_SHORT).show();
            //showPublicButton();
        });
    }
    private void loadProof(String url){
        ImageView imageView = findViewById(R.id.imageView4);
        //firestore = FirebaseFirestore.getInstance();

        // Загрузка изображения в Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(url);
        // Получение URL изображения
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // URL изображения
            String imageUrl = uri.toString();

            // Отображение изображения с использованием Picasso
            //ImageView imageView = findViewById(R.id.imageView3);
            Picasso.get()
                    .load(imageUrl)
                    .into(imageView);
            imageView.setAdjustViewBounds(true);
        }).addOnFailureListener(e -> {
            // Обработка ошибки загрузки изображения
        });
    }
    private void updateLeaderList(){

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference sourceCollection = firestore.collection(SOURCE_COLLECTION);
        CollectionReference targetCollection = firestore.collection(TARGET_COLLECTION);

        sourceCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            String userId = documentSnapshot.getId();
                            if (documentSnapshot.contains("userPhotos")) {
                                // Get userPhotos map
                                Map<String, Object> userPhotos = (Map<String, Object>) documentSnapshot.get("userPhotos");
                                if (userPhotos != null && !userPhotos.isEmpty()) {
                                    // Save userPhotos in target collection
                                    Map<String, Object> targetMap = new HashMap<>();
                                    targetMap.put(TARGET_MAP, userPhotos);
                                    targetCollection.document(TARGET_DOCUMENT)
                                            .set(targetMap, /* SetOptions.merge() */ SetOptions.mergeFields(TARGET_MAP))
                                            .addOnSuccessListener(documentReference ->
                                                    System.out.println("User photos migrated successfully for user: " + userId))
                                            .addOnFailureListener(e ->
                                                    System.err.println("Failed to migrate user photos for user: " + userId));
                                }
                            }
                        }
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        System.err.println("Error retrieving documents: " + exception.getMessage());
                    }
                }
            }
        });
    }

    private void updateUsersPosts() throws ExecutionException, InterruptedException {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference sourceCollection = firestore.collection(SOURCE_COLLECTION);
        CollectionReference targetCollection = firestore.collection(TARGET_COLLECTION);

        sourceCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            String userId = documentSnapshot.getId();
                            if (documentSnapshot.contains("userPhotos")) {
                                // Get userPhotos map
                                Map<String, Object> userPhotos = (Map<String, Object>) documentSnapshot.get("userPhotos");
                                if (userPhotos != null && !userPhotos.isEmpty()) {
                                    for (Map.Entry<String, Object> entry : userPhotos.entrySet()) {
                                        String mapName = entry.getKey();
                                        Map<String, Object> photoMap = (Map<String, Object>) entry.getValue();

                                        // Add the "token" field to each photoMap
                                        photoMap.put("token", userId);

                                        String fullMapName = userId + "_" + mapName;
                                        Map<String, Object> targetMap = new HashMap<>();
                                        targetMap.put(fullMapName, photoMap);
                                        targetCollection.document(TARGET_DOCUMENT)
                                                .set(targetMap, SetOptions.merge())
                                                .addOnSuccessListener(documentReference ->
                                                        System.out.println("User photos migrated successfully for user: " + userId + ", map: " + fullMapName))
                                                .addOnFailureListener(e ->
                                                        System.err.println("Failed to migrate user photos for user: " + userId + ", map: " + fullMapName));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        System.err.println("Error retrieving documents: " + exception.getMessage());
                    }
                }
            }
        });



    }

    private static void addLeader(DocumentReference leadersRef, Map<String, Object> Leaders) {
        // Создание нового объекта лидера

        leadersRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> leaders = documentSnapshot.getData();
            if (leaders != null) {
                leaders.put("globalLeaders", Leaders);
            }
            if (leaders != null) {
                leadersRef.set(leaders);
            }
        });
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}