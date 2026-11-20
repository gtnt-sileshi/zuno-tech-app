package com.zunotech.app.adapter.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.zunotech.app.R;
import com.zunotech.app.item.ItemData;

/** Horizontal card list for blog sections on the Home screen. */
public class AdapterHomeBlog extends RecyclerView.Adapter<AdapterHomeBlog.MyViewHolder> {

    private final List<ItemData> arrayList;
    private final RecyclerItemClickListener listener;

    public AdapterHomeBlog(List<ItemData> arrayList, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        View card;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_blog_title);
            image = view.findViewById(R.id.iv_blog_image);
            card = view.findViewById(R.id.rl_home_blog);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_home_blog, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ItemData item = arrayList.get(position);
        holder.title.setText(item.getTitle());
        Picasso.get()
                .load(item.getThumb())
                .centerCrop()
                .resize(300, 300)
                .placeholder(R.drawable.material_design_default)
                .into(holder.image);
        holder.card.setOnClickListener(v -> listener.onClick(holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener {
        void onClick(int position);
    }
}
