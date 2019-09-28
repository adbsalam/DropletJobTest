package com.salam.dropletjobtest.homeScreenActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.salam.dropletjobtest.MainActivity;
import com.salam.dropletjobtest.R;
import com.salam.dropletjobtest.user_Model.UsersModel;
import com.squareup.picasso.Picasso;


/**
 * Home Screen shows the user Data
 * Loads pictures and data into designated fields
 * Updates Navigation Drawer
 */
public class HomeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //image uri to load into ImageView, will be changed once received
    String img_URI = "default";

    //Firebase Storage
    FirebaseStorage storage;
    StorageReference storageReference;

    //Firebase user and database
    FirebaseUser fuser;
    DatabaseReference reference;

    /**
     * @param savedInstanceState Create new if savedinstantstate is null
     *Loads data into fields
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);


        final ImageView img_Profile_dp = findViewById(R.id.img_profile_dp);
        final TextView tv_fullName =  findViewById(R.id.tv_Fullname);
        final TextView tv_Current_Location = findViewById(R.id.tv_CurrentLocation);
        final TextView tv_about = findViewById(R.id.tv_about);

        //toolbar and navigation drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //Loading data into navigation drawer
        View headerView = navigationView.getHeaderView(0);
        final TextView full_Name_Nav = headerView.findViewById(R.id.nav_name);
        final TextView email_nav = headerView.findViewById(R.id.nav_email);
        final ImageView nav_DP = headerView.findViewById(R.id.nav_imageView);

        //Firebase initialisations
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.DB_REF_USR)).child(fuser.getUid());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Load data into EditText so user can see what existing data is
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Load data from firebase into user model and create an object
                UsersModel userData = dataSnapshot.getValue(UsersModel.class);
                assert userData != null;
                tv_fullName.setText(userData.getFullName());
                tv_about.setText(userData.getAbout());
                tv_Current_Location.setText(userData.getCurrentAddress());
                img_URI = userData.getImageURL();
                full_Name_Nav.setText(userData.getFullName());
                email_nav.setText(fuser.getEmail());
                if (userData.getImageURL().equals("default"))
                {
                    img_Profile_dp.setBackgroundResource(R.drawable.ic_launcher_background);
                    nav_DP.setBackgroundResource(R.drawable.ic_launcher_background);
                }else {
                    Picasso.get().load(img_URI).into(nav_DP);
                    Picasso.get().load(img_URI).into(img_Profile_dp);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
         Intent edit_Profile = new Intent(HomeScreen.this, EditProfile.class);
         startActivity(edit_Profile);
            return true;
        }
        if (id == R.id.action_log_out){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeScreen.this, MainActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // Handle the camera action
        }  else if (id == R.id.nav_edit_profile) {
            Intent edit_Profile = new Intent(HomeScreen.this, EditProfile.class);
            startActivity(edit_Profile);
        } else if (id == R.id.nav_log_out) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeScreen.this, MainActivity.class));
                finish();
                return true;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
