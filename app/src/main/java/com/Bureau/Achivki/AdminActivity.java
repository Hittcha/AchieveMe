package com.Bureau.Achivki;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AdminActivity extends AppCompatActivity {

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

        leaderListUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Получение ссылки на коллекцию "Users" в базе данных
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference usersRef = db.collection("Users");

                DocumentReference leadersRef = db.collection("Leaders").document("IiSkYx3cqYvapeN6W8wc");

                // Создание запроса для сортировки пользователей по полю "score"
                Query scoreQuery = usersRef.orderBy("score", Query.Direction.DESCENDING).limit(6);

                // Получение отсортированных данных
                scoreQuery.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Обработка полученных данных
                        Map<String, Object> leaders = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Получение данных пользователя
                            String name = document.getString("name");
                            String profileImageUrl = document.getString("profileImageUrl");
                            String token = document.getId();
                            int score = Objects.requireNonNull(document.getLong("score")).intValue();

                            // Дальнейшая обработка данных...
                            System.out.println("name " + name + " profileImageUrl " + profileImageUrl + " token " + token + " score " + score);

                            Map<String, Object> newLeader = new HashMap<>();
                            newLeader.put("name", name);
                            newLeader.put("profileImageUrl", profileImageUrl);
                            newLeader.put("token", token);
                            newLeader.put("score", score);

                            leaders.put(name, newLeader);
                        }
                        addLeader(leadersRef, leaders);
                    } else {
                        // Обработка ошибок при получении данных
                        Toast.makeText(AdminActivity.this, "Что то пошло не так.", Toast.LENGTH_SHORT).show();
                    }
                });
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