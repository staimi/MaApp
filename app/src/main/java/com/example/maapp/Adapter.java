package com.example.maapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.MViewHolder> {

    private final ArrayList<example_item> list;
    public RecyclerOnClickListener mRecyclerOnClickListener;
    private static final int TYPE_NEWS = 1;
    private static final int TYPE_POSTS = 2;


    public interface RecyclerOnClickListener{
        void onNewsClicked(int position);
    }

    public Adapter(ArrayList<example_item> list ,RecyclerOnClickListener recyclerOnClickListener) {
        this.list = list;
        this.mRecyclerOnClickListener = recyclerOnClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if(this.list.get(position).getHOLDER_SELECTOR() == 1){
            return TYPE_NEWS;
        }else{
            return TYPE_POSTS;
        }
    }

    @NonNull
    @Override
    public MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_NEWS:
                return new NViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.example_recycler_view_item, parent, false), mRecyclerOnClickListener);
            case TYPE_POSTS:
                return new viewHolderPosts(LayoutInflater.from(parent.getContext()).inflate(R.layout.example_recycler_view_posts_item, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
        example_item currentItem = list.get(position);

        if(holder.getItemViewType() == TYPE_NEWS){
            NViewHolder nViewHolderHolder = (NViewHolder) holder;
            setUpProfileView(nViewHolderHolder);
            nViewHolderHolder.textView1.setText(currentItem.getText1());
        }else {
            viewHolderPosts mPostsHolder = (viewHolderPosts) holder;
            mPostsHolder.textView1.setText(currentItem.getText1());
            mPostsHolder.textView2.setText(currentItem.getText2());
            if(!(currentItem.getBitmap() == null)){
                mPostsHolder.avatarImageView.setImageBitmap(currentItem.getBitmap());
            }else {
                mPostsHolder.avatarImageView.getLayoutParams().height = 0;
                mPostsHolder.avatarImageView.getLayoutParams().width = 0;

            }

        }
    }
    //////////

    public class MViewHolder extends  RecyclerView.ViewHolder {
        public MViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    //////////

    private void setUpProfileView(NViewHolder mHolder) {
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class NViewHolder extends MViewHolder implements View.OnClickListener{
        public TextView textView1;
        public RecyclerOnClickListener mRecyclerOnClickListener;

        public NViewHolder(@NonNull View v, RecyclerOnClickListener recyclerOnClickListener) {
            super(v);
            textView1 = v.findViewById(R.id.textView1RecyclerView);
            this.mRecyclerOnClickListener = recyclerOnClickListener;

            v.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            mRecyclerOnClickListener.onNewsClicked(getBindingAdapterPosition());
            Log.d("OnClickListener", String.valueOf(getBindingAdapterPosition()));
        }
    }

    //// viewHolder for posts

    public class viewHolderPosts extends MViewHolder{
        public TextView textView1, textView2;
        public ImageView avatarImageView;

        public viewHolderPosts(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.text1postRecyclerView);
            textView2 = itemView.findViewById(R.id.text2postRecyclerView);
            avatarImageView = itemView.findViewById(R.id.postImageRecyclerView);
        }
    }
}