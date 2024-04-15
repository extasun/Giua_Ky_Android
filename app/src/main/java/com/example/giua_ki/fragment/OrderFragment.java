package com.example.giua_ki.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.giua_ki.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class OrderFragment extends Fragment {
    DatabaseReference databaseReference;
    View view;
    public OrderFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Orders");
        view = inflater.inflate(R.layout.fragment_order, container, false);
        setControl();
        return view;
    }

    private void setControl() {
    }

}