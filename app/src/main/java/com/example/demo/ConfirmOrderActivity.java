package com.example.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private String lastOrderID;
    private final List<CartResponse.CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        apiService = ApiClient.getClient().create(ApiService.class);
        userID = SessionManager.getUserID(this);
        if (userID == null) {
            finish();
            return;
        }

        registerAddressLauncher();
        initViews();
        loadData();
        setupClickListeners();
        handleDeepLink(getIntent());
    }

    private void registerAddressLauncher() {
        selectAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String method = result.getData().getStringExtra("deliveryMethod");
                        if ("pickup".equals(method)) {
                            currentDeliveryMethod = "pickup";
                            updateDeliveryUI();
                        } else {
                            currentDeliveryMethod = "delivery";
                            currentAddress = result.getData().getStringExtra("address");
                            apiCustomer = result.getData().getStringExtra("customer");
                            updateDeliveryUI();
                        }
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
    }

    private void loadData() {
        fetchCartProducts(); // Luôn load từ API
        loadRecommended();
        fetchUserInfo();
    }

    private void updateDeliveryUI() {
        layoutAddressBlock.setVisibility(View.VISIBLE);
        if ("pickup".equals(currentDeliveryMethod)) {
            tvAddress.setText("Đơn đặt và nhận tại cửa hàng");
        } else {
            tvAddress.setText(currentAddress);
        }
        tvCustomer.setText(apiCustomer);
    }

    private void fetchUserInfo() {
        String name = SessionManager.getUserName(this);
        String phone = SessionManager.getUserPhone(this);
        apiCustomer = (name != null && phone != null) ? name + " | " + phone : "Khách hàng";
        updateDeliveryUI();
    }

    private void fetchCartProducts() {
        Log.d("CONFIRM_CART", "Bắt đầu load giỏ hàng từ API");
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                Log.d("CONFIRM_CART", "Response code: " + r.code());
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    cartItemsLocal.clear();
                    cartItemsLocal.addAll(r.body().getData().getItems());
                    Log.d("CONFIRM_CART", "Items loaded: " + cartItemsLocal.size());
                    if (cartItemsLocal.isEmpty()) {
                        Toast.makeText(ConfirmOrderActivity.this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
                    cartAdapter.setConfirmMode(true);
                    recyclerOrderItems.setAdapter(cartAdapter);
                    updateTotals(r.body().getData().getTotalMoney());
                } else {
                    Log.e("CONFIRM_CART", "Response thất bại hoặc data null");
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e("CONFIRM_CART", "Lỗi mạng: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
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
                    recommendedAdapter = new RecommendedAdapter(list, ConfirmOrderActivity.this);
                    recyclerRecommended.setAdapter(recommendedAdapter);
                }
            }
            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {}
        });
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
        Log.d("PLACE_ORDER", "Bắt đầu tạo đơn hàng, items in local: " + cartItemsLocal.size());
        if (cartItemsLocal.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
            return;
        }
        long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        long shipping = "delivery".equals(currentDeliveryMethod) ? 50000 : 0;
        long fee = 10000;
        long totalInvoice = subtotal + shipping + fee;

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", currentPaymentMethod);
        body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
        body.put("deliver", "delivery".equals(currentDeliveryMethod));
        body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
        body.put("note", "");
        body.put("totalInvoice", totalInvoice); // ← GỬI ĐI CHO BE

        Log.d("PLACE_ORDER", "Gửi totalInvoice: " + totalInvoice); // ← LOG ĐỂ CHECK

        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
                Log.d("PLACE_ORDER", "Response code: " + r.code());
                if (r.isSuccessful() && r.body() != null) {
                    lastOrderID = r.body().getData().getOrderID();
                    Log.d("PLACE_ORDER", "Đơn hàng thành công: " + lastOrderID);

                    if (PAYMENT_METHOD_CASH.equals(currentPaymentMethod)) {
                        Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) {
                            requestMomoPayment(lastOrderID, userID);
                        } else {
                            requestVnpayPayment(lastOrderID, userID);
                        }
                    }
                } else {
                    String msg = r.body() != null ? r.body().getMessage() : r.message();
                    Log.e("PLACE_ORDER", "Lỗi: " + msg);
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Log.e("PLACE_ORDER", "Lỗi mạng: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi đặt hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // BỔ SUNG LOG + MỞ LINK
    private void requestMomoPayment(String orderID, String userID) {
        Log.d("MOMO", "Gửi: orderID=" + orderID + ", userID=" + userID);
        CreateMomoRequest request = new CreateMomoRequest(orderID, userID);
        apiService.createPaymentMomo(request).enqueue(new Callback<CreateMomoResponse>() {
            @Override
            public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> r) {
                Log.d("MOMO", "Code: " + r.code() + ", URL: " + call.request().url());
                if (r.isSuccessful() && r.body() != null && r.body().getPayUrl() != null) {
                    String payUrl = r.body().getPayUrl();
                    Log.d("MOMO", "payUrl: " + payUrl);
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                    startActivity(i);
                    Toast.makeText(ConfirmOrderActivity.this, "Đang mở MoMo...", Toast.LENGTH_LONG).show();
                } else {
                    String msg = r.body() != null ? r.body().getMessage() : r.message();
                    Log.e("MOMO", "Lỗi: " + msg);
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateMomoResponse> call, Throwable t) {
                Log.e("MOMO", "Lỗi mạng: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi mạng MoMo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestVnpayPayment(String orderID, String userID) {
        Log.d("VNPAY", "Tạo thanh toán VNPay - orderID: " + orderID);

        CreateVnpayRequest request = new CreateVnpayRequest(orderID, userID);

        // ĐỔI Call<CreateVnpayResponse> → Call<String>
        apiService.createPaymentVnpayString(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("VNPAY", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String payUrl = response.body().trim().replace("\"", ""); // Xóa dấu " ở đầu/cuối
                    Log.d("VNPAY", "URL nhận được: " + payUrl);

                    if (payUrl.startsWith("http")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                        startActivity(intent);
                        Toast.makeText(ConfirmOrderActivity.this, "Đang mở VNPay...", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ConfirmOrderActivity.this, "Link không hợp lệ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi server VNPay: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("VNPAY", "Lỗi mạng: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối VNPay", Toast.LENGTH_LONG).show();
            }
        });
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
    protected void onResume() {
        super.onResume();
    }

    private void confirmPaymentStatus(String orderID) {
        Log.d("CONFIRM", "Kiểm tra trạng thái orderID=" + orderID);
        apiService.confirmPaymentStatus(orderID).enqueue(new Callback<ConfirmPaymentResponse>() {
            @Override
            public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    Log.d("CONFIRM", "status=" + r.body().getStatus());
                    if ("done".equals(r.body().getStatus())) {
                        Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if ("failed".equals(r.body().getStatus())) {
                        Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                Log.e("CONFIRM", "Lỗi: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        Uri data = intent.getData();
        String host = data.getHost(); // "payment-success" hoặc "payment-failed"

        if ("payment-success".equals(host)) {
            Toast.makeText(this, "Thanh toán VNPay thành công!", Toast.LENGTH_LONG).show();
            finish();
        } else if ("payment-failed".equals(host)) {
            Toast.makeText(this, "Thanh toán VNPay thất bại!", Toast.LENGTH_LONG).show();
            // Có thể gọi API hủy đơn nếu cần
        }
    }

}