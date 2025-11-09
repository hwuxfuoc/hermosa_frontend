// CartFragment.java
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

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        userID = SessionManager.getUserID(requireContext());
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
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
                            cartItems = new ArrayList<>(res.body().getData().getItems());
                            adapter = new CartAdapter(cartItems, requireContext(), FragmentCart.this);
                            adapter.setCheckListener(FragmentCart.this);
                            adapter.setConfirmMode(false);
                            recyclerView.setAdapter(adapter);
                            onUpdateTotal();
                        } else {
                            cartItems.clear();
                            updateEmptyUI();
                        }
                    }

                    @Override
                    public void onFailure(Call<CartResponse> call, Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
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

    private void gotoConfirm() {
        List<CartResponse.CartItem> selected = new ArrayList<>();
        for (CartResponse.CartItem item : cartItems) {
            if (item.isSelected()) selected.add(item);
        }

        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Chọn ít nhất 1 món", Toast.LENGTH_SHORT).show();
            return;
        }

        long total = selected.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);
        intent.putExtra("selectedItems", new ArrayList<>(selected));
        intent.putExtra("totalMoney", total);
        startActivity(intent);
    }

    private void updateEmptyUI() {
        if (adapter != null) adapter.notifyDataSetChanged();
        tvTotal.setText("Tổng: 0đ");
        cbSelectAll.setChecked(false);
    }

    @Override public void onUpdateTotal() {
        long total = cartItems.stream()
                .filter(CartResponse.CartItem::isSelected)
                .mapToLong(CartResponse.CartItem::getSubtotal)
                .sum();
        tvTotal.setText(String.format("Tổng: %,dđ", total));
        cbSelectAll.setChecked(cartItems.stream().allMatch(CartResponse.CartItem::isSelected));
    }

    @Override public void onCartUpdated() {
        loadCart();
    }

    @Override public void onResume() {
        super.onResume();
        loadCart();
    }
}