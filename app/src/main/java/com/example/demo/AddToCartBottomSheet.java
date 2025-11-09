/*
package com.example.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;  // ĐÚNG RỒI!

import com.example.demo.models.Product;

public class AddToCartBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT = "product";
    private Product product;

    public static AddToCartBottomSheet newInstance(Product product) {
        AddToCartBottomSheet fragment = new AddToCartBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
        setCancelable(true);

        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId;

        // FIX: XỬ LÝ NULL AN TOÀN
        String category = product.getCategory();
        if (category == null) {
            category = "cake"; // mặc định
        }

        switch (category.toLowerCase()) {
            case "drink":
                layoutId = R.layout.layout_add_item_drink;
                break;
            case "food":
                layoutId = R.layout.layout_add_item_food;
                break;
            default:
                layoutId = R.layout.layout_add_item_cake;
                break;
        }

        View view = inflater.inflate(layoutId, container, false);

        TextView tvTitle = view.findViewById(R.id.text_title_edit);
        ImageView imgProduct = view.findViewById(R.id.image_product_cart);
        TextView tvName = view.findViewById(R.id.text_name_cart);
        TextView tvPrice = view.findViewById(R.id.text_price_cart);
        Button btnAdd = view.findViewById(R.id.button_add_to_cart);

        tvTitle.setText("Thêm " + product.getName());
        imgProduct.setImageResource(product.getImageResId());
        tvName.setText(product.getName());
        tvPrice.setText(product.getPrice());

        btnAdd.setOnClickListener(v -> {
            addToCart(product);
            Toast.makeText(getContext(), product.getName() + " đã thêm vào giỏ!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    private void addToCart(Product newProduct) {
        for (Product p : ProductData.cartList) {
            if (p.getName().equals(newProduct.getName())) {
                p.setQuantity(p.getQuantity() + 1);
                return;
            }
        }
        newProduct.setQuantity(1);
        ProductData.cartList.add(newProduct);
    }
}*/
// AddToCartBottomSheet.java – ĐÃ SỬA HOÀN CHỈNH
// AddToCartBottomSheet.java – ĐÃ SỬA HOÀN CHỈNH (DÙNG NÚT TRONG HÌNH)
// AddToCartBottomSheet.java – ĐÃ SỬA HOÀN CHỈNH (DÙNG NÚT TRONG HÌNH)
// AddToCartBottomSheet.java – ĐÃ SỬA HOÀN CHỈNH (DÙNG NÚT TRONG HÌNH)
package com.example.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demo.adapters.CheckboxAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CheckboxItem;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.Product;
import com.example.demo.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddToCartBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT = "product";
    private Product product;
    private int quantity = 1;
    private String selectedSize = "medium";
    private List<String> selectedToppings = new ArrayList<>();
    private String note = "";

    public static AddToCartBottomSheet newInstance(Product product) {
        AddToCartBottomSheet fragment = new AddToCartBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
        setCancelable(true);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "cake";

        // Tương thích Java 11: Dùng switch statement
        int layoutId;
        switch (category) {
            case "drink":
                layoutId = R.layout.layout_add_item_drink;
                break;
            case "food":
                layoutId = R.layout.layout_add_item_food;
                break;
            default:
                layoutId = R.layout.layout_add_item_cake;
                break;
        }

        View view = inflater.inflate(layoutId, container, false);

        // === UI ===
        TextView tvTitle = view.findViewById(R.id.text_title_edit);
        ImageView imgProduct = view.findViewById(R.id.image_product_cart);
        TextView tvName = view.findViewById(R.id.text_name_cart);
        TextView tvPrice = view.findViewById(R.id.text_price_cart);
        TextView tvQuantity = view.findViewById(R.id.text_quantity);
        ImageButton btnMinus = view.findViewById(R.id.button_minus);
        ImageButton btnPlus = view.findViewById(R.id.button_plus);
        RecyclerView rvOptions = view.findViewById(R.id.recycler_checkboxes);
        Button btnAddToCart = view.findViewById(R.id.button_add_to_cart); // NÚT TRONG HÌNH

        // === HIỂN THỊ THÔNG TIN SẢN PHẨM ===
        tvTitle.setText("Thêm " + product.getName());
        imgProduct.setImageResource(product.getImageResId());
        tvName.setText(product.getName());

        // FIX LỖI: getPrice() trả String → parse thành int
        String priceStr = product.getPrice(); // ví dụ: "85.000" hoặc "85000"
        int priceValue = 0;
        try {
            priceValue = Integer.parseInt(priceStr.replaceAll("[^0-9]", "")); // loại bỏ dấu chấm, phẩy, chữ
        } catch (Exception e) {
            priceValue = 0; // nếu lỗi → hiển thị 0
        }
        tvPrice.setText(String.format("%,d VND / pc", priceValue));

        tvQuantity.setText(String.valueOf(quantity));

        // === CHECKBOX OPTIONS (Size + Topping) ===
        List<CheckboxItem> options = new ArrayList<>();
        options.add(new CheckboxItem("Size S", false));
        options.add(new CheckboxItem("Size M", true));
        options.add(new CheckboxItem("Size L", false));
        if ("drink".equals(category)) {
            options.add(new CheckboxItem("Trân châu", false));
            options.add(new CheckboxItem("Pudding", false));
            options.add(new CheckboxItem("Thạch", false));
        }

        CheckboxAdapter adapter = new CheckboxAdapter(options);
        rvOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOptions.setAdapter(adapter);

        // === TĂNG / GIẢM SỐ LƯỢNG ===
        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // === NÚT "Add to Cart" – GỌI API BACKEND ===
        btnAddToCart.setOnClickListener(v -> {
            // Lấy size/topping đã chọn
            selectedSize = "medium"; // mặc định
            selectedToppings.clear();
            for (CheckboxItem item : options) {
                if (item.isChecked()) {
                    if (item.getLabel().startsWith("Size")) {
                        selectedSize = item.getLabel().substring(5).trim().toLowerCase();
                    } else {
                        selectedToppings.add(item.getLabel());
                    }
                }
            }

            // Gọi API /cart/add
            addToCartViaApi();
        });

        return view;
    }

    // === GỌI API THÊM VÀO GIỎ (BACKEND) ===
    /*private void addToCartViaApi() {
        String userID = SessionManager.getUserID(requireContext());
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("productID", product.getProductID());
        body.put("quantity", quantity);
        body.put("size", selectedSize);
        body.put("topping", selectedToppings);
        body.put("note", note);

        ApiClient.getClient().create(ApiService.class)
                .addToCart(body)
                .enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> res) {
                        if (res.isSuccessful()) {
                            Toast.makeText(getContext(), product.getName() + " đã thêm vào giỏ!", Toast.LENGTH_SHORT).show();
                            // GỌI RELOAD GIỎ HÀNG TỪ MainActivity
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).reloadCartFragment();
                            }
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Lỗi thêm vào giỏ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }*/
    // === GỌI API THÊM VÀO GIỎ (BACKEND) ===
    private void addToCartViaApi() {
        String userID = SessionManager.getUserID(requireContext());
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("productID", product.getProductID());
        body.put("quantity", quantity);
        body.put("size", selectedSize);
        body.put("topping", selectedToppings);
        body.put("note", note);

        // LOG REQUEST
        Log.d("CART_ADD", "=== GỌI /cart/add ===");
        Log.d("CART_ADD", "UserID: " + userID);
        Log.d("CART_ADD", "ProductID: " + product.getProductID());
        Log.d("CART_ADD", "Quantity: " + quantity);
        Log.d("CART_ADD", "Size: " + selectedSize);
        Log.d("CART_ADD", "Topping: " + selectedToppings);
        Log.d("CART_ADD", "Note: " + note);

        ApiClient.getClient().create(ApiService.class)
                .addToCart(body)
                .enqueue(new Callback<CommonResponse>() {  // DÙNG CommonResponse
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> res) {
                        Log.d("CART_ADD", "=== RESPONSE /cart/add ===");
                        Log.d("CART_ADD", "Code: " + res.code());

                        if (res.isSuccessful() && res.body() != null) {
                            CommonResponse resp = res.body();
                            Log.d("CART_ADD", "Status: " + resp.getStatus());
                            Log.d("CART_ADD", "Message: " + resp.getMessage());

                            // LOG RAW BODY (để xem backend trả gì)
                            try {
                                String raw = new Gson().toJson(resp);
                                Log.d("CART_ADD", "Raw JSON: " + raw);
                            } catch (Exception e) {
                                Log.e("CART_ADD", "Không parse được JSON");
                            }

                            Toast.makeText(getContext(), product.getName() + " đã thêm vào giỏ!", Toast.LENGTH_SHORT).show();

                            // RELOAD GIỎ HÀNG (dùng CartResponse ở FragmentCart)
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).reloadCartFragment();
                            }
                            dismiss();

                        } else {
                            Log.e("CART_ADD", "Lỗi: " + res.code());
                            if (res.errorBody() != null) {
                                try {
                                    Log.e("CART_ADD", "Error: " + res.errorBody().string());
                                } catch (Exception e) { /* ignore */ }
                            }
                            Toast.makeText(getContext(), "Lỗi thêm vào giỏ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {
                        Log.e("CART_ADD", "Lỗi mạng: " + t.getMessage(), t);
                        Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
