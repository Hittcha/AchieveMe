package com.Bureau.Achivki;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AchievementDescriptionActivity extends AppCompatActivity {

    private TextView descMessage;

    private Button addButton;

    private Button delButton;

    private Button backButton;

    private DatabaseReference ref;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_description);

        descMessage = findViewById(R.id.desc_message);

        Intent intent = getIntent();
        String data = intent.getStringExtra("data_key");

        System.out.println(data);

        // Подключаемся к базе данных Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Получаем ссылку на узел с текстом
        DatabaseReference myRef = database.getReference("Achiv/" + data);

        // Получаем значение этого узла
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 3. Получаем значение узла
                String text = dataSnapshot.child("desc").getValue().toString();

                // 4. Выводим значение на экран
                System.out.println(text);

                descMessage.setText(text);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Обработка ошибок
                System.out.println("Failed to read value." + error.toException());
            }
        });

        mAuth = FirebaseAuth.getInstance();

        ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("Achiv");


        addButton = findViewById(R.id.submit_button);

        delButton = findViewById(R.id.delete_button);

        backButton = findViewById(R.id.Backbutton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child(data).setValue(true);
            }
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child(data).setValue(false);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchievementDescriptionActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });
    }
}