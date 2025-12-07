package com.example.demo.api;

import com.example.demo.models.AddUpdateResponse;
import com.example.demo.models.AddressRequest;
import com.example.demo.models.AddressResponse;
import com.example.demo.models.AuthResponse;
import com.example.demo.models.CancelOrderRequest;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.ConfirmPaymentResponse;
import com.example.demo.models.CreateMomoRequest;
import com.example.demo.models.CreateMomoResponse;
import com.example.demo.models.CreateVnpayRequest;
import com.example.demo.models.CreateVnpayResponse;
import com.example.demo.models.MapboxSuggestionResponse;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.NotificationListResponse;
import com.example.demo.models.OrderListResponse;
import com.example.demo.models.OrderResponse;
import com.example.demo.models.VoucherResponse;

import retrofit2.Call;
import retrofit2.http.*;


import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.HTTP;

public interface ApiService {

    // ---- AUTH ----
    @POST("user/login")
    Call<AuthResponse> login(@Body Map<String, String> body);
    @POST("user/signup")
    Call<CommonResponse> signup(@Body Map<String, Object> body);

    @POST("user/verify-otp")
    Call<CommonResponse> verifyOtp(@Body Map<String, String> body);

    @POST("user/resend-otp")
    Call<CommonResponse> resendOtp(@Body Map<String, String> body);

    @PUT("user/set-password-username")
    Call<CommonResponse> setPasswordUsername(@Body Map<String, String> body);

    @PUT("user/change-password")
    Call<CommonResponse> changePassword(@Body Map<String, String> body);

    @POST("user/forgot-password")
    Call<CommonResponse> forgotPassword(@Body Map<String, String> body);

    @HTTP(method = "DELETE", path = "user/delete", hasBody = true)
    Call<CommonResponse> deleteAccount(@Body Map<String, String> body);
    @POST("user/social-login")
    Call<AuthResponse> socialLogin(@Body Map<String, String> body);

    // ---- CART ----

    @GET("cart/view-and-caculate-total-money")
    Call<CartResponse> viewCart(@Query("userID") String userID);
    @POST("cart/add")
    Call<CommonResponse> addToCart(@Body Map<String, Object> body);

    @PUT("cart/update-increase")
    Call<CommonResponse> increaseItem(@Body Map<String, Object> body);

    @PUT("cart/update-decrease")
    Call<CommonResponse> decreaseItem(@Body Map<String, Object> body);

    @HTTP(method = "DELETE", path = "cart/delete", hasBody = true)
    Call<CommonResponse> deleteItem(@Body Map<String, Object> body);

    @DELETE("cart/delete-all")
    Call<CommonResponse> deleteAll(@Body Map<String, Object> body);

    @POST("order/create")
    Call<OrderResponse> createOrder(@Body Map<String, Object> body);

    @PUT("order/change-order-status")
    Call<CommonResponse> changeOrderStatus(@Query("orderID") String orderID, @Query("status") String status);

    @HTTP(method = "DELETE", path = "order/cancel", hasBody = true)
    //Call<CommonResponse> cancelOrder(@Body Map<String, Object> body);


    @GET("order/view-all")
    Call<OrderListResponse> viewAllOrders();
    @GET("order/list")
    Call<CommonResponse> getOrdersByDate(@Query("startDate") String startDate, @Query("endDate") String endDate);


    @POST("order/review-order-and-products")
    Call<CommonResponse> reviewOrderAndProducts(
            @Query("orderID") String orderID,
            @Body Map<String, Object> body
    );

    @PUT("order/change-order-review")
    Call<CommonResponse> changeOrderReview(
            @Query("orderID") String orderID,
            @Body Map<String, Object> body
    );
    /* @POST("payment-momo/create-payment-momo")
     Call<CreateMomoResponse> createPaymentMomo(@Body CreateMomoRequest body);
     @GET("payment-momo/confirm")
     Call<Map<String, Object>> confirmMomoPayment(@Query("orderID") String orderID);
     @GET("payment-momo/confirm")
     Call<ConfirmPaymentResponse> confirmPaymentStatus(@Query("orderID") String orderID);
     @POST("payment-momo/momo-notify")
     Call<Object> notifyMomoPayment(@Body Map<String, Object> body);
     @POST("payment-vnpay/create-payment-vnpay")
     Call<String> createPaymentVnpayString(@Body CreateVnpayRequest body);

     @GET("payment-vnpay/check-payment-status")
     Call<ConfirmPaymentResponse> confirmVnpayStatus(@Query("vnp_TxnRef") String orderID);*/
    @GET("menu/all-product")
    Call<MenuResponse> getAllProducts();
    @GET("menu/product")
    Call<MenuResponse.SingleProductResponse> getProductDetail(@Query("productID") String productID);
    @GET("order/view")
    Call<OrderResponse> viewOrder(@Query("orderID") String orderID);
    @GET("order/order-history")
    Call<OrderListResponse> getOrderHistory(@Query("userID") String userID);
    @HTTP(method = "DELETE", path = "order/cancel", hasBody = true)
    Call<CommonResponse> cancelOrder(@Body CancelOrderRequest body);
    @POST("address/add")
    Call<AddUpdateResponse> addAddress(@Body AddressRequest request);
    @GET("address/show")
    Call<AddressResponse> getListAddress(@Query("userID") String userID);

