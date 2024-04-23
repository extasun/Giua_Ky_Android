package com.example.giua_ki.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.giua_ki.R;
import com.example.giua_ki.adapter.CartAdapter;
import com.example.giua_ki.database.DataHandler;
import com.example.giua_ki.fragment.CartFragment;
import com.example.giua_ki.listener.OnTaskCompleted;
import com.example.giua_ki.model.CartModel;
import com.example.giua_ki.paid.GoogleSheetsTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CreateOrder extends AppCompatActivity implements OnTaskCompleted {
    ArrayList<CartModel> orderModelArrayList;
    AlertDialog alertDialog;
    String dateTime;
    int trangThai=0;
    private GoogleSheetsTask googleSheetsTask;
    String randomDescription;
    String orderId;
    private ImageView ivPayment;
    private TextView payment_methods;
    private EditText edtAddress;
    private ImageView  ivUpdateAddress;
    private ImageView  ivEditAddress;

    private boolean isQrSaved = false;
    private TextView tvTotalPrice;
    private Button btnOrder;
    private TextView tvChangePayment;
    private RecyclerView recyclerViewOrder;
    private ImageView ivBack;
    private FusedLocationProviderClient fusedLocationClient;
    private final CartAdapter myCartAdapter = new CartAdapter(DataHandler.orderModelArrayList);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_order);
        randomDescription = generateRandomDescription();
        setControl();
        setButtonOrder();
        setDataForOrder();
        setRecyclerViewOrder();
        setBack();
        setEdit();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ivUpdateAddress.setOnClickListener(v -> updateAddressFromLocation());
        ivEditAddress.setOnClickListener(v -> setEdit());
    }

    private void updateAddressFromLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            getAddressFromCoordinates(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
    }
    private void getAddressFromCoordinates(double latitude, double longitude) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + latitude + "&lon=" + longitude;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        JSONObject addressObject = response.getJSONObject("address");
                        String road = addressObject.getString("road");
                        String quarter = addressObject.optString("quarter", "");
                        String suburb = addressObject.optString("suburb", "");
                        String city = addressObject.getString("city");
                        String address = road;
                        if (!quarter.isEmpty()) {
                            address += ", " + quarter;
                        }
                        if (!suburb.isEmpty()) {
                            address += ", " + suburb;
                        }
                        address += ", " + city;
                        edtAddress.setText(address);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("Error.Response", String.valueOf(error)));

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void setBack() {
        ivBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void setRecyclerViewOrder() {
        recyclerViewOrder.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrder.setAdapter(myCartAdapter);
    }

    private void setDataForOrder() {
            DataHandler.fetchDataForOrder(recyclerViewOrder, tvTotalPrice);
    }

    private void setButtonOrder() {
        btnOrder.setOnClickListener(v -> {
            thanhToanHoaDon();
        });
        tvChangePayment.setOnClickListener(v -> payment_method());
    }

    private void setControl() {
        edtAddress = findViewById(R.id.edtAddress);
        ivEditAddress = findViewById(R.id.ivEditAddress);
        ivUpdateAddress = findViewById(R.id.ivUpdateAddress);
        payment_methods = findViewById(R.id.payment_methods);
        ivPayment = findViewById(R.id.ivPayment);
        tvTotalPrice = findViewById(R.id.txtTotalPrice);
        btnOrder = findViewById(R.id.btnOrder);
        tvChangePayment = findViewById(R.id.tvChangePayment);
        recyclerViewOrder = findViewById(R.id.recyclerViewOrder);
        ivBack = findViewById(R.id.ivBack);

    }

    private void setQrSaved(ImageView qrCodeImageView, ImageButton btnSaveQR) {
        btnSaveQR.setOnClickListener(v -> {
            if (isQrSaved) {
                Snackbar.make(v, R.string.qr_saved, Snackbar.LENGTH_LONG).show();
                return;
            }

            qrCodeImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(qrCodeImageView.getDrawingCache());
            qrCodeImageView.setDrawingCacheEnabled(false);
            try {
                String path = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).toString();
                OutputStream fOut;
                File file = new File(path, "QRCodeThanhToan.jpg");
                fOut = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                fOut.flush();
                fOut.close();
                MediaStore.Images.Media.insertImage(this.getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
                Snackbar.make(v, R.string.qr_save, Snackbar.LENGTH_LONG).show();
                isQrSaved = true;
            } catch (Exception e) {
                Log.d("QRSave", "QR Code save failed: " + e.getMessage());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void thanhToanHoaDon() {
        if (payment_methods.getText().equals(getString(R.string.scan_qr))) {
            LayoutInflater inflater = LayoutInflater.from(CreateOrder.this);
            View overlayView = inflater.inflate(R.layout.qr, null);
            TextView nameTextView = overlayView.findViewById(R.id.nameTextView);
            TextView amountTextView = overlayView.findViewById(R.id.amountTextView);
            TextView descriptionTextView = overlayView.findViewById(R.id.descriptionTextView);
            TextView timeTextView=overlayView.findViewById(R.id.timeTextView);
            nameTextView.setText(R.string.name_host);
            amountTextView.setText(getString(R.string.price_bank)+tvTotalPrice.getText());
            descriptionTextView.setText(getString(R.string.note_bank)+randomDescription);
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(overlayView);
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            ImageButton btnSaveQR = overlayView.findViewById(R.id.btnSaveQR);
            setQrSaved(qrCodeImageView,btnSaveQR);
            ImageButton backButton = alertDialog.findViewById(R.id.backButton);
            if (backButton != null) {
                backButton.setOnClickListener(view -> alertDialog.dismiss());
            }
            ImageButton successButton = alertDialog.findViewById(R.id.successButton);
            assert successButton != null;
            successButton.setOnClickListener(view -> {
                googleSheetsTask = new GoogleSheetsTask(this);
                googleSheetsTask.execute();
                if(trangThai==1){
                    thongBaoThanhCong();
                    alertDialog.dismiss();
                    DataHandler.addToOrder(orderId,tvTotalPrice,dateTime,orderModelArrayList);
                    clearCart();
                    finish();
                }else {
                    Snackbar.make(view, R.string.cantfind, Snackbar.LENGTH_LONG).show();
                }
            });

            new CountDownTimer(600000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long secondsRemaining = millisUntilFinished / 1000;
                    long minutes = secondsRemaining / 60;
                    long seconds = secondsRemaining % 60;
                    String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                    timeTextView.setText(getString(R.string.time_con_lai) + timeRemaining);

                }

                public void onFinish() {
                    alertDialog.dismiss();
                }
            }.start();
        }else if (payment_methods.getText().equals("Thanh toán ZaloPay")){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            ZaloPaySDK.init(554, Environment.SANDBOX);
            try {
                com.example.giua_ki.zaloPay.Api.CreateOrder orderApi = new com.example.giua_ki.zaloPay.Api.CreateOrder();
                JSONObject data = orderApi.createOrder("1000");
                String code=data.getString("returncode");
                if (code.equals("1")) {
                    String token = data.getString("zptranstoken");
                    ZaloPaySDK.getInstance().payOrder(this, token, "demozpdk://app", new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
                            thongBaoThanhCong();
                            DataHandler.addToOrder(orderId,tvTotalPrice,dateTime,orderModelArrayList);
                            clearCart();
                            finish();
                        }

                        @Override
                        public void onPaymentCanceled(String s, String s1) {
                            Toast.makeText(CreateOrder.this, "Thanh toán bị hủy", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                            Toast.makeText(CreateOrder.this, "Thanh toán thất bại", Toast.LENGTH_LONG).show();
                        }

                    });
                }
            }
            catch (Exception e) {
                Toast.makeText(CreateOrder.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }


        }else{
            thongBaoThanhCong();
            DataHandler.addToOrder(orderId,tvTotalPrice,dateTime,orderModelArrayList);
            clearCart();
            finish();
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
                        Toast.makeText(CreateOrder.this, R.string.nhap_dia_chi, Toast.LENGTH_SHORT).show();
                    } else {
                        edtAddress.setEnabled(false);
                        edtAddress.setFocusable(false);
                        ivEditAddress.setImageResource(R.drawable.edit);
                        isEditing = false;
                    }
                }
            }
        });

    }
    public void payment_method() {
        final String[] paymentMethods = getResources().getStringArray(R.array.payment_methods);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.payment_method);
        builder.setItems(paymentMethods, (dialog, which) -> {
            String selectedPaymentMethod = paymentMethods[which];
            payment_methods.setText(selectedPaymentMethod);
            switch (selectedPaymentMethod) {
                case "Thanh toán khi nhận hàng":
                    ivPayment.setImageResource(R.drawable.cash);
                    break;
                case "Quét mã QR":
                    ivPayment.setImageResource(R.drawable.qrcode);
                    break;
                case "Thanh toán ZaloPay":
                    ivPayment.setImageResource(R.drawable.zalo);
                    break;
                default:
                    break;
            }
            dialog.dismiss();
        });
        builder.show();
    }
    public void thongBaoThanhCong() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.coffee_icon)
                .setContentTitle(getString(R.string.thanh_toan_thanh_cong))
                .setContentText(getString(R.string.don_hang_dang_xu_ly))
                .setContentInfo(getString(R.string.thong_tin));

        notificationManager.notify(1, notificationBuilder.build());
    }
    @Override
    public void onTaskCompleted(String price, String describe) {
        double price1=Double.parseDouble(price);
        double price2 = Double.parseDouble(tvTotalPrice.getText().toString().replace(",", ""));
        String des=randomDescription;
        des="2k";
        if(price1>=price2&&describe.contains(des)){
            trangThai=1;
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}