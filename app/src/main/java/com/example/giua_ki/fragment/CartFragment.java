package com.example.giua_ki.fragment;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giua_ki.R;
import com.example.giua_ki.adapter.CartAdapter;
import com.example.giua_ki.database.DataHandler;
import com.example.giua_ki.listener.OnTaskCompleted;
import com.example.giua_ki.model.CartModel;
import com.example.giua_ki.paid.GoogleSheetsTask;
import com.example.giua_ki.zaloPay.Api.CreateOrder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CartFragment extends Fragment {

    private TextView txtEmptyCart;
    private TextView txtTotalPrice;
    private RecyclerView recyclerView;
    private Button btnCreateOrder;
    private LinearLayout llBuy;
    private final CartAdapter myCartAdapter = new CartAdapter(new ArrayList<>());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        setControl(view);
        setRecyclerView();
        setDataForCart();
        btnCreateOrder.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.giua_ki.activity.CreateOrder.class);
            startActivity(intent);
        });
        return view;
    }


    private void setDataForCart() {
        DataHandler.fetchDataForCart(recyclerView, txtEmptyCart, txtTotalPrice,llBuy);
    }

    private void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myCartAdapter);
    }

    private void setControl(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        txtEmptyCart = view.findViewById(R.id.txtEmptyCart);
        txtTotalPrice = view.findViewById(R.id.txtTotalPrice);
        btnCreateOrder = view.findViewById(R.id.btnCreateOrder);
        llBuy = view.findViewById(R.id.llBuy);
    }


}
