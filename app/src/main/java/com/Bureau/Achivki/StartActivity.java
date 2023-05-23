package com.Bureau.Achivki;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class StartActivity extends AppCompatActivity {

    private final String appVersion = "0.25";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.appBackGround));
        }

        FirebaseApp.initializeApp(this);

        TextView text1 = findViewById(R.id.textView1);
        TextView text2 = findViewById(R.id.textView2);
        TextView text3 = findViewById(R.id.textView3);


        boolean result = random50_50();
        if (result) {
            System.out.println("Выпало Орел");
            text1.setVisibility(View.VISIBLE);

        } else {
            System.out.println("Выпала Решка");
            text2.setVisibility(View.VISIBLE);
        }

        FirebaseFirestore db;

        db = FirebaseFirestore.getInstance();

        DocumentReference mAuthDocRef = db.collection("Manifest").document("version");

        mAuthDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String remoteConfigVersion = documentSnapshot.getString("name");

                //String versionDate = documentSnapshot.getString("date");


                assert remoteConfigVersion != null;
                if (remoteConfigVersion.equals(appVersion)){
                    // Проверяем авторизацию пользователя
                    if (!isUserLoggedIn()) {
                        redirectToLoginScreen();
                    }else{
                        redirectToMainScreen();
                    }
                }else{
                    text1.setVisibility(View.GONE);
                    text2.setVisibility(View.GONE);
                    text3.setVisibility(View.VISIBLE);
                }

            } else {
                // документ не найден
                Toast.makeText(StartActivity.this, "Не удалось установить соединение с базой данных", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isUserLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    private void redirectToLoginScreen() {
        // Здесь перенаправление пользователя на экран авторизации.
        Intent intent = new Intent(StartActivity.this, StartMainActivity.class);
        startActivity(intent);
    }
    private void redirectToMainScreen() {
        // Здесь перенаправление пользователя на главный экран.
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        // intent.putExtra("Category_key", name);
        startActivity(intent);
    }
    public static boolean random50_50() {
        Random random = new Random();
        return random.nextBoolean();
    }
}