package com.salam.dropletjobtest.registerActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.salam.dropletjobtest.R;

import java.util.HashMap;
import java.util.Objects;


/**
 * Register Activity recieved Address from Location activity
 * and rest of the data is inout by the user
 * performs validation checks
 * upload the data to Firebase
 */
public class Register extends AppCompatActivity {

    ProgressBar progressBar;
    RelativeLayout busy_layout;
    TextView tv_Please_wait;

    FirebaseAuth auth;
    DatabaseReference dbref;
    String address_EXTRA;

    /** onCreate method to create and start the activity
     * @param savedInstanceState saved instant state if saved
     * Initialise UI elements
     * Validation of TextFields
     * Button OnClickListner checks Edit Text for validation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.REGISTER_L));

        final EditText et_FullName = findViewById(R.id.et_Fullname);
        final EditText et_Email = findViewById(R.id.et_Email);
        final EditText et_CurrentLocation = findViewById(R.id.et_CurrentLocation);
        final EditText et_Password = findViewById(R.id.et_Password);
        final EditText et_About = findViewById(R.id.et_about);
        Button btn_Submit = findViewById(R.id.btn_submit);

        busy_layout =  findViewById(R.id.busy_layout);
        tv_Please_wait = findViewById(R.id.tv_wait);
        progressBar =  findViewById(R.id.progress_circular_register);

         //Get Address extra from Location Activity and store in String variable
        Intent get_Address_Extra = getIntent();
        address_EXTRA = get_Address_Extra.getStringExtra(getString(R.string.ADDRESS_KEY));
         //autofill location
        et_CurrentLocation.setText(address_EXTRA);

        auth = FirebaseAuth.getInstance();

        //Hiding visibility of progress bar UI, will be visible when background task will run to create profile
        progressBar.setVisibility(View.INVISIBLE);
        busy_layout.setVisibility(View.INVISIBLE);
        tv_Please_wait.setVisibility(View.INVISIBLE);

        //Click Listner to start Submit method in AsyncTask
        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validation Checks on EditTexts to make sure nothing is left empty
                if (et_FullName.getText().length() == 0)
                {   et_FullName.setError(getString(R.string.FULL_N_ERR));
                    et_FullName.requestFocus();
                    return;
                }
                if (et_About.getText().length() == 0)
                {   et_About.setError(getString(R.string.ABOUT_ERR));
                    et_About.requestFocus();
                    return;
                }
                if (et_CurrentLocation.getText().length() == 0)
                {   et_CurrentLocation.setError(getString(R.string.LOC_ERR));
                    et_CurrentLocation.requestFocus();
                    return;
                }
                //Email Verification with valid syntax, //Accepted Email Pattern abc123@abc.abc
                String email = et_Email.getText().toString().trim();
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (!email.matches(emailPattern))
                {   et_Email.setError(getString(R.string.EMAIL_ERR));
                    et_Email.requestFocus();
                    return;
                }
                if (et_Password.getText().length() == 0 || et_Password.getText().length() < 8)
                {   et_Password.setError(getString(R.string.PASS_ERR));
                    et_Password.requestFocus();
                    return; }

                //Pass params to asyncTask and execute asyncTask
                MyAsyncTask asynctask = new MyAsyncTask(et_FullName.getText().toString(),
                        et_Email.getText().toString(),et_Password.getText().toString()
                        ,et_About.getText().toString(),et_CurrentLocation.getText().toString());
                asynctask.execute();

                //Make progressbar and related UI visible, make the activity disabled while profile is created
                progressBar.setVisibility(View.VISIBLE);
                busy_layout.setVisibility(View.VISIBLE);
                tv_Please_wait.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                /* Register Method can be run in Main thread if needed but Asynctask can save app from ANR in case of network latency or when
                * database have a large number of entries
                *     register(txt_FullName.getText().toString(),txt_Email.getText().toString(),txt_Password.getText().toString()
                        ,txt_About.getText().toString(),txt_CurrentLocation.getText().toString());
                */

            }
        });
    }

    /**Register User to Firebase Storage
     *  @param fullName Full Name of the user.
     * @param eMail Email Address of User.
     * @param password Password set by the User.
     * @param about Bio of the user
     * @param currentLocation current Location, as recieved from getStringExtra from Location Activity
     *           check if email already exists or not
     */
    private void register(final String fullName,final String eMail, final String password, final String about, final String currentLocation) {

            //check if email already exists
            auth.fetchSignInMethodsForEmail(eMail).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    //if email dosnt already exist then make a new account
                    if (Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getSignInMethods()).size() == 0){
                        auth.createUserWithEmailAndPassword(eMail, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser firebaseUser = auth.getCurrentUser();
                                            assert firebaseUser != null;
                                            final String userid = firebaseUser.getUid();

                                            //write to database, fields created in database
                                            dbref = FirebaseDatabase.getInstance().getReference(getString(R.string.DB_USER)).child(userid);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put(getString(R.string.ID_R), userid);
                                            hashMap.put(getString(R.string.FULLNAME_R), fullName);
                                            hashMap.put(getString(R.string.ABOUT_R), about);
                                            hashMap.put(getString(R.string.CUR_ADR_R), currentLocation);
                                            hashMap.put(getString(R.string.IMG_URL), getString(R.string.DEFAULT));
                                            hashMap.put(getString(R.string.email_db), eMail );
                                            dbref.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        busy_layout.setVisibility(View.INVISIBLE);
                                                        tv_Please_wait.setVisibility(View.INVISIBLE);
                                                        Toast.makeText(Register.this, getString(R.string.PROFILE_CREATED), Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        busy_layout.setVisibility(View.INVISIBLE);
                                                        tv_Please_wait.setVisibility(View.INVISIBLE);
                                                        Toast.makeText(Register.this, "Something went Wrong, try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                    //email already exist, show error message in toast and enable activity
                    else {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        busy_layout.setVisibility(View.INVISIBLE);
                        tv_Please_wait.setVisibility(View.INVISIBLE);
                        Toast.makeText(Register.this, getString(R.string.emai_using), Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });

    }


    @SuppressLint("StaticFieldLeak")
    public class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        String fullName;
        String Email;
        String password;
        String about;
        String currentLocation;
        String email;

        /** MyAsyncTask is to create register details
         *  @param fullName Full Name of the user.
         * @param Email Email Address of User.
         * @param password Password set by the User.
         * @param about Bio of the user
         * @param currentLocation current Location of user
         *  */
         MyAsyncTask(final String fullName, final String Email,
                     final String password, final String about, final String currentLocation){
           this.fullName = fullName;
           this.Email = Email;
           this.password = password;
           this.about = about;
           this.currentLocation = currentLocation;
        }

        /**
         * `doInBackground` is run on a separate, UI will not be update
         * from this thread but will be only run the method to update the database
         */
        @Override
        protected Void doInBackground(Void... params) {
           register(fullName, Email, password, about, currentLocation);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}

