/*
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

        recyclerRecommended.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRecommended.setHasFixedSize(false); // cho phép cuộn mượt
    }

    private void loadData() {
        fetchCartProducts(); // Luôn load từ API
        loadRecommended();
        fetchUserInfo();
    }

    */
/*private void updateDeliveryUI() {
        layoutAddressBlock.setVisibility(View.VISIBLE);
        if ("pickup".equals(currentDeliveryMethod)) {
            tvAddress.setText("Đơn đặt và nhận tại cửa hàng");
        } else {
            tvAddress.setText(currentAddress);
        }
        tvCustomer.setText(apiCustomer);
    }*//*

    private void updateDeliveryUI() {
        layoutAddressBlock.setVisibility(View.VISIBLE);

        if ("pickup".equals(currentDeliveryMethod)) {
            tvAddress.setText("Đơn đặt và nhận tại cửa hàng");
            tvCustomer.setVisibility(View.GONE); // ✅ BỔ SUNG: Ẩn tên + SĐT khi nhận tại quán
        } else {
            tvAddress.setText(currentAddress);
            tvCustomer.setText(apiCustomer);     // ✅ BỔ SUNG: Hiển thị tên + SĐT từ SessionManager
            tvCustomer.setVisibility(View.VISIBLE);
        }
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
        Log.d("VNPAY", "Bắt đầu tạo thanh toán VNPAY - orderID: " + orderID);

        CreateVnpayRequest request = new CreateVnpayRequest(orderID, userID);

        // DÙNG LẠI CALL CHUẨN - NHẬN JSON ĐẸP
        apiService.createPaymentVnpay(request).enqueue(new Callback<CreateVnpayResponse>() {
            @Override
            public void onResponse(Call<CreateVnpayResponse> call, Response<CreateVnpayResponse> response) {
                Log.d("VNPAY", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String payUrl = response.body().getUrl();
                    Log.d("VNPAY", "URL nhận được: " + payUrl);

                    if (payUrl != null && payUrl.startsWith("http")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(ConfirmOrderActivity.this, "Đang mở VNPAY...", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ConfirmOrderActivity.this, "Link thanh toán không hợp lệ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi server VNPAY", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateVnpayResponse> call, Throwable t) {
                Log.e("VNPAY", "Lỗi mạng: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối VNPAY", Toast.LENGTH_LONG).show();
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
        if (lastOrderID != null) {
            confirmPaymentStatus(lastOrderID);
        }
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
}
*/
/*
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
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleMoMoRedirect(intent);
    }

    // HÀM CHÍNH – BẮT LINK THANH TOÁN THÀNH CÔNG TỪ MOMO
    private void handleMoMoRedirect(Intent intent) {
        Uri data = intent.getData();
        if (data == null) return;

        String path = data.getPath();
        if (path == null || !path.contains("payment-momo/callback")) return;

        String resultCode = data.getQueryParameter("resultCode");

        if ("0".equals(resultCode)) {
            // THANH TOÁN THÀNH CÔNG 100%
            String orderInfo = data.getQueryParameter("orderInfo");
            String transId = data.getQueryParameter("transId") != null ? data.getQueryParameter("transId") : "N/A";

            String realOrderID = "";
            if (orderInfo != null) {
                Matcher m = Pattern.compile("ORD-[0-9]+").matcher(orderInfo);
                if (m.find()) realOrderID = m.group(0);
            }

            Toast.makeText(this, "Thanh toán MoMo thành công!\nMã GD: " + transId, Toast.LENGTH_LONG).show();

            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("PAYMENT_SUCCESS", true);
            i.putExtra("ORDER_ID", realOrderID);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();

        } else {
            Toast.makeText(this, "Thanh toán MoMo thất bại hoặc đã hủy", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // === TẤT CẢ CODE CŨ CỦA BẠN GIỮ NGUYÊN TỪ ĐÂY TRỞ XUỐNG ===
    // (chỉ cần giữ lại các hàm placeOrder, requestMomoPayment, v.v.)
    // ĐÃ ĐƯỢC TỐI ƯU SẠCH SẼ DƯỚI ĐÂY

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


    */
/*private void registerAddressLauncher() {
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
    }*//*


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
        */
