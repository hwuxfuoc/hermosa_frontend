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

    private void registerAddressLauncher() {
        selectAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String method = result.getData().getStringExtra("newMethod"); // Thay "deliveryMethod" bằng "newMethod" như trong SelectAddressActivity
                        if ("pickup".equals(method)) {
                            currentDeliveryMethod = "pickup";
                            currentAddress = "";
                            apiCustomer = "";
                            updateDeliveryUI();
                        } else if ("delivery".equals(method)) {
                            currentDeliveryMethod = "delivery";
                            currentAddress = result.getData().getStringExtra("newAddress");
                            apiCustomer = result.getData().getStringExtra("newCustomerInfo");
                            if (currentAddress == null) currentAddress = "";
                            if (apiCustomer == null) apiCustomer = "";
                            updateDeliveryUI();
                        }
                    } else {
                        Log.w("ADDRESS_LAUNCHER", "Kết quả không hợp lệ hoặc bị hủy");
                        // Có thể giữ nguyên giá trị hiện tại nếu người dùng hủy
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
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
            tvCustomer.setText("");
        } else {
            tvAddress.setText(currentAddress.isEmpty() ? "Chưa chọn địa chỉ" : currentAddress);
            tvCustomer.setText(apiCustomer.isEmpty() ? "Chưa có thông tin khách hàng" : apiCustomer);
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
        tvEditAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAddressActivity.class);
            selectAddressLauncher.launch(intent);
            tvEditAddress.setTextColor(getResources().getColor(android.R.color.holo_blue_light)); // Thay đổi màu tạm thời khi nhấn
        });
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
/* private void placeOrder() {
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
       Log.d("PLACE_ORDER", "Bắt đầu tạo đơn hàng, items in local: " + cartItemsLocal.size());
       if (cartItemsLocal.isEmpty()) {
           Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
           return;
       }
       long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
       long shipping = "delivery".equals(currentDeliveryMethod) ? 50000 : 0;
       long fee = 10000;
       long totalInvoice = subtotal + shipping + fee;

       if ("delivery".equals(currentDeliveryMethod) && (currentAddress.isEmpty() || apiCustomer.isEmpty())) {
           Toast.makeText(this, "Vui lòng chọn địa chỉ và thông tin khách hàng!", Toast.LENGTH_LONG).show();
           return;
       }

       Map<String, Object> body = new HashMap<>();
       body.put("userID", userID);
       body.put("paymentMethod", currentPaymentMethod);
       body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
       body.put("deliver", "delivery".equals(currentDeliveryMethod));
       body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
       body.put("customerInfo", "delivery".equals(currentDeliveryMethod) ? apiCustomer : ""); // Thêm thông tin khách
       body.put("note", "");
       body.put("totalInvoice", totalInvoice);

       Log.d("PLACE_ORDER", "Gửi body: " + body.toString());
       apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
           @Override
           public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
               Log.d("PLACE_ORDER", "Response code: " + r.code() + ", Body: " + (r.body() != null ? r.body().toString() : "null"));
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

}*/
/*
package com.example.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import com.example.demo.utils.SpaceItemDecoration;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmOrderActivity extends AppCompatActivity
        implements FragmentPaymentMethodBottomSheet.PaymentMethodListener,
        CartAdapter.OnCartUpdateListener,
        RecommendedAdapter.OnAddToCartListener {

    public static final String PAYMENT_METHOD_CASH = "cash";
    public static final String PAYMENT_METHOD_MOMO = "momo";
    public static final String PAYMENT_METHOD_VNPAY = "vnpay";
    private static final String TAG = "ConfirmOrderActivity";
    private static final long CLICK_DEBOUNCE_MS = 500; // Ngăn click nhanh
    private long lastEditAddressClickTime = 0; // Thời gian click cuối cùng của tvEditAddress

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
    private Gson gson = new Gson(); // Thêm Gson để chuyển Map sang JSON

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        apiService = ApiClient.getClient().create(ApiService.class);
        userID = SessionManager.getUserID(this);
        if (userID == null) {
            Log.e(TAG, "UserID is null, closing activity");
            finish();
            return;
        }
        Log.d(TAG, "onCreate: UserID = " + userID);

        registerAddressLauncher();
        initViews();
        loadData();
        setupClickListeners();
        handleDeepLink(getIntent());
    }

    private void registerAddressLauncher() {
        Log.d(TAG, "Registering AddressLauncher");
        selectAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "AddressLauncher result: code=" + result.getResultCode() + ", data=" + result.getData());
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String method = result.getData().getStringExtra("newMethod");
                        Log.d(TAG, "Received method: " + method);
                        if ("pickup".equals(method)) {
                            currentDeliveryMethod = "pickup";
                            currentAddress = "";
                            apiCustomer = "";
                            updateDeliveryUI();
                            Log.d(TAG, "Switched to pickup mode");
                        } else if ("delivery".equals(method)) {
                            currentDeliveryMethod = "delivery";
                            currentAddress = result.getData().getStringExtra("newAddress");
                            apiCustomer = result.getData().getStringExtra("newCustomerInfo");
                            if (currentAddress == null) currentAddress = "";
                            if (apiCustomer == null) apiCustomer = "";
                            updateDeliveryUI();
                            Log.d(TAG, "Switched to delivery mode, address=" + currentAddress + ", customer=" + apiCustomer);
                        } else {
                            Log.w(TAG, "Invalid delivery method: " + method);
                            Toast.makeText(this, "Invalid delivery method", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Address selection cancelled or invalid, code=" + result.getResultCode());
                    }
                });
    }

    private void initViews() {
        Log.d(TAG, "Initializing views");
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

        if (tvEditAddress == null) {
            Log.e(TAG, "tvEditAddress not found in layout!");
        } else {
            Log.d(TAG, "tvEditAddress mapped successfully");
        }

        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommended.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRecommended.setHasFixedSize(false);

        int spacingInPx = dpToPx(6);
        recyclerRecommended.addItemDecoration(new SpaceItemDecoration(spacingInPx));
        Log.d(TAG, "Views initialized successfully");
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void loadData() {
        Log.d(TAG, "Loading data");
        fetchCartProducts();
        loadRecommended();
        fetchUserInfo();
    }

    private void updateDeliveryUI() {
        Log.d(TAG, "Cập nhật giao diện giao hàng, method=" + currentDeliveryMethod);
        if (cartItemsLocal.isEmpty()) {
            layoutAddressBlock.setVisibility(View.GONE);
            Log.d(TAG, "Ẩn layoutAddressBlock vì giỏ hàng trống");
        } else {
            layoutAddressBlock.setVisibility(View.VISIBLE);
            if ("pickup".equals(currentDeliveryMethod)) {
                tvAddress.setText("Đơn đặt và nhận tại cửa hàng");
                tvCustomer.setText("");
                Log.d(TAG, "Đặt sang giao diện pickup");
            } else {
                tvAddress.setText(currentAddress.isEmpty() ? "Chưa chọn địa chỉ" : currentAddress);
                tvCustomer.setText(apiCustomer.isEmpty() ? "Chưa có thông tin khách hàng" : apiCustomer);
                Log.d(TAG, "Đặt sang giao diện delivery, address=" + tvAddress.getText() + ", customer=" + tvCustomer.getText());
            }
        }
    }

    private void fetchUserInfo() {
        Log.d(TAG, "Fetching user info");
        String name = SessionManager.getUserName(this);
        String phone = SessionManager.getUserPhone(this);
        apiCustomer = (name != null && phone != null) ? name + " | " + phone : "Khách hàng";
        Log.d(TAG, "User info fetched: " + apiCustomer);
        updateDeliveryUI();
    }

    private void fetchCartProducts() {
        Log.d(TAG, "Fetching cart products for userID: " + userID);
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                Log.d(TAG, "Cart response code: " + r.code());
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    cartItemsLocal.clear();
                    cartItemsLocal.addAll(r.body().getData().getItems());
                    Log.d(TAG, "Cart items loaded: " + cartItemsLocal.size());
                    if (cartItemsLocal.isEmpty()) {
                        Log.w(TAG, "Empty cart");
                        Toast.makeText(ConfirmOrderActivity.this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
                    cartAdapter.setConfirmMode(true);
                    recyclerOrderItems.setAdapter(cartAdapter);
                    updateTotals(r.body().getData().getTotalMoney());
                } else {
                    Log.e(TAG, "Cart response failed or data null, message: " + (r.body() != null ? r.body().getStatus() : r.message()));
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e(TAG, "Cart fetch error: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecommended() {
        Log.d(TAG, "Loading recommended products");
        apiService.getAllProducts().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<Product> list = new ArrayList<>();
                    for (MenuResponse.MenuItem item : r.body().getData()) {
                        Product product = Product.fromMenuItem(item);
                        list.add(product);
                    }
                    recommendedAdapter = new RecommendedAdapter(
                            ConfirmOrderActivity.this,
                            list,
                            ConfirmOrderActivity.this
                    );
                    recyclerRecommended.setAdapter(recommendedAdapter);
                    Log.d(TAG, "Recommended products loaded: " + list.size());
                } else {
                    Log.e(TAG, "Recommended products failed, message: " + (r.body() != null ? r.body().getStatus() : r.message()));
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                Log.e(TAG, "Recommended products error: " + t.getMessage());
            }
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
        Log.d(TAG, "Totals updated: subtotal=" + subtotal + ", total=" + total);
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked, closing activity");
            finish();
        });
        btnCash.setOnClickListener(v -> onPaymentMethodSelected(PAYMENT_METHOD_CASH));
        btnMomo.setOnClickListener(v -> onPaymentMethodSelected(PAYMENT_METHOD_MOMO));
        btnOtherPayment.setOnClickListener(v -> {
            Log.d(TAG, "Other payment button clicked, showing payment sheet");
            FragmentPaymentMethodBottomSheet f = new FragmentPaymentMethodBottomSheet();
            f.setPaymentMethodListener(ConfirmOrderActivity.this);
            f.show(getSupportFragmentManager(), "payment_sheet");
        });
        tvEditAddress.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastEditAddressClickTime < CLICK_DEBOUNCE_MS) {
                Log.d(TAG, "Ignoring rapid click on tvEditAddress");
                return;
            }
            lastEditAddressClickTime = currentTime;
            Log.d(TAG, "Edit address button clicked");
            Intent intent = new Intent(this, SelectAddressActivity.class);
            Log.d(TAG, "Launching Intent: " + intent);
            PackageManager pm = getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            if (!activities.isEmpty()) {
                try {
                    selectAddressLauncher.launch(intent);
                    Log.d(TAG, "Intent launched successfully");
                    tvEditAddress.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                } catch (Exception e) {
                    Log.e(TAG, "Error launching SelectAddressActivity: " + e.getMessage());
                    Toast.makeText(this, "Cannot open address selection screen", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "SelectAddressActivity not found in system");
                Toast.makeText(this, "Cannot open address selection screen", Toast.LENGTH_SHORT).show();
            }
        });
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    @Override
    public void onPaymentMethodSelected(String method) {
        Log.d(TAG, "Payment method selected: " + method);
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
        Log.d(TAG, "Starting order placement, items in local: " + cartItemsLocal.size());
        if (cartItemsLocal.isEmpty()) {
            Log.w(TAG, "Empty cart, showing toast");
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
            return;
        }
        long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        long shipping = "delivery".equals(currentDeliveryMethod) ? 50000 : 0;
        long fee = 10000;
        long totalInvoice = subtotal + shipping + fee;

        if ("delivery".equals(currentDeliveryMethod) && (currentAddress.isEmpty() || apiCustomer.isEmpty())) {
            Log.w(TAG, "Delivery selected but address or customer info empty");
            Toast.makeText(this, "Vui lòng chọn địa chỉ và thông tin khách hàng!", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", currentPaymentMethod);
        body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
        body.put("deliver", "delivery".equals(currentDeliveryMethod));
        body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
        body.put("customerInfo", "delivery".equals(currentDeliveryMethod) ? apiCustomer : "");
        body.put("note", "");
        body.put("totalInvoice", totalInvoice);

        Log.d(TAG, "Sending order body: " + body.toString());
        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
                Log.d(TAG, "Order response code: " + r.code() + ", Body: " + (r.body() != null ? r.body().toString() : "null"));
                if (r.isSuccessful() && r.body() != null) {
                    lastOrderID = r.body().getData().getOrderID();
                    Log.d(TAG, "Order created successfully: " + lastOrderID);
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
                    Log.e(TAG, "Order error: " + msg);
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Log.e(TAG, "Order network error: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi đặt hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestMomoPayment(String orderID, String userID) {
        Log.d(TAG, "Yêu cầu thanh toán MoMo, orderID=" + orderID + ", userID=" + userID);
        CreateMomoRequest request = new CreateMomoRequest(orderID, userID);
        apiService.createPaymentMomo(request).enqueue(new Callback<CreateMomoResponse>() {
            @Override
            public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> r) {
                Log.d(TAG, "Mã phản hồi MoMo: " + r.code() + ", URL: " + call.request().url());
                if (r.isSuccessful() && r.body() != null && r.body().getPayUrl() != null) {
                    String payUrl = r.body().getPayUrl();
                    Log.d(TAG, "MoMo payUrl: " + payUrl);
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                    i.putExtra("orderID", orderID); // Lưu orderID để xử lý sau
                    startActivity(i);
                    Toast.makeText(ConfirmOrderActivity.this, "Đang mở MoMo...", Toast.LENGTH_LONG).show();
                } else {
                    String msg = r.body() != null ? r.body().getMessage() : r.message();
                    Log.e(TAG, "Lỗi MoMo: " + msg);
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateMomoResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng MoMo: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi mạng MoMo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestVnpayPayment(String orderID, String userID) {
        Log.d(TAG, "Yêu cầu thanh toán VNPay, orderID=" + orderID);
        CreateVnpayRequest request = new CreateVnpayRequest(orderID, userID);
        apiService.createPaymentVnpayString(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, "Mã phản hồi VNPay: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    String payUrl = response.body().trim().replace("\"", "");
                    Log.d(TAG, "VNPay payUrl: " + payUrl);
                    if (payUrl.startsWith("http")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                        intent.putExtra("orderID", orderID); // Lưu orderID để xử lý sau
                        startActivity(intent);
                        Toast.makeText(ConfirmOrderActivity.this, "Đang mở VNPay...", Toast.LENGTH_LONG).show();
                    } else {
                        Log.w(TAG, "URL VNPay không hợp lệ: " + payUrl);
                        Toast.makeText(ConfirmOrderActivity.this, "Link không hợp lệ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Lỗi VNPay: " + response.message());
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi server VNPay: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng VNPay: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối VNPay", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmPaymentStatus(String orderID, String status) {
        Log.d(TAG, "Gửi trạng thái thanh toán cho orderID=" + orderID + ", status=" + status);
        // Chuyển Map sang JSON để gửi dưới dạng String
        Map<String, String> body = new HashMap<>();
        body.put("orderID", orderID);
        body.put("status", status);
        String requestBody = gson.toJson(body);

        apiService.confirmPaymentStatus(requestBody).enqueue(new Callback<ConfirmPaymentResponse>() {
            @Override
            public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    ConfirmPaymentResponse response = r.body();
                    Log.d(TAG, "Trạng thái thanh toán được xác nhận: status=" + response.getStatus() +
                            ", method=" + response.getMethod() +
                            ", time=" + response.getTime());
                    switch (response.getStatus()) {
                        case "done":
                            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case "failed":
                            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thất bại!", Toast.LENGTH_SHORT).show();
                            break;
                        case "pending":
                            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán đang xử lý!", Toast.LENGTH_SHORT).show();
                            break;
                        case "not_done":
                            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán chưa hoàn tất!", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Log.w(TAG, "Trạng thái không rõ: " + response.getStatus());
                            Toast.makeText(ConfirmOrderActivity.this, "Trạng thái thanh toán không rõ!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    Log.e(TAG, "Lỗi xác nhận trạng thái thanh toán: " + r.message()); // Sử dụng r.message() thay vì getMessage()
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi xác nhận thanh toán: " + r.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi xác nhận trạng thái: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối khi xác nhận thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCartUpdated() {
        Log.d(TAG, "Cart updated");
        long newSubtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        updateTotals(newSubtotal);
        fetchCartProducts();
    }

    @Override
    public void onAddToCart(Product product) {
        Log.d(TAG, "Adding product to cart: " + product.getProductID());
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
                if (r.isSuccessful()) {
                    Log.d(TAG, "Product added to cart successfully");
                    fetchCartProducts();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e(TAG, "Error adding product to cart: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi thêm món", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume được gọi");
        updateDeliveryUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called with intent: " + intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    */
