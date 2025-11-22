package com.example.demo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demo.R;
import com.example.demo.adapters.CartAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartResponse;
import com.example.demo.ConfirmOrderActivity;
import com.example.demo.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class FragmentCart extends Fragment implements CartAdapter.OnCartUpdateListener, CartAdapter.OnItemCheckListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView tvTotal, tvEdit;
    private CheckBox cbSelectAll;
    private Button btnCheckout;
    private View emptyCartView;
    private List<CartResponse.CartItem> cartItems = new ArrayList<>();
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvEdit = view.findViewById(R.id.tvEdit);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        emptyCartView = view.findViewById(R.id.tv_empty_cart); // ← ĐÚNG ID trong XML

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        userID = SessionManager.getUserID(requireContext());
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            showEmptyState();
            return view;
        }

        loadCart();

        tvEdit.setOnClickListener(v -> {
            if (adapter != null) {
                boolean isEdit = !adapter.isEditMode();
                tvEdit.setText(isEdit ? "Hoàn thành" : "Sửa");
                adapter.setEditMode(isEdit);
            }
        });

        cbSelectAll.setOnCheckedChangeListener((btn, isChecked) -> selectAll(isChecked));

        btnCheckout.setOnClickListener(v -> gotoConfirm());

        return view;
    }

    private void loadCart() {
        ApiClient.getClient().create(ApiService.class)
                .viewCart(userID)
                .enqueue(new Callback<CartResponse>() {
                    @Override
                    public void onResponse(Call<CartResponse> call, Response<CartResponse> res) {
                        if (!isAdded()) return;

                        if (res.isSuccessful() && res.body() != null && res.body().getData() != null) {
                            cartItems.clear();
                            cartItems.addAll(res.body().getData().getItems());

                            Log.d("CART_LOAD", "Cart loaded, items count: " + cartItems.size());

                            if (cartItems.isEmpty()) {
                                // GIỎ TRỐNG → HIỆN EMPTY + XÓA ADAPTER
                                showEmptyState();
                                recyclerView.setAdapter(null);
                                return;
                            }

                            // CÓ MÓN → HIỆN DANH SÁCH
                            adapter = new CartAdapter(cartItems, requireContext(), FragmentCart.this);
                            adapter.setCheckListener(FragmentCart.this);
                            adapter.setConfirmMode(false);
                            recyclerView.setAdapter(adapter);
                            hideEmptyState();
                            onUpdateTotal();

                        } else {
                            // SERVER TRẢ RỖNG HOẶC LỖI
                            cartItems.clear();
                            showEmptyState();
                            recyclerView.setAdapter(null); // ← QUAN TRỌNG
                        }
                    }

                    @Override
                    public void onFailure(Call<CartResponse> call, Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                        cartItems.clear();
                        showEmptyState();
                        recyclerView.setAdapter(null); // ← QUAN TRỌNG
                        Log.e("CART_LOAD", "Network error: " + t.getMessage());
                    }
                });
    }

    private void selectAll(boolean select) {
        for (CartResponse.CartItem item : cartItems) {
            item.setSelected(select);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        onUpdateTotal();
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        if (emptyCartView != null) {
            emptyCartView.setVisibility(View.VISIBLE);
        }
        tvTotal.setText("Tổng: 0đ");
        cbSelectAll.setChecked(false);
        cbSelectAll.setVisibility(View.GONE);
        btnCheckout.setEnabled(false);
    }

    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        if (emptyCartView != null) {
            emptyCartView.setVisibility(View.GONE);
        }
        cbSelectAll.setVisibility(View.VISIBLE);
        btnCheckout.setEnabled(true);
    }

    private void gotoConfirm() {
        List<CartResponse.CartItem> selected = new ArrayList<>();
        for (CartResponse.CartItem item : cartItems) {
            if (item.isSelected()) selected.add(item);
        }
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Chọn ít nhất 1 món", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUpdateTotal() {
        long total = cartItems.stream()
                .filter(CartResponse.CartItem::isSelected)
                .mapToLong(CartResponse.CartItem::getSubtotal)
                .sum();
        tvTotal.setText(String.format("Tổng: %,dđ", total));

        boolean allSelected = !cartItems.isEmpty() &&
                cartItems.stream().allMatch(CartResponse.CartItem::isSelected);
        cbSelectAll.setChecked(allSelected);
    }

    @Override
    public void onCartUpdated() {
        loadCart(); // ← TỰ ĐỘNG RELOAD → HIỆN EMPTY NẾU TRỐNG
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userID != null) {
            loadCart();
        }
    }
}