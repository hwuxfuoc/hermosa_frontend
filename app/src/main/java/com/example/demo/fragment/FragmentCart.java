package com.example.demo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapter.CartAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.model.CartItem;
import com.example.demo.model.CommonResponse;
import com.example.demo.model.MenuItem;
import com.example.demo.model.MenuResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCart extends Fragment implements CartAdapter.OnAction {

    private RecyclerView rv;
    private TextView tvTotal;
    private CheckBox cbSelectAll;
    private Button btnPurchase;
    private CartAdapter adapter;
    private List<CartItem> items = new ArrayList<>();
    private List<MenuItem> menuItems = new ArrayList<>();
    private ApiService api;
    private String userID;
    private Map<String, Boolean> selectedItems = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.recyclerViewCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        btnPurchase = view.findViewById(R.id.btnPurchase);

        api = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userID = prefs.getString("USER_ID", null);

        if (userID == null) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new CartAdapter(requireContext(), items, userID, this);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        cbSelectAll.setOnCheckedChangeListener((btn, isChecked) -> {
            for (CartItem item : items) {
                item.setSelected(isChecked);
                selectedItems.put(item.getId(), isChecked);
            }
            adapter.notifyDataSetChanged();
            updateTotal();
        });

        btnPurchase.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã chọn " + getSelectedCount() + " món - Tổng: " + tvTotal.getText(), Toast.LENGTH_LONG).show();
        });

        loadMenuAndCart();
    }

    private void loadMenuAndCart() {
        api.getAllProducts().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    menuItems.clear();
                    for (MenuResponse.MenuItem item : response.body().getData()) {
                        MenuItem menuItem = new MenuItem();
                        menuItem.setProductID(item.getProductID());
                        menuItem.setName(item.getName());
                        menuItem.setPicture(item.getPicture());
                        menuItems.add(menuItem);
                    }
                    loadCart(); // Sau khi có menu → load giỏ hàng
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi tải menu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        api.viewCart(userID).enqueue(new Callback<com.example.demo.model.CartResponse>() {
            @Override
            public void onResponse(Call<com.example.demo.model.CartResponse> call, Response<com.example.demo.model.CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    items.clear();
                    selectedItems.clear();

                    List<com.example.demo.model.CartResponse.CartItem> serverItems = response.body().getData().getItems();
                    if (serverItems != null) {
                        for (com.example.demo.model.CartResponse.CartItem serverItem : serverItems) {
                            // CHUYỂN ĐỔI THỦ CÔNG → CartItem (dùng trong app)
                            CartItem localItem = new CartItem();
                            localItem.setId(serverItem.getId());
                            localItem.setProductID(serverItem.getProductID());
                            localItem.setName(serverItem.getName());
                            localItem.setPrice(serverItem.getPrice());
                            localItem.setQuantity(serverItem.getQuantity());
                            localItem.setSubtotal(serverItem.getSubtotal());
                            localItem.setSize(serverItem.getSize());
                            localItem.setTopping(serverItem.getTopping() != null ? serverItem.getTopping().toArray(new String[0]) : new String[0]);
                            localItem.setNote(serverItem.getNote());
                            localItem.setImageUrl(serverItem.getPicture());

                            // Tìm ảnh + màu từ menu
                            for (MenuItem menu : menuItems) {
                                if (menu.getProductID().equals(serverItem.getProductID())) {
                                    localItem.setImageUrl(menu.getPicture());
                                    localItem.setColor(getColorByName(menu.getName()));
                                    break;
                                }
                            }

                            localItem.setSelected(true);
                            items.add(localItem);
                            selectedItems.put(localItem.getId(), true);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateTotal();
                    cbSelectAll.setChecked(true);
                }
            }

            @Override
            public void onFailure(Call<com.example.demo.model.CartResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getColorByName(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("strawberry")) return 0xFFF0BCBC;
        if (lower.contains("lemon")) return 0xFFF7E8A4;
        if (lower.contains("smooth")) return 0xFFFFC1C1;
        if (lower.contains("matcha")) return 0xFFE6F5D6;
        if (lower.contains("sandwich")) return 0xFFF9E4B7;
        return 0xFFF5F5F5;
    }

    private void updateTotal() {
        long total = 0;
        int count = 0;
        for (CartItem item : items) {
            if (selectedItems.getOrDefault(item.getId(), false)) {
                total += item.getSubtotal();
                count++;
            }
        }
        tvTotal.setText(String.format("Tổng: %,dđ", total));
        btnPurchase.setText(String.format("Mua hàng (%d)", count));
    }

    private int getSelectedCount() {
        int count = 0;
        for (Boolean b : selectedItems.values()) if (b) count++;
        return count;
    }

    // IMPLEMENT ĐẦY ĐỦ 5 METHOD
    @Override
    public void onToggleSelect(CartItem item, boolean selected) {
        selectedItems.put(item.getId(), selected);
        item.setSelected(selected);
        boolean allChecked = selectedItems.values().stream().allMatch(b -> b);
        cbSelectAll.setChecked(allChecked);
        updateTotal();
    }

    @Override
    public void onIncrease(CartItem item) {
        item.setQuantity(item.getQuantity() + 1);
        item.updateSubtotal();
        updateTotal();
        adapter.notifyDataSetChanged();
        syncToServer(item);
    }

    @Override
    public void onDecrease(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            item.updateSubtotal();
            updateTotal();
            adapter.notifyDataSetChanged();
            syncToServer(item);
        }
    }

    @Override
    public void onDelete(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());

        api.deleteItem(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    items.remove(item);
                    selectedItems.remove(item.getId());
                    adapter.notifyDataSetChanged();
                    updateTotal();
                    Toast.makeText(requireContext(), "Đã xóa món!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi xóa món!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDataChanged() {
        updateTotal();
    }

    private void syncToServer(CartItem item) {
        // Backend chưa có API update quantity → tạm bỏ
    }
}