package com.Bureau.Achivki;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegistrationActivity extends AppCompatActivity {

    private Button registrationButton;

    private TextView emailEditText;
    private TextView passwordEditText;
    private TextView userNameEditText;

    private FirebaseDatabase database;
    private DatabaseReference ref;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registrationButton = findViewById(R.id.register_button);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        userNameEditText = findViewById(R.id.userName_edit_text);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        DatabaseReference achivRef = database.getReference("Achiv");

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty() || userNameEditText.getText().toString().isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        ref.child("Users").child(mAuth.getCurrentUser().getUid()).child("email").setValue(emailEditText.getText().toString());
                                        ref.child("Users").child(mAuth.getCurrentUser().getUid()).child("userName").setValue(userNameEditText.getText().toString());

                                        //ref.child("Users").child(mAuth.getCurrentUser().getUid()).child("Achiv");

                                        achivRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot achivSnapshot : dataSnapshot.getChildren()) {
                                                    String achivName = achivSnapshot.getKey().toString();
                                                    ref.child("Users").child(mAuth.getCurrentUser().getUid()).child("Achiv").child(achivName).setValue(false);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(RegistrationActivity.this, "Такой пользователь уже существует дюдя", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}