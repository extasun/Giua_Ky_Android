package com.example.giua_ki.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giua_ki.R;
import com.example.giua_ki.adapter.BannerAdapter;
import com.example.giua_ki.adapter.ProductAdapter;
import com.example.giua_ki.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private final ArrayList<ProductModel> productModelList = new ArrayList<>();
    private final ArrayList<ProductModel> searchModelList = new ArrayList<>();
    private SearchView searchView;
    private ProductAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<String> categories = new ArrayList<>();
    Handler handler;
    Runnable runnable;
    View view;
    private int currentProduct = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        adapter = new ProductAdapter(productModelList);
        recyclerView.setAdapter(adapter);
        searchView = view.findViewById(R.id.svCoffees);
        fetchDataFromFirebase();

        return view;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
    private void setBanner(View view, List<ProductModel> productModelList) {
        RecyclerView recyclerViewBanner = view.findViewById(R.id.recyclerViewBanner);
        recyclerViewBanner.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        BannerAdapter bannerAdapter = new BannerAdapter(productModelList);
        recyclerViewBanner.setAdapter(bannerAdapter);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentProduct >= productModelList.size()) {
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
    private List<ProductModel> discountedProducts() {
        List<ProductModel> discountedProducts = new ArrayList<>();
        for (ProductModel product : productModelList) {
            if (product.getDiscount() > 0) {
                discountedProducts.add(product);
            }
        }
        return discountedProducts;
    }
    private void fetchDataFromFirebase() {
        FirebaseDatabase.getInstance().getReference("Products")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ProductModel productModel = snapshot.getValue(ProductModel.class);
                            if (productModel != null) {
                                productModelList.add(productModel);
                                searchModelList.add(productModel);
                            }
                        }
                        setBanner(view,productModelList);
                        Log.d("productModelList", String.valueOf(productModelList.size()));
                        adapter.notifyDataSetChanged();

                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                List<ProductModel> filteredList = filter(searchModelList, newText);
                                adapter.updateList(filteredList);
                                recyclerView.setAdapter(adapter);
                                return true;
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
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
}