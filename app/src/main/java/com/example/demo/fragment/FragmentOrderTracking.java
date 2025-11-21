/*
package com.example.demo.fragment; // Đổi package theo project của bạn

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group; // Quan trọng cho Timeline
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.demo.R; // Import R của project bạn

public class FragmentOrderTracking extends Fragment {

    // --- 1. KHAI BÁO BIẾN VIEW ---
    private Group groupStepShipping;        // Nhóm timeline xe máy
    private MaterialCardView cardAddressShip; // Thẻ địa chỉ giao hàng
    private MaterialCardView cardPickupNote;  // Thẻ thông báo tại quán
    private ImageView ivStep4;              // Icon đích (Pin hoặc Túi)

    private TextView tvStatusTitle, tvStatusDesc, tvTotalPrice, tvCancelNote;
    private MaterialButton btnCancelOrder;
    private ImageView btnBack;

    // --- CẤU HÌNH TEST ---
    // Đổi thành TRUE để xem giao diện SHIP
    // Đổi thành FALSE để xem giao diện TẠI QUÁN
    private boolean IS_DELIVERY_MODE = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Ánh xạ (Binding)
        initViews(view);

        // 3. Thiết lập giao diện dựa trên loại đơn (Ship/Pickup)
        setupHybridUI(IS_DELIVERY_MODE);

        // 4. Đổ dữ liệu giả lập (Mock Data)
        fillData();

        // 5. Xử lý sự kiện Click
        handleEvents();
    }

    private void initViews(View view) {
        // Timeline & Logic Views
        groupStepShipping = view.findViewById(R.id.groupStepShipping);
        cardAddressShip = view.findViewById(R.id.cardAddressShip);
        cardPickupNote = view.findViewById(R.id.cardPickupNote);
        ivStep4 = view.findViewById(R.id.ivStep4);

        // Info Views
        tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        tvStatusDesc = view.findViewById(R.id.tvStatusDesc);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvCancelNote = view.findViewById(R.id.tvCancelNote);

        // Buttons
        btnCancelOrder = view.findViewById(R.id.btnCancelOrder);
        btnBack = view.findViewById(R.id.btnBack);
    }

*/
/**
     * LOGIC QUAN TRỌNG NHẤT: ĐIỀU KHIỂN UI THEO LOẠI ĐƠN*//*



    private void setupHybridUI(boolean isDelivery) {
        if (isDelivery) {
            // --- CHẾ ĐỘ GIAO HÀNG (SHIP) ---

            // 1. Hiện timeline đầy đủ (4 bước)
            groupStepShipping.setVisibility(View.VISIBLE);

            // 2. Icon đích là Ghim vị trí
            ivStep4.setImageResource(R.drawable.ic_location_outline);

            // 3. Hiện thẻ địa chỉ, Ẩn thông báo quán
            cardAddressShip.setVisibility(View.VISIBLE);
            cardPickupNote.setVisibility(View.GONE);

        } else {
            // --- CHẾ ĐỘ TẠI QUÁN (PICKUP) ---

            // 1. Ẩn timeline xe máy (Tự co lại còn 3 bước)
            groupStepShipping.setVisibility(View.GONE);

            // 2. Icon đích là Túi mua hàng
            ivStep4.setImageResource(R.drawable.ic_shopping_bag);

            // 3. Ẩn thẻ địa chỉ, Hiện thông báo quán
            cardAddressShip.setVisibility(View.GONE);
            cardPickupNote.setVisibility(View.VISIBLE);
        }
    }

    private void fillData() {
        // Set các text cơ bản
        tvTotalPrice.setText("660.000 VND");

        if (IS_DELIVERY_MODE) {
            tvStatusTitle.setText("Đang xử lý");
            tvStatusDesc.setText("Đang gửi đơn đặt hàng của bạn...");
        } else {
            tvStatusTitle.setText("Chờ xác nhận");
            tvStatusDesc.setText("Vui lòng đợi quán xác nhận đơn...");
        }

        // --- XỬ LÝ TEXT MÀU ĐỎ CHO DÒNG "Lưu ý:" ---
        String noteText = "Lưu ý: Bạn chỉ có thể hủy đơn trong quá trình xác nhận";
        SpannableString spannable = new SpannableString(noteText);
        // Tô đỏ 7 ký tự đầu (Lưu ý:)
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#B71C1C")),
                0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Set font đậm cho chữ Lưu ý (Optional)
        spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvCancelNote.setText(spannable);
    }

    private void handleEvents() {
        // Nút Back
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Nút Hủy Đơn -> Hiện Popup
        btnCancelOrder.setOnClickListener(v -> showCancelConfirmDialog());
    }

*/
/**
     * HIỂN THỊ POPUP HỦY ĐƠN (Custom Dialog)*//*



    private void showCancelConfirmDialog() {
        if (getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Liên kết với file XML popup bạn đã tạo
        dialog.setContentView(R.layout.fragment_order_cancel_confirm);

        Window window = dialog.getWindow();
        if (window == null) return;

        // Làm nền trong suốt để bo góc đẹp
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        // Ánh xạ nút trong Dialog
        MaterialButton btnAgree = dialog.findViewById(R.id.btnAgree);
        MaterialButton btnCancelAction = dialog.findViewById(R.id.btnCancelAction);

        // Xử lý nút Đồng ý hủy
        btnAgree.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(getContext(), "Đã hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
            // Thực hiện logic hủy API ở đây...
            // Sau đó quay về màn hình Home
        });

        // Xử lý nút Không hủy (Đóng popup)
        btnCancelAction.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
*/
package com.example.demo.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.R; // Nhớ đổi package R
import com.example.demo.adapters.OrderDetailAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
/*import com.example.demo.models.CancelOrderRequest;*/
import com.example.demo.models.CancelOrderRequest;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.Order;
/*import com.example.demo.models.OrderDetailResponse;*/
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOrderTracking extends Fragment {

    // --- KHAI BÁO VIEW ---
    // 1. Header Groups
    private View layoutHeaderPending, layoutHeaderConfirmed;
    private TextView tvStatusTitle, tvStatusDesc; // Pending Header
    private TextView tvStatusTag, tvTimeEstimate, tvStatusMsg; // Confirmed Header

    // 2. Content Groups
    private View layoutItemList, layoutPaymentInfo;
    private TextView tvTotalPriceList, tvTotalPayment, tvPaymentMethodName;
    private ImageView ivPaymentMethod;
    private RecyclerView rvOrderItems;

    // 3. Timeline
    private Group groupStepShipping;
    private ImageView ivStep1, ivStep2, ivStep3, ivStep4;
    private View line1, line2, line3;

    // 4. Info Cards & Footer
    private MaterialCardView cardAddressShip, cardPickupNote;
    private TextView tvStoreName, tvUserName, tvAddressName, tvCancelNote;
    private MaterialButton btnCancelOrder;
    private ImageView btnBack;

    // --- DATA ---
    private String currentOrderID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nhận ID từ Bundle
        if (getArguments() != null) {
            currentOrderID = getArguments().getString("ORDER_ID");
        } else {
            currentOrderID = "ORD-123TEST"; // Test ID
        }

        initViews(view);
        setupWarningText();

        // Gọi API lấy dữ liệu
        /*fetchOrderData(currentOrderID);*/

        /*handleEvents();*/
    }

    private void initViews(View view) {
        // Header
        layoutHeaderPending = view.findViewById(R.id.layoutHeaderPending);
        layoutHeaderConfirmed = view.findViewById(R.id.layoutHeaderConfirmed);
        tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        tvStatusDesc = view.findViewById(R.id.tvStatusDesc);
        tvStatusTag = view.findViewById(R.id.tvStatusTag);
        tvTimeEstimate = view.findViewById(R.id.tvTimeEstimate);
        tvStatusMsg = view.findViewById(R.id.tvStatusMsg);

        // Timeline
        groupStepShipping = view.findViewById(R.id.groupStepShipping);
        ivStep1 = view.findViewById(R.id.ivStep1);
        ivStep2 = view.findViewById(R.id.ivStep2);
        ivStep3 = view.findViewById(R.id.ivStep3);
        ivStep4 = view.findViewById(R.id.ivStep4);
        line1 = view.findViewById(R.id.line1);
        line2 = view.findViewById(R.id.line2);
        line3 = view.findViewById(R.id.line3);

        // Content Body
        layoutItemList = view.findViewById(R.id.layoutItemList);
        layoutPaymentInfo = view.findViewById(R.id.layoutPaymentInfo);
        tvTotalPriceList = view.findViewById(R.id.tvTotalPriceList);
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment);
        tvPaymentMethodName = view.findViewById(R.id.tvPaymentMethodName);
        ivPaymentMethod = view.findViewById(R.id.ivPaymentMethod);
        rvOrderItems = view.findViewById(R.id.rvOrderItems);

        // Footer Info
        cardAddressShip = view.findViewById(R.id.cardAddressShip);
        cardPickupNote = view.findViewById(R.id.cardPickupNote);
        tvStoreName = view.findViewById(R.id.tvStoreName);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvAddressName = view.findViewById(R.id.tvAddressName);
        tvCancelNote = view.findViewById(R.id.tvCancelNote);

        // Buttons
        btnCancelOrder = view.findViewById(R.id.btnCancelOrder);
        btnBack = view.findViewById(R.id.btnBack);
    }

    // ============================================================
    // 1. GỌI API VÀ CẬP NHẬT UI
    // ============================================================
    /*private void fetchOrderData(String orderId) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body().getData();
                    if (order != null) {
                        updateFullUI(order);
                    }
                }
            }
            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void updateFullUI(Order order) {
        // 1. Format Tiền
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String priceString = format.format(order.getTotalInvoice());
        tvTotalPriceList.setText(priceString);
        tvTotalPayment.setText(priceString);

        // 2. Cập nhật Giao diện Ship/Pickup (Hybrid Logic)
        setupHybridLayout(order.isDeliver());
        if (order.isDeliver()) {
            tvAddressName.setText(order.getDeliverAddress());
        }

        // 3. Cập nhật Trạng thái (Pending vs Confirmed)
        updateUIByStatus(order.getStatus());

        // 4. Cập nhật Timeline màu sắc
        updateTimelineVisuals(order.getStatus(), order.isDeliver());

        // 5. Đổ dữ liệu món ăn (Chỉ hiện khi Pending)
        if (order.getProducts() != null) {
            OrderDetailAdapter adapter = new OrderDetailAdapter(order.getProducts());
            rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
            rvOrderItems.setNestedScrollingEnabled(false);
            rvOrderItems.setAdapter(adapter);
        }

        // 6. Update Payment Info
        tvPaymentMethodName.setText(order.getPaymentMethod()); // "Momo" or "Cash"
    }

    // ============================================================
    // 2. LOGIC HYBRID (SHIP / TẠI QUÁN)
    // ============================================================
    private void setupHybridLayout(boolean isDelivery) {
        if (isDelivery) {
            // --- SHIP ---
            groupStepShipping.setVisibility(View.VISIBLE); // Hiện xe máy
            cardAddressShip.setVisibility(View.VISIBLE);   // Hiện địa chỉ
            cardPickupNote.setVisibility(View.GONE);       // Ẩn note quán
            ivStep4.setImageResource(R.drawable.ic_location_outline); // Icon Ghim
        } else {
            // --- TẠI QUÁN ---
            groupStepShipping.setVisibility(View.GONE);    // Ẩn xe máy
            cardAddressShip.setVisibility(View.GONE);      // Ẩn địa chỉ
            cardPickupNote.setVisibility(View.VISIBLE);    // Hiện note quán
            ivStep4.setImageResource(R.drawable.ic_shopping_bag); // Icon Túi
        }
    }

    // ============================================================
    // 3. LOGIC ẨN HIỆN HEADER & CONTENT (MỚI)
    // ============================================================
    private void updateUIByStatus(String status) {
        // Nếu là pending -> Giao diện cũ. Khác pending -> Giao diện mới.
        boolean isPending = "pending".equalsIgnoreCase(status);

        if (isPending) {
            // --- CHẾ ĐỘ CHỜ XỬ LÝ ---
            layoutHeaderPending.setVisibility(View.VISIBLE);
            layoutHeaderConfirmed.setVisibility(View.GONE);

            layoutItemList.setVisibility(View.VISIBLE); // Hiện list món
            layoutPaymentInfo.setVisibility(View.GONE); // Ẩn thanh toán

            btnCancelOrder.setVisibility(View.VISIBLE); // Cho phép hủy
            tvCancelNote.setVisibility(View.VISIBLE);
        } else {
            // --- CHẾ ĐỘ ĐÃ NHẬN / ĐANG GIAO ---
            layoutHeaderPending.setVisibility(View.GONE);
            layoutHeaderConfirmed.setVisibility(View.VISIBLE);

            layoutItemList.setVisibility(View.GONE);    // Ẩn list món
            layoutPaymentInfo.setVisibility(View.VISIBLE); // Hiện thanh toán

            // Cập nhật Text cho Tag đỏ
            if (status.equalsIgnoreCase("preparing")) {
                tvStatusTag.setText("Chuẩn bị");
                tvStatusMsg.setText("On time. Quán đang chuẩn bị món...");
            } else if (status.equalsIgnoreCase("shipping") || status.equalsIgnoreCase("ready")) {
                tvStatusTag.setText("Giao hàng");
                tvStatusMsg.setText("Tài xế đang trên đường giao hàng...");
            } else if (status.equalsIgnoreCase("done")) {
                tvStatusTag.setText("Hoàn tất");
                tvStatusTag.setBackgroundColor(Color.parseColor("#4CAF50")); // Xanh lá
                tvStatusMsg.setText("Đơn hàng đã hoàn thành.");
            }

            btnCancelOrder.setVisibility(View.GONE); // Không cho hủy
            tvCancelNote.setVisibility(View.GONE);
        }
    }

    // ============================================================
    // 4. LOGIC TÔ MÀU TIMELINE
    // ============================================================
    private void updateTimelineVisuals(String status, boolean isDelivery) {
        int level = 0;
        switch (status.toLowerCase()) {
            case "pending": level = 0; break;
            case "preparing": level = 1; break;
            case "shipping":
            case "ready": level = 2; break;
            case "done": level = 3; break;
        }

        int active = Color.parseColor("#4CAF50"); // Xanh
        int inactive = Color.parseColor("#E0E0E0"); // Xám

        // Reset
        setColor(ivStep1, inactive); setColor(ivStep2, inactive);
        setColor(ivStep3, inactive); setColor(ivStep4, inactive);
        line1.setBackgroundColor(inactive); line2.setBackgroundColor(inactive); line3.setBackgroundColor(inactive);

        // Tô màu
        if (level >= 0) setColor(ivStep1, active);
        if (level >= 1) { line1.setBackgroundColor(active); setColor(ivStep2, active); }

        if (isDelivery) { // Ship
            if (level >= 2) { line2.setBackgroundColor(active); setColor(ivStep3, active); }
            if (level >= 3) { line3.setBackgroundColor(active); setColor(ivStep4, active); }
        } else { // Tại quán (Bỏ bước 2)
            if (level >= 2) {
                line3.setBackgroundColor(active); // Nối thẳng Bếp -> Đích
                setColor(ivStep4, active);
            }
        }
    }

    private void setColor(ImageView iv, int color) {
        iv.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /// ============================================================
    // UPDATE HÀM GỌI API HỦY ĐƠN
    // ============================================================
    private void callApiCancel() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        CancelOrderRequest req = new CancelOrderRequest(currentOrderID);

        api.cancelOrder(req).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    // KHI HỦY THÀNH CÔNG -> HIỆN POPUP THÔNG BÁO
                    showCancelSuccessDialog();
                } else {
                    Toast.makeText(getContext(), "Lỗi: Không thể hủy đơn này", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ============================================================
    // HÀM HIỂN THỊ POPUP THÀNH CÔNG (MỚI)
    // ============================================================
    private void showCancelSuccessDialog() {
        if (getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Liên kết với file XML mới tạo
        dialog.setContentView(R.layout.fragment_cancel_success);

        // Setup nền trong suốt
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false); // Bắt buộc bấm nút mới thoát được

        // 1. Ánh xạ View trong Dialog
        TextView tvOrderID = dialog.findViewById(R.id.tvOrderID);
        TextView tvCancelTime = dialog.findViewById(R.id.tvCancelTime);
        MaterialButton btnReturn = dialog.findViewById(R.id.btnReturnHome);

        // 2. Set dữ liệu thực tế
        tvOrderID.setText("Mã đơn: " + currentOrderID);

        // Lấy thời gian hiện tại
        String currentTime = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvCancelTime.setText("Thời gian hủy: " + currentTime);

        // 3. Xử lý nút "Quay lại giỏ hàng"
        btnReturn.setOnClickListener(v -> {
            dialog.dismiss();
            // Điều hướng về màn hình trước đó (Giỏ hàng hoặc Home)
            if (getActivity() != null) {
                // Cách 1: Quay lại màn hình trước
                getActivity().onBackPressed();

                // Cách 2 (Nếu muốn về thẳng Home):
                // NavController navController = Navigation.findNavController(getView());
                // navController.navigate(R.id.action_tracking_to_home);
            }
        });

        dialog.show();
    }

    private void setupWarningText() {
        SpannableString s = new SpannableString("Lưu ý: Bạn chỉ có thể hủy đơn trong quá trình xác nhận");
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#B71C1C")), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvCancelNote.setText(s);
    }
}