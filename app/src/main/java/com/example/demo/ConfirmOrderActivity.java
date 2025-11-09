/*
package com.example.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.demo.fragment.PaymentMethodBottomSheetFragment;
import com.example.demo.models.*;
import com.example.demo.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class ConfirmOrderActivity extends AppCompatActivity
        implements PaymentMethodBottomSheetFragment.PaymentMethodListener,
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
    private String currentDeliveryMethod = "pickup"; // mặc định nhận tại quán
    private String lastOrderID;
    private final List<CartResponse.CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        apiService = ApiClient.getClient().create(ApiService.class);
        userID = SessionManager.getUserID(this);
        if (userID == null) { finish(); return; }

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
    }

    private void loadData() {
        fetchCartProducts();
        loadRecommended();
        fetchUserInfo();
    }

    private void updateDeliveryUI() {
        if ("pickup".equals(currentDeliveryMethod)) {
            layoutAddressBlock.setVisibility(View.VISIBLE);
            tvAddress.setText("Đơn đặt và nhận tại cửa hàng");
            tvCustomer.setText(apiCustomer);
        } else {
            layoutAddressBlock.setVisibility(View.VISIBLE);
            tvAddress.setText(currentAddress);
            tvCustomer.setText(apiCustomer);
        }
    }

    private void fetchUserInfo() {
        String name = SessionManager.getUserName(this);
        String phone = SessionManager.getUserPhone(this);
        apiCustomer = (name != null && phone != null) ? name + " | " + phone : "Khách hàng";
        updateDeliveryUI();
    }

    */
/*private void fetchCartProducts() {
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    cartItemsLocal.clear();
                    cartItemsLocal.addAll(r.body().getData().getItems());
                    cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
                    recyclerOrderItems.setAdapter(cartAdapter);
                    updateTotals(r.body().getData().getTotalMoney());
                }
            }
            @Override public void onFailure(Call<CartResponse> call, Throwable t) {}
        });
    }*//*

    private void fetchCartProducts() {
        ArrayList<CartResponse.CartItem> selectedItems =
                (ArrayList<CartResponse.CartItem>) getIntent().getSerializableExtra("selectedItems");
        long totalMoneyFromCart = getIntent().getLongExtra("totalMoney", 0);

        if (selectedItems != null && !selectedItems.isEmpty()) {
            cartItemsLocal.clear();
            cartItemsLocal.addAll(selectedItems);

            cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
            cartAdapter.setConfirmMode(true);
            recyclerOrderItems.setAdapter(cartAdapter);

            updateTotals(totalMoneyFromCart);
        } else {
            apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
                @Override
                public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                    if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                        cartItemsLocal.clear();
                        cartItemsLocal.addAll(r.body().getData().getItems());
                        cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
                        cartAdapter.setConfirmMode(true);
                        recyclerOrderItems.setAdapter(cartAdapter);
                        updateTotals(r.body().getData().getTotalMoney());
                    }
                }
                @Override public void onFailure(Call<CartResponse> call, Throwable t) {
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadRecommended() {
        apiService.getAllProducts().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<Product> list = new ArrayList<>();
                    for (MenuResponse.MenuItem item : r.body().getData()) {
                        list.add(Product.fromMenuItem(item)); // HOÀN HẢO!
                    }
                    recommendedAdapter = new RecommendedAdapter(list, ConfirmOrderActivity.this);
                    recyclerRecommended.setAdapter(recommendedAdapter);
                }
            }
            @Override public void onFailure(Call<MenuResponse> call, Throwable t) {}
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
            PaymentMethodBottomSheetFragment f = new PaymentMethodBottomSheetFragment();
            f.setPaymentMethodListener(ConfirmOrderActivity.this); // ĐÃ FIX
            f.show(getSupportFragmentManager(), "payment_sheet");
        });
        tvEditAddress.setOnClickListener(v -> selectAddressLauncher.launch(new Intent(this, SelectAddressActivity.class)));
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    */
/*private void selectPaymentMethod(String method) {
        currentPaymentMethod = method;
        int red = 0xFFA71317;
        int gray = 0xFFADABAB;
        btnCash.setBackgroundResource(method.equals(PAYMENT_METHOD_CASH) ? R.drawable.payment_option_selected : R.drawable.payment_option_default);
        btnCash.setTextColor(method.equals(PAYMENT_METHOD_CASH) ? red : gray);
        btnMomo.setBackgroundResource(method.equals(PAYMENT_METHOD_MOMO) || method.equals(PAYMENT_METHOD_VNPAY) ? R.drawable.payment_option_selected : R.drawable.payment_option_default);
        btnMomo.setTextColor(method.equals(PAYMENT_METHOD_MOMO) || method.equals(PAYMENT_METHOD_VNPAY) ? red : gray);
        btnMomo.setText(method.equals(PAYMENT_METHOD_VNPAY) ? "VNPay" : "Momo");
    }*//*


    private void placeOrder() {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", currentPaymentMethod);
        body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
        body.put("deliver", "delivery".equals(currentDeliveryMethod));
        body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
        body.put("note", "");

        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    lastOrderID = r.body().getData().getOrderID();
                    if (PAYMENT_METHOD_CASH.equals(currentPaymentMethod)) {
                        Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) requestMomoPayment(lastOrderID, userID);
                        else requestVnpayPayment(lastOrderID, userID);
                    }
                }
            }
            @Override public void onFailure(Call<OrderResponse> call, Throwable t) {}
        });
    }

    private void requestMomoPayment(String orderID, String userID) {
        apiService.createPaymentMomo(new CreateMomoRequest(orderID, userID))
                .enqueue(new Callback<CreateMomoResponse>() {
                    @Override public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getPayUrl() != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(r.body().getPayUrl())));
                        }
                    }
                    @Override public void onFailure(Call<CreateMomoResponse> call, Throwable t) {}
                });
    }

    private void requestVnpayPayment(String orderID, String userID) {
        apiService.createPaymentVnpay(new CreateVnpayRequest(orderID, userID))
                .enqueue(new Callback<CreateVnpayResponse>() {
                    @Override public void onResponse(Call<CreateVnpayResponse> call, Response<CreateVnpayResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getUrl() != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(r.body().getUrl())));
                        }
                    }
                    @Override public void onFailure(Call<CreateVnpayResponse> call, Throwable t) {}
                });
    }
    private void loadCart() {
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful()) {
                    cartAdapter = new CartAdapter(response.body().getData().getItems(), userID, ConfirmOrderActivity.this);
                    recyclerOrderItems.setAdapter(cartAdapter);
                    updateTotals(response.body().getData().getTotalMoney());
                }
            }
            @Override public void onFailure(Call<CartResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ", Toast.LENGTH_SHORT).show();
            }
        });
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
    @Override public void onCartUpdated() { fetchCartProducts(); }
    @Override public void onAddToCart(Product product) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("productID", product.getProductID());
        body.put("quantity", 1);
        body.put("size", "medium");
        body.put("topping", new ArrayList<>());
        body.put("note", "");

        apiService.addToCart(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) loadCart();
            }
            @Override public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (lastOrderID != null) {
            apiService.confirmPaymentStatus(lastOrderID).enqueue(new Callback<ConfirmPaymentResponse>() {
                @Override public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> r) {
                    if (r.isSuccessful() && "done".equals(r.body().getStatus())) {
                        Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                @Override public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {}
            });
        }
    }
}
*/
// ConfirmOrderActivity.java
// This activity handles order confirmation UI without checkboxes (as per UI logic: review selected items only).
// Receives selected items from CartFragment, displays them, allows quantity edit/delete.
// Logic synced with backend: When placing order, calls /order/create which processes the full cart but filters by quantity > 0.
// No changes to payment, delivery, or other core logic.