/*recyclerRecommended.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRecommended.setHasFixedSize(false);*//*

        recyclerRecommended.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRecommended.setHasFixedSize(false);

        // Thêm khoảng cách 6dp cho recyclerRecommended (ngang)
        int spacingInPx = dpToPx(6); // Convert 6dp sang px
        recyclerRecommended.addItemDecoration(new SpaceItemDecoration(spacingInPx));
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
                        Product product = Product.fromMenuItem(item);
                        // Optionally pre-fetch details if needed, but rely on adapter's API call
                        list.add(product);
                    }
                    recommendedAdapter = new RecommendedAdapter(
                            ConfirmOrderActivity.this,
                            list,
                            ConfirmOrderActivity.this
                    );
                    recyclerRecommended.setAdapter(recommendedAdapter);
                } else {
                    Log.e("LOAD_RECOMMENDED", "API call failed or null response");
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                Log.e("LOAD_RECOMMENDED", "API failure: " + t.getMessage());
            }
        });
    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
    */
/*private void placeOrder() {
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
    }*//*

    private void placeOrder() {
        if (cartItemsLocal.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
            return;
        }

        // TÍNH LẠI Y HỆT NHƯƠNG PHÁP BE ĐANG LÀM (chỉ lấy price gốc × quantity)
        long beWillCalculateTotal = 0;
        for (CartResponse.CartItem item : cartItemsLocal) {
            // BE chỉ lấy price gốc từ menu × quantity, bỏ qua size & topping
            Long basePrice = item.getPrice();           // ← quan trọng: phải có field price trong CartItem
            int quantity = item.getQuantity();
            if (basePrice != null) {
                beWillCalculateTotal += basePrice * quantity;
            }
        }

        // Phí ship + phí dịch vụ thì BE không tính → FE phải cộng vào để tổng giống thực tế
        long shippingFee = "delivery".equals(currentDeliveryMethod) ? 50000L : 0L;
        long serviceFee = 10000L;

        // Gửi đúng cái BE sẽ dùng + phí mà BE không tính
        long totalInvoiceForBE = beWillCalculateTotal + shippingFee + serviceFee;

        Log.d("PLACE_ORDER", "BE sẽ tính được: " + beWillCalculateTotal);
        Log.d("PLACE_ORDER", "FE gửi totalInvoice: " + totalInvoiceForBE);

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", currentPaymentMethod);
        body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
        body.put("deliver", "delivery".equals(currentDeliveryMethod));
        body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
        body.put("note", "");
        body.put("totalInvoice", totalInvoiceForBE);     // ← ĐÚNG CÁI BE MUỐN
        body.put("tipsforDriver", 0);

        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    String orderID = r.body().getData().getOrderID();

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
                    String msg = "Đặt hàng thất bại";
                    if (r.errorBody() != null) {
                        try { msg = r.errorBody().string(); } catch (Exception e) {}
                    } else if (r.body() != null) {
                        msg = r.body().getMessage();
                    }
                    Toast.makeText(ConfirmOrderActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // BỔ SUNG LOG + MỞ LINK
    */
/*private void requestMomoPayment(String orderID, String userID) {
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
    }*//*

    private void requestMomoPayment(String orderID, String userID) {
        Log.d("MOMO", "Chuẩn bị mở MoMo cho đơn: " + orderID);
        CreateMomoRequest request = new CreateMomoRequest(orderID, userID);

        apiService.createPaymentMomo(request).enqueue(new Callback<CreateMomoResponse>() {
            @Override
            public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getPayUrl() != null) {
                    String payUrl = r.body().getPayUrl();
                    Log.d("MOMO", "Mở MoMo: " + payUrl);

                    // Mở MoMo
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                    startActivity(intent);

                    Toast.makeText(ConfirmOrderActivity.this,
                            "Đang chuyển sang MoMo để thanh toán...", Toast.LENGTH_LONG).show();

                    // BẮT ĐẦU POLLING SAU KHI MỞ MOMO
                    startPaymentPolling(orderID);

                } else {
                    Toast.makeText(ConfirmOrderActivity.this,
                            "Lỗi tạo thanh toán MoMo", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateMomoResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this,
                        "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    */
