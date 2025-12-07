/*
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
                            cartItems.clear();
                            showEmptyState();
                            recyclerView.setAdapter(null);
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
        // 1. Lọc ra các món đã được tick chọn (CheckBox) để xử lý
        List<CartResponse.CartItem> selectedItems = new ArrayList<>();
        long tempTotal = 0;

        for (CartResponse.CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
                tempTotal += item.getSubtotal();
            }
        }

        // 2. Validate: Phải chọn ít nhất 1 món mới cho đi tiếp
        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn chưa chọn sản phẩm nào để thanh toán!", Toast.LENGTH_SHORT).show();
            Log.w("CART_DEBUG", "User bấm Mua hàng nhưng chưa tick chọn món nào.");
            return;
        }

        // 3. Ghi Log chi tiết để Debug (Xem user đang mua gì)
        Log.d("CART_DEBUG", "========== BẮT ĐẦU CHECKOUT ==========");
        Log.d("CART_DEBUG", "UserID: " + userID);
        Log.d("CART_DEBUG", "Số lượng món đã chọn: " + selectedItems.size());
        Log.d("CART_DEBUG", "Tổng tiền tạm tính (tại giỏ): " + tempTotal);

        for (CartResponse.CartItem item : selectedItems) {
            Log.d("CART_DEBUG", " + Món: " + item.getName() + " | SL: " + item.getQuantity() + " | Giá: " + item.getSubtotal());
        }
        Log.d("CART_DEBUG", "======================================");

        // 4. Chuyển sang màn hình Xác nhận đơn hàng
        Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);
        startActivity(intent);
    }

    */
/*private void gotoConfirm() {
        // 1. Kiểm tra xem có món nào được chọn không
        boolean hasSelection = cartItems.stream().anyMatch(CartResponse.CartItem::isSelected);

        if (!hasSelection) {
            Toast.makeText(requireContext(), "Vui lòng chọn ít nhất 1 món để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Log kiểm tra (Ghi log như bạn yêu cầu)
        Log.d("CART_CHECKOUT", "User bấm đặt hàng. Số lượng món: " + cartItems.size());

        // 3. Chuyển sang màn hình Xác nhận
        Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);
        // Nếu muốn truyền tổng tiền tạm tính sang để hiển thị luôn
        // intent.putExtra("TOTAL_PRICE", tvTotal.getText().toString());
        startActivity(intent);
    }*//*


    */
/*private void gotoConfirm() {
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
    }*//*


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
}*/
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
import com.example.demo.models.Order;
import com.example.demo.models.OrderResponse;
import com.example.demo.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        emptyCartView = view.findViewById(R.id.tv_empty_cart);

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

        // Xử lý sự kiện "Chọn tất cả"
        cbSelectAll.setOnClickListener(v -> {
            boolean isChecked = cbSelectAll.isChecked();
            selectAll(isChecked);
        });

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
                                showEmptyState();
                                recyclerView.setAdapter(null);
                                return;
                            }

                            // Khởi tạo Adapter
                            adapter = new CartAdapter(cartItems, requireContext(), FragmentCart.this);
                            adapter.setCheckListener(FragmentCart.this);
                            adapter.setConfirmMode(false);
                            recyclerView.setAdapter(adapter);

                            hideEmptyState();
                            onUpdateTotal(); // Tính lại tổng tiền ban đầu

                        } else {
                            cartItems.clear();
                            showEmptyState();
                            recyclerView.setAdapter(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<CartResponse> call, Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                        cartItems.clear();
                        showEmptyState();
                        recyclerView.setAdapter(null);
                        Log.e("CART_LOAD", "Network error: " + t.getMessage());
                    }
                });
    }

    private void selectAll(boolean select) {
        for (CartResponse.CartItem item : cartItems) {
            item.setSelected(select);
        }
        // Cập nhật lại toàn bộ danh sách để checkbox thay đổi theo
        if (adapter != null) adapter.notifyDataSetChanged();
        onUpdateTotal();
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        if (emptyCartView != null) emptyCartView.setVisibility(View.VISIBLE);
        tvTotal.setText("Tổng: 0đ");
        cbSelectAll.setChecked(false);
        cbSelectAll.setVisibility(View.GONE);
        btnCheckout.setEnabled(false);
    }

    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        if (emptyCartView != null) emptyCartView.setVisibility(View.GONE);
        cbSelectAll.setVisibility(View.VISIBLE);
        btnCheckout.setEnabled(true);
    }

