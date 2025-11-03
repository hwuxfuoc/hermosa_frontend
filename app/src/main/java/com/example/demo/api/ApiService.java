package com.example.demo.api;

import com.example.demo.models.CartItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;
public interface ApiService {

    //cart
    @GET("cart")
    Call<List<CartItem>>getCartItems();

    @POST("cart")
    Call<CartItem>addToCart(@Body CartItem item);

    //thay duong dan
    @PUT("cart/{id}")
    Call<CartItem> updateCartItem(@Path("id") int id, @Body CartItem item);

}
