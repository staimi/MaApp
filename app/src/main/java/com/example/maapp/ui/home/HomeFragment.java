package com.example.maapp.ui.home;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maapp.Adapter;
import com.example.maapp.R;
import com.example.maapp.VerticalSpacingItemDecorator;
import com.example.maapp.example_item;
import com.example.maapp.postDetails;
import com.example.maapp.splash_screen;
import com.example.maapp.ui.gallery.GalleryFragment;
import com.example.maapp.userActivity;
import com.example.maapp.webViewClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements Adapter.RecyclerOnClickListener {
    View v;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    ArrayList<example_item> recyclerViewData;
    ArrayList<String> linksFromWebsites, allArticlesTitles;
    Intent intent;
    DatabaseReference mDatabase;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    List<postDetails> postDetailsData;
    Bitmap[] bitmapFirebase;
    int postsCount, LAST_USER_POST;

    /// variables for post downloading
    FirebaseDatabase firebaseDatabasePost;
    DatabaseReference databaseReferencePost;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_home, container, false);
        linksFromWebsites = new ArrayList<>();
        allArticlesTitles = new ArrayList<>();
        postDetailsData = new ArrayList<>();
        bitmapFirebase = new Bitmap[100];

        //startActivity(new Intent(v.getContext(), splash_screen.class));

        intent = new Intent(v.getContext(), webViewClass.class);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /// firebase for post downloading
        firebaseDatabasePost = FirebaseDatabase.getInstance();
        databaseReferencePost = firebaseDatabasePost.getReference("userPosts");

        recyclerViewData = new ArrayList<example_item>();
        mRecyclerView = v.findViewById(R.id.recyclerViewHome);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
        // I have to add a mOnClickListener to RecyclerView
        mAdapter = new Adapter(recyclerViewData,  this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                Log.d("onScrolled", "x = "+i2 + " y = "+i3);
            }
        });

        int postsCount = 20;
        LAST_USER_POST = 0;


        dwn dw = new dwn();
        dw.execute(String.valueOf(postsCount));

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    public void setRecyclerViewData(ArrayList<String> link) {

        String time = "";
        String post = "";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        Bitmap bitmap = BitmapFactory.decodeResource(requireContext().getResources(), R.drawable.logo_bmp, options);
        int postsCount = postDetailsData.size();
        int a = postDetailsData.size();
        Log.d("postsCount", String.valueOf(postsCount));

        for(int i = 0; i < link.size(); i++){

            try {
            JSONObject jsonObject = new JSONObject(link.get(i));

                if(!jsonObject.getString("url").isEmpty()){
                    String title = jsonObject.getString("title");
                    String links = jsonObject.getString("url");
                    this.recyclerViewData.add(new example_item(title , links, 1));

                    if(postDetailsData.get(i) != null && LAST_USER_POST != 1){
                        time = postDetailsData.get(i).getTime();
                        post = postDetailsData.get(i).getPostText();
                        Log.d("time & links new", time + " " + post);
                        if(bitmapFirebase[i] != null){
                            this.recyclerViewData.add(new example_item(post, time, bitmapFirebase[i], 2));
                        }else {
                            this.recyclerViewData.add(new example_item(post, time, null, 2));
                        }

                        Log.d("postDetailData from i", postDetailsData.get(i).getPostText());
                    }else{
                        LAST_USER_POST = 1;
                    }
                }
            }catch (Exception e){
                Log.i("Something went wrong", "");
            }

        }
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onNewsClicked(int position) {

    String link = recyclerViewData.get(position).getText2();
    intent.putExtra("link", link);
        startActivity(intent);
    }


    class dwn extends AsyncTask<String, String, ArrayList<String>> {
        OkHttpClient client = new OkHttpClient();
        String jsonResponseString = "";
        ArrayList<String> articlesData = new ArrayList<>();
        ProgressDialog pg = new ProgressDialog(v.getContext());

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            Log.d("Strings", strings[0]);
            int countToDownload = Integer.parseInt(strings[0]);
            int firstPost = countToDownload - 20;

            // posts downloading
            readPosts();

            // news downloading
            try {
            Request request = new Request.Builder()
                    .url("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty")
                    .get()
                    .build();
            // close connection if code = 200, 200 means complete


            Response response = client.newCall(request).execute();
            jsonResponseString = response.body().string();

            Log.d("Response", jsonResponseString);

                // close connection if code = 200, 200 means complete
                final int code = response.code(); // can be any value
                if (code == 200) {
                    final ResponseBody body = response.body(); // body exists, I have to close it
                    body.close(); // I close it explicitly
                }

                // creating Json and get links
                JSONArray jsonArrayNumbersOfArticles = new JSONArray(jsonResponseString);

                for(int i = firstPost; i <countToDownload; i ++){
                    String numberOfLink = jsonArrayNumbersOfArticles.getString(i);
                    Request requestLinks = new Request.Builder()
                            .url("https://hacker-news.firebaseio.com/v0/item/"+ numberOfLink +".json?print=pretty")
                            .get()
                            .build();
                    Response responseLinks = client.newCall(requestLinks).execute();

                    articlesData.add(responseLinks.body().string());
                }
            }

            catch (IOException| IllegalStateException | JSONException ioException){
                ioException.printStackTrace();
            }
            return articlesData;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.setMessage("Downloading");
            pg.show();
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);

            if(pg.isShowing()){
                pg.dismiss();
            }
            try {
                setRecyclerViewData(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /////// post download from firebase
    // Beispielsfilelocation gs://maapp-c71c6.appspot.com/images/posts/07-05-2021 21:13:45/OipGsjMUqfcxxCTNu5x3gB89j7t2.jpg
    public void downloadAvatarFromFirebase(String link, int index) {
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference img = storageReference.child(link);

            final long ONE_MEGABYTE = 1024 * 1024;
            //final long BYTE = 768 * 768;

            img.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    bitmapFirebase[index] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                    Log.d("downloadAvatarFromFirebase", "error");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            Log.d("Recycler", "Moved");
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }
    };

    void readPosts(){
        Log.d("readPosts", "started");
        try {
            databaseReferencePost.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot postsnap : snapshot.getChildren()){
                        postDetails postDetails = postsnap.getValue(postDetails.class);
                        postDetailsData.add(postDetails);
                    }

                    for(int i = 0; i < postDetailsData.size(); i++){
                        if(postDetailsData.get(i).getPicture().equals("false") == false){
                            downloadAvatarFromFirebase(postDetailsData.get(i).getPicture(),i);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } catch (Exception exception){
            exception.printStackTrace();
        }
    }
}