/*private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) {
            Log.d(TAG, "Không có dữ liệu deep link");
            return;
        }
        Uri data = intent.getData();
        String host = data.getHost();
        String orderID = intent.getStringExtra("orderID");
        Log.d(TAG, "Xử lý deep link, host: " + host + ", orderID: " + orderID);

        if (orderID == null) {
            Log.w(TAG, "Không có orderID trong deep link, không thể xác nhận trạng thái");
            Toast.makeText(this, "Lỗi: Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("payment-success".equals(host) || "momo-payment-success".equals(host)) {
            Log.d(TAG, "Deep link thanh toán thành công cho orderID: " + orderID);
            confirmPaymentStatus(orderID, "done");
        } else if ("payment-failed".equals(host) || "momo-payment-failed".equals(host)) {
            Log.d(TAG, "Deep link thanh toán thất bại cho orderID: " + orderID);
            confirmPaymentStatus(orderID, "failed");
        } else {
            Log.w(TAG, "Deep link không hợp lệ: " + host);
            Toast.makeText(this, "Liên kết không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }*//*

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) {
            Log.d(TAG, "Không có dữ liệu deep link");
            return;
        }
        Uri data = intent.getData();
        String host = data.getHost();
        String orderID = intent.getStringExtra("orderID");
        Log.d(TAG, "Xử lý deep link, host: " + host + ", orderID: " + orderID);

        if (orderID == null) {
            Log.w(TAG, "Không có orderID trong deep link, không thể xác nhận trạng thái");
            Toast.makeText(this, "Lỗi: Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("payment-success".equals(host) || "momo-payment-success".equals(host)) {
            Log.d(TAG, "Deep link thanh toán thành công cho orderID: " + orderID);
            confirmPaymentStatus(orderID, "done");
            // Redirect sau khi xác nhận thành công
            Intent successIntent = new Intent(this, SuccessActivity.class); // Thay SuccessActivity bằng activity bạn muốn
            successIntent.putExtra("orderID", orderID);
            startActivity(successIntent);
            finish(); // Đóng activity hiện tại nếu cần
        } else if ("payment-failed".equals(host) || "momo-payment-failed".equals(host)) {
            Log.d(TAG, "Deep link thanh toán thất bại cho orderID: " + orderID);
            confirmPaymentStatus(orderID, "failed");
            // Có thể redirect sang màn hình lỗi nếu cần
            Toast.makeText(this, "Thanh toán thất bại, vui lòng thử lại!", Toast.LENGTH_LONG).show();
        } else {
            Log.w(TAG, "Deep link không hợp lệ: " + host);
            Toast.makeText(this, "Liên kết không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}*/