    @POST("address/delete")
    Call<AddressResponse> deleteAddress(@Body AddressRequest request);

    @PUT("address/update")
    Call<AddUpdateResponse> updateAddress(@Body AddressRequest request);

    @GET("address/suggestion")
    Call<MapboxSuggestionResponse> getSuggestion(@Query("input") String input);
    // 1. Gợi ý voucher khả dụng cho user (POST /voucher/suggestion)
    // Body cần: userID
    @POST("voucher/suggestion")
    Call<VoucherResponse> getVoucherSuggestion(@Body Map<String, String> body);

    // 2. Lấy danh sách voucher khả dụng của user (GET /voucher/available-user)
    @GET("voucher/available-user")
    Call<VoucherResponse> getAvailableVouchers(@Query("userID") String userID);

    // 3. Áp dụng voucher thủ công (PUT /voucher/apply)
    // Body cần: voucherCode, orderID
    // Trả về OrderResponse (vì BE trả về data là Order đã update giá)
    @PUT("voucher/apply")
    Call<OrderResponse> applyVoucher(@Body Map<String, String> body);

    // 4. Tự động áp dụng voucher tốt nhất (PUT /voucher/auto-apply)
    // Body cần: orderID
    // Trả về VoucherResponse (để lấy discountAmount và bestVoucher) hoặc OrderResponse tùy cách bạn dùng
    @PUT("voucher/auto-apply")
    Call<VoucherResponse> autoApplyVoucher(@Body Map<String, String> body);

    // 5. Xác nhận đã sử dụng voucher (Gọi sau khi thanh toán thành công)
    // Body cần: voucherCode, orderID
    @PUT("voucher/confirm-use")
    Call<CommonResponse> confirmVoucherUse(@Body Map<String, String> body);
    /*@POST("user/save-token") // Thay bằng endpoint thật của Backend bạn
    Call<CommonResponse> saveFcmToken(@Body Map<String, String> body);*/

    @POST("notification/save-fcm-token")
    Call<CommonResponse>saveFcmToken(@Body Map<String,String>body);
    @POST("notification/send-to-users")
    Call<CommonResponse>sendNotificationToUser(@Body Map<String,Object>body);
    @POST("notification/create")
    Call<CommonResponse>createNotification(@Body Map<String,Object>body);
    @GET("notification/list")
    Call<NotificationListResponse>getNotificationList(@Query("userID") String userID);
    /*@POST("notification/send-all")
    Call<>*/
    /*@POST("momo/create-payment-momo")
    Call<CreateMomoResponse> createPaymentMomo(@Body CreateMomoRequest body);

    @GET("momo/confirm")
    Call<ConfirmPaymentResponse> confirmPaymentStatus(@Query("orderID") String orderID);*/

    @POST("momo/momo-notify")
    Call<Object> notifyMomoPayment(@Body Map<String, Object> body);

    @POST("vnpay/create-payment-vnpay")
    Call<String> createPaymentVnpayString(@Body CreateVnpayRequest body);

    @GET("vnpay/check-payment-status")
    Call<ConfirmPaymentResponse> confirmVnpayStatus(@Query("vnp_TxnRef") String orderID);
    // Trong ApiService.java
    @HTTP(method = "DELETE", path = "order/delete-interrupt-order", hasBody = true)
    Call<CommonResponse> deleteInterruptOrder(@Body Map<String, String> body);
    @POST("deliver/calculate-fee")
    Call<CommonResponse> calculateShippingFee(@Body Map<String, Object> body);
    // 1. Tạo thanh toán
    @POST("momo/create")
    Call<CreateMomoResponse> createPaymentMomo(@Body CreateMomoRequest request);

    // 2. Kiểm tra trạng thái (Polling)
    // BE: router.get('/confirm', ...) lấy orderID từ query param
    @GET("momo/confirm")
    Call<ConfirmPaymentResponse> confirmPaymentStatus(@Query("orderID") String orderID);
    @POST("vnpay/create")
    Call<String> createPaymentVnpay(@Body Map<String, String> body);
}