package com.sparkycuber.stms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class LoginActivity extends AppCompatActivity {
    EditText txtEmail,txtUserName,txtPassword;
    Button btnRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtEmail= findViewById(R.id.email);
        txtUserName=findViewById(R.id.userName);
        txtPassword= findViewById(R.id.password);
        btnRegister= findViewById(R.id.register);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email= txtEmail.getText().toString().trim();
                String user= txtUserName.getText().toString().trim();
                String pass= txtPassword.getText().toString().trim();



                if( TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this,"please enter email",Toast.LENGTH_LONG).show();
                    return;
                }
                if( TextUtils.isEmpty(user)){
                    Toast.makeText(LoginActivity.this,"please enter username",Toast.LENGTH_LONG).show();
                    return;
                }
                if( TextUtils.isEmpty(pass)){
                    Toast.makeText(LoginActivity.this,"please enter password",Toast.LENGTH_LONG).show();
                    return;
                }

                if(pass.length()<8){
                    Toast.makeText(LoginActivity.this,"password too short",Toast.LENGTH_LONG).show();
                    return;

                }

                startActivity(new Intent(LoginActivity.this,MapsActivity.class));
                Toast.makeText(LoginActivity.this,"registration success",Toast.LENGTH_LONG).show();



                                }


                            });
                }
            }




