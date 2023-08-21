package com.Bureau.Achivki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        ImageButton backButton = findViewById(R.id.BackButton);
        backButton.setOnClickListener(v -> {

            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();

        });

        Intent intentFromMain = getIntent();
        String achieveName = intentFromMain.getStringExtra("Achieve_key");
        String userToken = intentFromMain.getStringExtra("userToken");
        String url = intentFromMain.getStringExtra("url");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usersRef = db.collection("Users").document(currentUser.getUid());
        String userId = currentUser.getUid();

        Switch badPicture = findViewById(R.id.switch1);
        Switch badContent = findViewById(R.id.switch2);

        Button suggestButton = findViewById(R.id.sendReportButton);
        suggestButton.setOnClickListener(v -> {

            boolean isBadPictureEnabled = badPicture.isChecked();
            boolean isBadContentEnabled = badContent.isChecked();


            DocumentReference usersLogsRef = db.collection("UsersLogs").document("UserReports");
            usersLogsRef.get().addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> userAchievements = documentSnapshot.getData();
                if (userAchievements == null) {
                    // Если не существует, создаем новый документ
                    userAchievements = new HashMap<>();
                    userAchievements.put(userId, new HashMap<>());
                } else if (!userAchievements.containsKey(userId)) {
                    // Если Map achieve не существует, создаем его
                    userAchievements.put(userId, new HashMap<>());
                }

                // Получаем текущий Map achieve из документа пользователя
                Map<String, Object> achieveMap = (Map<String, Object>) userAchievements.get(userId);

                // Создаем новый Map с информацией о новом достижении
                Map<String, Object> newAchieveMap = new HashMap<>();
                newAchieveMap.put("name", achieveName);
                newAchieveMap.put("userToken", userToken);
                newAchieveMap.put("url", url);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                String time = sdf.format(calendar.getTime());
                newAchieveMap.put("time", time);
                //newAchieveMap.put("token", userID);

                newAchieveMap.put("BadPicture", isBadPictureEnabled);
                newAchieveMap.put("BadContent", isBadContentEnabled);


                // Добавляем новое достижение в Map achieve пользователя
                achieveMap.put(achieveName, newAchieveMap);

                // Сохраняем обновленный Map achieve в Firestore
                userAchievements.put(userId, achieveMap);
                usersLogsRef.set(userAchievements)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getApplicationContext(), "Жалоба успешно отправлена", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getApplicationContext(), "Ошибка при отправке жалобы", Toast.LENGTH_SHORT).show();
                        });

            });
        });
    }
}