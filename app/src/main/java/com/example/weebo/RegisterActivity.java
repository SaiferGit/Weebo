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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText userEmail, userPassword, userConfirmPassword;
    private TextView alreadyHaveAccountLink;

    // for firebase authentication
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef; // for referencing firebase database root

    // for displaying progress dialog
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // initializing firebase services
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference(); // referencing the firebase db, for our case it is referencing weebo-b58fc

        //defining variables with layouts
        InitializeFields();

        // if a user has an account already
        alreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        // creating a new account
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString(); // getting the text from email field and converting it into string
        String pass = userPassword.getText().toString();
        String confirmpass = userConfirmPassword.getText().toString();

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

        else if (TextUtils.isEmpty(confirmpass) ){
            userConfirmPassword.requestFocus();
            userConfirmPassword.setError("This Field is required to save your password!");
            return;

        }

        else if(!pass.equals(confirmpass)){
            userConfirmPassword.requestFocus();
            userConfirmPassword.setError("Your Passwords don't match!");
            return;

        }
        // if the requirements are fullfilled, let the user to login the system.
        else{

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait,while we are creating your new Account..");
            loadingBar.setCanceledOnTouchOutside(true); // if the loading bar is appeared and user clicked on the screen, it wont' dissapair untill authentication
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //if the user is authenticated successfully
                            if(task.isSuccessful())
                            {
                                String deviceToken = FirebaseInstanceId.getInstance().getToken(); // getting device token



                                //if the task is successful then store the mail and pass
                                String currenUserID = mAuth.getCurrentUser().getUid();
                                rootRef.child("Users").child(currenUserID).setValue("");

                                // storing device token during registration
                                rootRef.child("Users").child(currenUserID).child("device_token")
                                        .setValue(deviceToken);

                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"Account Created Successfully..", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                            // if any error occurred
                            else
                            {
                                String message = task.getException().toString(); // it will get the type of the error msg that has been occurred
                                Toast.makeText(RegisterActivity.this,"Error: " + message, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        // user cant' go back to register activity once he reaches to main activity
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeFields() {
        createAccountButton = (Button) findViewById(R.id.register_button);
        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        alreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);
        userConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);

        loadingBar = new ProgressDialog(this);
    }
}
