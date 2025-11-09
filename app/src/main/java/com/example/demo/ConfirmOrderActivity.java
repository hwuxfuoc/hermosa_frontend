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
import com.example.demo.adapter.CartAdapter;
import com.example.demo.adapter.RecommendedAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.fragment.PaymentMethodBottomSheetFragment;
import com.example.demo.model.*;
import com.example.demo.model.CartResponseWrapper;
import com.example.demo.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class ConfirmOrderActivity extends AppCompatActivity
        implements PaymentMethodBottomSheetFragment.PaymentMethodListener,
        CartAdapter.OnAction,
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
    private final List<CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

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
                        } else {
                            currentDeliveryMethod = "delivery";
                            currentAddress = result.getData().getStringExtra("address");
                            apiCustomer = result.getData().getStringExtra("customer");
                        }
                        updateDeliveryUI();
                        updateTotals(calculateLocalTotal());
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
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> r) {
                if (r.isSuccessful() && r.body() != null && "Success".equals(r.body().getStatus())) {
                    cartItemsLocal.clear();

                    List<CartResponse.CartItem> serverItems = r.body().getData().getItems();
                    if (serverItems != null) {
                        for (CartResponse.CartItem serverItem : serverItems) {
                            // CHUYỂN ĐỔI THỦ CÔNG: CartResponse.CartItem → CartItem
                            CartItem localItem = new CartItem();
                            localItem.setId(serverItem.getId());
                            localItem.setProductID(serverItem.getProductID());
                            localItem.setName(serverItem.getName());
                            localItem.setPrice(serverItem.getPrice());
                            localItem.setQuantity(serverItem.getQuantity());
                            localItem.setSubtotal(serverItem.getSubtotal());
                            localItem.setSize(serverItem.getSize());
                            localItem.setTopping(serverItem.getTopping() != null ? serverItem.getTopping().toArray(new String[0]) : new String[0]);
                            localItem.setNote(serverItem.getNote());
                            localItem.setImageUrl(serverItem.getPicture());

                            // Màu nền theo category
                            int color = 0xFFFFFFFF;
                            if ("drink".equals(serverItem.getCategory())) color = 0xFFE3F2FD;
                            else if ("cake".equals(serverItem.getCategory())) color = 0xFFFFF3E0;
                            else if ("lunch".equals(serverItem.getCategory())) color = 0xFFF1F8E9;
                            localItem.setColor(color);

                            localItem.setSelected(true);

                            cartItemsLocal.add(localItem);
                        }
                    }

                    cartAdapter = new CartAdapter(
                            ConfirmOrderActivity.this,
                            cartItemsLocal, // Vẫn là List<CartItem> → Adapter chạy ngon!
                            userID,
                            ConfirmOrderActivity.this
                    );
                    recyclerOrderItems.setAdapter(cartAdapter);
                    updateTotals(calculateLocalTotal());
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
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

    private long calculateLocalTotal() {
        long total = 0;
        for (CartItem item : cartItemsLocal) {
            if (item.isSelected()) {
                total += item.getSubtotal();
            }
        }
        return total;
    }

    private void updateTotals(long subtotal) {
        long shipping = "delivery".equals(currentDeliveryMethod) ? 50000 : 0;
        long fee = 10000;
        long total = subtotal + shipping + fee;

        tvSubtotal.setText(String.format("%,d đ", subtotal));
        tvShipping.setText(String.format("%,d đ", shipping));
        tvFee.setText(String.format("%,d đ", fee));
        tvTotalPayment.setText(String.format("%,d đ", total));
        btnPlaceOrder.setText("Đặt hàng - " + String.format("%,d đ", total));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCash.setOnClickListener(v -> onPaymentMethodSelected(PAYMENT_METHOD_CASH));
        btnMomo.setOnClickListener(v -> onPaymentMethodSelected(PAYMENT_METHOD_MOMO));
        btnOtherPayment.setOnClickListener(v -> {
            PaymentMethodBottomSheetFragment f = new PaymentMethodBottomSheetFragment();
            f.setPaymentMethodListener(ConfirmOrderActivity.this);
            f.show(getSupportFragmentManager(), "payment_sheet");
        });
        tvEditAddress.setOnClickListener(v -> selectAddressLauncher.launch(new Intent(this, SelectAddressActivity.class)));
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
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
                        Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        if (PAYMENT_METHOD_MOMO.equals(currentPaymentMethod)) {
                            requestMomoPayment(lastOrderID, userID);
                        } else {
                            requestVnpayPayment(lastOrderID, userID);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi đặt hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestMomoPayment(String orderID, String userID) {
        apiService.createPaymentMomo(new CreateMomoRequest(orderID, userID))
                .enqueue(new Callback<CreateMomoResponse>() {
                    @Override
                    public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getPayUrl() != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(r.body().getPayUrl())));
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateMomoResponse> call, Throwable t) {}
                });
    }

    private void requestVnpayPayment(String orderID, String userID) {
        apiService.createPaymentVnpay(new CreateVnpayRequest(orderID, userID))
                .enqueue(new Callback<CreateVnpayResponse>() {
                    @Override
                    public void onResponse(Call<CreateVnpayResponse> call, Response<CreateVnpayResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getUrl() != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(r.body().getUrl())));
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateVnpayResponse> call, Throwable t) {}
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

    // TĂNG GIẢM XÓA CHỌN MÓN - HOẠT ĐỘNG 100%
    @Override
    public void onIncrease(CartItem item) {
        item.setQuantity(item.getQuantity() + 1);
        item.updateSubtotal();
        syncItemToServer(item);
        cartAdapter.notifyDataSetChanged();
        updateTotals(calculateLocalTotal());
    }

    @Override
    public void onDecrease(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            item.updateSubtotal();
            syncItemToServer(item);
            cartAdapter.notifyDataSetChanged();
            updateTotals(calculateLocalTotal());
        }
    }

    @Override
    public void onDelete(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());

        apiService.deleteItem(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                if (r.isSuccessful() && r.body() != null && "Success".equals(r.body().getStatus())) {
                    cartItemsLocal.remove(item);
                    cartAdapter.notifyDataSetChanged();
                    updateTotals(calculateLocalTotal());
                    Toast.makeText(ConfirmOrderActivity.this, "Đã xóa món!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onToggleSelect(CartItem item, boolean selected) {
        item.setSelected(selected);
        updateTotals(calculateLocalTotal());
    }

    @Override
    public void onDataChanged() {
        updateTotals(calculateLocalTotal());
    }

    private void syncItemToServer(CartItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("itemID", item.getId());

        Call<CommonResponse> call = apiService.increaseItem(body);
        if (item.getQuantity() <= 1) {
            call = apiService.decreaseItem(body);
        }

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                if (r.isSuccessful()) {
                    fetchCartProducts(); // Đồng bộ lại từ server
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
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
                if (r.isSuccessful()) {
                    fetchCartProducts();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lastOrderID != null) {
            apiService.confirmPaymentStatus(lastOrderID).enqueue(new Callback<ConfirmPaymentResponse>() {
                @Override
                public void onResponse(Call<ConfirmPaymentResponse> call, Response<ConfirmPaymentResponse> r) {
                    if (r.isSuccessful() && "done".equals(r.body().getStatus())) {
                        Toast.makeText(ConfirmOrderActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ConfirmPaymentResponse> call, Throwable t) {}
            });
        }
    }
}