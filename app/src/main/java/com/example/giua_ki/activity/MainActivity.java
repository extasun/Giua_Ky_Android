package com.example.giua_ki.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.giua_ki.R;
import com.example.giua_ki.adapter.AdapterViewPager;
import com.example.giua_ki.database.DataHandler;
import com.example.giua_ki.fragment.AccountFragment;
import com.example.giua_ki.fragment.CartFragment;
import com.example.giua_ki.fragment.HomeFragment;
import com.example.giua_ki.fragment.OrderFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_nav);
        setControl();
        setupViewPagerAndBottomNav();
        setUpBadge();
    }

    private void setControl() {
        viewPager2 = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNav);
    }

    private void setUpBadge() {
        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.itCart);
        DataHandler.countItemsInCart(count -> {
            if (count > 0) {
                badgeDrawable.setNumber(count);
                badgeDrawable.setVisible(true);
            } else {
                badgeDrawable.setVisible(false);
            }
        });
    }

    private void setupViewPagerAndBottomNav() {
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
