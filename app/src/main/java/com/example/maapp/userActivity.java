package com.example.maapp;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.sip.SipSession;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.*;
import com.example.maapp.ui.home.HomeFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageButton;
import pl.droidsonroids.gif.GifImageView;

import static android.Manifest.permission_group.CAMERA;
import static com.example.maapp.R.drawable.blank_profile_picture;

public class userActivity extends AppCompatActivity {

    EditText editTextuserName, editTextuserSecondName, editTextuserDescription, editTextuserPhoneNumber;
    ImageView profilePicture;
    ImageButton switchToHomeActivity;
    private DatabaseReference mDatabase;
    private static final int RESULT_LOAD_IMAGE = 1;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent intent = new Intent(this, navigation_drawer.class);

        switchToHomeActivity = findViewById(R.id.switchToHome);

        editTextuserDescription = findViewById(R.id.editTextUserDescription);
        editTextuserName = findViewById(R.id.editTextTextPersonName);
        editTextuserSecondName = findViewById(R.id.editTextTextPersonSecondName);
        editTextuserPhoneNumber = findViewById(R.id.editTextPhoneNumber);

        editTextuserDescription.addTextChangedListener(new TextChange(editTextuserDescription));
        editTextuserName.addTextChangedListener(new TextChange(editTextuserName));
        editTextuserSecondName.addTextChangedListener(new TextChange(editTextuserSecondName));
        editTextuserPhoneNumber.addTextChangedListener(new TextChange(editTextuserPhoneNumber));

        profilePicture = findViewById(R.id.imageViewProfilePicture);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(userActivity.this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        new download().execute("2");

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromAlbum();
            }
        });

        switchToHomeActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });

    }


    private void getImageFromAlbum() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
                } catch (Exception exp) {
                    Log.i("Error", exp.toString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                profilePicture.setImageBitmap(null);
                profilePicture.setImageBitmap(selectedImage);
                saveProfilePicture(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(userActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(userActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    public void saveProfilePicture(Uri uri) {

        String userID = currentUser.getUid();
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        try {
            StorageReference storageReference = storage.getReference().child("images/users/" + userID + ".jpg");
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.cancel();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.cancel();
                    Toast.makeText(userActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadAvatarFromFirebase() {
                try {
                    profilePicture.setImageBitmap(null);
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

                    StorageReference img = storageReference.child("images/users/" + currentUser.getUid().toString() + ".jpg");

                    final long ONE_MEGABYTE = 1024 * 1024;
                    img.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            profilePicture.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }


    public void readFirebaseData() {

        mDatabase.child("usersData").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String textViewDescription = "";
                String textViewName = "";
                String textViewSecondName = "";
                String textViewPhoneNumber = "";

                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    showAlertDialogButtonClicked();
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));

                    /// data to split
                    String dataFromFirebase = String.valueOf(task.getResult().getValue());

                    /// patterns
                    Pattern description = Pattern.compile("description=(.*?)x8Nm2O");
                    Pattern name = Pattern.compile("userName=(.*?)x8Nm2O");
                    Pattern secondName = Pattern.compile("secondName=(.*?)x8Nm2O");
                    Pattern phoneNumber = Pattern.compile("phoneNumber=(.*?)x8Nm2O");

                    /// tool to split the string
                    Matcher mDescription = description.matcher(dataFromFirebase);
                    Matcher mName = name.matcher(dataFromFirebase);
                    Matcher mSecondName = secondName.matcher(dataFromFirebase);
                    Matcher mPhoneNumber = phoneNumber.matcher(dataFromFirebase);

                    while (mSecondName.find()){
                        textViewSecondName+=mSecondName.group(1);
                    }

                    while (mPhoneNumber.find()){
                        textViewPhoneNumber+=mPhoneNumber.group(1);
                    }

                    while (mDescription.find()){
                        textViewDescription+=mDescription.group(1);
                    }

                    while (mName.find()){
                        textViewName+=mName.group(1);
                    }

                    editTextuserDescription.setText(textViewDescription);
                    editTextuserName.setText(textViewName);
                    editTextuserSecondName.setText(textViewSecondName);
                    editTextuserPhoneNumber.setText(textViewPhoneNumber);
                    ////////////////////
                }
            }
        });
    }


    private class TextChange implements TextWatcher {
        View view;
        private TextChange (View v) {
            view = v;
        }//end constructor

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d("Editable info", String.valueOf(s));
            String charValue = String.valueOf(s);

            //// x8Nm20 is a pattern that makes split easier
            switch ( view.getId()) {
                case R.id.editTextTextPersonName:
                    mDatabase.child("usersData").child(currentUser.getUid()).child("x8Nm2OuserName").setValue(charValue + "x8Nm2O");
                    break;
                case R.id.editTextTextPersonSecondName:
                    mDatabase.child("usersData").child(currentUser.getUid()).child("x8Nm2OsecondName").setValue(charValue + "x8Nm2O");
                    break;
                case R.id.editTextPhoneNumber:
                    mDatabase.child("usersData").child(currentUser.getUid()).child("x8Nm2OphoneNumber").setValue(charValue + "x8Nm2O");
                    break;
                case R.id.editTextUserDescription:
                    mDatabase.child("usersData").child(currentUser.getUid()).child("x8Nm2Odescription").setValue(charValue + "x8Nm2O");
                    break;

            }//end switch
    }//end method onTextChanged

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }//end inner class TextChange

    //// alert dialog

    public void showAlertDialogButtonClicked() {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Something went wrong");
        builder.setMessage("Would you like to continue or try again?");

        // add the buttons
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setNegativeButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                readFirebaseData();
                downloadAvatarFromFirebase();
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    class download extends AsyncTask<String, String, String>{
    ProgressDialog pg;
    String result;


        @SuppressLint("ResourceType")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg = ProgressDialog.show(userActivity.this, "Loading", null);
            Log.d("onPreExecute", "start");
        }
        @Override
        protected String doInBackground(String... strings) {
            downloadAvatarFromFirebase();
            readFirebaseData();


            int value = Integer.parseInt(strings[0]);
            for(int i = 0; i <= value ;i++){
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.v("Error: ", e.toString());
                }
                result = "Please wait for " + (value - i ) + " seconds";
                //publishProgress(result);
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
       pg.dismiss();
            Log.d("onPostExecute", "finish");
        }
    }
}



