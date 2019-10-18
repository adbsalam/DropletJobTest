package com.salam.dropletjobtest;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.salam.dropletjobtest.homeScreenActivity.HomeScreen;
import com.salam.dropletjobtest.loginActivity.Login;
import com.salam.dropletjobtest.registerActivity.Location_Permissions;
import java.util.Objects;

/**
 * @Author Muhammad AbdulSalam
 * Login App for Droplet Test
 * Uploading Project on Saturday 28 sep 2019 1300hours
 */


/**
 * MainActivity helps checking existing Firebase User
 * if there is an existing user , Main activity will automatically
 * Login the user
 * Otherwise usre can select to login or Register
 */
public class MainActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference dbref;

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("mathUtility");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();



        //Initialising declared elements
        Button btn_login = findViewById(R.id.btn_login);
        Button btn_register = findViewById(R.id.btn_register);
        ImageView img_Logo = findViewById(R.id.img_logo);
        TextView textView = findViewById(R.id.tv_cpp);

        textView.setText(stringFromJNI());

        //animations
        img_Logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        btn_login.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
        btn_register.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));

        //btn_login Listner, move to Login Activity
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivity = new Intent(MainActivity.this, Login.class);
                startActivity(loginActivity);
            }
        });

        //btn_register Listner , go to Location Permissions before moving to Location Activity
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(MainActivity.this, Location_Permissions.class);
                startActivity(registerActivity);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //check for already logged in User, if yes then move to Home activity
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        dbref = FirebaseDatabase.getInstance().getReference(getString(R.string.DB_USER));
        if (firebaseUser !=null){
            Intent registerActivity = new Intent(MainActivity.this, HomeScreen.class);
            startActivity(registerActivity);
            finish();
        }
    }

    public native String stringFromJNI();


}
