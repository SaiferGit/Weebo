package com.example.weebo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    // defining firebase services
    private FirebaseAuth mAuth;

    private Button loginButton, phoneloginbutton;
    private EditText userEmail, userPassword;
    private TextView needNewAccountLink, forgetPasswordlink;

    // for displaying progress dialog
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initializing firebase services
        mAuth = FirebaseAuth.getInstance();

        // defining variables with layouts
        InitializeFields();

        // if a user doesn't have an account then
        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        });
    }

    private void allowUserToLogin() {

        String email = userEmail.getText().toString(); // getting the text from email field and converting it into string
        String pass = userPassword.getText().toString();

        // if the fields are empty
        if(TextUtils.isEmpty(email) ){
            userEmail.requestFocus();
            userEmail.setError("This Field is required to sign into your account!");
            return;
        }

        else if(TextUtils.isEmpty(pass) ){
            userPassword.requestFocus();
            userPassword.setError("This Field is required to secure your account");
            return;
        }


        // if the requirements are fullfilled, let the user to login the system.
        else{

            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait,while we are signing into your Account..");
            loadingBar.setCanceledOnTouchOutside(true); // if the loading bar is appeared and user clicked on the screen, it wont' dissapair untill authentication
            loadingBar.show();


            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //if the user is authenticated successfully
                            if(task.isSuccessful())
                            {
                                sendUserToMainActivity();
                                Toast.makeText(LoginActivity.this,"Logged in Successful..", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }

                            // if any error occurred
                            else
                            {
                                String message = task.getException().toString(); // it will get the type of the error msg that has been occurred
                                Toast.makeText(LoginActivity.this,"Error: " + message, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }



    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);

        // user cant' go back to login activity once he reaches to main activity
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void InitializeFields() {
        loginButton = (Button) findViewById(R.id.login_button);
        phoneloginbutton = (Button) findViewById(R.id.phone_login_button);
        userEmail = (EditText) findViewById(R.id.login_email);
        userPassword = (EditText) findViewById(R.id.login_password);
        needNewAccountLink = (TextView) findViewById(R.id.need_new_account_link);
        forgetPasswordlink = (TextView) findViewById(R.id.forget_password_link);

        loadingBar = new ProgressDialog(this);
    }

}
