package com.example.maapp.ui.gallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.maapp.R;
import com.example.maapp.postDetails;
import com.example.maapp.userActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class GalleryFragment extends Fragment {

    Button postUploadButton;
    EditText postEditText;
    private DatabaseReference mDatabase;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    Calendar calendar;
    SimpleDateFormat simpledateformat;
    ImageButton imageButtonPictureUpload;
    private static final int RESULT_LOAD_IMAGE = 1;
    View root;
    Uri selectedImage;
    postDetails postDetails;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        root = inflater.inflate(R.layout.fragment_gallery, container, false);
        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });

        postEditText = root.findViewById(R.id.editTextTextPost);
        postUploadButton = root.findViewById(R.id.buttonPostUpload);
        imageButtonPictureUpload = root.findViewById(R.id.imageButtonPictureUpload);
        selectedImage = null;

        ///firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.d("Current user", String.valueOf(currentUser));


        // set standard colour
        imageButtonPictureUpload.setBackgroundColor(Color.parseColor("#CCCCCC"));



        /// onClick for upload a picture
        imageButtonPictureUpload.setOnClickListener(new View.OnClickListener() {
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


        //// button onClickListener
        postUploadButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onClick(View view) {
                //code
                String[] postText = new String[3];
                calendar = Calendar.getInstance();
                simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String date = simpledateformat.format(calendar.getTime());
                Log.d("Time from internet", date);
                postText[0] = date;
                postText[1] = String.valueOf(postEditText.getText());

                if (postEditText.getText().length() < 5){
                    Toast.makeText(root.getContext(), "Write something more", Toast.LENGTH_SHORT).show();
                }else {

                    if(selectedImage!=null){
                        Log.i("postText[0]", postText[0]);
                        uploadPictureOnFirebase(selectedImage, postText[0]);
                        postText[2] = "images/posts/"+postText[0]+"/"+currentUser.getUid()+".jpg";
                    }
                    else{
                        Toast.makeText(root.getContext(), "Post without picture", Toast.LENGTH_SHORT).show();
                        postText[2] = "false";
                    }


                    new postUpload().execute(postText);
                }
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "started");
        Bitmap bitmap = null;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            this.selectedImage = data.getData();
            Log.d("U selectedImage", String.valueOf(selectedImage));

            imageButtonPictureUpload.setBackgroundColor(Color.parseColor("#666666"));


        }else {
            Toast.makeText(root.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    /// data upload
    class postUpload extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            postDetails = new postDetails(strings[0], strings[1], strings[2]);

            Log.d("AsyncTask", strings[0]);
            mDatabase.child("userPosts").child(currentUser.getUid()+" time: " + strings[0]).setValue(postDetails);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            postEditText.setBackgroundResource(R.drawable.fui_ic_anonymous_white_24dp);
            postEditText.setText("Sending...");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    postEditText.setBackgroundResource(R.drawable.borders);
                }
            }, 1000);

            postEditText.setText("");
        }
    }

    ///// Read external storage permission

    public void checkPermissionForReadExternalStorage() throws Exception {
        Log.d("Permission", "checking");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                requestPermissionForReadExternalStorage();
            }else {
                Log.d("Permission", "Granted");
            }
        }
    }

    /// ask for external storage permission

    public void requestPermissionForReadExternalStorage() throws Exception {

        try {
            ActivityCompat.requestPermissions((Activity) root.getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    2000);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /////// upload picture
    void uploadPictureOnFirebase (Uri uri, String time){
        ProgressDialog progressDialog = new ProgressDialog(root.getContext());
        Log.d("savePostPicture", "started");
        String userID = currentUser.getUid();
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        try {
            StorageReference storageReference = storage.getReference().child("images").child("posts").child(time).child(userID + ".jpg");
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.cancel();
                    imageButtonPictureUpload.setBackgroundColor(Color.parseColor("#CCCCCC"));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.cancel();
                    Toast.makeText(root.getContext(), "Failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}