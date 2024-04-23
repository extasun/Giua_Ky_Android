package com.example.giua_ki.database;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giua_ki.adapter.CartAdapter;
import com.example.giua_ki.model.CartModel;
import com.example.giua_ki.model.OrderModel;
import com.example.giua_ki.model.ProductModel;
import com.example.giua_ki.model.SizeModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DataHandler {
    public static ArrayList<CartModel> orderModelArrayList = new ArrayList<>();
    public static int CartItemCount = 0;
    public static void addToOrder(String orderId, TextView orderTotalPrice, String dateTime, ArrayList<CartModel> s) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        String orderKey = ordersRef.push().getKey();
        OrderModel orderModel = new OrderModel();
        orderModel.setOrderId(orderId);
        orderModel.setTotalPrice(orderTotalPrice.getText().toString());
        orderModel.setDateTime(dateTime);
        orderModel.setOrderDetails(s);
        assert orderKey != null;
        ordersRef.child(orderKey).setValue(orderModel);
    }

    public static void addToCart(ProductModel productModel, SizeModel selectedSize, int quantity) {
        String productID = productModel.getName() + "_" + selectedSize.getSize();
        DatabaseReference cartReference = FirebaseDatabase.getInstance().getReference("Carts").child("UNIQUE_USER_ID");
        cartReference.child(productID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double finalPrice = productModel.getDiscount() > 0 ? productModel.getFinalPrice() : productModel.getPrice();
                        if (snapshot.exists()) {
                            CartModel cartModel = snapshot.getValue(CartModel.class);
                            if (cartModel != null) {
                                cartModel.setQuantity(cartModel.getQuantity() + quantity);
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("quantity", cartModel.getQuantity());
                                updateData.put("totalPrice", cartModel.getQuantity() * (finalPrice + selectedSize.getPrice()));
                                updateData.put("sizePrice", selectedSize.getPrice());
                                cartReference.child(productID)
                                        .updateChildren(updateData);
                            }
                        } else {
                            CartModel cartModel = new CartModel(
                                    productModel.getName(),
                                    productModel.getImageUrl(),
                                    quantity,
                                    finalPrice + selectedSize.getPrice(),
                                    finalPrice + selectedSize.getPrice(),
                                    selectedSize.getSize(),
                                    selectedSize.getPrice()
                            );
                            cartReference.child(productID)
                                    .setValue(cartModel);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CartHandler", "addToCart onCancelled: " + error.getMessage());
                    }
                });
    }
    public static void countItemsInCart(CartItemCountCallback callback) {
        DatabaseReference cartReference = FirebaseDatabase.getInstance().getReference("Carts").child("UNIQUE_USER_ID");
        cartReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int cartItemCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CartModel cartModel = snapshot.getValue(CartModel.class);
                    if (cartModel != null) {
                        cartItemCount += cartModel.getQuantity();
                    }
                }
                callback.onCartItemCount(cartItemCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DataHandler", "countItemsInCart onCancelled: " + databaseError.getMessage());
            }
        });
    }
    public interface CartItemCountCallback {
        void onCartItemCount(int count);
    }
    public static void fetchDataForCart(
            RecyclerView recyclerView,
            TextView txtEmptyCart,
            TextView txtTotalPrice,
            LinearLayout llBuy
    ) {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Carts");
    databaseReference.child("UNIQUE_USER_ID")
            .addValueEventListener(new ValueEventListener() {
                @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<CartModel> cartModelArrayList = new ArrayList<>();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            CartModel cartModel = snapshot.getValue(CartModel.class);
                            if (cartModel != null) {
                                cartModelArrayList.add(cartModel);
                            }
                            orderModelArrayList = cartModelArrayList;
                        }
                        recyclerView.setVisibility(View.VISIBLE);
                        txtEmptyCart.setVisibility(View.GONE);
                        llBuy.setVisibility(View.VISIBLE);
                        CartAdapter adapter = (CartAdapter) recyclerView.getAdapter();
                        if (adapter != null) {
                            adapter.updateData(cartModelArrayList);
                        }

                    } else {
                        recyclerView.setVisibility(View.GONE);
                        llBuy.setVisibility(View.GONE);
                        txtEmptyCart.setVisibility(View.VISIBLE);
                    }
                    double totalPrice = 0.0;
                    for (CartModel cartModel : cartModelArrayList) {
                        totalPrice += cartModel.getTotalPrice();
                    }
                    NumberFormat vndFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                    txtTotalPrice.setText(vndFormat.format(totalPrice)+"đ");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("CartFragment", "onCancelled: " + databaseError.getMessage());
                }
            });

}
    public static void fetchDataForOrder(
            RecyclerView recyclerView,
            TextView txtTotalPrice
    ) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Carts");
        databaseReference.child("UNIQUE_USER_ID")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<CartModel> cartModelArrayList = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                CartModel cartModel = snapshot.getValue(CartModel.class);
                                if (cartModel != null) {
                                    cartModelArrayList.add(cartModel);
                                }
                                orderModelArrayList = cartModelArrayList;
                            }
                            CartAdapter adapter = (CartAdapter) recyclerView.getAdapter();
                            if (adapter != null) {
                                adapter.updateData(cartModelArrayList);
                            }
                        double totalPrice = 0.0;
                        for (CartModel cartModel : cartModelArrayList) {
                            totalPrice += cartModel.getTotalPrice();
                        }
                        NumberFormat vndFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                        totalPrice += 20000;
                        txtTotalPrice.setText(vndFormat.format(totalPrice));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("CartFragment", "onCancelled: " + databaseError.getMessage());
                    }
                });

    }
}
