package com.example.giua_ki.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giua_ki.R;
import com.example.giua_ki.adapter.BannerAdapter;
import com.example.giua_ki.adapter.CategoryAdapter;
import com.example.giua_ki.adapter.ProductAdapter;
import com.example.giua_ki.model.CategoryModel;
import com.example.giua_ki.model.ProductModel;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private final ArrayList<ProductModel> productModelList = new ArrayList<>();
    private final ArrayList<ProductModel> searchModelList = new ArrayList<>();
    private SearchView searchView;
    private ProductAdapter adapter;
    private RecyclerView recyclerViewProducts;
    private RecyclerView recyclerViewCategory;
    private CategoryModel currentCategory = null;
    private CategoryAdapter adapterCategory = null;
    private Handler handler;
    private Runnable runnable;
    private View view;
    private int currentProduct = 0;
    private TextView tvHello;
    private RecyclerView recyclerViewBanner;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        setControl();
        fetchDataFromFirebase();
        fetchBannerFromFirebase();
        setSearchView();
        loadCategories();
        setHello();
        setRecyclerViewProducts();
        return view;
    }

    private void setRecyclerViewProducts() {
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        adapter = new ProductAdapter(productModelList);
        recyclerViewProducts.setAdapter(adapter);
    }

    private void setControl() {
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        recyclerViewCategory = view.findViewById(R.id.recyclerViewCategory);
        searchView = view.findViewById(R.id.svCoffees);
        tvHello = view.findViewById(R.id.tvHello);
        recyclerViewBanner = view.findViewById(R.id.recyclerViewBanner);
    }

    private void setHello() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 10) {
            tvHello.setText(R.string.goodmorning);
        } else if (hour >= 10 && hour < 13) {
            tvHello.setText(R.string.goodluch);
        } else if (hour >= 13 && hour < 18) {
            tvHello.setText(R.string.goodafternoon);
        } else {
            tvHello.setText(R.string.goodnight);
        }
    }

    private void setSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<ProductModel> filteredList = filter(searchModelList, newText);
                adapter.updateList(filteredList);
                recyclerViewProducts.setAdapter(adapter);
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
    private void fetchBannerFromFirebase() {
        List<String> bannerImages = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Banner")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String bannerUrl = snapshot.getValue(String.class);
                            if (bannerUrl != null) {
                                bannerImages.add(bannerUrl);
                            }
                        }
                        setBanner(bannerImages);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("bannerfb", "Fetch data cancelled: " + databaseError.getMessage());
                    }
                });
    }
    private void setBanner(List<String> bannerImages) {
        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        recyclerViewBanner.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewBanner.setAdapter(bannerAdapter);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentProduct >= bannerImages.size()) {
                    currentProduct = 0;
                    recyclerViewBanner.scrollToPosition(currentProduct);
                } else {
                    recyclerViewBanner.smoothScrollToPosition(currentProduct);
                }
                currentProduct++;
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    private void fetchDataFromFirebase() {
        FirebaseDatabase.getInstance().getReference("Products")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        productModelList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ProductModel productModel = snapshot.getValue(ProductModel.class);
                            if (productModel != null) {
                                productModelList.add(productModel);
                                searchModelList.add(productModel);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("productsfb", "Fetch data cancelled: " + databaseError.getMessage());
                    }
                });
    }

    public List<ProductModel> filter(List<ProductModel> models, String query) {
        String query1 = query.toLowerCase(Locale.getDefault());
        List<ProductModel> filteredList = new ArrayList<>();
        for (ProductModel model : models) {
            String text = model.getName().toLowerCase(Locale.getDefault());
            if (text.contains(query1)) {
                filteredList.add(model);
            }
        }
        return filteredList;
    }
    private void loadCategories() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getActivity());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY);
        recyclerViewCategory.setLayoutManager(layoutManager);
        FirebaseDatabase.getInstance().getReference("Categories")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<CategoryModel> categoryList = new ArrayList<>();
                        for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                            CategoryModel category = categorySnapshot.getValue(CategoryModel.class);
                            categoryList.add(category);
                        }
                        adapterCategory = new CategoryAdapter(categoryList, selectedCategory -> {
                            currentCategory = selectedCategory;
                            if (currentCategory.getName().equals("All")) {
                                adapter.updateList(productModelList);
                                adapter.notifyDataSetChanged();
                            } else {
                                filterProducts(currentCategory);
                            }
                            adapterCategory.notifyDataSetChanged();
                        });
                        recyclerViewCategory.setAdapter(adapterCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }
    @SuppressLint("NotifyDataSetChanged")
    private void filterProducts(CategoryModel selectedCategory) {
        List<ProductModel> filteredList = new ArrayList<>();
        for (ProductModel product : productModelList) {
            if (product.getCategory() != null && selectedCategory.getName() != null && product.getCategory().equals(selectedCategory.getName())) {
                filteredList.add(product);
            }
        }
        adapter.updateList(filteredList);
        adapter.notifyDataSetChanged();
    }
}