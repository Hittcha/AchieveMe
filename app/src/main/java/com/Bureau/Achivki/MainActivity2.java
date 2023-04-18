package com.Bureau.Achivki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kotlin.text.UStringsKt;


public class MainActivity2 extends AppCompatActivity {

    public boolean coco;

   // private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private String text;
    private TextView welcomeMessage;

    private Button backButton;

    public int value1 = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        backButton = findViewById(R.id.Backbutton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Achiv");

       /* myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int value = dataSnapshot.child("count").getValue(Integer.class);

                for (int i = 1; i <= value; i++) {
                    // Создаем новую кнопку и считываем название ачивки
                    String name = dataSnapshot.child("name/"+i).getValue().toString();
                    Button button = new Button(MainActivity2.this);
                    button.setText(name);

                    // Устанавливаем параметры макета кнопки
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    button.setLayoutParams(layoutParams);

                    // Устанавливаем слушатель нажатия кнопки
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Обработка нажатия кнопки
                            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });

                    // Добавляем кнопку в макет
                    LinearLayout layout = findViewById(R.id.LinLayout);
                    layout.addView(button);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Обработка ошибки
            }
        });*/


        DatabaseReference buttonsRef = database.getReference("Achiv");

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("Achiv");

        // здесь "buttons" - название узла в Firebase базе данных

       // System.out.println(ref.child("coco").get);
       // ref.getKey();
       /* ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ac = snapshot.child("coco").getValue().toString();
                System.out.println(ac);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
*/
        buttonsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot buttonSnapshot : dataSnapshot.getChildren()) {

                    String buttonName = buttonSnapshot.getKey().toString();
                    //String re = ref.getKey();
                    String key = dataSnapshot.getChildren().toString();
                    //System.out.println(key);
                    System.out.println(dataSnapshot.child(buttonName+"/id").getValue());

                    //long id = (long) dataSnapshot.child(buttonName+"/id").getValue();
                   // System.out.println(id);

                    String id = dataSnapshot.child(buttonName+"/id").getValue().toString();

                    Button button = new Button(MainActivity2.this);
                    button.setText(buttonName);
                    button.setBackgroundColor(Color.BLUE);

                    // здесь можно добавить дополнительные параметры для кнопки, например, размеры, цвет, обработчик нажатия и т.д.

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutParams.setMargins(20, 20, 20, 20);
                    button.setBackgroundColor(Color.GREEN);

                    button.setLayoutParams(layoutParams);

                    button.setTag(buttonName);


                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String ac = snapshot.child(buttonName).getValue().toString();

                            boolean test = (boolean) snapshot.child(buttonName).getValue();

                            System.out.println(ac);

                            for (DataSnapshot buttonSnapshot : snapshot.getChildren()) {

                                if (test == false){
                                    button.setBackgroundColor(Color.RED);
                                }
                                else{
                                    button.setBackgroundColor(Color.GREEN);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Обработка нажатия кнопки
                            Intent intent = new Intent(MainActivity2.this, AchievementDescriptionActivity.class);
                            intent.putExtra("data_key", buttonName);
                            startActivity(intent);
                        }
                    });

                    LinearLayout scrollView = findViewById(R.id.scrollView1);
                    scrollView.addView(button);
                    //LinearLayout layout = findViewById(R.id.LinLayout);
                    //layout.addView(button); // добавляем кнопку на экран мобильного приложения
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }
}