package com.example.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.demo.fragment.PaymentMethodBottomSheetFragment;
import com.example.demo.models.*;
import com.example.demo.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class ConfirmOrderActivity extends AppCompatActivity
        implements PaymentMethodBottomSheetFragment.PaymentMethodListener,
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
    private String currentDeliveryMethod = "pickup"; // mặc định nhận tại quán
    private String lastOrderID;
    private final List<CartResponse.CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        apiService = ApiClient.getClient().create(ApiService.class);
        userID = SessionManager.getUserID(this);
        if (userID == null) { finish(); return; }

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
    }

    private void loadData() {
        fetchCartProducts(); // Load selected items (no checkboxes)
        loadRecommended();
        fetchUserInfo();
    }

    private void updateDeliveryUI() {
        if ("pickup".equals(currentDeliveryMethod)) {
            layoutAddressBlock.setVisibility(View.VISIBLE);
            tvAddress.setText("Đơn đặt và nhận tại cửa hàng");
            tvCustomer.setText(apiCustomer);
        } else {
            layoutAddressBlock.setVisibility(View.VISIBLE);
            tvAddress.setText(currentAddress);
            tvCustomer.setText(apiCustomer);
        }
    }

    private void fetchUserInfo() {
        String name = SessionManager.getUserName(this);
        String phone = SessionManager.getUserPhone(this);
        apiCustomer = (name != null && phone != null) ? name + " | " + phone : "Khách hàng";
        updateDeliveryUI();
    }

    // Fetch and display only selected items from Cart (no full cart load, as per UI logic)
    private void fetchCartProducts() {
        // Receive selected items from CartFragment
        ArrayList<CartResponse.CartItem> selectedItems =
                (ArrayList<CartResponse.CartItem>) getIntent().getSerializableExtra("selectedItems");
        long totalMoneyFromCart = getIntent().getLongExtra("totalMoney", 0);

        if (selectedItems != null && !selectedItems.isEmpty()) {
            cartItemsLocal.clear();
            cartItemsLocal.addAll(selectedItems);

            // Use CartAdapter in confirm mode (hide checkboxes, show delete)
            cartAdapter = new CartAdapter(cartItemsLocal, userID, this);
            cartAdapter.setConfirmMode(true);
            recyclerOrderItems.setAdapter(cartAdapter);

            updateTotals(totalMoneyFromCart);
        } else {
            // Fallback: If no selected items, load full cart (but this should not happen normally)
            apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
                @Override
                public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                    if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                        cartItemsLocal.clear();
                        cartItemsLocal.addAll(r.body().getData().getItems());
                        cartAdapter = new CartAdapter(cartItemsLocal, userID, ConfirmOrderActivity.this);
                        cartAdapter.setConfirmMode(true);
                        recyclerOrderItems.setAdapter(cartAdapter);
                        updateTotals(r.body().getData().getTotalMoney());
                    }
                }
                @Override public void onFailure(Call<CartResponse> call, Throwable t) {
                    Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadRecommended() {
        apiService.getAllProducts().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<Product> list = new ArrayList<>();
                    for (MenuResponse.MenuItem item : r.body().getData()) {
                        list.add(Product.fromMenuItem(item)); // HOÀN HẢO!
                    }
                    recommendedAdapter = new RecommendedAdapter(list, ConfirmOrderActivity.this);
                    recyclerRecommended.setAdapter(recommendedAdapter);
                }
            }
            @Override public void onFailure(Call<MenuResponse> call, Throwable t) {}
        });
    }

    // Update totals (subtotal + shipping + fee)
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
            PaymentMethodBottomSheetFragment f = new PaymentMethodBottomSheetFragment();
            f.setPaymentMethodListener(ConfirmOrderActivity.this); // ĐÃ FIX
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
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", currentPaymentMethod);
        body.put("paymentStatus", PAYMENT_METHOD_CASH.equals(currentPaymentMethod) ? "done" : "not_done");
        body.put("deliver", "delivery".equals(currentDeliveryMethod));
        body.put("deliverAddress", "delivery".equals(currentDeliveryMethod) ? currentAddress : "Nhận tại quán");
        body.put("note", "");

        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    lastOrderID = r.body().getData().getOrderID();
                    if (PAYMENT_METHOD_CASH.equals(currentPaymentMethod)) {
                        Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) requestMomoPayment(lastOrderID, userID);
                        else requestVnpayPayment(lastOrderID, userID);
                    }
                }
            }
            @Override public void onFailure(Call<OrderResponse> call, Throwable t) {}
        });
    }

    private void requestMomoPayment(String orderID, String userID) {
        apiService.createPaymentMomo(new CreateMomoRequest(orderID, userID))
                .enqueue(new Callback<CreateMomoResponse>() {
                    @Override public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getPayUrl() != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(r.body().getPayUrl())));
                        }
                    }
                    @Override public void onFailure(Call<CreateMomoResponse> call, Throwable t) {}
                });
    }

    private void requestVnpayPayment(String orderID, String userID) {
        apiService.createPaymentVnpay(new CreateVnpayRequest(orderID, userID))
                .enqueue(new Callback<CreateVnpayResponse>() {
                    @Override public void onResponse(Call<CreateVnpayResponse> call, Response<CreateVnpayResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getUrl() != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(r.body().getUrl())));
                        }
                    }
                    @Override public void onFailure(Call<CreateVnpayResponse> call, Throwable t) {}
                });
    }

    // Update after cart changes (quantity or delete) - recalculate subtotal from local list
    @Override public void onCartUpdated() {
        long newSubtotal = cartItemsLocal.stream()
                .mapToLong(CartResponse.CartItem::getSubtotal)
                .sum();
        updateTotals(newSubtotal);
    }

    @Override public void onAddToCart(Product product) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("productID", product.getProductID());
        body.put("quantity", 1);
        body.put("size", "medium");
        body.put("topping", new ArrayList<>());
        body.put("note", "");

        apiService.addToCart(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) fetchCartProducts(); // Refresh after add (but only selected)
            }
            @Override public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (lastOrderID != null) {
            apiService.confirmPaymentStatus(lastOrderID).enqueue(new Callback<ConfirmPaymentResponse>() {
                @Override public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> r) {
                    if (r.isSuccessful() && "done".equals(r.body().getStatus())) {
                        Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                @Override public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {}
            });
        }
    }
}