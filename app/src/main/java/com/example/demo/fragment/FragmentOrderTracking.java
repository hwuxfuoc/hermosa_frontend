package com.example.demo.fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CancelOrderRequest;
import com.example.demo.models.CartItem;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.Order;
import com.example.demo.models.OrderResponse;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOrderTracking extends Fragment {

    // --- 1. KHAI BÁO VIEW (Khớp với fragment_order_status.xml) ---

    // Toolbar
    private ImageView btnBack;

    // Header Status (Phần trên cùng)
    private LinearLayout layoutHeaderPending;    // Layout cho trạng thái Đang xử lý
    private LinearLayout layoutHeaderConfirmed;  // Layout cho trạng thái Chuẩn bị/Giao hàng/Hoàn tất

    // Text trong Header
    private TextView tvStatusTitle, tvStatusDesc; // Của Pending
    private TextView tvStatusTag, tvTimeEstimate, tvStatusMsg; // Của Confirmed

    // Timeline (Dòng thời gian)
    private ImageView ivStep1, ivStep2, ivStep3, ivStep4;
    private View line1, line2, line3;

    // Thông tin đơn hàng (Giá, List món)
    private TextView tvTotalPriceList;
    private RecyclerView rvOrderItems;

    // Thông tin địa chỉ (Store & User)
    private TextView tvStoreName, tvAddressName, tvDetailAddress;

    // Nút chức năng
    private MaterialButton btnCancelOrder;
    private TextView tvCancelNote;

    // Biến dữ liệu
    private String currentOrderID;
    // Timeline views
    private View progressBar;
    private View progressLine;
    private ImageView ivStore, ivUser;

    // --- 2. KHỞI TẠO FRAGMENT ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        initViews(view);

        // 2. Lấy dữ liệu OrderID từ màn hình trước (nếu có)
        if (getArguments() != null) {
            currentOrderID = getArguments().getString("ORDER_ID", "");
        }

        // 3. Setup sự kiện Click
        setupEvents();

        // 4. Load dữ liệu (Giả lập hoặc gọi API)
        loadOrderData();
    }

    // --- 3. ÁNH XẠ VIEW (FIND VIEW BY ID) ---
    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);

        layoutHeaderPending = view.findViewById(R.id.layoutHeaderPending);
        layoutHeaderConfirmed = view.findViewById(R.id.layoutHeaderConfirmed);

        tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        tvStatusDesc = view.findViewById(R.id.tvStatusDesc);

        tvStatusTag = view.findViewById(R.id.tvStatusTag);
        tvTimeEstimate = view.findViewById(R.id.tvTimeEstimate);
        tvStatusMsg = view.findViewById(R.id.tvStatusMsg);

        // Timeline
        ivStep1 = view.findViewById(R.id.ivStep1);
        ivStep2 = view.findViewById(R.id.ivStep2);
        ivStep3 = view.findViewById(R.id.ivStep3);
        ivStep4 = view.findViewById(R.id.ivStep4);
        line1 = view.findViewById(R.id.line1);
        line2 = view.findViewById(R.id.line2);
        line3 = view.findViewById(R.id.line3);

        // Info Area
        tvTotalPriceList = view.findViewById(R.id.tvTotalPriceList);
        tvStoreName = view.findViewById(R.id.tvStoreName);
        tvAddressName = view.findViewById(R.id.tvAddressName);
        tvDetailAddress = view.findViewById(R.id.tvDetailAddress);

        btnCancelOrder = view.findViewById(R.id.btnCancelOrder);
        tvCancelNote = view.findViewById(R.id.tvCancelNote);

        // RecyclerView Config
        rvOrderItems = view.findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setNestedScrollingEnabled(false); // Để scroll mượt trong ScrollView

        progressBar = view.findViewById(R.id.progressBar);
        progressLine = view.findViewById(R.id.progressLine);
        ivStore = view.findViewById(R.id.ivStore);
        ivUser = view.findViewById(R.id.ivUser);

    }

    // --- 4. SỰ KIỆN CLICK ---
    private void setupEvents() {
        // Nút Back
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Nút Hủy Đơn
        btnCancelOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Hủy đơn hàng")
                    .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?\nĐơn hàng sẽ bị hủy vĩnh viễn.")
                    .setPositiveButton("Hủy đơn", (dialog, which) -> cancelOrder(currentOrderID))
                    .setNegativeButton("Giữ lại", null)
                    .show();
        });
    }

    // --- 5. LOGIC LOAD DATA (MOCK DATA & UPDATE UI) ---
    private void loadOrderData() {
        if (currentOrderID == null || currentOrderID.isEmpty()) {
            Toast.makeText(getContext(), "Không có mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.viewOrder(currentOrderID).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Order order = response.body().getData();
                    updateUI(order);
                } else {
                    Toast.makeText(getContext(), "Lỗi tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

private void updateUI(Order order) {
        if (getContext() == null) return;

        // 1. Hiển thị thông tin chung (Format tiền tệ)
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String priceStr = formatter.format(order.getFinalTotal()) + " VND";

        tvTotalPriceList.setText(priceStr);

        // Hiển thị thông tin quán & địa chỉ user (Nếu null thì set rỗng)
        tvStoreName.setText(order.getStoreName() != null ? order.getStoreName() : "Cửa hàng");
        tvAddressName.setText(order.getDeliverAddress() != null ? order.getDeliverAddress() : "Địa chỉ nhận hàng");

        // Nếu có trường chi tiết địa chỉ trong Order thì set vào, nếu không dùng tạm Address
        tvDetailAddress.setText(order.getDeliverAddress());

        // 2. Hiển thị danh sách món ăn (RecyclerView)
        if (order.getProducts() != null) {
            OrderItemAdapter adapter = new OrderItemAdapter(order.getProducts());
            rvOrderItems.setAdapter(adapter);
        }

        // 3. Xử lý Logic Trạng thái (Quan trọng nhất)
        updateStatusTimeline(order.getStatus());
    }

        // --- 7. LOGIC TIMELINE & STATUS ---
        private void updateStatusTimeline(String status) {
            if (status == null) status = "pending";

            // Reset
            ivStore.setBackgroundResource(R.drawable.circle_gray);
            ivUser.setBackgroundResource(R.drawable.circle_gray);
            progressLine.setBackgroundResource(R.drawable.progress_track_gray);
            stopDeliveryAnimation();

            switch (status.toLowerCase()) {
                case "pending":
                case "confirmed":
                case "cooking":
                    // Đang xử lý / chuẩn bị → chỉ sáng icon quán
                    ivStore.setBackgroundResource(R.drawable.circle_green);
                    break;

                case "shipping":
                case "delivering":
                    // Đang giao → icon quán xanh + hiệu ứng chạy
                    ivStore.setBackgroundResource(R.drawable.circle_green);
                    startDeliveryAnimation();
                    break;

                case "done":
                case "completed":
                    // Hoàn tất → cả 2 icon xanh
                    ivStore.setBackgroundResource(R.drawable.circle_green);
                    ivUser.setBackgroundResource(R.drawable.circle_green);
                    progressLine.setBackgroundResource(R.drawable.progress_bar_green); // Đổ đầy xanh
                    break;
            }
        }

    private void startDeliveryAnimation() {
        progressBar.setVisibility(View.VISIBLE);

        // Tạo animation chạy từ trái sang phải
        ObjectAnimator animator = ObjectAnimator.ofFloat(progressBar, "translationX",
                -progressBar.getWidth(), progressLine.getWidth());

        animator.setDuration(3000); // 3 giây chạy 1 vòng
        animator.setRepeatCount(ValueAnimator.INFINITE); // Lặp mãi mãi
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void stopDeliveryAnimation() {
        progressBar.setVisibility(View.GONE);
        progressBar.clearAnimation();
    }

    // Helper: Tô màu các bước
    private void highlightTimeline(int step) {
        int activeColor = Color.parseColor("#4CAF50"); // Màu Xanh lá (Giống trong XML icon)
        // Hoặc dùng màu xanh dương: Color.parseColor("#2196F3");

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

    // Helper: Reset màu về xám
    private void resetTimelineColors() {
        int grayColor = Color.parseColor("#E0E0E0"); // Xám nhạt

        ivStep1.clearColorFilter();
        ivStep2.clearColorFilter();
        ivStep3.clearColorFilter();
        ivStep4.clearColorFilter();

        line1.setBackgroundColor(grayColor);
        line2.setBackgroundColor(grayColor);
        line3.setBackgroundColor(grayColor);
    }

    // --- 8. INNER ADAPTER CLASS (Cho RecyclerView) ---
    // Đặt class này ngay bên trong Fragment để tiện copy-paste
    public static class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
        private final List<CartItem> itemList;

        public OrderItemAdapter(List<CartItem> itemList) {
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Bạn có thể tạo file layout item_order_detail.xml riêng
            // Ở đây dùng layout đơn giản có sẵn của Android để demo
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = itemList.get(position);

            // Giả định CartItem có các getter này (nếu chưa có thì bạn thêm vào model CartItem)
            // String displayText = item.getQuantity() + "x  " + item.getName();
            // holder.tvName.setText(displayText);

            // Code demo nếu CartItem chưa hoàn thiện:
            holder.tvName.setText("1x  Món ăn demo " + position);
            holder.tvPrice.setText("50.000 đ");
        }

        @Override
        public int getItemCount() {
            return itemList == null ? 0 : itemList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(android.R.id.text1);
                tvPrice = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    private void cancelOrder(String orderID) {
        if (orderID == null || orderID.isEmpty()) return;

        btnCancelOrder.setEnabled(false);
        btnCancelOrder.setText("Đang hủy...");

        // TẠO OBJECT CancelOrderRequest
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderID(orderID);

        ApiClient.getClient().create(ApiService.class)
                .cancelOrder(request)
                .enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                        btnCancelOrder.setEnabled(true);
                        btnCancelOrder.setText("Hủy đơn hàng");

                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã hủy đơn hàng thành công!", Toast.LENGTH_LONG).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Hủy đơn thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {
                        btnCancelOrder.setEnabled(true);
                        btnCancelOrder.setText("Hủy đơn hàng");
                        Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}