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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demo.adapters.CheckboxAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.CheckboxItem;
import com.example.demo.models.CommonResponse;
import com.example.demo.models.Product;
import com.example.demo.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddToCartBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT = "product";
    private Product product;
    private int quantity = 1;
    private String selectedSize = "medium"; // backend: small, medium, large
    private final List<String> selectedToppings = new ArrayList<>();
    private String note = ""; // có thể dùng sau

    // === TẠO INSTANCE ===
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
        if (product == null) {
            Log.e("CART_BS", "LỖI: Product null khi mở BottomSheet!");
            Toast.makeText(getContext(), "Lỗi sản phẩm", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        Log.d("CART_BS", "Nhận product: " + product.getName() + " | ID: " + product.getProductID());
        setCancelable(true);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "cake";

        int layoutId;
        if ("drink".equals(category)) {
            layoutId = R.layout.layout_add_item_drink;
        } else if ("food".equals(category)) {
            layoutId = R.layout.layout_add_item_food;
        } else {
            layoutId = R.layout.layout_add_item_cake;
        }

        View view = inflater.inflate(layoutId, container, false);

        // === ÁNH XẠ UI ===
        TextView tvTitle = view.findViewById(R.id.text_title_edit);
        ImageView imgProduct = view.findViewById(R.id.image_product_cart);
        TextView tvName = view.findViewById(R.id.text_name_cart);
        TextView tvPrice = view.findViewById(R.id.text_price_cart);
        TextView tvQuantity = view.findViewById(R.id.text_quantity);
        ImageButton btnMinus = view.findViewById(R.id.button_minus);
        ImageButton btnPlus = view.findViewById(R.id.button_plus);
        RecyclerView rvOptions = view.findViewById(R.id.recycler_checkboxes);
        Button btnAddToCart = view.findViewById(R.id.button_add_to_cart);

        // === HIỂN THỊ SẢN PHẨM ===
        tvTitle.setText("Thêm " + product.getName());
        /*if (product.getImageResId() != 0) {
            imgProduct.setImageResource(product.getImageResId());
        }*/
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food)
                .into(imgProduct);

        CardView cardBackground = view.findViewById(R.id.item_background);
        if (cardBackground != null) {
            cardBackground.setCardBackgroundColor(product.getColor());
        }
        tvName.setText(product.getName());

        // DÙNG getPriceLong() → trả int
        int priceValue = (int) product.getPriceLong(); // ÉP KIỂU long → int
        tvPrice.setText(String.format("%,d đ", priceValue));

        tvQuantity.setText(String.valueOf(quantity));

        // === DANH SÁCH CHECKBOX ===
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

        // === NÚT THÊM VÀO GIỎ ===
        btnAddToCart.setOnClickListener(v -> {
            // Lấy size/topping
            selectedSize = "medium";
            selectedToppings.clear();
            for (CheckboxItem item : options) {
                if (item.isChecked()) {
                    String label = item.getLabel();
                    if (label.startsWith("Size")) {
                        String sizeShort = label.substring(5).trim().toLowerCase();
                        if ("s".equals(sizeShort)) {
                            selectedSize = "small";
                        } else if ("m".equals(sizeShort)) {
                            selectedSize = "medium";
                        } else if ("l".equals(sizeShort)) {
                            selectedSize = "large";
                        }
                    } else {
                        selectedToppings.add(label);
                    }
                }
            }

            addToCartViaApi();
        });

        return view;
    }

    // === GỌI API /cart/add ===
    private void addToCartViaApi() {
        String userID = SessionManager.getUserID(requireContext());
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        String productID = product.getProductID();
        if (productID == null || productID.isEmpty() || "UNKNOWN".equals(productID)) {
            Log.e("CART_ADD", "LỖI: productID không hợp lệ! ID = " + productID + " | Name: " + product.getName());
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy mã sản phẩm!", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userID", userID);
        body.put("productID", productID);
        body.put("quantity", quantity);
        body.put("size", selectedSize);
        body.put("topping", selectedToppings);
        body.put("note", note);

        Log.d("CART_ADD", "GỌI /cart/add → ProductID: " + productID + ", Size: " + selectedSize);

        ApiClient.getClient().create(ApiService.class)
                .addToCart(body)
                .enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                        Log.d("CART_ADD", "RESPONSE Code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            CommonResponse resp = response.body();
                            Log.d("CART_ADD", "Status: " + resp.getStatus());

                            if ("Success".equals(resp.getStatus())) {
                                Toast.makeText(getContext(), product.getName() + " đã thêm vào giỏ!", Toast.LENGTH_SHORT).show();
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).reloadCartFragment();
                                }
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "Lỗi: " + resp.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi server", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {
                        Log.e("CART_ADD", "Lỗi mạng: " + t.getMessage());
                        Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
