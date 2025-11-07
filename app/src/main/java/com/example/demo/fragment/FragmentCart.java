package com.example.demo.fragment;

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
import com.example.demo.EditItemBottomSheet;
import com.example.demo.ProductData;
import com.example.demo.R;
import com.example.demo.adapter.CartAdapter;
import com.example.demo.model.Product;
import java.util.ArrayList;
import java.util.List;

public class FragmentCart extends Fragment implements CartAdapter.OnAction {

    private RecyclerView recyclerViewCart;
    private CheckBox checkAll;
    private TextView tvTotal;
    private Button btnMuaHang;
    private CartAdapter adapter;
    private List<Product> cartList = new ArrayList<>();
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        checkAll = view.findViewById(R.id.checkAll);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnMuaHang = view.findViewById(R.id.btnMuaHang);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartList = ProductData.cartList;

        adapter = new CartAdapter(requireContext(), cartList, this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewCart.setAdapter(adapter);

        // Check tất cả
        checkAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Product item : cartList) {
                item.setSelected(isChecked);
            }
            adapter.notifyDataSetChanged();
            updateTotal();
        });

        // Nút Mua hàng
        btnMuaHang.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
        });

        // Nút Sửa / Hoàn tất
        TextView textEdit = view.findViewById(R.id.text_edit_cart);
        textEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            adapter.setEditMode(isEditMode);
            adapter.notifyDataSetChanged();
            textEdit.setText(isEditMode ? "Hoàn tất" : "Sửa");
        });

        updateTotal();
    }

    private void updateTotal() {
        int total = 0;
        for (Product item : cartList) {
            if (item.isSelected()) {
                // ĐÃ SỬA: getPrice() trả String → parse thành int
                try {
                    int price = Integer.parseInt(item.getPrice().replaceAll("[^0-9]", ""));
                    total += price * item.getQuantity();
                } catch (Exception e) {
                    // Nếu lỗi parse → bỏ qua hoặc dùng giá mặc định
                }
            }
        }
        tvTotal.setText(String.format("Tổng: %,d VND", total));
    }

    @Override
    public void onIncrease(Product item) {
        item.setQuantity(item.getQuantity() + 1);
        adapter.notifyDataSetChanged();
        updateTotal();
    }

    @Override
    public void onDecrease(Product item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            adapter.notifyDataSetChanged();
            updateTotal();
        }
    }

    @Override
    public void onDelete(Product item) {
        cartList.remove(item);
        adapter.notifyDataSetChanged();
        updateTotal();
    }

    @Override
    public void onToggleSelect(Product item, boolean selected) {
        item.setSelected(selected);
        updateTotal();
    }

    @Override
    public void onEdit(Product item) {
        EditItemBottomSheet bottomSheet = EditItemBottomSheet.newInstance(item);
        bottomSheet.show(getChildFragmentManager(), "EditItem");
    }
}