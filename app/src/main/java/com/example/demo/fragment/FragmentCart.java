/*
package com.example.demo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.CartAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartItem;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;

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
    private CartAdapter adapter;
    private List<CartItem> items = new ArrayList<>();
    private ApiService api;
    private String userID;

    // Lưu trạng thái các item được chọn
    private Map<String, Boolean> selectedItems = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.recyclerViewCart);
        tvTotal = view.findViewById(R.id.tvTotal);

        api = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userID = prefs.getString("USER_ID", null);

        if (userID == null) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new CartAdapter(requireContext(), items, this);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        loadCart();
    }

    private void loadCart() {
        api.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    CartResponse.Data data = response.body().getData();
                    items.clear();
                    if (data.getItems() != null) items.addAll(data.getItems());
                    adapter.notifyDataSetChanged();

                    tvTotal.setText(String.format("Tổng: %,d đ", data.getTotalMoney()));

                    // Đặt mặc định tất cả là chọn
                    selectedItems.clear();
                    for (CartItem item : items) {
                        selectedItems.put(item.getId(), true);
                    }
                } else {
                    Toast.makeText(requireContext(), "Không có dữ liệu giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e("API", "loadCart", t);
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================== CartAdapter.OnAction ==================

    @Override
    public void onIncrease(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());

        api.increaseItem(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    item.setQuantity(item.getQuantity() + 1);
                    adapter.notifyDataSetChanged();
                    loadCart(); // tải lại tổng tiền
                } else {
                    Toast.makeText(requireContext(), "Không tăng được số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDecrease(CartItem item) {
        if (item.getQuantity() <= 1) {
            Toast.makeText(requireContext(), "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());

        api.decreaseItem(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    item.setQuantity(item.getQuantity() - 1);
                    adapter.notifyDataSetChanged();
                    loadCart();
                } else {
                    Toast.makeText(requireContext(), "Không giảm được số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
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
                    adapter.notifyDataSetChanged();
                    loadCart();
                } else {
                    Toast.makeText(requireContext(), "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //trang bo sung kiem tra lai
    @Override
    public void onDataChanged() {
        double selectedTotal = 0;
        if (items != null) {
            for (CartItem item : items) {
                if (item.isSelected()) { // Chỉ cộng tiền item nào được tick
                    selectedTotal += item.getPrice() * item.getQuantity();
                }
            }
        }
        // Cập nhật TextView tổng tiền (và nút Mua hàng)
        tvTotal.setText(String.format("Tổng: %,.0f đ", selectedTotal));
        // TODO: Cập nhật nút Mua hàng
        // btnPurchase.setText(String.format("Mua hàng (%,.0f đ)", selectedTotal));
    }
    @Override
    public void onToggleSelect(CartItem item, boolean selected) {
        selectedItems.put(item.getId(), selected);
        updateTotal();
    }

    private void updateTotal() {
        int total = 0;
        for (CartItem item : items) {
            if (selectedItems.getOrDefault(item.getId(), false)) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        tvTotal.setText(String.format("Tổng: %,d đ", total));
    }
}
*/
// File: FragmentCart.java (ĐÃ FIX HOÀN CHỈNH)
package com.example.demo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.adapters.CartAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartItem;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.MenuItem;

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
    private CheckBox cbAll;
    private TextView btnPurchase;
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
        cbAll = view.findViewById(R.id.cbAll);
        btnPurchase Protect = view.findViewById(R.id.btnPurchase);

        api = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userID = prefs.getString("USER_ID", null);

        if (userID == null) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new CartAdapter(requireContext(), items, this);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        cbAll.setOnCheckedChangeListener((btn, isChecked) -> {
            for (CartItem item : items) {
                item.setSelected(isChecked);
                selectedItems.put(item.getId(), isChecked);
            }
            adapter.notifyDataSetChanged();
            updateTotal();
        });

        loadMenuAndCart();
    }

    private void loadMenuAndCart() {
        // B1: Lấy toàn bộ menu để có ảnh + màu
        api.getAllMenu().enqueue(new Callback<List229<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    menuItems = response.body();
                    loadCart(); // Sau khi có menu → mới load giỏ hàng
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi tải menu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        api.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    items.clear();
                    selectedItems.clear();
                    List<CartItem> serverItems = response.body().getCartData().getItems();
                    if (serverItems != null) {
                        for (CartItem serverItem : serverItems) {
                            // Tìm menu tương ứng để lấy ảnh + màu
                            for (MenuItem menu : menuItems) {
                                if (menu.getProductID().equals(serverItem.getProductID())) {
                                    serverItem.setPicture(menu.getPicture());
                                    serverItem.setBackgroundColor(getColorByName(menu.getName()));
                                    break;
                                }
                            }
                            items.add(serverItem);
                            selectedItems.put(serverItem.getId(), true);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateTotal();
                    cbAll.setChecked(true);
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
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
        for (CartItem item : items) {
            if (selectedItems.getOrDefault(item.getId(), false)) {
                total += item.getSubtotal();
            }
        }
        tvTotal.setText(String.format("Tổng: %,d đ", total));
        btnPurchase.setText(String.format("Mua hàng (%,d đ)", total));
    }

    @Override
    public void onToggleSelect(CartItem item, boolean selected) {
        selectedItems.put(item.getId(), selected);
        item.setSelected(selected);
        boolean allChecked = true;
        for (CartItem i : items) {
            if (!selectedItems.getOrDefault(i.getId(), false)) {
                allChecked = false;
                break;
            }
        }
        cbAll.setChecked(allChecked);
        updateTotal();
    }

    @Override
    public void onDataChanged() {
        updateTotal();
    }

    // onIncrease, onDecrease, onDelete giữ nguyên như cũ
    // Chỉ thêm loadCart() sau mỗi hành động
}