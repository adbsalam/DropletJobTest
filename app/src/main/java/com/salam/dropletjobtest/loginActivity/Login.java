package com.salam.dropletjobtest.loginActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.salam.dropletjobtest.R;
import com.salam.dropletjobtest.homeScreenActivity.HomeScreen;
import java.util.Objects;

/**
 * Login Activity Takes input as Email and Password
 * Check for the user on Firebase
 * Logs in the User and starts Home Activity
 */
public class Login extends AppCompatActivity {

    EditText et_Email, et_Password;
    Button btn_Login;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.LOGIN_L));


        et_Email = findViewById(R.id.txt_login_Email);
        et_Password = findViewById(R.id.txt_password_login);
        btn_Login = findViewById(R.id.btn_log_in);
        auth = FirebaseAuth.getInstance();
        //animations
        et_Email.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
        et_Password.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        btn_Login.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));


        btn_Login.setOnClickListener(new View.OnClickListener() {
            /**
             * @param v View
             *  EditText Validations
             *  Email authentication to create Firebase Login credentials
             */
            @Override
            public void onClick(View v) {

                String email = et_Email.getText().toString().trim();
                //Email Verification with valid syntax as abc123@abc.abc
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (!email.matches(emailPattern))
                {   et_Email.setError(getString(R.string.FORMAT_EMAIL_ERR));
                    et_Email.requestFocus();
                    return;
                }
                if (et_Password.getText().length() == 0 || et_Password.getText().length() < 8)
                {   et_Password.setError(getString(R.string.PASS_ERR_L));
                    et_Password.requestFocus();
                    return;
                }

                auth.signInWithEmailAndPassword(et_Email.getText().toString(), et_Password.getText().toString())
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified
                                if (!task.isSuccessful()) {
                                    // there was an error
                                        Toast.makeText(Login.this, getString(R.string.AUTH_FAIL_ERR), Toast.LENGTH_LONG).show();

                                } else {
                                    Intent intent = new Intent(Login.this, HomeScreen.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }


}
