package com.example.demo.fragment;

import android.content.Intent;
import android.os.Bundle;
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
    private boolean isCartLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvEdit = view.findViewById(R.id.tvEdit);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        emptyCartView = view.findViewById(R.id.tv_empty_cart);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Tạo adapter trước
        adapter = new CartAdapter(cartItems, requireContext(), this);
        adapter.setCheckListener(this);
        recyclerView.setAdapter(adapter);

        userID = SessionManager.getUserID(requireContext());
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            showEmptyState();
            return view;
        }

        loadCart();

        tvEdit.setOnClickListener(v -> {
            boolean isEdit = !adapter.isEditMode();
            tvEdit.setText(isEdit ? "Hoàn thành" : "Sửa");
            adapter.setEditMode(isEdit);
        });

        cbSelectAll.setOnCheckedChangeListener((btn, isChecked) -> selectAll(isChecked));
        btnCheckout.setOnClickListener(v -> gotoConfirm());

        return view;
    }

    private void loadCart() {
        if (isCartLoaded) return;
        isCartLoaded = true;

        ApiClient.getClient().create(ApiService.class)
                .viewCart(userID)
                .enqueue(new Callback<CartResponse>() {
                    @Override
                    public void onResponse(Call<CartResponse> call, Response<CartResponse> res) {
                        if (!isAdded()) return;

                        List<CartResponse.CartItem> items = new ArrayList<>();
                        if (res.isSuccessful() && res.body() != null) {
                            CartResponse.Data data = res.body().getData();
                            if (data != null) {
                                items = data.getItems();
                            }
                        }

                        cartItems.clear();
                        cartItems.addAll(items);
                        adapter.notifyDataSetChanged();

                        if (cartItems.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                            onUpdateTotal();
                        }
                    }

                    @Override
                    public void onFailure(Call<CartResponse> call, Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            showEmptyState();
                        }
                    }
                });
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyCartView.setVisibility(View.VISIBLE);
        tvTotal.setText("Tổng: 0đ");
        cbSelectAll.setChecked(false);
        cbSelectAll.setVisibility(View.GONE);
        btnCheckout.setEnabled(false);
    }

    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyCartView.setVisibility(View.GONE);
        cbSelectAll.setVisibility(View.VISIBLE);
        btnCheckout.setEnabled(true);
    }

    private void selectAll(boolean select) {
        for (CartResponse.CartItem item : cartItems) {
            item.setSelected(select);
        }
        adapter.notifyDataSetChanged();
        onUpdateTotal();
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
        long total = selected.stream().mapToLong(i -> i.getSubtotal()).sum();
        Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);
        intent.putExtra("selectedItems", new ArrayList<>(selected));
        intent.putExtra("totalMoney", total);
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
        isCartLoaded = false;
        loadCart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userID != null) {
            isCartLoaded = false;
            loadCart();
        }
    }
}