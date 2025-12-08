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
import com.example.demo.models.Product;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
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

    private TextView tvTotalPriceList, tvTotalPayment, tvPaymentMethodName;
    private View layoutPaymentInfo;
    private TextView tvStoreName, tvAddressName, tvDetailAddress;
    private MaterialButton btnCancelOrder;
    private MaterialButton btnSubmitReview; // Nút đánh giá
    private TextView tvCancelNote;
    private RecyclerView rvOrderItems;

    // --- DATA & API ---
    private String currentOrderID;
    private ApiService apiService;
    private boolean isDialogShown = false;

    // Lưu list Product để truyền cho FragmentReview
    private final List<Product> currentOrderProducts = new ArrayList<>();

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

        // Polling mỗi 5s
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentOrderID != null && getView() != null) {
                    loadOrderDataFromApi(currentOrderID);
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, 5000);
            }
        }, 5000);
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
        btnSubmitReview = view.findViewById(R.id.btnSubmitReview);
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

        btnCancelOrder.setOnClickListener(v -> showConfirmCancelDialog());

        btnSubmitReview.setOnClickListener(v -> {
            FragmentReview reviewFragment = new FragmentReview();
            Bundle bundle = new Bundle();
            bundle.putString("ORDER_ID", currentOrderID);
            bundle.putSerializable("PRODUCTS", new ArrayList<>(currentOrderProducts)); // Truyền bản sao an toàn
            reviewFragment.setArguments(bundle);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, reviewFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    // ================== CÁC DIALOG ==================
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
            cancelOrderApi(currentOrderID);
        });
        btnCancelAction.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showCancelSuccessDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_cancel_success, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvOrderID = dialogView.findViewById(R.id.tvOrderID);
        if (tvOrderID != null) tvOrderID.setText("Mã đơn: " + currentOrderID);

        MaterialButton btnReturnHome = dialogView.findViewById(R.id.btnReturnHome);
        btnReturnHome.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });
        dialog.show();
    }

    private void showOrderDoneDialog() {
        if (getContext() == null || isDialogShown) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_pickup_success, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);
        btnConfirm.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        isDialogShown = true;
    }

    // ================== API ==================
    private void cancelOrderApi(String orderID) {
        HashMap<String, String> body = new HashMap<>();
        body.put("orderID", orderID);

        apiService.cancelOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equalsIgnoreCase(response.body().getStatus())) {
                    showCancelSuccessDialog();
                    updateStatusTimeline("cancelled");
                } else {
                    Toast.makeText(getContext(), "Hủy thất bại", Toast.LENGTH_SHORT).show();
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
                if (response.isSuccessful() && response.body() != null && "Success".equalsIgnoreCase(response.body().getStatus())) {
                    updateUI(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Log.e("API", "Load order failed: " + t.getMessage());
            }
        });
    }

    // ================== CẬP NHẬT UI ==================
    private void updateUI(Order order) {
        if (getContext() == null || order == null) return;

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String priceStr = formatter.format(order.getFinalTotal()) + " VND";
        tvTotalPriceList.setText(priceStr);
        if (tvTotalPayment != null) tvTotalPayment.setText(priceStr);
        if (layoutPaymentInfo != null) layoutPaymentInfo.setVisibility(View.VISIBLE);

        if (tvPaymentMethodName != null) {
            String method = order.getPaymentMethod();
            tvPaymentMethodName.setText("momo".equalsIgnoreCase(method) ? "Ví MoMo" : "Tiền mặt");
        }

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

        // Danh sách món + chuyển CartItem → Product
        if (order.getProducts() != null) {
            rvOrderItems.setAdapter(new OrderItemAdapter(order.getProducts()));

            currentOrderProducts.clear();
            for (CartItem item : order.getProducts()) {
                Product p = new Product(
                        item.getName(),
                        String.valueOf(item.getPrice()),
                        item.getImageUrl(), // ← giả sử CartItem có getImageUrl()
                        0xFFA71317,
                        item.getProductID()
                );
                p.setQuantity(item.getQuantity());
                p.setSize(item.getSize());
                String[] toppings = item.getTopping();
                p.setTopping(toppings != null ? toppings : new String[0]);
                currentOrderProducts.add(p);
            }
        }

        updateStatusTimeline(order.getStatus());

        if ("done".equalsIgnoreCase(order.getStatus()) || "completed".equalsIgnoreCase(order.getStatus())) {
            showOrderDoneDialog();
            btnSubmitReview.setVisibility(View.VISIBLE);
        } else {
            btnSubmitReview.setVisibility(View.GONE);
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

    private void highlightTimeline(int step) {
        int activeColor = Color.parseColor("#4CAF50");
        if (step >= 1) ivStep1.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        if (step >= 2) { line1.setBackgroundColor(activeColor); ivStep2.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN); }
        if (step >= 3) { line2.setBackgroundColor(activeColor); ivStep3.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN); }
        if (step >= 4) { line3.setBackgroundColor(activeColor); ivStep4.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN); }
    }

    private void resetTimelineColors() {
        int grayColor = Color.parseColor("#E0E0E0");
        ivStep1.clearColorFilter(); ivStep2.clearColorFilter(); ivStep3.clearColorFilter(); ivStep4.clearColorFilter();
        line1.setBackgroundColor(grayColor); line2.setBackgroundColor(grayColor); line3.setBackgroundColor(grayColor);
    }

    // Adapter hiển thị món trong tracking
    public static class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
        private final List<CartItem> itemList;

        public OrderItemAdapter(List<CartItem> itemList) { this.itemList = itemList; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = itemList.get(position);
            holder.tvName.setText(item.getQuantity() + "x  " + item.getName());
            DecimalFormat fmt = new DecimalFormat("###,###,###");
            holder.tvPrice.setText(fmt.format(item.getSubtotal()) + " đ");
            holder.tvPrice.setTextColor(Color.BLACK);
        }

        @Override public int getItemCount() { return itemList.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(android.R.id.text1);
                tvPrice = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}