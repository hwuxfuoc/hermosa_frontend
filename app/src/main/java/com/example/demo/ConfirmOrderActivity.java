package com.example.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.adapters.CartAdapter;
import com.example.demo.adapters.RecommendedAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.fragment.FragmentOrderTracking;
import com.example.demo.fragment.FragmentPaymentMethodBottomSheet;
import com.example.demo.models.*;
import com.example.demo.utils.SessionManager;
import com.example.demo.utils.SpaceItemDecoration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmOrderActivity extends AppCompatActivity
        implements FragmentPaymentMethodBottomSheet.PaymentMethodListener,
        CartAdapter.OnCartUpdateListener,
        RecommendedAdapter.OnAddToCartListener {

    public static final String PAYMENT_METHOD_CASH = "cash";
    public static final String PAYMENT_METHOD_MOMO = "momo";
    public static final String PAYMENT_METHOD_VNPAY = "vnpay";

    private RecyclerView recyclerOrderItems, recyclerRecommended;
    private Button btnPlaceOrder;
    private ImageButton btnBack;
    private TextView tvAddress, tvCustomer, tvSubtotal, tvShipping, tvDiscount, tvFee, tvTotalPayment;
    private TextView btnMomo, btnCash, btnOtherPayment;
    private View layoutAddressBlock;
    private TextView tvEditAddress;
    private TextView tvaddvoucher;
    private TextView btnTip;
    private TextView tvTip, tvTipLable, tvDiscountLable;
    private TextView tvaddnote;
    private TextView tvnotecontent;
    private TextView btnTipNone;

    private CartAdapter cartAdapter;
    private RecommendedAdapter recommendedAdapter;
    private ApiService apiService;

    private String userID, currentAddress = "", apiCustomer = "";
    private String currentNote="";
    private long currentTipAmount=0;
    private String currentPaymentMethod = PAYMENT_METHOD_CASH;
    private String currentDeliveryMethod = "pickup";
    private String lastOrderID = "";
    private String lastMomoRequestId = "";

    private final List<CartResponse.CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;
    private ActivityResultLauncher<Intent> selectVoucherLauncher;
    private Voucher appliedVoucher = null;
    private String selectedVoucherCode = null;
    private double currentDiscountAmount = 0;
    private String currentAddressID = null;

    private static final String TAG = "MOMO_DEBUG";
    private boolean isOrderPlacedSuccess = false;
    private String existingOrderID = null;
    private Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private boolean isPolling = false;
    private double autoDiscountAmount = 0;
    private String autoVoucherCode = null;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    private void L(String msg) {
        String time = sdf.format(new Date());
        Log.d(TAG, time + " | " + msg);
    }

    private void L(String msg, Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        L(msg + "\n" + sw.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        L("╔══════════════════════════════════════════════════");
        L("ConfirmOrderActivity onCreate() - START");
        L("Package: " + getPackageName());
        L("Intent data: " + (getIntent().getData() != null ? getIntent().getData().toString() : "null"));

        apiService = ApiClient.getClient().create(ApiService.class);
        userID = SessionManager.getUserID(this);
        if (userID == null) {
            L("userID = null → finish activity");
            finish();
            return;
        }
        L("userID = " + userID);


        registerAddressLauncher();
        registerVoucherLauncher();
        initViews();

        if (getIntent().hasExtra("ORDER_ID")) {
            existingOrderID = getIntent().getStringExtra("ORDER_ID");
            L("Nhận được Order ID từ Cart: " + existingOrderID);
        } else {
            Toast.makeText(this, "Thiếu mã đơn hàng!", Toast.LENGTH_SHORT).show();
        }
        if (getIntent().hasExtra("AUTO_DISCOUNT")) {
            autoDiscountAmount = getIntent().getDoubleExtra("AUTO_DISCOUNT", 0);
            autoVoucherCode = getIntent().getStringExtra("AUTO_VOUCHER_CODE");

            this.currentDiscountAmount = autoDiscountAmount;
            this.selectedVoucherCode = autoVoucherCode;

            Log.d(TAG, "Nhận được Voucher tự động: " + autoVoucherCode + " | Giảm: " + autoDiscountAmount);
        }
        loadData();
        setupClickListeners();

        handleDeepLink(getIntent());

    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }
    private void updateVoucherUI() {
        if (selectedVoucherCode != null && !selectedVoucherCode.isEmpty()) {
            tvaddvoucher.setText(selectedVoucherCode);
            tvaddvoucher.setTextColor(ContextCompat.getColor(this, R.color.smoothie_strawberry)); // Hoặc R.color.red tùy resource

            if (tvDiscount != null) {
                tvDiscount.setVisibility(View.VISIBLE);
                tvDiscount.setText(String.format("- %,d VND", (long) currentDiscountAmount));
            }
            if (tvDiscountLable != null) tvDiscountLable.setVisibility(View.VISIBLE);

        } else {
            tvaddvoucher.setText("Thêm");
            tvaddvoucher.setTextColor(ContextCompat.getColor(this, R.color.black));

            if (tvDiscount != null) tvDiscount.setVisibility(View.GONE);
            if (tvDiscountLable != null) tvDiscountLable.setVisibility(View.GONE);
        }
    }

    // ================= XỬ LÝ DEEP LINK MOMO HOÀN CHỈNH & CHÍNH XÁC NHẤT =================
    private void handleMoMoRedirect(Intent intent) {
        Uri data = intent.getData();
        if (data == null) {
            L("handleMoMoRedirect → Uri = null (bình thường nếu không phải từ MoMo)");
            return;
        }

        L("DEEP LINK NHẬN ĐƯỢC TỪ MOMO:");
        L("Full URL     → " + data.toString());
        L("Scheme       → " + data.getScheme());
        L("Host         → " + data.getHost());
        L("Path         → " + data.getPath());
        L("Query        → " + data.getQuery());

        String path = data.getPath();
        if (path == null || !path.contains("payment-momo/callback")) {
            L("KHÔNG phải callback MoMo hợp lệ → bỏ qua");
            return;
        }

        String resultCode = data.getQueryParameter("resultCode");
        String orderId = data.getQueryParameter("orderId");
        String orderInfo = data.getQueryParameter("orderInfo");
        String transId = data.getQueryParameter("transId");
        String message = data.getQueryParameter("message");

        L("resultCode   → " + resultCode);
        L("orderId (MoMo requestId) → " + orderId);
        L("transId      → " + transId);
        L("orderInfo    → " + orderInfo);
        L("message      → " + message);

        String realOrderID = extractOrderIDFromOrderInfo(orderInfo);
        lastOrderID = realOrderID;

        if ("0".equals(resultCode)) {
            L("THANH TOÁN MOMO THÀNH CÔNG QUA DEEP LINK!");
            L("OrderID thực → " + realOrderID);

            Toast.makeText(this, "Thanh toán MoMo thành công!\nMã GD: " + transId, Toast.LENGTH_LONG).show();

            stopPolling();

            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("PAYMENT_SUCCESS", true);
            i.putExtra("ORDER_ID", realOrderID);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();

        } else {
            L("THANH TOÁN THẤT BẠI HOẶC HỦY - resultCode = " + resultCode);
            Toast.makeText(this, "Thanh toán thất bại: " + (message != null ? message : "Không rõ lỗi"), Toast.LENGTH_LONG).show();
            stopPolling();
        }
    }
    private void openWebBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được trình duyệt", Toast.LENGTH_SHORT).show();
        }
    }
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "hermosaapp".equals(data.getScheme())) {
            String host = data.getHost();

            if ("payment-success".equals(host)) {
                // VNPay báo thành công -> Chuyển màn hình
                Toast.makeText(this, "Thanh toán VNPay thành công!", Toast.LENGTH_LONG).show();
                navigateToSuccess(existingOrderID);
            } else if ("payment-failed".equals(host)) {
                Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_LONG).show();
                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText("Đặt hàng");
            }
        }
    }

    private void stopPolling() {
        isPolling = false;
        pollingHandler.removeCallbacks(pollingRunnable);
    }


    private String extractOrderIDFromOrderInfo(String orderInfo) {
        if (orderInfo == null) return "Unknown";
        Matcher m = Pattern.compile("ORD-[0-9]+").matcher(orderInfo);
        return m.find() ? m.group(0) : "Unknown";
    }
    private void registerAddressLauncher() {
        selectAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String method = data.getStringExtra("deliveryMethod");

                        long subtotal = 0;
                        for (CartResponse.CartItem item : cartItemsLocal) {
                            subtotal += item.getSubtotal();
                        }

                        if ("delivery".equals(method)) {
                            currentDeliveryMethod = "delivery";
                            currentAddress = data.getStringExtra("address");
                            currentAddressID = data.getStringExtra("addressID");
                            apiCustomer = data.getStringExtra("customer");

                            updateDeliveryUI();

                            currentShippingFee = 0;
                            tvShipping.setText("Đang tính...");

                            updateTotalsWithDiscount(subtotal, (long) currentDiscountAmount);

                            getFeePreview();

                        } else {
                            currentDeliveryMethod = "pickup";

                            currentShippingFee = 0;
                            currentAddressID = null;
                            currentAddress = "";

                            updateDeliveryUI();

                            tvShipping.setText("0 VND");

                            updateTotalsWithDiscount(subtotal, (long) currentDiscountAmount);
                        }
                    }
                });
    }

    private void deletePendingOrder(String orderID) {
        if (orderID == null || orderID.isEmpty()) return;

        L("Đang dọn dẹp đơn hàng nháp: " + orderID);

        Map<String, String> body = new HashMap<>();
        body.put("orderID", orderID);

        apiService.deleteInterruptOrder(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                L("Dọn dẹp đơn hàng nháp " + orderID + " thành công (hoặc không có đơn để xóa).");
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                L("Lỗi dọn dẹp đơn hàng nháp: " + t.getMessage());
            }
        });
    }

    private void registerVoucherLauncher() {
        selectVoucherLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Nhận voucher từ Activity chọn
                        appliedVoucher = (Voucher) result.getData().getSerializableExtra("selectedVoucher");
                        if (appliedVoucher != null) {
                            applyVoucherToOrder(appliedVoucher);
                        }
                    }
                });
    }
    private void applyVoucherToOrder(Voucher voucher) {
        this.selectedVoucherCode = voucher.getVoucherCode();

        long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        double discount = 0;

        if ("percentage".equals(voucher.getDiscountType())) {
            discount = subtotal * (voucher.getDiscountValue() / 100);
        } else {
            discount = voucher.getDiscountValue();
        }

        this.currentDiscountAmount = discount;

        updateTotalsWithDiscount(subtotal, (long) discount);

        TextView tvAddVoucherText = findViewById(R.id.tvaddvoucher);
        if (tvAddVoucherText != null) {
            tvAddVoucherText.setText(voucher.getVoucherCode());
            tvAddVoucherText.setTextColor(getResources().getColor(R.color.smoothie_strawberry));
        }
    }

    private void updateTotalsWithDiscount(long subtotal, long discount) {
        long shipping = "delivery".equals(currentDeliveryMethod) ? currentShippingFee : 0L;
        long fee = 0L;

        long total = subtotal + shipping + fee + currentTipAmount - discount;
        if (total < 0) total = 0;

        tvSubtotal.setText(String.format("%,d VND", subtotal));
        tvShipping.setText(String.format("%,d VND", shipping));
        tvFee.setText(String.format("%,d VND", fee));

        if (currentTipAmount > 0) {
            tvTip.setText(String.format("%,d VND", currentTipAmount));
            tvTip.setVisibility(View.VISIBLE);
            if (tvTipLable != null) tvTipLable.setVisibility(View.VISIBLE);
        } else {
            tvTip.setVisibility(View.GONE);
            if (tvTipLable != null) tvTipLable.setVisibility(View.GONE);
        }

        if (discount > 0) {
            tvDiscount.setText(String.format("- %,d VND", discount));
            tvDiscount.setVisibility(View.VISIBLE);
            if (tvDiscountLable != null) tvDiscountLable.setVisibility(View.VISIBLE);
        } else {
            tvDiscount.setVisibility(View.GONE);
            if (tvDiscountLable != null) tvDiscountLable.setVisibility(View.GONE);
        }

        tvTotalPayment.setText(String.format("%,d VND", total));

        if (btnPlaceOrder != null) {
            btnPlaceOrder.setText("Đặt hàng - " + String.format("%,d VND", total));
        }
    }

    private void initViews() {
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnBack = findViewById(R.id.btnBack);
        tvAddress = findViewById(R.id.tvAddress);
        tvCustomer = findViewById(R.id.tvCustomer);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvDiscount=findViewById(R.id.tvDiscount);
        tvFee = findViewById(R.id.tvFee);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        btnMomo = findViewById(R.id.btnMomo);
        btnCash = findViewById(R.id.btnCash);
        btnOtherPayment = findViewById(R.id.btnOtherPayment);
        layoutAddressBlock = findViewById(R.id.layoutAddressBlock);
        tvEditAddress = findViewById(R.id.tvEditAddress);
        tvaddvoucher=findViewById(R.id.tvaddvoucher);
        btnTip=findViewById(R.id.btnTip);
        tvaddnote=findViewById(R.id.tvaddnote);
        btnTipNone=findViewById(R.id.btnTipNone);
        tvnotecontent=findViewById(R.id.tvnotecontent);
        tvTip=findViewById(R.id.tvTip);
        tvTipLable=findViewById(R.id.tvTipLable);
        tvDiscountLable=findViewById(R.id.tvDiscountLabel);
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerRecommended.addItemDecoration(new SpaceItemDecoration(dpToPx(6)));
    }

    private void loadData() {
        fetchCartProducts();
        loadRecommended();
        fetchUserInfo();
    }

    private void updateDeliveryUI() {
        layoutAddressBlock.setVisibility(View.VISIBLE);
        tvAddress.setText("pickup".equals(currentDeliveryMethod) ? "Đơn đặt và nhận tại cửa hàng" : currentAddress);
        tvCustomer.setText(apiCustomer.isEmpty() ? "Khách hàng" : apiCustomer);
    }

    private void fetchUserInfo() {
        String name = SessionManager.getUserName(this);
        String phone = SessionManager.getUserPhone(this);
        apiCustomer = (name != null && phone != null) ? name + " | " + phone : "Khách hàng";
        updateDeliveryUI();
    }

    private void fetchCartProducts() {
        /*L("Bắt đầu load giỏ hàng từ API");*/
        if (getIntent().hasExtra("SELECTED_ITEMS")) {
            ArrayList<CartResponse.CartItem> itemsFromIntent =
                    (ArrayList<CartResponse.CartItem>) getIntent().getSerializableExtra("SELECTED_ITEMS");

            if (itemsFromIntent != null && !itemsFromIntent.isEmpty()) {
                cartItemsLocal.clear();
                cartItemsLocal.addAll(itemsFromIntent);

                L("Đã nhận " + cartItemsLocal.size() + " món từ Giỏ hàng.");

                cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
                cartAdapter.setConfirmMode(true);
                recyclerOrderItems.setAdapter(cartAdapter);

                long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
                updateTotalsWithDiscount(subtotal, (long) currentDiscountAmount);

                updateVoucherUI();

                return;
            }
        }

        L("Không có dữ liệu Intent, load lại toàn bộ giỏ hàng...");
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    cartItemsLocal.clear();
                    cartItemsLocal.addAll(r.body().getData().getItems());
                    L("Load giỏ hàng thành công - " + cartItemsLocal.size() + " món");

                    if (cartItemsLocal.isEmpty()) {
                        Toast.makeText(ConfirmOrderActivity.this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
                    cartAdapter.setConfirmMode(true);
                    recyclerOrderItems.setAdapter(cartAdapter);
                    updateTotals(r.body().getData().getTotalMoney());
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                L("Lỗi load giỏ hàng", t);
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecommended() {
        apiService.getAllProducts().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<Product> list = new ArrayList<>();
                    for (MenuResponse.MenuItem item : r.body().getData()) {
                        list.add(Product.fromMenuItem(item));
                    }
                    recommendedAdapter = new RecommendedAdapter(ConfirmOrderActivity.this, list, ConfirmOrderActivity.this);
                    recyclerRecommended.setAdapter(recommendedAdapter);
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                L("Lỗi load sản phẩm gợi ý", t);
            }
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void updateTotals(long subtotal) {
        long shipping = "delivery".equals(currentDeliveryMethod) ? 50000 : 0;
        long fee = 10000;
        long total = subtotal + shipping + fee;

        tvSubtotal.setText(String.format("%,d VND", subtotal));
        tvShipping.setText(String.format("%,d VND", shipping));
        tvFee.setText(String.format("%,d VND", fee));
        tvTotalPayment.setText(String.format("%,d VND", total));
        btnPlaceOrder.setText("Đặt hàng - " + String.format("%,d VND", total));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCash.setOnClickListener(v -> onPaymentMethodSelected(PAYMENT_METHOD_CASH));
        btnMomo.setOnClickListener(v -> onPaymentMethodSelected(PAYMENT_METHOD_MOMO));
        btnOtherPayment.setOnClickListener(v -> {
            FragmentPaymentMethodBottomSheet f = new FragmentPaymentMethodBottomSheet();
            f.setPaymentMethodListener(ConfirmOrderActivity.this);
            f.show(getSupportFragmentManager(), "payment_sheet");
        });

        tvaddvoucher.setOnClickListener(v -> {
            Intent intent = new Intent(this, VoucherSelectionActivity.class);
            if (selectedVoucherCode != null) {
                intent.putExtra("CURRENT_VOUCHER_CODE", selectedVoucherCode);
            }
            selectVoucherLauncher.launch(intent);
        });
        tvEditAddress.setOnClickListener(v -> selectAddressLauncher.launch(new Intent(this, SelectAddressActivity.class)));
        btnTipNone.setOnClickListener(v->{updateTipSelection(0);});
        btnTip.setOnClickListener(v->{showCustomTipDialog();});
        tvaddnote.setOnClickListener(v->{showNoteDialog();});
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void updateTipSelection(long amount){
        L("Người dùng chọn Tip: " + amount);
        int red = 0xFFA71317;
        int gray = 0xFFADABAB;
        this.currentTipAmount=amount;
        if(amount==0){
            btnTipNone.setBackgroundResource(R.drawable.payment_option_selected);
            btnTipNone.setTextColor(red);
            btnTip.setBackgroundResource(R.drawable.payment_option_default);
            btnTip.setTextColor(gray);
        }
        else{
            btnTip.setBackgroundResource(R.drawable.payment_option_selected);
            btnTipNone.setBackgroundResource(R.drawable.payment_option_default);
            btnTip.setTextColor(red);
            btnTipNone.setTextColor(gray);
        }
        if ("delivery".equals(currentDeliveryMethod) && currentAddressID != null) {
            getFeePreview();
        } else {
            long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
            updateTotalsWithDiscount(subtotal, (long) currentDiscountAmount);
        }
    }

    private void showCustomTipDialog(){
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(this);
        builder.setTitle("Nhập số tiền tip cho tài xế");
        final android.widget.EditText input= new android.widget.EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Đồng ý",(dialog,which)->{
            String tipstr=input.getText().toString();
            if(!tipstr.isEmpty()){
                long tip= Long.parseLong(tipstr);
                updateTipSelection(tip);
            }
        });
        builder.setNegativeButton("Hủy",(dialog,which)->{dialog.cancel();});
        builder.show();
    }

    private void showNoteDialog(){
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(this);
        builder.setTitle("Ghi chú cho đơn hàng");
        final android.widget.EditText input=new android.widget.EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Lưu",(dialog,which)->{
            currentNote=input.getText().toString();
            if(currentNote.isEmpty()){
                tvnotecontent.setText("Ghi chú");
            }
            else{
                String displayNote=currentNote.length()>20 ? currentNote.substring(0,17)+"...":currentNote;
                tvnotecontent.setText(displayNote);
            }
        });
        builder.setNegativeButton("Hủy", ((dialog, which) -> dialog.cancel()));
        builder.show();
    }

    @Override
    public void onPaymentMethodSelected(String method) {
        currentPaymentMethod = method;
        int red = 0xFFA71317;
        int white = 0xFFFFFFFF;

        btnCash.setBackgroundResource(method.equals("cash") ? R.drawable.payment_option_selected : R.drawable.payment_option_default);
        btnCash.setTextColor(method.equals("cash") ? red : white);
        btnMomo.setBackgroundResource(method.equals("momo") || method.equals("vnpay") ? R.drawable.payment_option_selected : R.drawable.payment_option_default);
        btnMomo.setTextColor(method.equals("momo") || method.equals("vnpay") ? red : white);
        btnMomo.setText(method.equals("vnpay") ? "VNPay" : "Momo");
    }

    private long currentShippingFee = 0;

    private void getFeePreview() {
        if (userID == null || currentAddressID == null) {
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("addressID", currentAddressID);
        body.put("tipsforDriver", currentTipAmount);

        long currentCartTotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        body.put("currentTotalCart", currentCartTotal);

        tvShipping.setText("Đang tính...");

        apiService.calculateShippingFee(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    Object dataObj = response.body().getData();
                    if (dataObj instanceof Map) {
                        Map<String, Object> data = (Map<String, Object>) dataObj;

                        double feeDouble = (Double) data.get("deliveryFee");
                        double totalDouble = (Double) data.get("finalTotal");

                        long deliveryFee = (long) feeDouble;
                        long finalTotal = (long) totalDouble;

                        currentShippingFee = deliveryFee;

                        tvShipping.setText(String.format("%,d VND", deliveryFee));
                        tvTotalPayment.setText(String.format("%,d VND", finalTotal));

                        if (btnPlaceOrder != null) {
                            btnPlaceOrder.setText("Đặt hàng - " + String.format("%,d VND", finalTotal));
                        }
                    }
                } else {
                    tvShipping.setText("Lỗi tính phí");
                    Log.e(TAG, "Lỗi API: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                tvShipping.setText("Lỗi mạng");
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void placeOrder() {
        if (existingOrderID == null || existingOrderID.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("delivery".equals(currentDeliveryMethod) && (currentAddress == null || currentAddress.isEmpty())) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (PAYMENT_METHOD_CASH.equals(currentPaymentMethod)) {
            processCashPayment();
        }
        else if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) {
            processMomoPayment();
        }
        else if (PAYMENT_METHOD_VNPAY.equals(currentPaymentMethod)) {
            processVnpayPayment();
        }
    }

    private void processCashPayment() {
        // Đơn hàng đã tạo sẵn ở Cart rồi, giờ chỉ cần báo thành công
        Toast.makeText(this, "Đặt hàng thành công (Tiền mặt)", Toast.LENGTH_LONG).show();
        navigateToSuccess(existingOrderID);
    }

    private void processMomoPayment() {
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang lấy link MoMo...");

        CreateMomoRequest request = new CreateMomoRequest(existingOrderID);

        apiService.createPaymentMomo(request).enqueue(new Callback<CreateMomoResponse>() {
            @Override
            public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String payUrl = response.body().getPayUrl();
                    if (payUrl != null) {
                        openWebBrowser(payUrl);
                        // MoMo Server-to-Server -> FE phải Polling để biết kết quả
                        startMomoPolling(existingOrderID);
                    }
                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi lấy link MoMo", Toast.LENGTH_SHORT).show();
                    btnPlaceOrder.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<CreateMomoResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                btnPlaceOrder.setEnabled(true);
            }
        });
    }

    private int pollingCount = 0;

    private static final long POLL_DELAY_MS = 3000;

    private static final int MAX_POLLING_RETRIES = 100;

    private void startMomoPolling(String orderID) {
        if (isPolling) return;

        isPolling = true;
        pollingCount = 0;
        btnPlaceOrder.setText("Đang chờ xác nhận MoMo...");
        btnPlaceOrder.setEnabled(false);

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPolling) return;

                if (pollingCount >= MAX_POLLING_RETRIES) {
                    stopPolling();
                    Toast.makeText(ConfirmOrderActivity.this, "Giao dịch đang xử lý hoặc quá thời gian chờ. Vui lòng kiểm tra lại lịch sử đơn hàng.", Toast.LENGTH_LONG).show();
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Kiểm tra lại");
                    return;
                }

                pollingCount++;
                Log.d(TAG, "Polling lần thứ: " + pollingCount + " (Đang gọi API...)");

                // Gọi API kiểm tra
                apiService.confirmPaymentStatus(orderID).enqueue(new Callback<ConfirmPaymentResponse>() {
                    @Override
                    public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> response) {
                        if (!isPolling) return;

                        if (response.isSuccessful() && response.body() != null) {
                            String status = response.body().getData().getPaymentStatus();

                            if ("done".equals(status)) {
                                L("Polling: Phát hiện thanh toán THÀNH CÔNG!");
                                stopPolling();
                                navigateToSuccess(orderID);
                            }
                            else {
                                pollingHandler.postDelayed(pollingRunnable, POLL_DELAY_MS);
                            }
                        } else {
                            L("Polling: Lỗi server " + response.code() + ", thử lại sau...");
                            pollingHandler.postDelayed(pollingRunnable, POLL_DELAY_MS);
                        }
                    }

                    @Override
                    public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                        if (!isPolling) return;

                        Log.e(TAG, "Polling lỗi kết nối (User có thể đang ở app MoMo): " + t.getMessage());

                        pollingHandler.postDelayed(pollingRunnable, POLL_DELAY_MS);
                    }
                });
            }
        };

        pollingHandler.post(pollingRunnable);
    }

    // ================= LOGIC VNPAY (DEEP LINK REDIRECT) =================
    private void processVnpayPayment() {
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang lấy link VNPay...");

        Map<String, String> body = new HashMap<>();
        body.put("orderID", existingOrderID);

        apiService.createPaymentVnpay(body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String vnpayUrl = response.body();
                    openWebBrowser(vnpayUrl);

                    btnPlaceOrder.setText("Vui lòng thanh toán trên Web...");
                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi tạo link VNPay", Toast.LENGTH_SHORT).show();
                    btnPlaceOrder.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Lỗi Call VNPay: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối VNPay", Toast.LENGTH_SHORT).show();
                btnPlaceOrder.setEnabled(true);
            }
        });
    }

    private void processVoucherLogic(String orderID) {
        if (selectedVoucherCode == null || selectedVoucherCode.isEmpty()) {
            L("STEP 2 SKIP: Không có voucher -> Thanh toán ngay");
            proceedToPayment(orderID);
            return;
        }

        L("STEP 2: Đang áp dụng voucher " + selectedVoucherCode + " cho đơn " + orderID);

        Map<String, String> body = new HashMap<>();
        body.put("voucherCode", selectedVoucherCode);
        body.put("orderID", orderID);

        apiService.applyVoucher(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful()) {
                    L("STEP 2 DONE: Áp dụng voucher thành công trên Server!");
                } else {
                    L("STEP 2 FAIL: Voucher lỗi (hết hạn/không hợp lệ). Server giữ giá gốc.");
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi áp dụng Voucher, sẽ thanh toán giá gốc", Toast.LENGTH_LONG).show();
                }

                proceedToPayment(orderID);
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                L("STEP 2 ERROR: Lỗi mạng voucher", t);
                proceedToPayment(orderID);
            }
        });
    }
    private void proceedToPayment(String orderID) {
        L("STEP 3: Bắt đầu thanh toán cho OrderID: " + orderID);

        btnPlaceOrder.setEnabled(true);
        btnPlaceOrder.setText("Đặt hàng");

        if (PAYMENT_METHOD_CASH.equals(currentPaymentMethod)) {
            Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();

            setResult(RESULT_OK);
            finish();
        }
        else if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) {
            requestMomoPayment(orderID, userID);
        }
        else {
            requestVnpayPayment(orderID, userID);
        }
    }
    private void resetPlaceOrderButton() {
        btnPlaceOrder.setEnabled(true);
        long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        updateTotalsWithDiscount(subtotal, (long) currentDiscountAmount);
    }

    private void requestMomoPayment(String orderID, String userID) {
        L("Gọi API MoMo cho đơn: " + orderID);
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang lấy link MoMo...");

        CreateMomoRequest request = new CreateMomoRequest(orderID);

        apiService.createPaymentMomo(request).enqueue(new Callback<CreateMomoResponse>() {
            @Override
            public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String payUrl = response.body().getPayUrl();
                    if (payUrl != null) {
                        openWebBrowser(payUrl);
                        startMomoPolling(existingOrderID);
                    } else {
                        Toast.makeText(ConfirmOrderActivity.this, "Không tìm thấy link thanh toán", Toast.LENGTH_SHORT).show();
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText("Thử lại");
                    }
                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi tạo đơn MoMo: " + response.message(), Toast.LENGTH_LONG).show();
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Thử lại");
                }
            }

            @Override
            public void onFailure(Call<CreateMomoResponse> call, Throwable t) {
                L("Lỗi mạng khi gọi MoMo", t);
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối Server! Kiểm tra lại mạng/Ngrok.", Toast.LENGTH_LONG).show();
                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText("Thử lại");
            }
        });
    }

    private void startPolling(String orderID) {
        if (isPolling) return;

        isPolling = true;
        L(">>> BẮT ĐẦU POLLING CHO ĐƠN: " + orderID);

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPolling) return;

                apiService.confirmPaymentStatus(orderID).enqueue(new Callback<ConfirmPaymentResponse>() {
                    @Override
                    public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> response) {
                        if (!isPolling) return;

                        if (response.isSuccessful() && response.body() != null) {
                            Order orderData = response.body().getData();

                            String status = (orderData != null) ? orderData.getPaymentStatus() : "unknown";
                            L("Polling check: " + status);

                            if ("done".equals(status)) {
                                stopPollingLogic();

                                onPaymentSuccess(orderData.getOrderID());
                            } else {
                                pollingHandler.postDelayed(pollingRunnable, POLL_DELAY_MS);
                            }
                        } else {
                            pollingHandler.postDelayed(pollingRunnable, POLL_DELAY_MS);
                        }
                    }

                    @Override
                    public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                        L("Polling lỗi mạng: " + t.getMessage());
                        pollingHandler.postDelayed(pollingRunnable, POLL_DELAY_MS);
                    }
                });
            }
        };

        pollingHandler.post(pollingRunnable);
    }

    private void stopPollingLogic() {
        isPolling = false;
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
        L(">>> ĐÃ DỪNG POLLING");
    }

    private void onPaymentSuccess(String orderID) {
        runOnUiThread(() -> {
            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();

            Intent i = new Intent(ConfirmOrderActivity.this, MainActivity.class);
            i.putExtra("PAYMENT_SUCCESS", true);
            i.putExtra("ORDER_ID", orderID);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    private void navigateToSuccess(String orderID) {
        L("Chuyển sang màn hình Tracking trong Activity hiện tại");

        View container = findViewById(R.id.fragment_container);
        if (container != null) {
            container.setVisibility(View.VISIBLE);
        }

        FragmentOrderTracking trackingFragment = new FragmentOrderTracking();

        Bundle args = new Bundle();
        args.putString("ORDER_ID", orderID);
        trackingFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, trackingFragment)
                .commit();

        isOrderPlacedSuccess = true;
    }

    private void requestVnpayPayment(String orderID, String userID) {
        Log.d(TAG, "Requesting VNPay payment - orderID: " + orderID);
        CreateVnpayRequest request = new CreateVnpayRequest(orderID, userID);
        apiService.createPaymentVnpayString(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, "VNPay response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    String payUrl = response.body().trim().replace("\"", "");
                    Log.d(TAG, "Received VNPay payUrl: " + payUrl);
                    if (payUrl.startsWith("http")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                        intent.putExtra("orderID", orderID); // Lưu orderID để xử lý sau
                        startActivity(intent);
                        Log.d(TAG, "Opening VNPay payment page with intent: " + intent.toString());
                        Toast.makeText(ConfirmOrderActivity.this, "Đang mở VNPay...", Toast.LENGTH_LONG).show();
                    } else {
                        Log.w(TAG, "Invalid VNPay URL: " + payUrl);
                        Toast.makeText(ConfirmOrderActivity.this, "Link không hợp lệ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "VNPay error: " + response.message());
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi server VNPay: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "VNPay network error: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối VNPay", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCartUpdated() {
        selectedVoucherCode = null;
        currentDiscountAmount = 0;
        appliedVoucher = null;

        if (tvaddvoucher != null) {
            tvaddvoucher.setText("Thêm");
            tvaddvoucher.setTextColor(getResources().getColor(R.color.black));
        }

        if (tvDiscount != null) tvDiscount.setVisibility(View.GONE);

        long newSubtotal = 0;
        for (CartResponse.CartItem item : cartItemsLocal) {
            newSubtotal += item.getSubtotal();
        }

        updateTotalsWithDiscount(newSubtotal, 0);
        fetchCartProducts();
    }

    @Override
    public void onAddToCart(Product product) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("productID", product.getProductID());
        body.put("quantity", 1);
        body.put("size", "medium");
        body.put("topping", new ArrayList<>());
        body.put("note", "");
        apiService.addToCart(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                if (r.isSuccessful()) fetchCartProducts();
            }
            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi thêm món", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void autoApplyBestVoucher(String orderID) {
        Map<String, String> body = new HashMap<>();
        body.put("orderID", orderID);

        apiService.autoApplyVoucher(body).enqueue(new Callback<VoucherResponse>() {
            @Override
            public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VoucherResponse res = response.body();

                    if (res.getBestVoucher() != null) {
                        double discount = res.getDiscountAmount();
                        String code = res.getBestVoucher().getVoucherCode();

                        Toast.makeText(ConfirmOrderActivity.this,
                                "Đã áp dụng voucher: " + code + " (Giảm " + discount + ")",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ConfirmOrderActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<VoucherResponse> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPolling && pollingRunnable != null) {
            L("User quay lại App -> Reset bộ đếm và Check trạng thái ngay lập tức");

            pollingCount = 0;

            pollingHandler.removeCallbacks(pollingRunnable);

            pollingHandler.postDelayed(pollingRunnable, 500);
        }
    }

    @Override
    protected void onDestroy() {
        L("onDestroy() → dọn dẹp polling");
        stopPolling();

        if (isFinishing() && !isOrderPlacedSuccess && existingOrderID != null) {
            L("Activity đóng mà chưa thanh toán xong. Xóa đơn nháp: " + existingOrderID);
            deletePendingOrder(existingOrderID);
        }

        super.onDestroy();
    }

}