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
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton, verifyButton;
    private EditText inputPhoneNumber, inputVerificationCode;
    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        initializeFields();
        loadingBar = new ProgressDialog(this);

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = inputPhoneNumber.getText().toString(); // getting the phone number

                // if the field are empty
                if(TextUtils.isEmpty(phoneNumber) ){
                    inputPhoneNumber.requestFocus();
                    inputPhoneNumber.setError("Please enter your phone number first..");
                    return;
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, while we are authenticating your phone..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerificationCode.getText().toString();

                // if the fields are empty
                if(TextUtils.isEmpty(verificationCode) ){
                    inputVerificationCode.requestFocus();
                    inputVerificationCode.setError("Please write the verification code first");
                    return;
                }
                else
                {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, while we are verifying your verification code..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                // if verification is successful, call this method

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                // if the user provide a invalid phone number or wrong verification code
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, please enter correct phone number with your country code..", Toast.LENGTH_LONG).show();

                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);


            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);


                // this method will be called whenever the code is sent to the user
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent to your phone number", Toast.LENGTH_SHORT).show();
            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // provide the currect code and ready to signin with phone
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations, you're logged in successfully...", Toast.LENGTH_LONG).show();

                            sendUserToMainActivity();
                        }
                        else
                        {
                            String msg = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);

        // user cant' go back to login activity once he reaches to main activity
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);
        finish();
    }

    private void initializeFields() {
        sendVerificationCodeButton = (Button) findViewById(R.id.phone_send_ver_code_button);
        verifyButton = (Button) findViewById(R.id.verify_button);
        inputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        inputVerificationCode = (EditText) findViewById(R.id.phone_verification_code_input);

    }
}
