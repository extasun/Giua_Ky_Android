package com.example.giua_ki.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.giua_ki.R;
import com.example.giua_ki.fragment.SizeDialogFragment;
import com.example.giua_ki.model.ProductModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductModel> productList;

    public ProductAdapter(List<ProductModel> productList) {
        this.productList = productList;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productPrice;
        ImageView productImage;
        TextView productDiscountPrice;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textProductName);
            productPrice = itemView.findViewById(R.id.textProductPrice);
            productImage = itemView.findViewById(R.id.imageProduct);
            productDiscountPrice = itemView.findViewById(R.id.textProductDiscountPrice);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ProductViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ProductModel product = productList.get(position);
        holder.productName.setText(product.getName());
        NumberFormat vndFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        if (product.getDiscount() > 0) {
            holder.productPrice.setPaintFlags(holder.productPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.productPrice.setText(vndFormat.format(product.getPrice()) + "đ");
            holder.productDiscountPrice.setText(vndFormat.format(product.getPrice() * (1 - product.getDiscount() / 100.0)) + "đ");
            holder.productDiscountPrice.setVisibility(View.VISIBLE);
        } else {
            holder.productPrice.setText(vndFormat.format(product.getPrice()) + "đ");
            holder.productDiscountPrice.setVisibility(View.GONE);
        }
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.productImage);
        holder.itemView.setOnClickListener(v -> {
            SizeDialogFragment dialog = SizeDialogFragment.newInstance(productList.get(position));
            dialog.show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), "SizeDialogFragment");
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<ProductModel> filteredList) {
        productList = filteredList;
    }
}