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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.ParseException;      // Sửa lỗi ParseException
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;            // Sửa lỗi TimeZone

public class FragmentOrderTracking extends Fragment {

    private ImageView btnBack;
    private LinearLayout layoutHeaderPending, layoutHeaderConfirmed;
    private TextView tvStatusTitle, tvStatusDesc, tvStatusTag, tvTimeEstimate, tvStatusMsg;
    private ImageView ivStep1, ivStep4;
    private View line1;

    private TextView tvTotalPriceList;
    private TextView tvTotalPayment;
    private TextView tvPaymentMethodName;
    private View layoutPaymentInfo;

    private TextView tvStoreName, tvAddressName, tvDetailAddress;
    private MaterialButton btnCancelOrder;
    private MaterialButton btnSubmitReview;
    private TextView tvCancelNote;
    private RecyclerView rvOrderItems;

    private String currentOrderID;
    private ApiService apiService;
    private boolean isDialogShown = false;
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
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentOrderID != null) {
                    loadOrderDataFromApi(currentOrderID); // Gọi lại API
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
        ivStep4 = view.findViewById(R.id.ivStep4);
        line1 = view.findViewById(R.id.line1);

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

        // Nút Hủy -> Hiện Dialog xác nhận
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

    private void cancelOrderApi(String orderID) {
        HashMap<String, String> body = new HashMap<>();
        body.put("orderID", orderID);

        // Gọi API
        apiService.cancelOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse res = response.body();

                    if ("Success".equalsIgnoreCase(res.getStatus())) {
                        Toast.makeText(getContext(), "Đã hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();

                        // BƯỚC 1: Cập nhật giao diện thành "Đã hủy" ngay lập tức
                        // Để người dùng thấy trạng thái thay đổi (chữ đỏ, ẩn nút hủy...)
                        updateStatusTimeline("cancelled", false);

                        // BƯỚC 2: Đợi 1 giây (1000ms) rồi mới hiện Dialog thông báo
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Kiểm tra nếu Fragment còn đang hiển thị thì mới hiện Dialog
                                if (isAdded() && getContext() != null) {
                                    showCancelSuccessDialog();
                                }
                            }
                        }, 1000); // 1000ms = 1 giây

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
    private void showCancelSuccessDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_cancel_success, null);
        builder.setView(dialogView);

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvOrderID = dialogView.findViewById(R.id.tvOrderID);
        if (tvOrderID != null) tvOrderID.setText("Mã đơn: " + currentOrderID);

        MaterialButton btnReturnHome = dialogView.findViewById(R.id.btnReturnHome);
        btnReturnHome.setOnClickListener(v -> {
            dialog.dismiss();

            // Logic: Quay lại mua hàng tiếp (Về trang chủ)
            Intent intent = new Intent(getContext(), MainActivity.class);
            // Xóa các màn hình cũ, chỉ giữ lại MainActivity mới
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        dialog.show();
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


    private void updateUI(Order order) {
        if (getContext() == null) return;

        boolean isPickup = false;
        String address = order.getDeliverAddress();
        if (address == null || address.isEmpty() || "null".equalsIgnoreCase(address)) {
            isPickup = true;
        }
        String timeDisplay = "---";

        if (order.getCreateAt() != null && !order.getCreateAt().isEmpty()) {
            try {
                SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                serverFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Server dùng UTC

                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                displayFormat.setTimeZone(TimeZone.getDefault());

                Date startTime = serverFormat.parse(order.getCreateAt());
                String strStart = displayFormat.format(startTime);

                String strEnd = "";

                boolean isDone = "done".equalsIgnoreCase(order.getStatus()) || "completed".equalsIgnoreCase(order.getStatus());

                if (isDone && order.getDoneIn() != null && !order.getDoneIn().isEmpty()) {
                    try {
                        Date doneTime = serverFormat.parse(order.getDoneIn());
                        strEnd = displayFormat.format(doneTime);
                    } catch (Exception e) {
                        strEnd = "??:??";
                    }
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(startTime);

                    if (isPickup) {
                        calendar.add(Calendar.MINUTE, 15);
                    } else {
                        calendar.add(Calendar.MINUTE, 30);
                    }
                    strEnd = displayFormat.format(calendar.getTime());
                }

                timeDisplay = strStart + " - " + strEnd;

            } catch (ParseException e) {
                e.printStackTrace();
                timeDisplay = "Đang cập nhật...";
            }
        }

        tvTimeEstimate.setText(timeDisplay);

        if (isPickup) {
            tvAddressName.setText("Nhận tại cửa hàng");
            tvDetailAddress.setVisibility(View.GONE);
        } else {
            tvAddressName.setText("Địa chỉ nhận hàng");
            tvDetailAddress.setText(address);
            tvDetailAddress.setVisibility(View.VISIBLE);
        }

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String priceStr = formatter.format(order.getFinalTotal()) + " VND";
        tvTotalPriceList.setText(priceStr);

        if (layoutPaymentInfo != null) layoutPaymentInfo.setVisibility(View.VISIBLE);
        if (tvTotalPayment != null) tvTotalPayment.setText(priceStr);

        if (tvPaymentMethodName != null) {
            String method = order.getPaymentMethod();
            tvPaymentMethodName.setText("momo".equalsIgnoreCase(method) ? "Ví MoMo" : "Tiền mặt");
        }
        tvStoreName.setText("Hermosa Coffee");

        if (order.getProducts() != null) {
            OrderItemAdapter adapter = new OrderItemAdapter(order.getProducts());
            rvOrderItems.setAdapter(adapter);
        }

        updateStatusTimeline(order.getStatus(), isPickup);

        if (("done".equalsIgnoreCase(order.getStatus()) || "completed".equalsIgnoreCase(order.getStatus()))
                && isPickup) {
            showOrderDoneDialog();
        }
        if ("done".equalsIgnoreCase(order.getStatus()) || "completed".equalsIgnoreCase(order.getStatus())) {
            btnSubmitReview.setVisibility(View.VISIBLE);
        } else {
            btnSubmitReview.setVisibility(View.GONE);
        }
    }

    private void updateStatusTimeline(String status, boolean isPickup) {
        if (status == null) status = "pending";

        switch (status.toLowerCase()) {
            case "pending":
                layoutHeaderPending.setVisibility(View.VISIBLE);
                layoutHeaderConfirmed.setVisibility(View.GONE);

                tvStatusTag.setText("Đang xử lý");
                tvStatusTag.setBackgroundColor(Color.parseColor("#FF9800"));
                tvStatusMsg.setText("Chúng tôi đã nhận đơn hàng của bạn");
                tvStatusMsg.setTextColor(Color.parseColor("#FF9800"));

                btnCancelOrder.setVisibility(View.VISIBLE);
                tvCancelNote.setVisibility(View.VISIBLE);
                btnSubmitReview.setVisibility(View.GONE);
                break;

            case "confirmed":
            case "cooking":
            case "shipping":
            case "done":
            case "completed":
                layoutHeaderPending.setVisibility(View.GONE);
                layoutHeaderConfirmed.setVisibility(View.VISIBLE);

                tvStatusTag.setText("Hoàn tất");
                tvStatusTag.setBackgroundColor(Color.parseColor("#388E3C"));

                if (isPickup) {
                    tvStatusMsg.setText("Đơn hàng đã sẵn sàng. Vui lòng ra quầy nhận đồ nhé!");
                    showOrderDoneDialog(); // Hiện popup nhận nước
                } else {
                    tvStatusMsg.setText("Đã giao hàng thành công!");
                }
                tvStatusMsg.setTextColor(Color.parseColor("#388E3C"));

                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                btnSubmitReview.setVisibility(View.VISIBLE);
                break;

            case "cancelled":
                layoutHeaderPending.setVisibility(View.VISIBLE);
                layoutHeaderConfirmed.setVisibility(View.GONE);

                tvStatusTitle.setText("Đơn hàng đã hủy");
                tvStatusTitle.setTextColor(Color.RED);
                tvStatusDesc.setText("Bạn đã hủy đơn hàng này");

                btnCancelOrder.setVisibility(View.GONE);
                tvCancelNote.setVisibility(View.GONE);
                btnSubmitReview.setVisibility(View.GONE);
                break;
        }
    }


    // Helper: Tô màu Timeline
    private void highlightTimeline(int step) {
        int activeColor = Color.parseColor("#4CAF50");
        if (step >= 1) ivStep1.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        if (step >= 2) {
            line1.setBackgroundColor(activeColor);
            ivStep4.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private void resetTimelineColors() {
        int grayColor = Color.parseColor("#E0E0E0");
        ivStep1.clearColorFilter();
        ivStep4.clearColorFilter();
        line1.setBackgroundColor(grayColor);
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