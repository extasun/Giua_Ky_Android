package com.example.giua_ki.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giua_ki.R;
import com.example.giua_ki.model.ProductModel;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private final List<ProductModel> productList;

    public BannerAdapter(List<ProductModel> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        ProductModel product = productList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.productImage);
        holder.productName.setText(product.getName());
        holder.productDiscount.setText(String.valueOf(50));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productDiscount;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.banner_product_image);
            productName = itemView.findViewById(R.id.banner_product_name);
            productDiscount = itemView.findViewById(R.id.banner_product_discount);
        }
    }
}