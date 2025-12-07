package com.example.demo.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.ConfirmOrderActivity;
import com.example.demo.MainActivity;
import com.example.demo.R;
import com.example.demo.models.CartItem;
import com.example.demo.models.Order;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// Import thư viện Retrofit nếu dùng
// import retrofit2.Call;
// import retrofit2.Callback;
// import retrofit2.Response;

public class FragmentOrderTracking extends Fragment {

    // --- 1. KHAI BÁO VIEW (Khớp với fragment_order_status.xml) ---

    // Toolbar
    private ImageView btnBack;

    // Header Status (Phần trên cùng)
    private String currentOrderID;
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
            // TODO: Gọi API hủy đơn hàng ở đây
            Toast.makeText(getContext(), "Đang gửi yêu cầu hủy đơn hàng...", Toast.LENGTH_SHORT).show();
            // cancelOrder(currentOrderID);
        });
    }

    // --- 5. LOGIC LOAD DATA (MOCK DATA & UPDATE UI) ---
    private void loadOrderData() {
        // === PHẦN NÀY BẠN THAY BẰNG GỌI API RETROFIT ===
        // Ví dụ: APIService.api.getOrder(currentOrderID).enqueue(...)

        // Hiện tại mình tạo dữ liệu GIẢ (Mock) để test UI chạy được ngay
        List<CartItem> mockItems = new ArrayList<>();
        // Lưu ý: CartItem cần có constructor hoặc setter
        // mockItems.add(new CartItem("Strawberry Cheese", 1, 55000));
        // mockItems.add(new CartItem("Matcha Latte", 2, 45000));

        // Giả lập Object Order (Sử dụng Model bạn đã cập nhật)
        // Dùng Anonymous Class để giả lập data vì Order của bạn ko có Constructor đầy đủ
        Order mockOrder = new Order() {
            @Override public String getStatus() { return "shipping"; } // <--- ĐỔI TRẠNG THÁI TEST Ở ĐÂY (pending, confirmed, shipping, done)
            @Override public long getFinalTotal() { return 660000; }
            @Override public String getStoreName() { return "Hermosa"; }
            @Override public String getStoreAddress() { return "27 Đường số 8, Linh Trung, Thủ Đức"; }
            @Override public String getDeliverAddress() { return "Trường ĐH Công nghệ Thông tin"; }
            // @Override public List<CartItem> getProducts() { return mockItems; } // Cần enable khi CartItem sẵn sàng
        };

        // Cập nhật giao diện với dữ liệu
        updateUI(mockOrder);
    }

//*


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

        // Reset về màu xám mặc định
        resetTimelineColors();

        switch (status.toLowerCase()) {
            case "pending": // Đang xử lý
                layoutHeaderPending.setVisibility(View.VISIBLE);
                layoutHeaderConfirmed.setVisibility(View.GONE);

                highlightTimeline(1); // Sáng bước 1

                btnCancelOrder.setVisibility(View.VISIBLE);
                tvCancelNote.setVisibility(View.VISIBLE);
                break;

            case "confirmed":
            case "cooking": // Đang chuẩn bị
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);

                tvStatusTag.setText("Chuẩn bị");
                tvStatusTag.setBackgroundColor(Color.parseColor("#E65100")); // Cam đậm
                tvStatusMsg.setText("Nhà hàng đang chuẩn bị món ăn...");
                tvStatusMsg.setTextColor(Color.parseColor("#E65100"));

                highlightTimeline(2); // Sáng bước 1, 2

                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;

            case "shipping": // Đang giao
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);

                tvStatusTag.setText("Đang giao");
                tvStatusTag.setBackgroundColor(Color.parseColor("#1976D2")); // Xanh dương
                tvStatusMsg.setText("Tài xế đang giao hàng đến bạn...");
                tvStatusMsg.setTextColor(Color.parseColor("#1976D2"));

                highlightTimeline(3); // Sáng bước 1, 2, 3

                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                break;

            case "done":
            case "completed": // Hoàn tất
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);

                tvStatusTag.setText("Hoàn tất");
                tvStatusTag.setBackgroundColor(Color.parseColor("#388E3C")); // Xanh lá
                tvStatusMsg.setText("Đơn hàng đã giao thành công!");
                tvStatusMsg.setTextColor(Color.parseColor("#388E3C"));

                highlightTimeline(4); // Sáng tất cả

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
                break;
        }
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
}
