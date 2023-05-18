package com.Bureau.Achivki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class StartActivity extends AppCompatActivity {

    private final String appVersion = "0.2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        TextView text1 = findViewById(R.id.textView1);
        TextView text2 = findViewById(R.id.textView2);


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

                }

            } else {
                // документ не найден
            }
        });

    }

    private boolean isUserLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    private void redirectToLoginScreen() {
        // Здесь перенаправление пользователя на экран авторизации.
        Intent intent = new Intent(StartActivity.this, LogInActivity.class);
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