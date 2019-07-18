package com.bytedance.androidcamp.network.dou;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.androidcamp.network.dou.model.Like;

import java.util.ArrayList;
import java.util.List;

public class likesAdapter extends RecyclerView.Adapter<likeViewHolder> {
    private List<Like> like=new ArrayList<>();
    public void refresh(List<Like> newNotes) {
        like.clear();
        if (newNotes != null) {
            like.addAll(newNotes);
        }
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public likeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_like,parent,false);
        return new likeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull likeViewHolder holder, int position) {
        holder.bind(like.get(position));

    }

    @Override
    public int getItemCount() {
        return like.size();
    }
}