/*private void gotoConfirm() {
        // 1. Lọc ra các món đã được tick chọn
        List<CartResponse.CartItem> selectedItems = new ArrayList<>();
        long tempTotal = 0;

        for (CartResponse.CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
                tempTotal += item.getSubtotal();
            }
        }

        // 2. Validate: Phải chọn ít nhất 1 món mới cho đi tiếp
        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn chưa chọn sản phẩm nào để thanh toán!", Toast.LENGTH_SHORT).show();
            Log.w("CART_DEBUG", "User bấm Mua hàng nhưng chưa tick chọn món nào.");
            return;
        }

        // 3. Ghi Log chi tiết để Debug
        Log.d("CART_DEBUG", "========== BẮT ĐẦU CHECKOUT ==========");
        Log.d("CART_DEBUG", "UserID: " + userID);
        Log.d("CART_DEBUG", "Số lượng món đã chọn: " + selectedItems.size());
        Log.d("CART_DEBUG", "Tổng tiền tạm tính (tại giỏ): " + tempTotal);

        for (CartResponse.CartItem item : selectedItems) {
            Log.d("CART_DEBUG", " + Món: " + item.getName() + " | SL: " + item.getQuantity() + " | Giá: " + item.getSubtotal());
        }
        Log.d("CART_DEBUG", "======================================");

        // 4. Chuyển sang màn hình Xác nhận đơn hàng
        Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);
        startActivity(intent);
    }*/
private void gotoConfirm() {
    List<CartResponse.CartItem> selectedItems = new ArrayList<>();
    long tempTotal = 0;

    for (CartResponse.CartItem item : cartItems) {
        if (item.isSelected()) {
            selectedItems.add(item);
            tempTotal += item.getSubtotal();
        }
    }

    // 2. Validate: Phải chọn ít nhất 1 món mới cho đi tiếp
    if (selectedItems.isEmpty()) {
        Toast.makeText(requireContext(), "Bạn chưa chọn sản phẩm nào để thanh toán!", Toast.LENGTH_SHORT).show();
        Log.w("CART_DEBUG", "User bấm Mua hàng nhưng chưa tick chọn món nào.");
        return;
    }

    // 3. Ghi Log chi tiết để Debug
    Log.d("CART_DEBUG", "========== BẮT ĐẦU CHECKOUT ==========");
    Log.d("CART_DEBUG", "UserID: " + userID);
    Log.d("CART_DEBUG", "Số lượng món đã chọn: " + selectedItems.size());
    Log.d("CART_DEBUG", "Tổng tiền tạm tính (tại giỏ): " + tempTotal);

    for (CartResponse.CartItem item : selectedItems) {
        Log.d("CART_DEBUG", " + Món: " + item.getName() + " | SL: " + item.getQuantity() + " | Giá: " + item.getSubtotal());
    }
    // 1. Lọc các món đã chọn và Validate
    // ... (Phần lọc và kiểm tra selectedItems giữ nguyên) ...

    // 2. Khóa nút
    btnCheckout.setEnabled(false);
    btnCheckout.setText("Đang tạo đơn...");

    // 3. CHỈ GỬI CÁC TRƯỜNG CẦN THIẾT (để Backend tự lấy giỏ hàng)
    Map<String, Object> body = new HashMap<>();
    body.put("userID", userID);

    // Note: Backend sẽ tự động đặt paymentStatus = "not_done" và status = "pending".

    Log.d("CART_DEBUG", "Đang gọi API tạo đơn chỉ với UserID...");

    // 4. GỌI API TẠO ĐƠN (chỉ cần userID)
    ApiClient.getClient().create(ApiService.class).createOrder(body).enqueue(new Callback<OrderResponse>() {
        @Override
        public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
            if (!isAdded()) return;
            btnCheckout.setEnabled(true);
            btnCheckout.setText("Mua hàng");

            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                Order orderData = response.body().getData();
                String orderID = response.body().getData().getOrderID();
                double autoDiscount = orderData.getDiscountAmount();
                String autoVoucherCode = orderData.getVoucherCodeApply();
                Log.d("CART_DEBUG", "TẠO ĐƠN THÀNH CÔNG: " + orderID);
                Log.d("CART_DEBUG", "Voucher tự động: " + autoVoucherCode + " - Giảm: " + autoDiscount);



                // 5. CHUYỂN MÀN HÌNH VÀ GỬI ORDER_ID
                Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);
                intent.putExtra("ORDER_ID", orderID);
                intent.putExtra("SELECTED_ITEMS", (Serializable) selectedItems);
                intent.putExtra("AUTO_DISCOUNT", autoDiscount);
                intent.putExtra("AUTO_VOUCHER_CODE", autoVoucherCode);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Tạo đơn thất bại", Toast.LENGTH_SHORT).show();
                Log.e("CART_DEBUG", "Lỗi API Tạo đơn: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<OrderResponse> call, Throwable t) {
            if (!isAdded()) return;
            btnCheckout.setEnabled(true);
            btnCheckout.setText("Mua hàng");
            Toast.makeText(requireContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            Log.e("CART_DEBUG", "Lỗi mạng: " + t.getMessage());
        }
    });
}

