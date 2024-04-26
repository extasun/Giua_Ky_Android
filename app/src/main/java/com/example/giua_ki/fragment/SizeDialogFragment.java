package com.example.giua_ki.fragment;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giua_ki.R;
import com.example.giua_ki.adapter.SizeAdapter;
import com.example.giua_ki.database.DataHandler;
import com.example.giua_ki.model.ProductModel;
import com.example.giua_ki.model.SizeModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saadahmedsoft.popupdialog.PopupDialog;
import com.saadahmedsoft.popupdialog.Styles;
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener;

import java.util.ArrayList;

public class SizeDialogFragment extends BottomSheetDialogFragment {

    private SizeAdapter sizeAdapter;
    private final ArrayList<SizeModel> sizeList1 = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_size_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sizeAdapter = new SizeAdapter(sizeList1);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(sizeAdapter);
        assert getArguments() != null;
        ProductModel product = (ProductModel) getArguments().getSerializable("product");
        assert product != null;
        String productId = product.getName();
        FirebaseDatabase.getInstance().getReference("Products").child(productId).child("sizes")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        sizeList1.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String size = snapshot.child("size").getValue(String.class);
                            Double price = snapshot.child("price").getValue(Double.class);
                            if (size != null && price != null) {
                                sizeList1.add(new SizeModel(size, price));
                            }
                        }
                        sizeAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("fetchData", "Fetch data cancelled: " + databaseError.getMessage());
                    }
                });

        Button btn_add = view.findViewById(R.id.btn_add_product_to_cart);
        ImageView buttonDecrease= view.findViewById(R.id.buttonDecrease);
        ImageView buttonIncrease= view.findViewById(R.id.buttonIncrease);
        TextView textQuantity= view.findViewById(R.id.textQuantity);
        buttonDecrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(textQuantity.getText().toString());
            if (quantity > 1) {
                quantity--;
                textQuantity.setText(String.valueOf(quantity));
            }
        });

        buttonIncrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(textQuantity.getText().toString());
            quantity++;
            textQuantity.setText(String.valueOf(quantity));
        });
        btn_add.setOnClickListener(v -> {
            SizeModel selectedSize = sizeAdapter.getSelectedSize();
            if (selectedSize != null) {
                int quantity = Integer.parseInt(textQuantity.getText().toString());
                DataHandler.addToCart(product, selectedSize,quantity);
                dismiss();
                thongBaoThanhCong();
            }else{
                Toast.makeText(getContext(), R.string.chon_size_khi_them_vao_cart, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void thongBaoThanhCong() {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
        int notificationId = 1; // Định danh duy nhất cho mỗi thông báo

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity(), NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.coffee_icon)
                .setContentTitle("Thêm thành công")
                .setContentText("Sản phẩm đã được thêm vào giỏ hàng của bạn")
                .setContentInfo(getString(R.string.thong_tin));

        notificationManager.notify(notificationId, notificationBuilder.build());
        Handler handler = new Handler();
        long delayInMilliseconds = 300;
        handler.postDelayed(() -> notificationManager.cancel(notificationId), delayInMilliseconds);
    }
    public static SizeDialogFragment newInstance(ProductModel product) {
        SizeDialogFragment fragment = new SizeDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }
}