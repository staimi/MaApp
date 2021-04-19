package com.example.maapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private static final String TAG = "RecyclerAdapter";

    private ArrayList<example_item> list = new ArrayList<>();
    private RecyclerOnClickListener mRecyclerOnClickListener;

    public RecyclerAdapter(ArrayList<example_item> list ,RecyclerOnClickListener recyclerOnClickListener) {
        this.list = list;
        this.mRecyclerOnClickListener = recyclerOnClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_recycler_view_item, parent, false);
        return new ViewHolder(v, mRecyclerOnClickListener);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        example_item currentItem = list.get(position);

        holder.textView1.setText(currentItem.getText1());
        holder.textView2.setText(currentItem.getText2());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView1, textView2;
        //public ImageView avatarImageView;
        RecyclerOnClickListener recyclerOnClickListener;

        public ViewHolder(View v, RecyclerOnClickListener recyclerOnClickListener) {
            super(v);
            textView1 = v.findViewById(R.id.textView1RecyclerView);
            textView2 = v.findViewById(R.id.textView2RecyclerView);
            //avatarImageView = (ImageView) v.findViewById(R.id.userAvatarRecyclerView);
            this.recyclerOnClickListener = recyclerOnClickListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            recyclerOnClickListener.onNewsClicked(getAdapterPosition());
            Log.d(TAG, "Position: " + getAdapterPosition());
        }
    }

    public interface RecyclerOnClickListener{
        void onNewsClicked(int position);
    }

    public RecyclerAdapter(ArrayList<example_item> List) {
        list = List;
    }


}