/*private void gotoConfirm() {
        // 1. Lọc ra các món đã được tick chọn
        ArrayList<CartResponse.CartItem> selectedItems = new ArrayList<>(); // Dùng ArrayList để dễ truyền Intent
        long tempTotal = 0;

        for (CartResponse.CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
                tempTotal += item.getSubtotal();
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn chưa chọn sản phẩm nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CART_DEBUG", "Số lượng món chuyển đi: " + selectedItems.size());

        // 2. Chuyển sang màn hình Xác nhận và GỬI KÈM DỮ LIỆU
        Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);

        // Truyền danh sách món đã chọn
        // Lưu ý: Class CartItem phải implements Serializable
        intent.putExtra("SELECTED_ITEMS", selectedItems);

        startActivity(intent);
    }

    private void gotoConfirm() {
        // 1. Lọc ra các món đã được tick chọn
        // Lưu ý: Dùng ArrayList để dễ truyền qua Intent (ArrayList implements Serializable)
        ArrayList<CartResponse.CartItem> selectedItems = new ArrayList<>();

        for (CartResponse.CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        // 2. Validate
        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ít nhất 1 món để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Chuyển màn hình & GỬI DỮ LIỆU (Phần bạn đang thiếu)
        Intent intent = new Intent(requireActivity(), ConfirmOrderActivity.class);

        // --- BỔ SUNG DÒNG NÀY ---
        // Truyền danh sách món ăn sang Activity kia
        // Yêu cầu: Class CartItem phải implements Serializable
        intent.putExtra("SELECTED_ITEMS", selectedItems);
        // ------------------------

        startActivity(intent);
    }*/

    @Override
    public void onUpdateTotal() {
        // Tính tổng tiền các món được chọn
        long total = cartItems.stream()
                .filter(CartResponse.CartItem::isSelected)
                .mapToLong(CartResponse.CartItem::getSubtotal)
                .sum();
        tvTotal.setText(String.format("Tổng: %,dđ", total));

        // Kiểm tra xem có đang chọn tất cả không để tick vào cbSelectAll
        boolean allSelected = !cartItems.isEmpty() &&
                cartItems.stream().allMatch(CartResponse.CartItem::isSelected);

        // Dùng setOnClickListener ở trên rồi nên ở đây chỉ setChecked thôi tránh loop vô hạn
        cbSelectAll.setOnCheckedChangeListener(null);
        cbSelectAll.setChecked(allSelected);
        cbSelectAll.setOnClickListener(v -> selectAll(cbSelectAll.isChecked()));
    }

    @Override
    public void onCartUpdated() {
        loadCart(); // Reload lại khi số lượng thay đổi hoặc xóa món
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userID != null) {
            loadCart();
        }
    }
}
