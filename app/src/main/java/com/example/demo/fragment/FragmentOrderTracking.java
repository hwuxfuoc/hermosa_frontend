/*
package com.example.demo.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.MainActivity;
import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartItem;
import com.example.demo.models.Order;
import com.example.demo.models.OrderResponse;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOrderTracking extends Fragment {

    // --- VIEW DECLARATION ---
    private ImageView btnBack;
    private LinearLayout layoutHeaderPending, layoutHeaderConfirmed;
    private TextView tvStatusTitle, tvStatusDesc, tvStatusTag, tvTimeEstimate, tvStatusMsg;
    private ImageView ivStep1, ivStep2, ivStep3, ivStep4;
    private View line1, line2, line3;

    // View hiển thị tiền
    private TextView tvTotalPriceList; // Tổng tiền món ăn (list)
    private TextView tvTotalPayment;   // Tổng tiền thanh toán cuối cùng (footer)
    private TextView tvPaymentMethodName; // Tên phương thức thanh toán
    private View layoutPaymentInfo;    // Layout chứa thông tin thanh toán (cần hiện lên)

    private TextView tvStoreName, tvAddressName, tvDetailAddress;
    private MaterialButton btnCancelOrder;
    private TextView tvCancelNote;
    private RecyclerView rvOrderItems;

    // --- DATA & API ---
    private String currentOrderID;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews(view);

        if (getArguments() != null) {
            currentOrderID = getArguments().getString("ORDER_ID", "");
        }

        setupEvents();

        if (currentOrderID != null && !currentOrderID.isEmpty()) {
            loadOrderDataFromApi(currentOrderID);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        layoutHeaderPending = view.findViewById(R.id.layoutHeaderPending);
        layoutHeaderConfirmed = view.findViewById(R.id.layoutHeaderConfirmed);

        tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        tvStatusDesc = view.findViewById(R.id.tvStatusDesc);
        tvStatusTag = view.findViewById(R.id.tvStatusTag);
        tvTimeEstimate = view.findViewById(R.id.tvTimeEstimate);
        tvStatusMsg = view.findViewById(R.id.tvStatusMsg);

        ivStep1 = view.findViewById(R.id.ivStep1);
        ivStep2 = view.findViewById(R.id.ivStep2);
        ivStep3 = view.findViewById(R.id.ivStep3);
        ivStep4 = view.findViewById(R.id.ivStep4);
        line1 = view.findViewById(R.id.line1);
        line2 = view.findViewById(R.id.line2);
        line3 = view.findViewById(R.id.line3);

        tvStoreName = view.findViewById(R.id.tvStoreName);
        tvAddressName = view.findViewById(R.id.tvAddressName);
        tvDetailAddress = view.findViewById(R.id.tvDetailAddress);
        btnCancelOrder = view.findViewById(R.id.btnCancelOrder);
        tvCancelNote = view.findViewById(R.id.tvCancelNote);

        // --- ÁNH XẠ CÁC VIEW TIỀN TỆ ---
        tvTotalPriceList = view.findViewById(R.id.tvTotalPriceList);
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment);
        tvPaymentMethodName = view.findViewById(R.id.tvPaymentMethodName);
        layoutPaymentInfo = view.findViewById(R.id.layoutPaymentInfo); // Quan trọng

        rvOrderItems = view.findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            // Quay về MainActivity hoặc màn hình trước
            if (getActivity() != null) {
                // Nếu đang ở MainActivity thì popFragment, nếu Activity riêng thì finish
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    getActivity().finish();
                }
            }
        });

        // Sự kiện nút Hủy
        btnCancelOrder.setOnClickListener(v -> {
            // Hiển thị Dialog xác nhận thay vì gọi API ngay
            showConfirmCancelDialog();
        });
    }

    // --- 1. HIỂN THỊ DIALOG XÁC NHẬN HỦY ---
    private void showConfirmCancelDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Inflate layout từ XML bạn cung cấp
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_order_cancel_confirm, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        // Làm nền trong suốt để bo góc đẹp hơn
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Ánh xạ các nút trong Dialog
        MaterialButton btnAgree = dialogView.findViewById(R.id.btnAgree);
        MaterialButton btnCancelAction = dialogView.findViewById(R.id.btnCancelAction);

        // Nút "Đồng ý" -> Gọi API hủy
        btnAgree.setOnClickListener(v -> {
            dialog.dismiss();
            cancelOrderApi(currentOrderID);
        });

        // Nút "Hủy" (Đóng dialog)
        btnCancelAction.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    // --- 2. GỌI API HỦY ĐƠN ---
    private void cancelOrderApi(String orderID) {
        // Tạo Body request
        HashMap<String, String> body = new HashMap<>();
        body.put("orderID", orderID);

        // Gọi API
        apiService.cancelOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse res = response.body();
                    if ("Success".equalsIgnoreCase(res.getStatus())) {
                        Toast.makeText(getContext(), "Đã hủy đơn hàng thành công!", Toast.LENGTH_LONG).show();

                        // Option A: Reload lại data để cập nhật giao diện (sẽ thấy trạng thái Đã hủy)
                        loadOrderDataFromApi(currentOrderID);

                        // Option B: Quay về trang chủ
                        // if (getActivity() != null) getActivity().onBackPressed();
                    } else {
                        Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể hủy đơn hàng lúc này", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderDataFromApi(String orderID) {
        apiService.getOrderDetail(orderID).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("Success".equalsIgnoreCase(response.body().getStatus())) {
                        updateUI(response.body().getData());
                    }
                }
            }
            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
            }
        });
    }

    // --- 3. CẬP NHẬT GIAO DIỆN & TIỀN ---
    private void updateUI(Order order) {
        if (getContext() == null) return;

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String priceStr = formatter.format(order.getFinalTotal()) + " VND";

        // Cập nhật giá tổng món ăn
        tvTotalPriceList.setText(priceStr);

        // Cập nhật giá tổng thanh toán (QUAN TRỌNG: Cần hiện Layout lên trước)
        if (layoutPaymentInfo != null) {
            layoutPaymentInfo.setVisibility(View.VISIBLE);
        }
        if (tvTotalPayment != null) {
            tvTotalPayment.setText(priceStr);
        }

        // Cập nhật tên phương thức thanh toán
        if (tvPaymentMethodName != null) {
            String method = order.getPaymentMethod();
            if ("momo".equalsIgnoreCase(method)) {
                tvPaymentMethodName.setText("Ví MoMo");
                // tvPaymentMethodName.setCompoundDrawables... (Set icon Momo nếu cần)
            } else {
                tvPaymentMethodName.setText("Tiền mặt");
            }
        }

        // Cập nhật địa chỉ
        String address = order.getDeliverAddress();
        if (address == null || address.isEmpty() || address.equalsIgnoreCase("null")) {
            tvAddressName.setText("Nhận tại cửa hàng");
            tvDetailAddress.setVisibility(View.GONE);
        } else {
            tvAddressName.setText("Địa chỉ nhận hàng");
            tvDetailAddress.setText(address);
            tvDetailAddress.setVisibility(View.VISIBLE);
        }

        tvStoreName.setText("Hermosa Coffee");

        // List món ăn
        if (order.getProducts() != null) {
            OrderItemAdapter adapter = new OrderItemAdapter(order.getProducts());
            rvOrderItems.setAdapter(adapter);
        }

        // Trạng thái đơn hàng
        updateStatusTimeline(order.getStatus());
    }

    private void updateStatusTimeline(String status) {
        if (status == null) status = "pending";
        resetTimelineColors();

        switch (status.toLowerCase()) {
            case "pending": // Cho phép hủy
                layoutHeaderPending.setVisibility(View.VISIBLE);
                layoutHeaderConfirmed.setVisibility(View.GONE);
                highlightTimeline(1);

                // HIỆN NÚT HỦY
                btnCancelOrder.setVisibility(View.VISIBLE);
                tvCancelNote.setVisibility(View.VISIBLE);
                break;

            case "confirmed":
            case "cooking": // Không cho hủy nữa
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);
                tvStatusTag.setText("Chuẩn bị");
                tvStatusTag.setBackgroundColor(Color.parseColor("#E65100"));
                tvStatusMsg.setText("Nhà hàng đang chuẩn bị món ăn...");
                tvStatusMsg.setTextColor(Color.parseColor("#E65100"));
                highlightTimeline(2);

                // ẨN NÚT HỦY
                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;

            case "shipping":
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);
                tvStatusTag.setText("Đang giao");
                tvStatusTag.setBackgroundColor(Color.parseColor("#1976D2"));
                tvStatusMsg.setText("Tài xế đang giao hàng...");
                tvStatusMsg.setTextColor(Color.parseColor("#1976D2"));
                highlightTimeline(3);
                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;

            case "done":
            case "completed":
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);
                tvStatusTag.setText("Hoàn tất");
                tvStatusTag.setBackgroundColor(Color.parseColor("#388E3C"));
                tvStatusMsg.setText("Giao hàng thành công!");
                tvStatusMsg.setTextColor(Color.parseColor("#388E3C"));
                highlightTimeline(4);
                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;

            case "cancelled":
                layoutHeaderPending.setVisibility(View.VISIBLE);
                layoutHeaderConfirmed.setVisibility(View.GONE);
                tvStatusTitle.setText("Đã Hủy");
                tvStatusTitle.setTextColor(Color.RED);
                tvStatusDesc.setText("Đơn hàng này đã bị hủy.");

                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;
        }
    }

    // ... (Giữ nguyên các hàm highlightTimeline, resetTimelineColors và Adapter) ...
    private void highlightTimeline(int step) {
        int activeColor = Color.parseColor("#4CAF50");
        if (step >= 1) ivStep1.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        if (step >= 2) {
            line1.setBackgroundColor(activeColor);
            ivStep2.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        }
        if (step >= 3) {
            line2.setBackgroundColor(activeColor);
            ivStep3.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        }
        if (step >= 4) {
            line3.setBackgroundColor(activeColor);
            ivStep4.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private void resetTimelineColors() {
        int grayColor = Color.parseColor("#E0E0E0");
        ivStep1.clearColorFilter();
        ivStep2.clearColorFilter();
        ivStep3.clearColorFilter();
        ivStep4.clearColorFilter();
        line1.setBackgroundColor(grayColor);
        line2.setBackgroundColor(grayColor);
        line3.setBackgroundColor(grayColor);
    }

    public static class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
        private final List<CartItem> itemList;
        public OrderItemAdapter(List<CartItem> itemList) { this.itemList = itemList; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = itemList.get(position);
            holder.tvName.setText(item.getQuantity() + "x  " + item.getName());
            DecimalFormat fmt = new DecimalFormat("###,###,###");
            holder.tvPrice.setText(fmt.format(item.getSubtotal()) + " đ");
            holder.tvPrice.setTextColor(Color.BLACK);
        }

        @Override public int getItemCount() { return itemList == null ? 0 : itemList.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(android.R.id.text1);
                tvPrice = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}*/
