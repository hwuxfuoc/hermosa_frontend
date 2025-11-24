package com.example.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.adapters.CartAdapter;
import com.example.demo.adapters.RecommendedAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
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
    private TextView tvAddress, tvCustomer, tvSubtotal, tvShipping, tvFee, tvTotalPayment;
    private TextView btnMomo, btnCash, btnOtherPayment;
    private View layoutAddressBlock;
    private TextView tvEditAddress;

    private CartAdapter cartAdapter;
    private RecommendedAdapter recommendedAdapter;
    private ApiService apiService;

    private String userID, currentAddress = "", apiCustomer = "";
    private String currentPaymentMethod = PAYMENT_METHOD_CASH;
    private String currentDeliveryMethod = "pickup";
    private String lastOrderID = "";
    private String lastMomoRequestId = ""; // ← Lưu requestId từ MoMo (rất quan trọng)

    private final List<CartResponse.CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;

    // ================= LOG SIÊU CHI TIẾT (GIỮ NGUYÊN CỦA BẠN) =================
    private static final String TAG = "MOMO_DEBUG";
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
    // ====================================================

    // Polling
    private Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;

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
        initViews();
        loadData();
        setupClickListeners();

        // Xử lý deep link ngay từ đầu (nếu mở từ MoMo)
        handleMoMoRedirect(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        L("╔══════════════════════════════════════════════════");
        L("onNewIntent() ĐƯỢC GỌI - Đây là lúc MoMo quay lại app!");
        L("New Intent data: " + (intent.getData() != null ? intent.getData().toString() : "null"));
        setIntent(intent);
        handleMoMoRedirect(intent);
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

        // Hỗ trợ mọi trường hợp: localhost, ngrok, IP thật, domain thật
        String path = data.getPath();
        if (path == null || !path.contains("payment-momo/callback")) {
            L("KHÔNG phải callback MoMo hợp lệ → bỏ qua");
            return;
        }

        String resultCode = data.getQueryParameter("resultCode");
        String orderId = data.getQueryParameter("orderId");     // ← Đây là requestId từ MoMo
        String orderInfo = data.getQueryParameter("orderInfo"); // ← Chứa ORD-xxx thật
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

            stopPolling(); // Dừng polling nếu đang chạy

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
                        String method = result.getData().getStringExtra("deliveryMethod");
                        if ("pickup".equals(method)) {
                            currentDeliveryMethod = "pickup";
                        } else {
                            currentDeliveryMethod = "delivery";
                            currentAddress = result.getData().getStringExtra("address");
                            apiCustomer = result.getData().getStringExtra("customer");
                        }
                        updateDeliveryUI();
                    }
                });
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
        tvFee = findViewById(R.id.tvFee);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        btnMomo = findViewById(R.id.btnMomo);
        btnCash = findViewById(R.id.btnCash);
        btnOtherPayment = findViewById(R.id.btnOtherPayment);
        layoutAddressBlock = findViewById(R.id.layoutAddressBlock);
        tvEditAddress = findViewById(R.id.tvEditAddress);

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
        // 1. ƯU TIÊN LẤY TỪ INTENT (khi người dùng chọn từ giỏ hàng)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SELECTED_CART_ITEMS")) {
            ArrayList<CartResponse.CartItem> selected = intent.getParcelableArrayListExtra("SELECTED_CART_ITEMS");
            if (selected != null && !selected.isEmpty()) {
                cartItemsLocal.clear();
                cartItemsLocal.addAll(selected);
                setupCartAdapter();
                updateTotals(calculateSubtotal());
                return; // ← THOÁT LUÔN, KHÔNG GỌI API
            }
        }

        // 2. Nếu không có → mới gọi API (trường hợp vào thẳng từ nơi khác – hiếm)
        Log.d("CONFIRM_CART", "Không có dữ liệu từ Intent → gọi API");
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    cartItemsLocal.clear();
                    cartItemsLocal.addAll(r.body().getData().getItems());
                    setupCartAdapter();
                    updateTotals(r.body().getData().getTotalMoney());
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCartAdapter() {
        cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
        cartAdapter.setConfirmMode(true);
        recyclerOrderItems.setAdapter(cartAdapter);
    }

    private long calculateSubtotal() {
        return cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
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
        tvEditAddress.setOnClickListener(v -> selectAddressLauncher.launch(new Intent(this, SelectAddressActivity.class)));
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    @Override
    public void onPaymentMethodSelected(String method) {
        currentPaymentMethod = method;
        int red = 0xFFA71317;
        int gray = 0xFFADABAB;

        btnCash.setBackgroundResource(method.equals("cash") ? R.drawable.payment_option_selected : R.drawable.payment_option_default);
        btnCash.setTextColor(method.equals("cash") ? red : gray);
        btnMomo.setBackgroundResource(method.equals("momo") || method.equals("vnpay") ? R.drawable.payment_option_selected : R.drawable.payment_option_default);
        btnMomo.setTextColor(method.equals("momo") || method.equals("vnpay") ? red : gray);
        btnMomo.setText(method.equals("vnpay") ? "VNPay" : "Momo");
    }

    private void placeOrder() {
        if (cartItemsLocal.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
            return;
        }

        long totalInvoiceForBE = 0;
        for (CartResponse.CartItem item : cartItemsLocal) {
            totalInvoiceForBE += item.getPrice() * item.getQuantity();
        }
        totalInvoiceForBE += "delivery".equals(currentDeliveryMethod) ? 50000L : 0L;
        totalInvoiceForBE += 10000L;

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", currentPaymentMethod);
        body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
        body.put("deliver", "delivery".equals(currentDeliveryMethod));
        body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
        body.put("note", "");
        body.put("totalInvoice", totalInvoiceForBE);
        body.put("tipsforDriver", 0);

        L("Gửi yêu cầu tạo đơn hàng - totalInvoice: " + totalInvoiceForBE);

        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    String orderID = r.body().getData().getOrderID();
                    lastOrderID = orderID;
                    L("Tạo đơn hàng thành công - OrderID: " + orderID);

                    if (PAYMENT_METHOD_CASH.equals(currentPaymentMethod)) {
                        Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) {
                        requestMomoPayment(orderID, userID);
                    } else {
                        requestVnpayPayment(orderID, userID);
                    }
                } else {
                    L("Tạo đơn hàng thất bại");
                    Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thất bại", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                L("Lỗi mạng khi tạo đơn", t);
            }
        });
    }

    private void requestMomoPayment(String orderID, String userID) {
        L("Bắt đầu tạo link MoMo cho đơn: " + orderID);
        CreateMomoRequest request = new CreateMomoRequest(orderID, userID);

        apiService.createPaymentMomo(request).enqueue(new Callback<CreateMomoResponse>() {
            @Override
            public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getPayUrl() != null) {
                    String payUrl = r.body().getPayUrl();
                    L("NHẬN PAYURL THÀNH CÔNG:");
                    L(payUrl);

                    // Lưu requestId từ payUrl để đối chiếu sau này (nếu cần)
                    Uri payUri = Uri.parse(payUrl);
                    lastMomoRequestId = payUri.getQueryParameter("orderId");

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                    startActivity(intent);

                    Toast.makeText(ConfirmOrderActivity.this, "Đang chuyển sang MoMo...", Toast.LENGTH_LONG).show();
                    startPaymentPolling(orderID);
                } else {
                    L("Lỗi tạo payUrl MoMo");
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi tạo thanh toán MoMo", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateMomoResponse> call, Throwable t) {
                L("Lỗi mạng khi tạo MoMo", t);
            }
        });
    }

    private void startPaymentPolling(String orderID) {
        stopPolling();
        L("BẮT ĐẦU POLLING thanh toán cho đơn: " + orderID);

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                apiService.confirmPaymentStatus(orderID).enqueue(new Callback<ConfirmPaymentResponse>() {
                    @Override
                    public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null && "done".equals(resp.body().getStatus())) {
                            L("POLLING PHÁT HIỆN THANH TOÁN THÀNH CÔNG!");
                            stopPolling();
                            runOnUiThread(() -> {
                                Toast.makeText(ConfirmOrderActivity.this, "Thanh toán MoMo thành công!", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(ConfirmOrderActivity.this, MainActivity.class);
                                i.putExtra("PAYMENT_SUCCESS", true);
                                i.putExtra("ORDER_ID", orderID);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            });
                        } else {
                            // ĐÚNG: gọi lại chính Runnable này
                            pollingHandler.postDelayed(pollingRunnable, 5000);
                        }
                    }

                    @Override
                    public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                        L("Polling lỗi mạng, thử lại sau 5s", t);
                        // ĐÚNG: dùng pollingRunnable đã lưu, không dùng "this"
                        pollingHandler.postDelayed(pollingRunnable, 5000);
                    }
                });
            }
        };

        // Bắt đầu lần đầu sau 8 giây
        pollingHandler.postDelayed(pollingRunnable, 8000);
    }

    private void stopPolling() {
        if (pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
            pollingRunnable = null;
            L("Đã dừng polling");
        }
    }

    private void requestVnpayPayment(String orderID, String userID) {
        // TODO: Implement VNPay nếu cần
    }

    @Override
    public void onCartUpdated() {
        long newSubtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        updateTotals(newSubtotal);
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

    @Override
    protected void onDestroy() {
        L("onDestroy() → dọn dẹp polling");
        stopPolling();
        super.onDestroy();
    }
}