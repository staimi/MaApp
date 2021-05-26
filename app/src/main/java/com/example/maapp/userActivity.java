package com.example.maapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class userActivity extends AppCompatActivity {

    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 0;
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

        Log.d("userActivity", "started");

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

        //////////////////////////////////////////////////////
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"LongLogTag", "IntentReset"})
            @Override
            public void onClick(View view) {
                try {
                    checkPermissionForReadExternalStorage();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        //////////////////////////////////////////////////////////
        switchToHomeActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });

    }

    /////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "started");
        Bitmap bitmap;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            Log.d("U selectedImage", String.valueOf(selectedImage));

            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            bitmap = BitmapFactory.decodeFile(picturePath);

            profilePicture.setImageBitmap(bitmap);
            saveProfilePicture(selectedImage);
        }else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }


    ///// Read external storage permission

    public void checkPermissionForReadExternalStorage(){
        Log.d("Permission", "checking");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED){
                requestPermissionForReadExternalStorage();
            }else {
                Log.d("Permission", "Granted");
            }
        }
    }

    /// ask for external storage permission

    public void requestPermissionForReadExternalStorage(){

        try {
            ActivityCompat.requestPermissions( userActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    public void saveProfilePicture(Uri uri) {

        Log.d("saveProfilePicture", "started");
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

                    StorageReference img = storageReference.child("images/users/" + currentUser.getUid() + ".jpg");

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
                    Log.d("Download Avatar", "Error");
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
            pg = ProgressDialog.show(userActivity.this, "Loading","Wait");
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