package com.example.demo.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.MainActivity;
import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CartItem;
import com.example.demo.models.Order;
import com.example.demo.models.OrderResponse;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOrderTracking extends Fragment {

    // --- VIEW DECLARATION ---
    private ImageView btnBack;
    private LinearLayout layoutHeaderPending, layoutHeaderConfirmed;
    private TextView tvStatusTitle, tvStatusDesc, tvStatusTag, tvTimeEstimate, tvStatusMsg;
    private ImageView ivStep1, ivStep2, ivStep3, ivStep4;
    private View line1, line2, line3;

    // View hiển thị tiền
    private TextView tvTotalPriceList;
    private TextView tvTotalPayment;
    private TextView tvPaymentMethodName;
    private View layoutPaymentInfo;

    private TextView tvStoreName, tvAddressName, tvDetailAddress;
    private MaterialButton btnCancelOrder;
    private TextView tvCancelNote;
    private RecyclerView rvOrderItems;

    // --- DATA & API ---
    private String currentOrderID;
    private ApiService apiService;
    private boolean isDialogShown = false; // Cờ để tránh hiện dialog lặp lại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews(view);

        if (getArguments() != null) {
            currentOrderID = getArguments().getString("ORDER_ID", "");
        }

        setupEvents();

        if (currentOrderID != null && !currentOrderID.isEmpty()) {
            loadOrderDataFromApi(currentOrderID);
        }
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        layoutHeaderPending = view.findViewById(R.id.layoutHeaderPending);
        layoutHeaderConfirmed = view.findViewById(R.id.layoutHeaderConfirmed);

        tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        tvStatusDesc = view.findViewById(R.id.tvStatusDesc);
        tvStatusTag = view.findViewById(R.id.tvStatusTag);
        tvTimeEstimate = view.findViewById(R.id.tvTimeEstimate);
        tvStatusMsg = view.findViewById(R.id.tvStatusMsg);

        ivStep1 = view.findViewById(R.id.ivStep1);
        ivStep2 = view.findViewById(R.id.ivStep2);
        ivStep3 = view.findViewById(R.id.ivStep3);
        ivStep4 = view.findViewById(R.id.ivStep4);
        line1 = view.findViewById(R.id.line1);
        line2 = view.findViewById(R.id.line2);
        line3 = view.findViewById(R.id.line3);

        tvStoreName = view.findViewById(R.id.tvStoreName);
        tvAddressName = view.findViewById(R.id.tvAddressName);
        tvDetailAddress = view.findViewById(R.id.tvDetailAddress);
        btnCancelOrder = view.findViewById(R.id.btnCancelOrder);
        tvCancelNote = view.findViewById(R.id.tvCancelNote);

        tvTotalPriceList = view.findViewById(R.id.tvTotalPriceList);
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment);
        tvPaymentMethodName = view.findViewById(R.id.tvPaymentMethodName);
        layoutPaymentInfo = view.findViewById(R.id.layoutPaymentInfo);

        rvOrderItems = view.findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    getActivity().finish();
                }
            }
        });

        // Nút Hủy -> Hiện Dialog xác nhận
        btnCancelOrder.setOnClickListener(v -> showConfirmCancelDialog());
    }

    // ================== CÁC HÀM HIỂN THỊ DIALOG (POPUP) ==================

    // 1. Dialog "Xác nhận muốn hủy đơn" (Trước khi hủy)
    private void showConfirmCancelDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_order_cancel_confirm, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton btnAgree = dialogView.findViewById(R.id.btnAgree);
        MaterialButton btnCancelAction = dialogView.findViewById(R.id.btnCancelAction);

        btnAgree.setOnClickListener(v -> {
            dialog.dismiss();
            cancelOrderApi(currentOrderID); // Gọi API hủy thật
        });
        btnCancelAction.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // 2. Dialog "Hủy thành công" (Sau khi API báo Success)
    private void showCancelSuccessDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_cancel_success, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Điền thông tin vào dialog (Mã đơn, Thời gian...)
        TextView tvOrderID = dialogView.findViewById(R.id.tvOrderID);
        if (tvOrderID != null) tvOrderID.setText("Mã đơn: " + currentOrderID);

        MaterialButton btnReturnHome = dialogView.findViewById(R.id.btnReturnHome);
        btnReturnHome.setOnClickListener(v -> {
            dialog.dismiss();
            // Quay về trang chủ MainActivity
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        dialog.show();
    }

    // 3. Dialog "Đơn hàng hoàn tất - Vui lòng nhận nước" (Khi status = done)
    private void showOrderDoneDialog() {
        if (getContext() == null || isDialogShown) return; // Nếu đã hiện rồi thì thôi

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_pickup_success, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            // Có thể làm gì đó sau khi user bấm Đồng ý, ví dụ reload lại trang
        });

        dialog.show();
        isDialogShown = true; // Đánh dấu đã hiện để không spam
    }

    // ================== CÁC HÀM GỌI API ==================

    private void cancelOrderApi(String orderID) {
        HashMap<String, String> body = new HashMap<>();
        body.put("orderID", orderID);

        apiService.cancelOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse res = response.body();
                    if ("Success".equalsIgnoreCase(res.getStatus())) {
                        // Hủy thành công -> Hiện Dialog thông báo thành công
                        showCancelSuccessDialog();

                        // Cập nhật giao diện bên dưới thành trạng thái "Đã hủy"
                        updateStatusTimeline("cancelled");
                    } else {
                        Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi hủy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderDataFromApi(String orderID) {
        apiService.getOrderDetail(orderID).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("Success".equalsIgnoreCase(response.body().getStatus())) {
                        updateUI(response.body().getData());
                    }
                }
            }
            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
            }
        });
    }

    // ================== CẬP NHẬT GIAO DIỆN ==================

    private void updateUI(Order order) {
        if (getContext() == null) return;

        // Tiền tệ
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String priceStr = formatter.format(order.getFinalTotal()) + " VND";
        tvTotalPriceList.setText(priceStr);
        if (layoutPaymentInfo != null) layoutPaymentInfo.setVisibility(View.VISIBLE);
        if (tvTotalPayment != null) tvTotalPayment.setText(priceStr);

        // Phương thức thanh toán
        if (tvPaymentMethodName != null) {
            String method = order.getPaymentMethod();
            tvPaymentMethodName.setText("momo".equalsIgnoreCase(method) ? "Ví MoMo" : "Tiền mặt");
        }

        // Địa chỉ
        String address = order.getDeliverAddress();
        if (address == null || address.isEmpty() || "null".equalsIgnoreCase(address)) {
            tvAddressName.setText("Nhận tại cửa hàng");
            tvDetailAddress.setVisibility(View.GONE);
        } else {
            tvAddressName.setText("Địa chỉ nhận hàng");
            tvDetailAddress.setText(address);
            tvDetailAddress.setVisibility(View.VISIBLE);
        }
        tvStoreName.setText("Hermosa Coffee");

        // List món
        if (order.getProducts() != null) {
            OrderItemAdapter adapter = new OrderItemAdapter(order.getProducts());
            rvOrderItems.setAdapter(adapter);
        }

        // Trạng thái & Hiện Dialog Done nếu cần
        updateStatusTimeline(order.getStatus());

        // KIỂM TRA ĐỂ HIỆN DIALOG "DONE"
        if ("done".equalsIgnoreCase(order.getStatus()) || "completed".equalsIgnoreCase(order.getStatus())) {
            showOrderDoneDialog();
        }
    }

    private void updateStatusTimeline(String status) {
        if (status == null) status = "pending";
        resetTimelineColors();

        switch (status.toLowerCase()) {
            case "pending":
                layoutHeaderPending.setVisibility(View.VISIBLE);
                layoutHeaderConfirmed.setVisibility(View.GONE);
                highlightTimeline(1);
                btnCancelOrder.setVisibility(View.VISIBLE);
                tvCancelNote.setVisibility(View.VISIBLE);
                break;

            case "confirmed":
            case "cooking":
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);
                tvStatusTag.setText("Chuẩn bị");
                tvStatusTag.setBackgroundColor(Color.parseColor("#E65100"));
                tvStatusMsg.setText("Nhà hàng đang chuẩn bị món ăn...");
                tvStatusMsg.setTextColor(Color.parseColor("#E65100"));
                highlightTimeline(2);
                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;

            case "shipping":
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);
                tvStatusTag.setText("Đang giao");
                tvStatusTag.setBackgroundColor(Color.parseColor("#1976D2"));
                tvStatusMsg.setText("Tài xế đang giao hàng...");
                tvStatusMsg.setTextColor(Color.parseColor("#1976D2"));
                highlightTimeline(3);
                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;

            case "done":
            case "completed":
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);
                tvStatusTag.setText("Hoàn tất");
                tvStatusTag.setBackgroundColor(Color.parseColor("#388E3C"));
                tvStatusMsg.setText("Đơn hàng đã hoàn thành!");
                tvStatusMsg.setTextColor(Color.parseColor("#388E3C"));
                highlightTimeline(4);
                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;

            case "cancelled":
                layoutHeaderPending.setVisibility(View.VISIBLE);
                layoutHeaderConfirmed.setVisibility(View.GONE);
                tvStatusTitle.setText("Đã Hủy");
                tvStatusTitle.setTextColor(Color.RED);
                tvStatusDesc.setText("Đơn hàng này đã bị hủy.");
                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;
        }
    }

    // Helper: Tô màu Timeline
    private void highlightTimeline(int step) {
        int activeColor = Color.parseColor("#4CAF50");
        if (step >= 1) ivStep1.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        if (step >= 2) {
            line1.setBackgroundColor(activeColor);
            ivStep2.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        }
        if (step >= 3) {
            line2.setBackgroundColor(activeColor);
            ivStep3.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        }
        if (step >= 4) {
            line3.setBackgroundColor(activeColor);
            ivStep4.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private void resetTimelineColors() {
        int grayColor = Color.parseColor("#E0E0E0");
        ivStep1.clearColorFilter();
        ivStep2.clearColorFilter();
        ivStep3.clearColorFilter();
        ivStep4.clearColorFilter();
        line1.setBackgroundColor(grayColor);
        line2.setBackgroundColor(grayColor);
        line3.setBackgroundColor(grayColor);
    }

    public static class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
        private final List<CartItem> itemList;
        public OrderItemAdapter(List<CartItem> itemList) { this.itemList = itemList; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = itemList.get(position);
            holder.tvName.setText(item.getQuantity() + "x  " + item.getName());
            DecimalFormat fmt = new DecimalFormat("###,###,###");
            holder.tvPrice.setText(fmt.format(item.getSubtotal()) + " đ");
            holder.tvPrice.setTextColor(Color.BLACK);
        }

        @Override public int getItemCount() { return itemList == null ? 0 : itemList.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(android.R.id.text1);
                tvPrice = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
