package com.example.giua_ki.fragment;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giua_ki.R;
import com.example.giua_ki.adapter.SizeAdapter;
import com.example.giua_ki.database.DataHandler;
import com.example.giua_ki.model.ProductModel;
import com.example.giua_ki.model.SizeModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SizeModel selectedSize = sizeAdapter.getSelectedSize();
                if (selectedSize != null) {
                    int quantity = Integer.parseInt(textQuantity.getText().toString());
                        DataHandler.addToCart(product, selectedSize,quantity);
                }
                PopupDialog dialog = PopupDialog.getInstance(requireContext());
                dialog.setStyle(Styles.SUCCESS)
                        .setHeading(getString(R.string.thanh_cong))
                        .setDescription(getString(R.string.them_sp_thanh_cong))
                        .showDialog(new OnDialogButtonClickListener() {
                            @Override
                            public void onDismissClicked(Dialog dialog) {
                                super.onDismissClicked(dialog);
                            }
                        });
                new Handler().postDelayed(dialog::dismissDialog, 1500);
                dismiss();
            }
        });
    }

    public static SizeDialogFragment newInstance(ProductModel product) {
        SizeDialogFragment fragment = new SizeDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }
}