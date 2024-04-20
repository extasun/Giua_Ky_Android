package com.example.giua_ki.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giua_ki.R;
import com.example.giua_ki.model.CartModel;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyCartViewHolder> {
    private Context context;
    private List<CartModel> cartModelList;

    public CartAdapter(List<CartModel> cartModelList) {
        this.cartModelList = cartModelList;
    }


    @NonNull
    @Override
    public MyCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new MyCartViewHolder(view);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull MyCartViewHolder holder, int position) {
        CartModel cartModel = cartModelList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(cartModel.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imageView);

        holder.txtName.setText(cartModel.getName());
        NumberFormat vndFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        holder.txtPrice.setText("Giá : " + vndFormat.format(cartModel.getPrice()-cartModel.getSizePrice()) + "đ");
        holder.txtQuantity.setText(String.valueOf(cartModel.getQuantity()));
        holder.txtSize.setText("Size : " + cartModel.getSize()+" (+"+vndFormat.format(cartModel.getSizePrice())+"đ)");
        holder.btnMinus.setOnClickListener(v -> minusCartItem(holder, cartModelList.get(position)));
        holder.btnPlus.setOnClickListener(v -> plusCartItem(holder, cartModelList.get(position)));
        holder.btnDel.setOnClickListener(v -> deleteCartItem(holder.itemView.getContext(), cartModelList, holder.getAdapterPosition()));
    }
    private void plusCartItem(MyCartViewHolder holder, CartModel cartModel) {
        int position = cartModelList.indexOf(cartModel);
        if (position != -1) {
            cartModel.setQuantity(cartModel.getQuantity()+1);
            cartModel.setTotalPrice(cartModel.getQuantity()*cartModel.getPrice());
            holder.txtQuantity.setText(new StringBuffer().append(cartModel.getQuantity()));
            updateFirebase(cartModel);
            notifyItemChanged(position);
        }
    }


    @SuppressLint("SuspiciousIndentation")
private void minusCartItem(MyCartViewHolder holder, CartModel cartModel) {
    if (cartModel.getQuantity() > 1) {
        cartModel.setQuantity(cartModel.getQuantity() - 1);
        cartModel.setTotalPrice(cartModel.getQuantity() * (cartModel.getPrice() + cartModel.getSizePrice())); // update total price
        holder.txtQuantity.setText(String.valueOf(cartModel.getQuantity()));
        updateFirebase(cartModel);
    } else {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Xóa sản phẩm khỏi giỏ hàng");
        builder.setMessage("Bạn có chắc chắn muốn xóa sản phẩm khỏi giỏ hàng?");
        builder.setPositiveButton("Có", (dialog, which) -> {
            deleteCartItem(holder.itemView.getContext(), cartModelList, holder.getAdapterPosition());
            dialog.dismiss();
        });
        builder.setNegativeButton("Không", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
    private void deleteCartItem(Context context, List<CartModel> cartModelList, int position) {
        if (position >= 0 && position < cartModelList.size()) {
            CartModel cartModel = cartModelList.get(position);
            String productID = cartModel.getName() + "_" + cartModel.getSize();
            FirebaseDatabase.getInstance().getReference("Carts")
                    .child("UNIQUE_USER_ID")
                    .child(productID)
                    .removeValue();
            cartModelList.remove(position);
            notifyItemRemoved(position);
        }
    }

    private void updateFirebase(CartModel cartModel) {
    String productID = cartModel.getName() + "_" + cartModel.getSize();
    FirebaseDatabase.getInstance().getReference("Carts")
            .child("UNIQUE_USER_ID")
            .child(productID)
            .setValue(cartModel);
    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<CartModel> newList) {
        cartModelList.clear();
        cartModelList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class MyCartViewHolder extends RecyclerView.ViewHolder {
        ImageView btnMinus, btnPlus, imageView,btnDel;
        TextView txtName, txtPrice, txtQuantity,txtSize;

        public MyCartViewHolder(@NonNull View itemView) {
            super(itemView);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            imageView = itemView.findViewById(R.id.imageView);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            btnDel=itemView.findViewById(R.id.btnDelete);
            txtSize=itemView.findViewById(R.id.txtSize);
        }


    }
}
