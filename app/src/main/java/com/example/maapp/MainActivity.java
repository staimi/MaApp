package com.example.maapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    TextView loginTextView, textViewRegister;
    Button loginButton;
    boolean register;
    private FirebaseAuth mAuth;
    //private final String TAG = "EmailPassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        register = true;

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button_login);
        loginTextView = findViewById(R.id.textViewLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(register == true)
                loginFunction(view);
                else{
                    if(String.valueOf(emailEditText.getText()).length() != 0 && String.valueOf(passwordEditText.getText())!= null){
                        register(String.valueOf(emailEditText.getText()), String.valueOf(passwordEditText.getText()));
                    }else {
                        Toast.makeText(MainActivity.this, "Login or password empty", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if(mAuth.getCurrentUser() != null){
            login();
        }else{

        }

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setText("Register");
                register = false;
            }
        });

    }
    public void loginFunction(View view){
        String email = String.valueOf(emailEditText.getText());
        String password = String.valueOf(passwordEditText.getText());

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            login();
                            Log.i("onComplete ", "Successsful");
                        } else {
                            Toast.makeText(MainActivity.this, "Wrong password or login", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    void login(){
        startActivity(new Intent(this, userActivity.class));
    }

    //// sign in
    public void register(String email, String password){
        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            login();
                            Log.i("User", "created");
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}