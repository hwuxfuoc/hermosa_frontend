package com.example.demo.api;

import com.example.demo.models.AuthResponse;
import com.example.demo.models.CartResponse;
import com.example.demo.models.CommonResponse;
import retrofit2.Call;
import retrofit2.http.*;


import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
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

    @HTTP(method = "DELETE", path = "cart/delete", hasBody = true)
    Call<CommonResponse> deleteItem(@Body Map<String, Object> body);

    @HTTP(method = "DELETE", path = "cart/delete-all", hasBody = true)
    Call<CommonResponse> deleteAll(@Body Map<String, Object> body);

    // ---- ORDER ----
    @POST("order/create")
    Call<CommonResponse> createOrder(@Body Map<String, Object> body);

    @PUT("order/change-order-status")
    Call<CommonResponse> changeOrderStatus(@Query("orderID") String orderID, @Query("status") String status);

    @HTTP(method = "DELETE", path = "order/cancel", hasBody = true)
    Call<CommonResponse> cancelOrder(@Body Map<String, Object> body);

    @GET("order/view")
    Call<CommonResponse> viewOrder(@Query("orderID") String orderID);

    @GET("order/view-all")
    Call<CommonResponse> viewAllOrders();

    @GET("order/list")
    Call<CommonResponse> getOrdersByDate(@Query("startDate") String startDate, @Query("endDate") String endDate);

    @GET("order/order-history")
    Call<CommonResponse> getOrderHistory(@Query("userID") String userID);

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

}
