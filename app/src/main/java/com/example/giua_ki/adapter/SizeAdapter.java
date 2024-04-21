package com.example.giua_ki.adapter;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giua_ki.R;
import com.example.giua_ki.model.SizeModel;

import java.util.ArrayList;


public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.ViewHolder> {

    private final ArrayList<SizeModel> sizeList;
    private SizeModel selectedSize;

    public SizeAdapter(ArrayList<SizeModel> sizeList) {
        this.sizeList = sizeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_size, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SizeModel sizeModel = sizeList.get(position);
        holder.size.setText(sizeModel.getSize());
        holder.price.setText("+"+ sizeModel.getPrice()+"đ");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                selectedSize = sizeModel;
                notifyDataSetChanged();
            }
        });

        if (selectedSize == sizeModel) {
            ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor(holder.itemView.getContext().getString(R.string.color_selected_size)));
        } else {
            ((CardView) holder.itemView).setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return sizeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView size;
        TextView price;

        public ViewHolder(View itemView) {
            super(itemView);
            size = itemView.findViewById(R.id.textSizeName);
            price = itemView.findViewById(R.id.textSizePrice);
        }
    }

    public SizeModel getSelectedSize() {
        return selectedSize;
    }
}