/*private void startPaymentPolling(String orderID) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long CHECK_INTERVAL = 3000L;
        final long MAX_WAIT_TIME = 180000L; // 3 phút

        // Cách 2 – dùng inner class → đẹp, sạch, không lỗi, không warning
        class PollingTask implements Runnable {
            private final long startTime = System.currentTimeMillis(); // thời gian bắt đầu

            @Override
            public void run() {
                // Quá 3 phút thì dừng
                if (System.currentTimeMillis() - startTime > MAX_WAIT_TIME) {
                    runOnUiThread(() -> Toast.makeText(ConfirmOrderActivity.this,
                            "Hết thời gian chờ thanh toán", Toast.LENGTH_SHORT).show());
                    return;
                }

                apiService.confirmPaymentStatus(orderID).enqueue(new Callback<ConfirmPaymentResponse>() {
                    @Override
                    public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && "done".equals(response.body().getStatus())) {

                            runOnUiThread(() -> {
                                Toast.makeText(ConfirmOrderActivity.this,
                                        "Thanh toán MoMo thành công!", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(ConfirmOrderActivity.this, MainActivity.class);
                                intent.putExtra("PAYMENT_SUCCESS", true);
                                intent.putExtra("ORDER_ID", orderID);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            });
                            return; // thoát hẳn polling
                        }
                        // Chưa xong → gọi lại chính nó
                        handler.postDelayed(PollingTask.this, CHECK_INTERVAL);
                    }

                    @Override
                    public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                        handler.postDelayed(PollingTask.this, CHECK_INTERVAL);
                    }
                });
            }
        }

        // Bắt đầu sau 7 giây
        handler.postDelayed(new PollingTask(), 7000L);
    }*//*

    private void startPaymentPolling(String orderID) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long CHECK_INTERVAL = 5000L;   // 5 giây/lần (tăng lên cho mạng chậm)
        final long MAX_WAIT_TIME = 300000L;  // 5 phút tối đa (đủ cho khách chậm thanh toán)

        // Dùng inner class → không bao giờ bị lỗi "might not have been initialized"
        class PollingTask implements Runnable {
            private final long startTime = System.currentTimeMillis();

            @Override
            public void run() {
                // Quá 5 phút thì dừng
                if (System.currentTimeMillis() - startTime > MAX_WAIT_TIME) {
                    runOnUiThread(() -> Toast.makeText(ConfirmOrderActivity.this,
                            "Hết thời gian chờ thanh toán", Toast.LENGTH_LONG).show());
                    return;
                }

                // Gọi API kiểm tra trạng thái
                apiService.confirmPaymentStatus(orderID).enqueue(new Callback<ConfirmPaymentResponse>() {
                    @Override
                    public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if ("done".equals(response.body().getStatus())) {
                                // THÀNH CÔNG → về app ngay lập tức
                                runOnUiThread(() -> {
                                    Toast.makeText(ConfirmOrderActivity.this,
                                            "Thanh toán MoMo thành công!", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(ConfirmOrderActivity.this, MainActivity.class);
                                    intent.putExtra("PAYMENT_SUCCESS", true);
                                    intent.putExtra("ORDER_ID", orderID);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                                return; // thoát hẳn polling
                            }
                        }

                        // Chưa done → tiếp tục poll dù mạng chậm cỡ nào
                        handler.postDelayed(PollingTask.this, CHECK_INTERVAL);
                    }

                    @Override
                    public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                        // DÙ LỖI GÌ: timeout, no internet, server die → VẪN TIẾP TỤC TH�Ử LẠI!!!
                        Log.w("MOMO_POLL", "Lỗi mạng, sẽ thử lại sau 5s: " + t.getMessage());
                        handler.postDelayed(PollingTask.this, CHECK_INTERVAL);
                    }
                });
            }
        }

        // Bắt đầu polling sau 8 giây (cho khách kịp mở MoMo + nhập mật khẩu)
        handler.postDelayed(new PollingTask(), 8000L);
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

    */
