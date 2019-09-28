package com.salam.dropletjobtest.homeScreenActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.salam.dropletjobtest.R;
import com.salam.dropletjobtest.user_Model.UsersModel;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

/**
 * EditProfile Loads profile data into EditTexts
 * User can edit the data
 * once update button is clicked
 * Firebase data is updated
 */
public class EditProfile extends AppCompatActivity {

    EditText et_Fullname, et_Current_Location, et_About;
    ImageView img_profileDP_editMode;

    //image URI to be default as initial, will be changed once recieved
    String img_URI = "default";

    //to be used for choosing and uploading image
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    //Firebase initialise
    FirebaseUser fUser;
    DatabaseReference reference;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        et_Fullname = findViewById(R.id.et_Fullname_EditMode);
        et_Current_Location = findViewById(R.id.et_CurrentLocation_EditMode);
        et_About = findViewById(R.id.et_about_EditMode);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.DB_REF_USR)).child(fUser.getUid());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final Button btn_update = findViewById(R.id.btn_update);
        img_profileDP_editMode = findViewById(R.id.img_profile_dp_editMode);
        img_profileDP_editMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //Value even Listner to update EditText with Firebase Data, Listens to value changes in Firebase data
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UsersModel userData = dataSnapshot.getValue(UsersModel.class);
                assert userData != null;
                et_Fullname.setText(userData.getFullName());
                et_About.setText(userData.getAbout());
                et_Current_Location.setText(userData.getCurrentAddress());
                img_URI = userData.getImageURL();
                if (userData.getImageURL().equals("default"))
                {
                    img_profileDP_editMode.setBackgroundResource(R.drawable.ic_launcher_background);
                }else {
                    Picasso.get().load(img_URI).into(img_profileDP_editMode);
                }

                Picasso.get().load(img_URI).into(img_profileDP_editMode);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        //new hashmap that needs to be updated in the Database
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_Fullname.getText().length() == 0)
                {   et_Fullname.setError(getString(R.string.FULL_N_ERR));
                    et_Fullname.requestFocus();
                    return;
                }
                if (et_About.getText().length() == 0)
                {   et_About.setError(getString(R.string.ABOUT_ERR));
                    et_About.requestFocus();
                    return;
                }
                if (et_Current_Location.getText().length() == 0)
                {   et_Current_Location.setError(getString(R.string.LOC_ERR));
                    et_Current_Location.requestFocus();
                    return;
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put(getString(R.string.IMG_URL), img_URI);
                map.put(getString(R.string.FULLNAME_R), et_Fullname.getText().toString());
                map.put(getString(R.string.CUR_ADR_R), et_Current_Location.getText().toString());
                map.put(getString(R.string.ABOUT_R), et_About.getText().toString());
                reference.updateChildren(map);
                finish();
            }
        });
    }

    /**
     * chooseImage() select image to be uploaded
     * opens imape picker
     */
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.SELECT_PIC)), PICK_IMAGE_REQUEST);
    }


    /**
     *
     * @param requestCode request code from image picker, type of request
     * @param resultCode result code , if results are ok or now
     * @param data image file that was selected
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                img_profileDP_editMode.setImageBitmap(bitmap);
                uploadImage();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * uploadImage the file on to Firebase Storage
     * Handles progressbar
     */
    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.UPLOADING));
            progressDialog.setMessage(getString(R.string.WAIT_P));
            progressDialog.show();
            final StorageReference ref = storageReference.child("images/"+ fUser.getUid());

            ref.putFile(filePath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                /**
                 *
                 * @param task uploadTask continue or not
                 * @return imageURL to be stored in Database
                 *
                 */
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(EditProfile.this, getString(R.string.UPLOADED), Toast.LENGTH_SHORT).show();
                        throw Objects.requireNonNull(task.getException());
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downUri = task.getResult();
                        assert downUri != null;
                        img_URI = downUri.toString();
                        progressDialog.dismiss();
                        Toast.makeText(EditProfile.this, getString(R.string.upload_pro), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfile.this, getString(R.string.FAILED)+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }



}
