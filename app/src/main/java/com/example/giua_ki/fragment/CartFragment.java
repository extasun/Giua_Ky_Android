package com.example.giua_ki.fragment;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giua_ki.R;
import com.example.giua_ki.adapter.MyCartAdapter;
import com.example.giua_ki.database.DataHandler;
import com.example.giua_ki.listener.OnTaskCompleted;
import com.example.giua_ki.model.CartModel;
import com.example.giua_ki.paid.GoogleSheetsTask;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class CartFragment extends Fragment implements OnTaskCompleted {
    ArrayList<CartModel> orderModelArrayList;
    AlertDialog alertDialog;
    String dateTime;
    int trangThai=0;
    private GoogleSheetsTask googleSheetsTask;
    String randomDescription;
    String orderId;
    private TextView txtEmptyCart;
    private ImageView ivPayment;
    private TextView txtTotalPrice;
    private TextView tvGiaTien;
    private TextView payment_methods;
    private ImageView ivEllipsis;
    private ScrollView llBuy;
    private RecyclerView recyclerView;
    private Button btnBuy;
    private TextView tvPhiGiaoHang;
    private TextView tvTotalPrice;
    private EditText edtAddress, edtPhone;
    private ImageView ivEditPhone, ivEditAddress;
    private final MyCartAdapter myCartAdapter = new MyCartAdapter(new ArrayList<>());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        randomDescription = generateRandomDescription();
        setControl(view);
        setRecyclerView();
        setDataForCart();
        btnBuy.setOnClickListener(v -> thanhToanHoaDon());
        setEdit();
        setElipsis();
        return view;
    }
    @SuppressLint("SetTextI18n")
    public void thanhToanHoaDon() {
        if (payment_methods.getText().equals("Quét mã QR")) {
            LayoutInflater inflater = LayoutInflater.from(requireActivity());
            View overlayView = inflater.inflate(R.layout.qr, null);
            TextView nameTextView = overlayView.findViewById(R.id.nameTextView);
            TextView amountTextView = overlayView.findViewById(R.id.amountTextView);
            TextView descriptionTextView = overlayView.findViewById(R.id.descriptionTextView);
            TextView timeTextView=overlayView.findViewById(R.id.timeTextView);
            nameTextView.setText("Tên người nhận : Nguyễn Tiến Nhật");
            amountTextView.setText("Số tiền :"+tvTotalPrice.getText());
            descriptionTextView.setText("Mô tả : "+randomDescription);
            ImageView qrCodeImageView = overlayView.findViewById(R.id.qrImageView);
            String bankId = "VCB";
            String accountNo = "1016010035";
            String amount = String.valueOf(tvTotalPrice.getText());
            String description = "2k";

            String imageUrl = "https://img.vietqr.io/image/"
                    + bankId + "-" + accountNo + "-" + "qr_only.png"
                    + "?amount=" + amount
                    + "&addInfo=" + description;

            Glide.with(this)
                    .load(imageUrl)
                    .into(qrCodeImageView);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity());
            alertDialogBuilder.setView(overlayView);
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            Button backButton = alertDialog.findViewById(R.id.backButton);
            if (backButton != null) {
                backButton.setOnClickListener(view -> alertDialog.dismiss());
            }
            Button successButton = alertDialog.findViewById(R.id.successButton);
            assert successButton != null;
            successButton.setOnClickListener(view -> {
                googleSheetsTask = new GoogleSheetsTask(CartFragment.this);
                googleSheetsTask.execute();
                if(trangThai==1){
                    thongBaoThanhCong();
                    alertDialog.dismiss();
                    DataHandler.addToOrder(orderId,tvTotalPrice,dateTime,orderModelArrayList);
                    clearCart();
                }else {
                    Snackbar.make(view, "Không tồn tại giao dịch", Snackbar.LENGTH_LONG).show();
                }
            });

            new CountDownTimer(600000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long secondsRemaining = millisUntilFinished / 1000;
                    long minutes = secondsRemaining / 60;
                    long seconds = secondsRemaining % 60;
                    String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                    timeTextView.setText("Thời gian còn lại: " + timeRemaining);

                }

                public void onFinish() {
                    alertDialog.dismiss();
                }
            }.start();
        }else{
            thongBaoThanhCong();
            DataHandler.addToOrder(orderId,tvTotalPrice,dateTime,orderModelArrayList);
            clearCart();
        }

    }
    public String generateRandomDescription() {
        int descriptionLength = 20;
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder description = new StringBuilder(descriptionLength);

        for (int i = 0; i < descriptionLength; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            description.append(randomChar);
        }

        return description.toString();
    }
    public static void clearCart() {
        DatabaseReference userCart = FirebaseDatabase
                .getInstance()
                .getReference("Carts")
                .child("UNIQUE_USER_ID");
        userCart.removeValue();
    }
    private void setElipsis() {
        ivEllipsis.setOnClickListener(v -> payment_method());
    }

    private void setEdit() {
        ivEditAddress.setOnClickListener(new View.OnClickListener() {
            boolean isEditing = false;

            @Override
            public void onClick(View v) {
                if (!isEditing) {
                    edtAddress.setEnabled(true);
                    edtAddress.setFocusableInTouchMode(true);
                    edtAddress.requestFocus();
                    ivEditAddress.setImageResource(R.drawable.edit_success);
                    isEditing = true;
                } else {
                    if (edtAddress.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
                    } else {
                        edtAddress.setEnabled(false);
                        edtAddress.setFocusable(false);
                        ivEditAddress.setImageResource(R.drawable.edit);
                        isEditing = false;
                    }
                }
            }
        });

        ivEditPhone.setOnClickListener(new View.OnClickListener() {
            boolean isEditing = false;

            @Override
            public void onClick(View v) {
                if (!isEditing) {
                    edtPhone.setEnabled(true);
                    edtPhone.setFocusableInTouchMode(true);
                    edtPhone.requestFocus();
                    ivEditPhone.setImageResource(R.drawable.edit_success);
                    isEditing = true;
                } else {
                    String phoneInput = edtPhone.getText().toString();
                    if (phoneInput.isEmpty()) {
                        Toast.makeText(getActivity(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                    } else if (!phoneInput.matches("^\\+?[0-9]{10,11}$")) {
                        Toast.makeText(getActivity(), "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                    } else {
                        edtPhone.setEnabled(false);
                        edtPhone.setFocusable(false);
                        ivEditPhone.setImageResource(R.drawable.edit);
                        isEditing = false;
                    }
                }
            }
        });
    }

    private void setDataForCart() {
        DataHandler.fetchDataForCart(recyclerView, txtEmptyCart, txtTotalPrice, llBuy, tvGiaTien, tvPhiGiaoHang, tvTotalPrice);
    }

    private void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myCartAdapter);
    }

    private void setControl(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        txtEmptyCart = view.findViewById(R.id.txtEmptyCart);
        txtTotalPrice = view.findViewById(R.id.txtTotalPrice);
        btnBuy = view.findViewById(R.id.btnBuy);
        llBuy = view.findViewById(R.id.scrollView2);
        tvGiaTien = view.findViewById(R.id.tvGiaTien);
        tvPhiGiaoHang = view.findViewById(R.id.tvPhiGiaoHang);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        edtAddress = view.findViewById(R.id.edtAddress);
        ivEditPhone = view.findViewById(R.id.ivEditPhone);
        ivEditAddress = view.findViewById(R.id.ivEditAddress);
        edtPhone = view.findViewById(R.id.edtPhone);
        payment_methods = view.findViewById(R.id.payment_methods);
        ivEllipsis = view.findViewById(R.id.ivEllipsis);
        ivPayment = view.findViewById(R.id.ivPayment);
    }
    public void payment_method() {
    final String[] paymentMethods = getResources().getStringArray(R.array.payment_methods);
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    builder.setTitle("Chọn phương thức thanh toán");
    builder.setItems(paymentMethods, (dialog, which) -> {
        String selectedPaymentMethod = paymentMethods[which];
        payment_methods.setText(selectedPaymentMethod);
        switch (selectedPaymentMethod) {
            case "Thanh toán khi nhận hàng":
                ivPayment.setImageResource(R.drawable.cash);
                break;
            case "Quét mã QR":
                ivPayment.setImageResource(R.drawable.qr);
                break;
            default:
                break;
        }
        dialog.dismiss();
    });
    builder.show();
}
    public void thongBaoThanhCong() {
        NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(requireActivity(), NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.coffee_icon)
                .setContentTitle("Thanh toán thành công")
                .setContentText("Đơn hàng của bạn đang được xử lý.")
                .setContentInfo("Thông tin");

        notificationManager.notify(1, notificationBuilder.build());
    }
    @Override
    public void onTaskCompleted(String price, String describe) {
        double price1=Double.parseDouble(price);
        double price2=Double.parseDouble((String) tvTotalPrice.getText());
        String des=randomDescription;
        des="2k";
        if(price1>=price2&&describe.contains(des)){
            trangThai=1;
        }
    }
}
