package com.example.maapp.ui.home;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maapp.R;
import com.example.maapp.RecyclerAdapter;
import com.example.maapp.example_item;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements RecyclerAdapter.RecyclerOnClickListener {
    View v;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    ArrayList<example_item> recyclerViewData;
    ArrayList<String> linksFromWebsites, allArticlesTitles;
    WebView webView;

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_home, container, false);
        linksFromWebsites = new ArrayList<>();
        allArticlesTitles = new ArrayList<>();
        webView = v.findViewById(R.id.webViewHomeFragment);

        recyclerViewData = new ArrayList<example_item>();
        mRecyclerView = v.findViewById(R.id.recyclerViewHome);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
        // I have to add a mOnClickListener to RecyclerView
        mAdapter = new RecyclerAdapter(recyclerViewData, this);


        dwn dw = new dwn();
        dw.execute();

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

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
    public void setRecyclerViewData(ArrayList<String> link) throws JSONException {


        for(int i = 0; i < link.size(); i++){

            JSONObject jsonObject = new JSONObject(link.get(i));

            try {
                if(!jsonObject.getString("url").isEmpty()){
                    String title = jsonObject.getString("title");
                    String links = jsonObject.getString("url");
                    this.recyclerViewData.add(new example_item(title , links));
                }

            }catch (Exception e){
                Log.i("Something went wrong", "again");
            }

        }

        Log.d("Arraylist size", String.valueOf(recyclerViewData.size()));

        mAdapter = new RecyclerAdapter(new ArrayList<>(recyclerViewData), this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onNewsClicked(int position) {
            //webView.setAlpha(1);
            //webView.loadUrl("");
            Log.d("RecyclerView", "clicked");
    }

    class dwn extends AsyncTask<String, String, ArrayList<String>> {
        OkHttpClient client = new OkHttpClient();
        String jsonResponseString = "";
        ArrayList<String> articlesData = new ArrayList<>();
        ArrayList<String> link = new ArrayList<>();
        ProgressDialog pg = new ProgressDialog(v.getContext());

        @Override
        protected ArrayList<String> doInBackground(String... strings) {

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
                for(int i = 0; i <20; i ++){
                    String numberOfLink = jsonArrayNumbersOfArticles.getString(i);
                    Request requestLinks = new Request.Builder()
                            .url("https://hacker-news.firebaseio.com/v0/item/"+ numberOfLink +".json?print=pretty")
                            .get()
                            .build();
                    Response responseLinks = client.newCall(requestLinks).execute();

                    articlesData.add(responseLinks.body().string());
                    Log.d("Link and other data", articlesData.get(i));
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
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}