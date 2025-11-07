package com.example.demo;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;

import com.example.demo.adapters.CartAdapter;
import com.example.demo.adapters.RecommendedAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.fragment.PaymentMethodBottomSheetFragment;
import com.example.demo.models.CartItem;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.CreateMomoRequest;
import com.example.demo.models.CreateMomoResponse;
import com.example.demo.models.CreateVnpayRequest;
import com.example.demo.models.CreateVnpayResponse;
import com.example.demo.models.OrderResponse;
import com.example.demo.utils.SessionManager;
import com.example.demo.SelectAddressActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmOrderActivity extends AppCompatActivity implements PaymentMethodBottomSheetFragment.PaymentMethodListener{
    public static final String PAYMENT_METHOD_CASH = "PAYMENT_CASH";
    public static final String PAYMENT_METHOD_MOMO = "PAYMENT_MOMO";
    public static final String PAYMENT_METHOD_VNPAY = "PAYMENT_VNPAY";
    private RecyclerView recyclerOrderItems, recyclerRecommended;
    private Button btnPlaceOrder;
    private ImageButton btnBack;
    private TextView tvAddress, tvCustomer;
    private TextView tvSubtotal, tvShipping, tvFee, tvTotalPayment;
    private TextView btnMomo, btnCash;
    private TextView btnOtherPaymentMethods;

    private View layoutAddressBlock; // View chứa địa chỉ
    private TextView tvEditAddress;  // Nút "Chỉnh sửa"
    private CartAdapter cartAdapter;
    private RecommendedAdapter recommendedAdapter;
    private ApiService apiService;
    private String userID;

    /*private String currentPaymentMethod = "Tiền mặt";*/
    private String currentAddress = "";
    private String currentDeliveryMethod = "delivery";
    private String apiCustomer = "";
    // ✅ THAY ĐỔI: Đặt giá trị mặc định bằng hằng số
    private String currentPaymentMethod = PAYMENT_METHOD_CASH;
    private double currentShippingFee = 50000.0;
    private double currentServiceFee = 10000.0;

    // ✅ BỔ SUNG BIẾN: giữ danh sách sản phẩm hiện có trong giỏ
    private final List<CartItem> cartItemsLocal = new ArrayList<>();
    private ActivityResultLauncher<Intent> selectAddressLauncher;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);
        registerAddressLauncher();
        initViews();
        setupRecyclerViews();

        if (!initApiAndSession()) return;

        loadData();
        setupClickListeners();

        selectPaymentMethod(currentPaymentMethod);
    }
    // ✅ BỔ SUNG HÀM MỚI NÀY VÀO TRONG CLASS
    private void registerAddressLauncher() {
        selectAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String newMethod = data.getStringExtra("newMethod");

                            if ("pickup".equals(newMethod)) {
                                // ✅ GỌI HÀM CẬP NHẬT UI
                                updateDeliveryMethodUI("pickup");

                            } else if ("delivery".equals(newMethod)) {
                                // (Code cập nhật địa chỉ... giữ nguyên)
                                String newAddress = data.getStringExtra("newAddress");
                                String newCustomerInfo = data.getStringExtra("newCustomerInfo");
                                currentAddress = newAddress;
                                tvAddress.setText(newAddress);
                                tvCustomer.setText(newCustomerInfo);

                                // ✅ GỌI HÀM CẬP NHẬT UI
                                updateDeliveryMethodUI("delivery");
                            }
                        }
                    }
                }
        );
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
        btnOtherPaymentMethods=findViewById(R.id.btnOtherPayment);
        layoutAddressBlock = findViewById(R.id.layoutAddressBlock);
        tvEditAddress = findViewById(R.id.tvEditAddress);
    }
    // TRONG FILE ConfirmOrderActivity.java

    private void updateDeliveryMethodUI(String method) {
        currentDeliveryMethod = method; // Lưu lại lựa chọn

        if (layoutAddressBlock == null) {
            return;
        }

        if (method.equals("delivery")) {
            // ✅ HIỆN KHỐI ĐỊA CHỈ
            layoutAddressBlock.setVisibility(View.VISIBLE);

            // (Bạn cũng nên cập nhật lại địa chỉ cũ ở đây
            // phòng trường hợp người dùng chọn lại "Giao hàng")
            tvAddress.setText(currentAddress); // currentAddress là địa chỉ thật
            tvCustomer.setText(apiCustomer); // apiCustomer là tên thật

        } else { // (Nếu là "pickup" - Tại quán)

            // ✅ ẨN KHỐI ĐỊA CHỈ (Bạn đã làm)
            /*layoutAddressBlock.setVisibility(View.GONE);*/

            // ✅ BƯỚC QUAN TRỌNG:
            // Mặc dù khối địa chỉ bị ẨN, một số phần (như tvAddress)
            // có thể vẫn nằm ngoài khối đó (tùy vào XML của bạn).
            // Để chắc chắn, hãy cập nhật TextViews.

            // GIẢ SỬ XML CỦA BẠN GIỐNG HÌNH 2 (image_74d3f6.png)
            // (Hình 2 cho thấy tvAddress và tvCustomer vẫn hiển thị)

            tvAddress.setText("Đơn đặt và nhận tại cửa hàng"); // <-- SỬA CHỮ Ở ĐÂY
            tvCustomer.setText(apiCustomer); // (Giữ nguyên tên khách hàng)
        }
    }
    private void setupRecyclerViews() {
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrderItems.setNestedScrollingEnabled(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerRecommended.setLayoutManager(layoutManager);
        recyclerRecommended.setNestedScrollingEnabled(false);
    }

    private boolean initApiAndSession() {
        userID = SessionManager.getUserID(this);
        if (userID == null) {
            Toast.makeText(this, "Chưa đăng nhập, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        apiService = ApiClient.getClient().create(ApiService.class);
        return true;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
        // ✅ THAY ĐỔI:
        // 1. Gán hành động CHỌN TRỰC TIẾP cho 2 nút chính
        btnCash.setOnClickListener(v -> selectPaymentMethod(PAYMENT_METHOD_CASH));
        btnMomo.setOnClickListener(v -> selectPaymentMethod(PAYMENT_METHOD_MOMO));

        // 2. Tạo listener MỞ SHEET
        View.OnClickListener openPaymentSheetListener = v -> {
            PaymentMethodBottomSheetFragment bottomSheet = new PaymentMethodBottomSheetFragment();
            bottomSheet.show(getSupportFragmentManager(), "PaymentMethodBottomSheet");
        };
        /*TextView tvEditAddress = findViewById(R.id.tvEditAddress);*/

        tvEditAddress.setOnClickListener(v -> {
            // 1. Tạo Intent để đi tới Màn hình 2
            Intent intent = new Intent(ConfirmOrderActivity.this, SelectAddressActivity.class);

            // 2. Mở màn hình 2 và chờ kết quả
            selectAddressLauncher.launch(intent);
        });
        // 3. Chỉ gán listener MỞ SHEET cho nút "Khác"
        btnOtherPaymentMethods.setOnClickListener(openPaymentSheetListener);
        updateDeliveryMethodUI(currentDeliveryMethod);
    }
    @Override
    public void onPaymentMethodSelected(String paymentMethod) {
        // Khi người dùng chọn xong, gọi hàm `selectPaymentMethod` bạn đã viết
        selectPaymentMethod(paymentMethod);
    }
    private void loadData() {
        fetchCartProducts();
        setupRecommended();
        fetchUserInfo();
    }

    private void fetchCartProducts() {
        apiService.viewCart(userID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<CartItem> items = response.body().getData().getItems();
                    cartItemsLocal.clear();
                    cartItemsLocal.addAll(items);

                    cartAdapter = new CartAdapter(ConfirmOrderActivity.this, cartItemsLocal, null);
                    recyclerOrderItems.setAdapter(cartAdapter);

                    updatePaymentSummary(calculateSubtotal());
                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ HÀM TÍNH TỔNG TIỀN GIỎ HÀNG
    private double calculateSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItemsLocal) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        return subtotal;
    }

    private void updatePaymentSummary(double subtotal) {
        double total = subtotal + currentShippingFee + currentServiceFee;
        tvSubtotal.setText(String.format("%,.0f VND", subtotal));
        tvShipping.setText(String.format("%,.0f VND", currentShippingFee));
        tvFee.setText(String.format("%,.0f VND", currentServiceFee));
        tvTotalPayment.setText(String.format("%,.0f VND", total));
        btnPlaceOrder.setText(String.format("Đặt hàng - %,.0f VND", total));
    }

    // TRONG FILE ConfirmOrderActivity.java

    private void fetchUserInfo() {
        // 1. ĐỌC DỮ LIỆU ĐÃ LƯU TỪ SESSIONMANAGER
        String name = SessionManager.getUserName(this);
        String phone = SessionManager.getUserPhone(this);
        String address = SessionManager.getUserAddress(this);

        // 2. Cập nhật biến toàn cục
        currentAddress = (address != null) ? address : "Vui lòng cập nhật địa chỉ";
        apiCustomer = (name != null && phone != null) ? (name + " | " + phone) : "Khách hàng";

        // 3. Cập nhật giao diện (UI)
        tvAddress.setText(currentAddress);
        tvCustomer.setText(apiCustomer);

        // 4. Cập nhật lại trạng thái hiển thị (quan trọng)
        updateDeliveryMethodUI(currentDeliveryMethod);
    }

    // ✅ SETUP LIST RECOMMENDED + LOGIC “THÊM NGAY VÀO GIỎ”
    private void setupRecommended() {
        List<Product> recommendedList = new ArrayList<>();
        recommendedList.add(new Product("Trà sữa Socola", "45000", R.drawable.drink_blueberry_smooth, 0));
        recommendedList.add(new Product("Bánh Dâu Kem", "55000", R.drawable.cake_strawberry_cheese, 0));

        recommendedAdapter = new RecommendedAdapter(recommendedList, product -> {
            CartItem newItem = CartItem.fromProduct(product);

            // Kiểm tra trùng
            int foundIndex = -1;
            for (int i = 0; i < cartItemsLocal.size(); i++) {
                CartItem existing = cartItemsLocal.get(i);
                if (existing.getName().equalsIgnoreCase(newItem.getName())) {
                    foundIndex = i;
                    break;
                }
            }

            if (foundIndex >= 0) {
                CartItem existing = cartItemsLocal.get(foundIndex);
                existing.setQuantity(existing.getQuantity() + 1);
                existing.setSubtotal((int) (existing.getPrice() * existing.getQuantity()));
                cartAdapter.notifyItemChanged(foundIndex);
            } else {
                cartItemsLocal.add(newItem);
                cartAdapter.notifyItemInserted(cartItemsLocal.size() - 1);
                recyclerOrderItems.scrollToPosition(cartItemsLocal.size() - 1);
            }

            updatePaymentSummary(calculateSubtotal());
        });

        recyclerRecommended.setAdapter(recommendedAdapter);
    }

    private void handlePlaceOrder() {
        String address = currentAddress;
        String note = "";
        String payment = currentPaymentMethod;
        String delivery = currentDeliveryMethod; // Lấy phương thức giao hàng

        // Chỉ kiểm tra địa chỉ nếu là "delivery"
        if (delivery.equals("delivery") && TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        // Truyền "delivery" vào hàm createOrder
        createOrder(address, note, payment, delivery);
    }

    private void selectPaymentMethod(String method) {
        currentPaymentMethod = method;
        int redColor = Color.parseColor("#A71317");
        int greyColor = Color.parseColor("#ADABAB");

        //trang thai dau tien
        btnCash.setBackgroundResource(R.drawable.payment_option_default);
        btnCash.setTextColor(greyColor);
        btnMomo.setBackgroundResource(R.drawable.payment_option_default);
        btnMomo.setTextColor(greyColor);
        if(method.equals(PAYMENT_METHOD_CASH)){
            btnCash.setTextColor(redColor);
            btnCash.setBackgroundResource(R.drawable.payment_option_selected);
        }
        else if(method.equals(PAYMENT_METHOD_MOMO)){
            btnMomo.setTextColor(redColor);
            btnMomo.setBackgroundResource(R.drawable.payment_option_selected);
        }
        //dang su dung momo la btn dai dien
        else if (method.equals(PAYMENT_METHOD_VNPAY)) {
            // CHỌN VNPAY (từ BottomSheet)
            btnMomo.setBackgroundResource(R.drawable.payment_option_selected);
            btnMomo.setTextColor(redColor);
            btnMomo.setText("VNPay");
        }
    }
    private void requestMomoPayment(String orderID, String userID) {
        CreateMomoRequest requestBody = new CreateMomoRequest(orderID, userID);

        apiService.createPaymentMomo(requestBody).enqueue(new Callback<CreateMomoResponse>() {
            @Override
            public void onResponse(Call<CreateMomoResponse> call, Response<CreateMomoResponse> response) {
                // progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getPayUrl() != null) {

                    String payUrl = response.body().getPayUrl();

                    // ✅ MỞ TRANG WEB THANH TOÁN MOMO
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                    startActivity(intent);

                    // Lưu lại orderID này để kiểm tra sau khi người dùng quay lại app
                    // SessionManager.saveProcessingOrder(orderID);


                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Không thể tạo link thanh toán MoMo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateMomoResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi khi gọi MoMo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createOrder(String address, String note, String paymentMethod, String deliveryMethod) {
        /*Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", paymentMethod.equals("Tiền mặt") ? "cash" : "momo");
        body.put("paymentStatus", "unpaid");
        body.put("deliver", "delivery");
        body.put("deliverAddress", address);
        body.put("note", note);*/

        String paymentApiString;
        if (paymentMethod.equals(PAYMENT_METHOD_MOMO)) {
            paymentApiString = "momo";
        } else if (paymentMethod.equals(PAYMENT_METHOD_VNPAY)) {
            paymentApiString = "vnpay";
        } else {
            paymentApiString = "cash";
        }
        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("paymentMethod", paymentApiString);
        body.put("paymentStatus", "unpaid");
        body.put("note", note);
        body.put("deliver", deliveryMethod);

        /*String paymentApiString;
        if (paymentMethod.equals(PAYMENT_METHOD_MOMO)) {
            paymentApiString = "momo";
        } else if (paymentMethod.equals(PAYMENT_METHOD_VNPAY)) {
            paymentApiString = "vnpay";
        } else {
            paymentApiString = "cash"; // Gán giá trị mặc định
        }*/

        if (deliveryMethod.equals("delivery")) {
            body.put("deliverAddress", address);
        } else {
            body.put("deliverAddress", "Nhận tại quán");
        }

        // Hiển thị loading (ví dụ: ProgressBar)
        // progressBar.setVisibility(View.VISIBLE);

        apiService.createOrder(body).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getStatus().trim().equalsIgnoreCase("Successful")) {

                    String newOrderID = response.body().getData().getOrderID();

                    if (paymentApiString.equals("cash")) {
                        Toast.makeText(ConfirmOrderActivity.this, "✅ Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (paymentApiString.equals("momo")) {
                        Toast.makeText(ConfirmOrderActivity.this, "Đang tạo link thanh toán MoMo...", Toast.LENGTH_SHORT).show();
                        requestMomoPayment(newOrderID, userID);
                    }
                    else if (paymentApiString.equals("vnpay")) {
                        Toast.makeText(ConfirmOrderActivity.this, "Đang tạo link thanh toán VNPAY...", Toast.LENGTH_SHORT).show();
                        requestVnpayPayment(newOrderID, userID); // Gọi hàm VNPAY mới
                    }

                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Không thể tạo đơn hàng!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                // progressBar.setVisibility(View.GONE);
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ✅ BỔ SUNG HÀM MỚI NÀY

    private void requestVnpayPayment(String orderID, String userID) {
        // 1. Dùng Request của VNPAY
        CreateVnpayRequest requestBody = new CreateVnpayRequest(orderID, userID);

        // 2. Gọi API createPaymentVnpay
        apiService.createPaymentVnpay(requestBody).enqueue(new Callback<CreateVnpayResponse>() {
            @Override
            public void onResponse(Call<CreateVnpayResponse> call, Response<CreateVnpayResponse> response) {

                // 3. Dùng getUrl() (Vì VNPAY trả về "url")
                if (response.isSuccessful() && response.body() != null && response.body().getUrl() != null) {

                    String payUrl = response.body().getUrl();

                    // 4. Mở trang web thanh toán VNPAY
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                    startActivity(intent);

                    // (Chuyển sang màn hình "Đang xử lý"...)

                } else {
                    Toast.makeText(ConfirmOrderActivity.this, "Không thể tạo link thanh toán VNPAY", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateVnpayResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi khi gọi VNPAY: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
