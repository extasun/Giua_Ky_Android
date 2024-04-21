package com.example.giua_ki.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.giua_ki.R;
import com.example.giua_ki.adapter.AdapterViewPager;
import com.example.giua_ki.fragment.AccountFragment;
import com.example.giua_ki.fragment.CartFragment;
import com.example.giua_ki.fragment.HomeFragment;
import com.example.giua_ki.fragment.OrderFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

import vn.zalopay.sdk.ZaloPaySDK;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ViewPager2 viewPager2;
    ArrayList<Fragment> fragmentArrayList = new ArrayList<>();

    boolean isSwipingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_nav);
        createBottomNav();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createBottomNav() {
        viewPager2=findViewById(R.id.viewPager);
        bottomNavigationView=findViewById(R.id.bottomNav);
        fragmentArrayList.add(new HomeFragment());
        fragmentArrayList.add(new CartFragment());
        fragmentArrayList.add(new OrderFragment());
        fragmentArrayList.add(new AccountFragment());
        AdapterViewPager adapterViewPager=new AdapterViewPager(this,fragmentArrayList);
        viewPager2.setAdapter(adapterViewPager);
        viewPager2.setUserInputEnabled(isSwipingEnabled);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) bottomNavigationView.setSelectedItemId(R.id.itHome);
                if (position == 1) bottomNavigationView.setSelectedItemId(R.id.itCart);
                if (position == 2) bottomNavigationView.setSelectedItemId(R.id.itOrder);
                if (position == 3) bottomNavigationView.setSelectedItemId(R.id.itAccount);
                super.onPageSelected(position);
            }
        });
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId()==R.id.itHome){
                viewPager2.setCurrentItem(0, true);
            }
            if (item.getItemId()==R.id.itCart){
                viewPager2.setCurrentItem(1, true);
            }
            if (item.getItemId()==R.id.itOrder){
                viewPager2.setCurrentItem(2, true);
            }
            if (item.getItemId()==R.id.itAccount){
                viewPager2.setCurrentItem(3, true);
            }
            return true;
        });
        bottomNavigationView.setOnTouchListener((v, event) -> true);
        bottomNavigationView.setOnClickListener(v -> {
            isSwipingEnabled = !isSwipingEnabled;
            viewPager2.setUserInputEnabled(isSwipingEnabled);
        });
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);

        // Gọi phương thức trong Fragment
        CartFragment cartFragment = (CartFragment) fragmentArrayList.get(1);
        if (cartFragment != null) {
            cartFragment.handleNewIntent(intent);
        }
    }
}