/*@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }*//*


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

}*/
/*
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

    private final List<CartResponse.CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;

    // ================= LOG SIÊU CHI TIẾT =================
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

        // Xử lý deep link ngay khi mở (có thể từ MoMo)
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

    // ================= XỬ LÝ DEEP LINK MOMO CHI TIẾT NHẤT =================
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
        L("Port         → " + data.getPort());
        L("Path         → " + data.getPath());
        L("Query        → " + data.getQuery());

        // Kiểm tra hợp lệ (hỗ trợ localhost, 10.0.2.2, ngrok, IP thật)
        boolean valid = ("http".equals(data.getScheme()) || "https".equals(data.getScheme()))
                && data.getPath() != null && data.getPath().contains("payment-momo/callback")
                && (data.getHost() != null && (
                data.getHost().contains("localhost") ||
                        data.getHost().contains("10.0.2.2") ||
                        data.getHost().contains("192.168") ||
                        data.getHost().contains("ngrok") ||
                        data.getPort() == 8000
        ));

        L("Valid deep link? → " + valid);
        if (!valid) {
            L("KHÔNG phải callback MoMo hợp lệ → bỏ qua");
            return;
        }

        String resultCode = data.getQueryParameter("resultCode");
        String orderInfo  = data.getQueryParameter("orderInfo");
        String transId    = data.getQueryParameter("transId");
        String message    = data.getQueryParameter("message");

        L("resultCode   → " + resultCode);
        L("transId      → " + transId);
        L("orderInfo    → " + orderInfo);
        L("message      → " + message);

        if ("0".equals(resultCode)) {
            String realOrderID = extractOrderIDFromOrderInfo(orderInfo);
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
            Toast.makeText(this, "Thanh toán thất bại: " + (message != null ? message : "Unknown"), Toast.LENGTH_LONG).show();
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
        L("Bắt đầu load giỏ hàng từ API");
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

        final long startTime = System.currentTimeMillis();
        final long MAX_WAIT = 5 * 60 * 1000L; // 5 phút

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > MAX_WAIT) {
                    L("Hết thời gian chờ thanh toán (5 phút)");
                    runOnUiThread(() -> Toast.makeText(ConfirmOrderActivity.this,
                            "Hết thời gian chờ thanh toán", Toast.LENGTH_SHORT).show());
                    return;
                }

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
                            // ← ĐÚNG: gọi lại chính Runnable này sau 5 giây
                            pollingHandler.postDelayed(pollingRunnable, 5000);
                        }
                    }

                    @Override
                    public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                        L("Polling lỗi mạng, thử lại sau 5s", t);
                        // ← ĐÚNG: vẫn dùng pollingRunnable, không dùng this
                        pollingHandler.postDelayed(pollingRunnable, 5000);
                    }
                });
            }
        };

        // Bắt đầu lần đầu sau 8 giây (cho MoMo xử lý IPN)
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
        // Giữ nguyên logic cũ nếu có
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
}*/
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
    private TextView tvAddress, tvCustomer, tvSubtotal, tvShipping,tvDiscount, tvFee, tvTotalPayment;
    private TextView btnMomo, btnCash, btnOtherPayment;
    private View layoutAddressBlock;
    private TextView tvEditAddress;
    private TextView tvaddvoucher;
    private TextView btnTip;
    private TextView tvTip,tvTipLable,tvDiscountLable;
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
    private String lastMomoRequestId = ""; // ← Lưu requestId từ MoMo (rất quan trọng)

    private final List<CartResponse.CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;
    private ActivityResultLauncher<Intent> selectVoucherLauncher;
    private Voucher appliedVoucher = null;
    private String selectedVoucherCode = null; // Lưu mã voucher để gửi lên Server
    private double currentDiscountAmount = 0;

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
        registerVoucherLauncher();
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
        // Lưu lại mã voucher để dùng khi Place Order
        this.selectedVoucherCode = voucher.getVoucherCode();

        long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        double discount = 0;

        if ("percentage".equals(voucher.getDiscountType())) {
            discount = subtotal * (voucher.getDiscountValue() / 100);
        } else {
            discount = voucher.getDiscountValue();
        }

        this.currentDiscountAmount = discount; // Lưu lại số tiền giảm

        // Cập nhật UI
        updateTotalsWithDiscount(subtotal, (long) discount);

        // Hiển thị tên voucher đã chọn lên giao diện
        TextView tvAddVoucherText = findViewById(R.id.tvaddvoucher); // Đảm bảo ID này đúng trong XML
        if (tvAddVoucherText != null) {
            tvAddVoucherText.setText(voucher.getVoucherCode());
            tvAddVoucherText.setTextColor(getResources().getColor(R.color.smoothie_strawberry));
        }
    }
    private void updateTotalsWithDiscount(long subtotal, long discount) {
        long shipping = "delivery".equals(currentDeliveryMethod) ? 50000L : 0L;
        long fee = 10000L;

        long total = subtotal + shipping + fee + currentTipAmount - discount;
        if (total < 0) total = 0;

        // Hiển thị
        tvSubtotal.setText(String.format("%,d VND", subtotal));
        tvShipping.setText(String.format("%,d VND", shipping));
        tvFee.setText(String.format("%,d VND", fee));
        if(currentTipAmount!=0){
            tvTip.setText(String.format("%,d VND",currentTipAmount));
            tvTip.setVisibility(View.VISIBLE);
            tvTipLable.setVisibility(View.VISIBLE);
        }if(currentTipAmount==0) {
            tvTip.setVisibility(View.GONE);
            tvTipLable.setVisibility(View.GONE);
        }

        if (discount > 0) {
            tvDiscount.setText(String.format("- %,d VND", discount));
            tvDiscount.setVisibility(View.VISIBLE);
            tvDiscountLable.setVisibility(View.VISIBLE);
        } else {
            tvDiscountLable.setVisibility(View.GONE);
            tvDiscount.setText("0 VND");
            tvDiscount.setVisibility(View.GONE);
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
        L("Bắt đầu load giỏ hàng từ API");
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
        tvaddvoucher.setOnClickListener(v->selectVoucherLauncher.launch(new Intent(this,VoucherSelectionActivity.class)));
        tvEditAddress.setOnClickListener(v -> selectAddressLauncher.launch(new Intent(this, SelectAddressActivity.class)));
        btnTipNone.setOnClickListener(v->{updateTipSelection(0);});
        btnTip.setOnClickListener(v->{showCustomTipDialog();});
        tvaddnote.setOnClickListener(v->{showNoteDialog();});
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }
    private void updateTipSelection(long amount){
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
        long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        updateTotalsWithDiscount(subtotal, (long) currentDiscountAmount);
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
        int gray = 0xFFADABAB;

        btnCash.setBackgroundResource(method.equals("cash") ? R.drawable.payment_option_selected : R.drawable.payment_option_default);
        btnCash.setTextColor(method.equals("cash") ? red : gray);
        btnMomo.setBackgroundResource(method.equals("momo") || method.equals("vnpay") ? R.drawable.payment_option_selected : R.drawable.payment_option_default);
        btnMomo.setTextColor(method.equals("momo") || method.equals("vnpay") ? red : gray);
        btnMomo.setText(method.equals("vnpay") ? "VNPay" : "Momo");
    }
    private void placeOrder() {
        // 1. Validate Giỏ hàng
        if (cartItemsLocal.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Validate Địa chỉ
        if ("delivery".equals(currentDeliveryMethod) && (currentAddress == null || currentAddress.isEmpty())) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Khóa nút
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang xử lý...");

        // 4. TÍNH TỔNG TIỀN CHUẨN TỪ SUBTOTAL (Backend đã tính sẵn)
        long totalInvoiceForBE = 0;
        for (CartResponse.CartItem item : cartItemsLocal) {
            // --- SỬA: Dùng getSubtotal() thay vì getPrice() * getQuantity() ---
            // Vì subtotal đã bao gồm tiền Size + Topping
            totalInvoiceForBE += item.getSubtotal();
        }

        // Cộng phí ship/dịch vụ (Phải khớp với logic hiển thị)
        long shippingFee = "delivery".equals(currentDeliveryMethod) ? 50000L : 0L;
        long serviceFee = 10000L;

        totalInvoiceForBE += shippingFee + serviceFee+currentTipAmount;

        // 5. Tạo Body Request
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", currentPaymentMethod);
        body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
        body.put("deliver", "delivery".equals(currentDeliveryMethod));
        body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
        body.put("note", currentNote);
        body.put("currentTipAmount",currentTipAmount);
        body.put("totalInvoice", totalInvoiceForBE);
        body.put("tipsforDriver", 0);

        // --- [QUAN TRỌNG] BỔ SUNG DÒNG NÀY ĐỂ GỬI VOUCHER LÊN SERVER ---
        if (selectedVoucherCode != null && !selectedVoucherCode.isEmpty()) {
            body.put("voucherCode", selectedVoucherCode);
            L("Gửi kèm Voucher Code: " + selectedVoucherCode);
        }
        // ---------------------------------------------------------------

        L("STEP 1: Tạo đơn hàng - Total: " + totalInvoiceForBE);

        // 6. Gọi API (Giữ nguyên logic cũ)
        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    String orderID = r.body().getData().getOrderID();
                    lastOrderID = orderID;
                    L("STEP 1 DONE: Có OrderID = " + orderID);
                    processVoucherLogic(orderID); // Chuyển sang bước Voucher
                } else {
                    L("Lỗi tạo đơn: " + r.message());
                    try {
                        // Log chi tiết lỗi từ server (nếu có)
                        L("Error Body: " + r.errorBody().string());
                    } catch (Exception e) {}

                    Toast.makeText(ConfirmOrderActivity.this, "Tạo đơn thất bại", Toast.LENGTH_LONG).show();
                    resetPlaceOrderButton();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                L("Lỗi mạng tạo đơn", t);
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                resetPlaceOrderButton();
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

                // Dù voucher thành công hay thất bại, vẫn tiếp tục thanh toán
                proceedToPayment(orderID);
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                L("STEP 2 ERROR: Lỗi mạng voucher", t);
                // Lỗi mạng khi add voucher -> Vẫn cho thanh toán (giá gốc)
                proceedToPayment(orderID);
            }
        });
    }
    private void proceedToPayment(String orderID) {
        L("STEP 3: Bắt đầu thanh toán cho OrderID: " + orderID);

        // Mở lại nút trước khi chuyển đi (để lỡ user quay lại)
        btnPlaceOrder.setEnabled(true);
        btnPlaceOrder.setText("Đặt hàng");

        if (PAYMENT_METHOD_CASH.equals(currentPaymentMethod)) {
            // Tiền mặt -> Xong luôn
            Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();

            setResult(RESULT_OK);
            finish();
        }
        else if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) {
            requestMomoPayment(orderID, userID);
        }
        else {
            // VNPay
            requestVnpayPayment(orderID, userID);
        }
    }
    private void resetPlaceOrderButton() {
        btnPlaceOrder.setEnabled(true);
        // Hiển thị lại giá tiền đang có trên màn hình
        long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        updateTotalsWithDiscount(subtotal, (long) currentDiscountAmount);
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
        // Reset voucher
        selectedVoucherCode = null;
        currentDiscountAmount = 0;
        appliedVoucher = null;

        if (tvaddvoucher != null) {
            tvaddvoucher.setText("Thêm");
            tvaddvoucher.setTextColor(getResources().getColor(R.color.black));
        }

        // Ẩn dòng giảm giá
        if (tvDiscount != null) tvDiscount.setVisibility(View.GONE);

        // TÍNH LẠI TỔNG TIỀN (SỬA LẠI ĐOẠN NÀY)
        long newSubtotal = 0;
        for (CartResponse.CartItem item : cartItemsLocal) {
            // Dùng subtotal để hiển thị đúng giá đã bao gồm topping/size
            newSubtotal += item.getSubtotal();
        }

        updateTotalsWithDiscount(newSubtotal, 0);
        fetchCartProducts(); // Load lại dữ liệu mới nhất từ server
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

                    // Nếu có voucher tốt nhất
                    if (res.getBestVoucher() != null) {
                        double discount = res.getDiscountAmount();
                        String code = res.getBestVoucher().getVoucherCode();

                        Toast.makeText(ConfirmOrderActivity.this,
                                "Đã áp dụng voucher: " + code + " (Giảm " + discount + ")",
                                Toast.LENGTH_SHORT).show();

                        // Cập nhật lại UI tiền (Tổng tiền - discount)
                        /*updateTotalUI(discount);*/
                    } else {
                        // Không có voucher phù hợp
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
    protected void onDestroy() {
        L("onDestroy() → dọn dẹp polling");
        stopPolling();
        super.onDestroy();
    }
}