package com.example.giua_ki.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.example.giua_ki.R;
import com.example.giua_ki.adapter.AdapterViewPager;
import com.example.giua_ki.fragment.AccountFragment;
import com.example.giua_ki.fragment.CartFragment;
import com.example.giua_ki.fragment.HomeFragment;
import com.example.giua_ki.fragment.OrderFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import vn.zalopay.sdk.ZaloPaySDK;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_nav);
        setupViewPagerAndBottomNav();
    }

    private void setupViewPagerAndBottomNav() {
        viewPager2 = findViewById(R.id.viewPager);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);

        fragmentArrayList.add(new HomeFragment());
        fragmentArrayList.add(new CartFragment());
        fragmentArrayList.add(new OrderFragment());
        fragmentArrayList.add(new AccountFragment());

        viewPager2.setAdapter(new AdapterViewPager(this, fragmentArrayList));
        viewPager2.setUserInputEnabled(false);
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
    }
}