package com.example.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import com.example.demo.utils.SpaceItemDecoration;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmOrderActivity extends AppCompatActivity
        implements FragmentPaymentMethodBottomSheet.PaymentMethodListener,
        CartAdapter.OnCartUpdateListener,
        RecommendedAdapter.OnAddToCartListener {

    public static final String PAYMENT_METHOD_CASH = "cash";
    public static final String PAYMENT_METHOD_MOMO = "momo";
    public static final String PAYMENT_METHOD_VNPAY = "vnpay";
    private static final String TAG = "ConfirmOrderActivity";
    private static final long CLICK_DEBOUNCE_MS = 500; // Ngăn click nhanh
    private long lastEditAddressClickTime = 0; // Thời gian click cuối cùng của tvEditAddress

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
    private Gson gson = new Gson(); // Thêm Gson để chuyển Map sang JSON

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        apiService = ApiClient.getClient().create(ApiService.class);
        userID = SessionManager.getUserID(this);
        if (userID == null) {
            Log.e(TAG, "UserID is null, closing activity");
            finish();
            return;
        }
        Log.d(TAG, "onCreate: UserID = " + userID);

        registerAddressLauncher();
        initViews();
        loadData();
        setupClickListeners();
        handleDeepLink(getIntent());
    }

    private void registerAddressLauncher() {
        Log.d(TAG, "Registering AddressLauncher");
        selectAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "AddressLauncher result: code=" + result.getResultCode() + ", data=" + result.getData());
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String method = result.getData().getStringExtra("newMethod");
                        Log.d(TAG, "Received method: " + method);
                        if ("pickup".equals(method)) {
                            currentDeliveryMethod = "pickup";
                            currentAddress = "";
                            apiCustomer = "";
                            updateDeliveryUI();
                            Log.d(TAG, "Switched to pickup mode");
                        } else if ("delivery".equals(method)) {
                            currentDeliveryMethod = "delivery";
                            currentAddress = result.getData().getStringExtra("newAddress");
                            apiCustomer = result.getData().getStringExtra("newCustomerInfo");
                            if (currentAddress == null) currentAddress = "";
                            if (apiCustomer == null) apiCustomer = "";
                            updateDeliveryUI();
                            Log.d(TAG, "Switched to delivery mode, address=" + currentAddress + ", customer=" + apiCustomer);
                        } else {
                            Log.w(TAG, "Invalid delivery method: " + method);
                            Toast.makeText(this, "Invalid delivery method", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Address selection cancelled or invalid, code=" + result.getResultCode());
                    }
                });
    }

    private void initViews() {
        Log.d(TAG, "Initializing views");
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

        if (tvEditAddress == null) {
            Log.e(TAG, "tvEditAddress not found in layout!");
        } else {
            Log.d(TAG, "tvEditAddress mapped successfully");
        }

        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommended.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRecommended.setHasFixedSize(false);

        int spacingInPx = dpToPx(6);
        recyclerRecommended.addItemDecoration(new SpaceItemDecoration(spacingInPx));
        Log.d(TAG, "Views initialized successfully");
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void loadData() {
        Log.d(TAG, "Loading data");
        fetchCartProducts();
        loadRecommended();
        fetchUserInfo();
    }

    private void updateDeliveryUI() {
        Log.d(TAG, "Updating delivery UI, method=" + currentDeliveryMethod);
        if (cartItemsLocal.isEmpty()) {
            layoutAddressBlock.setVisibility(View.GONE);
            Log.d(TAG, "Hiding layoutAddressBlock because cart is empty");
        } else {
            layoutAddressBlock.setVisibility(View.VISIBLE);
            if ("pickup".equals(currentDeliveryMethod)) {
                tvAddress.setText("Đơn đặt và nhận tại cửa hàng");
                tvCustomer.setText("");
                Log.d(TAG, "Switched to pickup UI");
            } else {
                tvAddress.setText(currentAddress.isEmpty() ? "Chưa chọn địa chỉ" : currentAddress);
                tvCustomer.setText(apiCustomer.isEmpty() ? "Chưa có thông tin khách hàng" : apiCustomer);
                Log.d(TAG, "Switched to delivery UI, address=" + tvAddress.getText() + ", customer=" + tvCustomer.getText());
            }
        }
    }

    private void fetchUserInfo() {
        Log.d(TAG, "Fetching user info");
        String name = SessionManager.getUserName(this);
        String phone = SessionManager.getUserPhone(this);
        apiCustomer = (name != null && phone != null) ? name + " | " + phone : "Khách hàng";
        Log.d(TAG, "User info fetched: " + apiCustomer);
        updateDeliveryUI();
    }

    private void fetchCartProducts() {
        Log.d(TAG, "Fetching cart products for userID: " + userID);
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                Log.d(TAG, "Cart API response - Code: " + r.code());
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    cartItemsLocal.clear();
                    cartItemsLocal.addAll(r.body().getData().getItems());
                    Log.d(TAG, "Cart items loaded: " + cartItemsLocal.size());
                    if (cartItemsLocal.isEmpty()) {
                        Log.w(TAG, "Cart is empty");
                        Toast.makeText(ConfirmOrderActivity.this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
                    cartAdapter.setConfirmMode(true);
                    recyclerOrderItems.setAdapter(cartAdapter);
                    updateTotals(r.body().getData().getTotalMoney());
                } else {
                    Log.e(TAG, "Cart API failed or data null, message: " + (r.body() != null ? r.body().getStatus() : r.message()));
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e(TAG, "Cart API failure - Error: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecommended() {
        Log.d(TAG, "Loading recommended products");
        apiService.getAllProducts().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<Product> list = new ArrayList<>();
                    for (MenuResponse.MenuItem item : r.body().getData()) {
                        Product product = Product.fromMenuItem(item);
                        list.add(product);
                    }
                    recommendedAdapter = new RecommendedAdapter(
                            ConfirmOrderActivity.this,
                            list,
                            ConfirmOrderActivity.this
                    );
                    recyclerRecommended.setAdapter(recommendedAdapter);
                    Log.d(TAG, "Recommended products loaded: " + list.size());
                } else {
                    Log.e(TAG, "Recommended API failed, message: " + (r.body() != null ? r.body().getStatus() : r.message()));
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                Log.e(TAG, "Recommended API failure - Error: " + t.getMessage());
            }
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
        Log.d(TAG, "Totals updated: subtotal=" + subtotal + ", total=" + total);
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked, closing activity");
            finish();
        });
        btnCash.setOnClickListener(v -> onPaymentMethodSelected(PAYMENT_METHOD_CASH));
        btnMomo.setOnClickListener(v -> onPaymentMethodSelected(PAYMENT_METHOD_MOMO));
        btnOtherPayment.setOnClickListener(v -> {
            Log.d(TAG, "Other payment button clicked, showing payment sheet");
            FragmentPaymentMethodBottomSheet f = new FragmentPaymentMethodBottomSheet();
            f.setPaymentMethodListener(ConfirmOrderActivity.this);
            f.show(getSupportFragmentManager(), "payment_sheet");
        });
        tvEditAddress.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastEditAddressClickTime < CLICK_DEBOUNCE_MS) {
                Log.d(TAG, "Ignoring rapid click on tvEditAddress");
                return;
            }
            lastEditAddressClickTime = currentTime;
            Log.d(TAG, "Edit address button clicked");
            Intent intent = new Intent(this, SelectAddressActivity.class);
            Log.d(TAG, "Launching Intent: " + intent);
            PackageManager pm = getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            if (!activities.isEmpty()) {
                try {
                    selectAddressLauncher.launch(intent);
                    Log.d(TAG, "Intent launched successfully");
                    tvEditAddress.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                } catch (Exception e) {
                    Log.e(TAG, "Error launching SelectAddressActivity: " + e.getMessage());
                    Toast.makeText(this, "Cannot open address selection screen", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "SelectAddressActivity not found in system");
                Toast.makeText(this, "Cannot open address selection screen", Toast.LENGTH_SHORT).show();
            }
        });
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    @Override
    public void onPaymentMethodSelected(String method) {
        Log.d(TAG, "Payment method selected: " + method);
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
        Log.d(TAG, "Starting order placement, items in local: " + cartItemsLocal.size());
        if (cartItemsLocal.isEmpty()) {
            Log.w(TAG, "Empty cart, showing toast");
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_LONG).show();
            return;
        }
        long subtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        long shipping = "delivery".equals(currentDeliveryMethod) ? 50000 : 0;
        long fee = 10000;
        long totalInvoice = subtotal + shipping + fee;

        if ("delivery".equals(currentDeliveryMethod) && (currentAddress.isEmpty() || apiCustomer.isEmpty())) {
            Log.w(TAG, "Delivery selected but address or customer info empty");
            Toast.makeText(this, "Vui lòng chọn địa chỉ và thông tin khách hàng!", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", currentPaymentMethod);
        body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
        body.put("deliver", "delivery".equals(currentDeliveryMethod));
        body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
        body.put("customerInfo", "delivery".equals(currentDeliveryMethod) ? apiCustomer : "");
        body.put("note", "");
        body.put("totalInvoice", totalInvoice);

        Log.d(TAG, "Sending order body: " + body.toString());
        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
                Log.d(TAG, "Order response code: " + r.code() + ", Body: " + (r.body() != null ? r.body().toString() : "null"));
                if (r.isSuccessful() && r.body() != null) {
                    lastOrderID = r.body().getData().getOrderID();
                    Log.d(TAG, "Order created successfully: " + lastOrderID);
                    if (PAYMENT_METHOD_CASH.equals(currentPaymentMethod)) {
                        Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) {
                            Log.d(TAG, "Initiating MoMo payment for orderID: " + lastOrderID);
                            requestMomoPayment(lastOrderID, userID);
                        } else {
                            Log.d(TAG, "Initiating VNPay payment for orderID: " + lastOrderID);
                            requestVnpayPayment(lastOrderID, userID);
                        }
                    }
                } else {
                    String msg = r.body() != null ? r.body().getMessage() : r.message();
                    Log.e(TAG, "Order error: " + msg);
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Log.e(TAG, "Order network error: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi đặt hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestMomoPayment(String orderID, String userID) {
        Log.d(TAG, "Requesting MoMo payment - orderID: " + orderID + ", userID: " + userID);
        CreateMomoRequest request = new CreateMomoRequest(orderID, userID);
        apiService.createPaymentMomo(request).enqueue(new Callback<CreateMomoResponse>() {
            @Override
            public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> r) {
                Log.d(TAG, "MoMo response code: " + r.code() + ", Request URL: " + call.request().url());
                if (r.isSuccessful() && r.body() != null && r.body().getPayUrl() != null) {
                    String payUrl = r.body().getPayUrl();
                    Log.d(TAG, "Received MoMo payUrl: " + payUrl);
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                    i.putExtra("orderID", orderID); // Lưu orderID để xử lý sau
                    startActivity(i);
                    Log.d(TAG, "Opening MoMo payment page with intent: " + i.toString());
                    Toast.makeText(ConfirmOrderActivity.this, "Đang mở MoMo...", Toast.LENGTH_LONG).show();
                } else {
                    String msg = r.body() != null ? r.body().getMessage() : r.message();
                    Log.e(TAG, "MoMo payment request failed: " + msg);
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateMomoResponse> call, Throwable t) {
                Log.e(TAG, "MoMo network error: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi mạng MoMo", Toast.LENGTH_LONG).show();
            }
        });
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

    private void confirmPaymentStatus(String orderID, String status) {
        Log.d(TAG, "Confirming payment status for orderID: " + orderID + ", status: " + status);
        Map<String, String> body = new HashMap<>();
        body.put("orderID", orderID);
        body.put("status", status);
        String requestBody = gson.toJson(body);

        apiService.confirmPaymentStatus(requestBody).enqueue(new Callback<ConfirmPaymentResponse>() {
            @Override
            public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    ConfirmPaymentResponse response = r.body();
                    Log.d(TAG, "Payment status confirmed - status: " + response.getStatus() + ", method: " + response.getMethod() + ", time: " + response.getTime());
                    switch (response.getStatus()) {
                        case "done":
                            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case "failed":
                            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thất bại!", Toast.LENGTH_SHORT).show();
                            break;
                        case "pending":
                            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán đang xử lý!", Toast.LENGTH_SHORT).show();
                            break;
                        case "not_done":
                            Toast.makeText(ConfirmOrderActivity.this, "Thanh toán chưa hoàn tất!", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Log.w(TAG, "Unknown payment status: " + response.getStatus());
                            Toast.makeText(ConfirmOrderActivity.this, "Trạng thái thanh toán không rõ!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    Log.e(TAG, "Payment status confirmation failed: " + r.message());
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi xác nhận thanh toán: " + r.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {
                Log.e(TAG, "Network error during payment status confirmation: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối khi xác nhận thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCartUpdated() {
        Log.d(TAG, "Cart updated");
        long newSubtotal = cartItemsLocal.stream().mapToLong(CartResponse.CartItem::getSubtotal).sum();
        updateTotals(newSubtotal);
        fetchCartProducts();
    }

    @Override
    public void onAddToCart(Product product) {
        Log.d(TAG, "Adding product to cart: " + product.getProductID());
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
                if (r.isSuccessful()) {
                    Log.d(TAG, "Product added to cart successfully");
                    fetchCartProducts();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e(TAG, "Error adding product to cart: " + t.getMessage());
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi thêm món", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called, checking for UI updates");
        updateDeliveryUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent triggered with intent: " + intent + ", data: " + (intent.getData() != null ? intent.getData().toString() : "null"));
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) {
            Log.d(TAG, "No deep link data received, intent is null or data is null");
            return;
        }
        Uri data = intent.getData();
        String host = data.getHost();
        String orderID = intent.getStringExtra("orderID");
        Log.d(TAG, "Handling deep link - Host: " + host + ", orderID: " + orderID + ", Full URI: " + data.toString());
        if (orderID == null) {
            Log.w(TAG, "No orderID found in deep link, cannot confirm status");
            Toast.makeText(this, "Lỗi: Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("payment-success".equals(host) || "momo-payment-success".equals(host)) {
            Log.d(TAG, "Deep link received - Payment success for orderID: " + orderID);
            confirmPaymentStatus(orderID, "done");
            // Redirect sau khi xác nhận thành công
            Log.d(TAG, "Redirecting to SuccessActivity for orderID: " + orderID);
            Intent successIntent = new Intent(this, SuccessActivity.class);
            successIntent.putExtra("orderID", orderID);
            startActivity(successIntent);
            finish(); // Đóng activity hiện tại
        } else if ("payment-failed".equals(host) || "momo-payment-failed".equals(host)) {
            Log.d(TAG, "Deep link received - Payment failed for orderID: " + orderID);
            confirmPaymentStatus(orderID, "failed");
            Toast.makeText(this, "Thanh toán thất bại, vui lòng thử lại!", Toast.LENGTH_LONG).show();
        } else {
            Log.w(TAG, "Invalid deep link host: " + host);
            Toast.makeText(this, "Liên kết không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
