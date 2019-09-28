package com.salam.dropletjobtest.registerActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.salam.dropletjobtest.R;

import java.util.Objects;

/**
 * Location Permissions using third Party Dexter
 * provides information on how to manually grand permissions
 * Checks for self check permissions
 */
public class Location_Permissions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.LOC_PERMISSION));

        setContentView(R.layout.activity_location__permissions);
        Button btn_GrantPermission = findViewById(R.id.btn_Grant_Permission);

        //check if permissions are already granted, if granted then move to Location_activity
        if (ContextCompat.checkSelfPermission(Location_Permissions.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            startActivity(new Intent(Location_Permissions.this, Location_Activity.class));
            finish();
            return;
        }

        btn_GrantPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dexter library to efficiently gain permission in runtime
                Dexter.withActivity(Location_Permissions.this)
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new PermissionListener() {
                            /**
                             * @param response Permissions Granted Response
                             */
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                startActivity(new Intent(Location_Permissions.this, Location_Activity.class));
                                finish();
                            }

                            /**
                             * @param response Permission Denied Response
                             *    If Permission permanently denied then  open a dialog
                             * that will help user on how to enable permission for Location Manually
                             */
                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                if (response.isPermanentlyDenied()){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Location_Permissions.this);
                                    builder.setTitle(getString(R.string.P_DENIED))
                                            .setMessage(getString(R.string.P_D_INSTRUCTIONS))
                                            .setNegativeButton(getString(R.string.CANCEL), null)
                                            .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    intent.setData(Uri.fromParts(getString(R.string.PACKAGE), getPackageName(), null));
                                                }
                                            }).show();
                                } else {
                                    Toast.makeText(Location_Permissions.this, getString(R.string.P_DENIED), Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

    }
}
