package com.example.demo.api;

import com.example.demo.models.AuthResponse;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.ConfirmPaymentResponse;
import com.example.demo.models.CreateMomoRequest;
import com.example.demo.models.CreateMomoResponse;
import com.example.demo.models.CreateVnpayRequest;
import com.example.demo.models.CreateVnpayResponse;
import com.example.demo.models.MenuResponse;
import com.example.demo.models.OrderListResponse;
import com.example.demo.models.OrderResponse;

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

    @HTTP(method = "DELETE", path = "api/user/delete", hasBody = true)
    Call<CommonResponse> deleteAccount(@Body Map<String, String> body);

    // ---- CART ----

    @GET("cart/view-and-caculate-total-money")
    Call<CartResponse> viewCart(@Query("userID") String userID);
    @POST("cart/add")
    Call<CommonResponse> addToCart(@Body Map<String, Object> body);

    @PUT("cart/update-increase")
    Call<CommonResponse> increaseItem(@Body Map<String, Object> body);

    @PUT("cart/update-decrease")
    Call<CommonResponse> decreaseItem(@Body Map<String, Object> body);

    // FIX: THAY @HTTP BẰNG @DELETE CHUẨN
    @DELETE("cart/delete")
    Call<CommonResponse> deleteItem(@Body Map<String, Object> body);

    @DELETE("cart/delete-all")
    Call<CommonResponse> deleteAll(@Body Map<String, Object> body);

    // ---- ORDER ----

    @POST("order/create")
    Call<OrderResponse> createOrder(@Body Map<String, Object> body);

    @PUT("order/change-order-status")
    Call<CommonResponse> changeOrderStatus(@Query("orderID") String orderID, @Query("status") String status);

    @HTTP(method = "DELETE", path = "order/cancel", hasBody = true)
    Call<CommonResponse> cancelOrder(@Body Map<String, Object> body);

    @GET("order/view")
    Call<CommonResponse> viewOrder(@Query("orderID") String orderID);

    @GET("order/view-all")
    Call<OrderListResponse> viewAllOrders();
    @GET("order/order-history")
    Call<OrderListResponse> getOrderHistory(@Query("userID") String userID);
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
    @POST("payment/create-payment-momo")
    Call<CreateMomoResponse> createPaymentMomo(@Body CreateMomoRequest body);


    @GET("payment/confirm")
    Call<ConfirmPaymentResponse> confirmPaymentStatus(@Query("orderID") String orderID);


    @POST("payment/create-payment-vnpay")
    Call<CreateVnpayResponse> createPaymentVnpay(@Body CreateVnpayRequest body);

    // MENU - LẤY TẤT CẢ SẢN PHẨM ĐỂ GỢI Ý
    @GET("menu/all-product")
    Call<MenuResponse> getAllProducts();

    // LẤY CHI TIẾT SẢN PHẨM ĐỂ LẤY ẢNH
    @GET("menu/product")
    Call<MenuResponse.SingleProductResponse> getProductDetail(@Query("productID") String productID);

}
