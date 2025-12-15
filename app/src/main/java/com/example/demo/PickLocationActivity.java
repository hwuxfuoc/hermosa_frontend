package com.example.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.adapters.SuggestionAdapter;
import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.MapboxSuggestionResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickLocationActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private EditText etSearchAddress;
    private MaterialButton btnSaveAddress;
    private ImageView btnBack;
    private FloatingActionButton fabMyLocation;
    private Button btnSearch;
    private RecyclerView rvSuggestions;

    private SuggestionAdapter suggestionAdapter;
    private Handler handler = new Handler();
    private Runnable searchRunnable;

    private String finalAddressName = "";
    private String finalStreet = "";
    private String finalWard = "";
    private String finalDistrict = "";
    private String finalCity = "";

    private boolean isUserInteracting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map_picker);

        initViews();
        setupMap();
        setupSearchLogic();
        setupClickEvents();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        etSearchAddress = findViewById(R.id.etSearchAddress);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);
        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.button);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        rvSuggestions = findViewById(R.id.rvSuggestions);

        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new SuggestionAdapter(new ArrayList<>(), this::onSuggestionSelected);
        rvSuggestions.setAdapter(suggestionAdapter);
    }

    private void setupMap() {
        if (mapView != null) {
            mapboxMap = mapView.getMapboxMap();
            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS);

            GesturesPlugin gesturesPlugin = GesturesUtils.getGestures(mapView);
            gesturesPlugin.addOnMoveListener(new OnMoveListener() {
                @Override
                public void onMoveBegin(@NonNull MoveGestureDetector detector) {
                    // Khi người dùng di chuyển map -> ẩn gợi ý và bàn phím
                    rvSuggestions.setVisibility(View.GONE);
                    hideKeyboard();
                }

                @Override
                public boolean onMove(@NonNull MoveGestureDetector detector) {
                    return false;
                }

                @Override
                public void onMoveEnd(@NonNull MoveGestureDetector detector) {
                    // Khi dừng di chuyển -> Lấy tọa độ tâm và reverse geocoding
                    if (mapboxMap != null) {
                        Point center = mapboxMap.getCameraState().getCenter();
                        if (center != null) {
                            getAddressFromCoordinates(center.latitude(), center.longitude());
                        }
                    }
                }
            });
        }
    }

    private void setupSearchLogic() {
        etSearchAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Chỉ gọi API khi người dùng đang tương tác nhập liệu
                if (!isUserInteracting) return;

                // Debounce: Đợi 500ms sau khi ngừng gõ mới gọi API
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        callBackendSuggestionApi(query);
                    } else {
                        rvSuggestions.setVisibility(View.GONE);
                    }
                };
                handler.postDelayed(searchRunnable, 500);
            }
        });

        // Đánh dấu người dùng đang tương tác
        etSearchAddress.setOnFocusChangeListener((v, hasFocus) -> isUserInteracting = hasFocus);
        etSearchAddress.setOnClickListener(v -> isUserInteracting = true);
    }

    private void callBackendSuggestionApi(String query) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getSuggestion(query).enqueue(new Callback<MapboxSuggestionResponse>() {
            @Override
            public void onResponse(Call<MapboxSuggestionResponse> call, Response<MapboxSuggestionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MapboxSuggestionResponse.SuggestionItem> list = response.body().getData();
                    if (list != null && !list.isEmpty()) {
                        rvSuggestions.setVisibility(View.VISIBLE);
                        suggestionAdapter.updateData(list);
                    } else {
                        rvSuggestions.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<MapboxSuggestionResponse> call, Throwable t) {
                Log.e("PickLocation", "Lỗi API: " + t.getMessage());
            }
        });
    }

    private void searchAndMoveToFirstResult(String query) {
        hideKeyboard();
        Toast.makeText(this, "Đang tìm kiếm...", Toast.LENGTH_SHORT).show();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getSuggestion(query).enqueue(new Callback<MapboxSuggestionResponse>() {
            @Override
            public void onResponse(Call<MapboxSuggestionResponse> call, Response<MapboxSuggestionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MapboxSuggestionResponse.SuggestionItem> list = response.body().getData();
                    if (list != null && !list.isEmpty()) {
                        // Chọn kết quả đầu tiên
                        MapboxSuggestionResponse.SuggestionItem topResult = list.get(0);
                        onSuggestionSelected(topResult);
                    } else {
                        Toast.makeText(PickLocationActivity.this, "Không tìm thấy địa điểm này", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PickLocationActivity.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<MapboxSuggestionResponse> call, Throwable t) {
                Toast.makeText(PickLocationActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- SỬA LẠI HÀM NÀY ĐỂ TRÁNH NULL ---
    private void onSuggestionSelected(MapboxSuggestionResponse.SuggestionItem item) {
        isUserInteracting = false;
        rvSuggestions.setVisibility(View.GONE);
        hideKeyboard();

        // Ưu tiên tên tiếng Việt, nếu không có lấy tên gốc
        String displayName = item.place_name_vi != null ? item.place_name_vi : item.place_name;

        // Cập nhật UI
        etSearchAddress.setText(displayName);

        // Lưu dữ liệu vào biến toàn cục (kiểm tra null an toàn)
        finalAddressName = displayName != null ? displayName : "";
        finalStreet = item.street != null ? item.street : finalAddressName; // Nếu không có tên đường, lấy full name
        finalWard = item.ward != null ? item.ward : "";
        finalDistrict = item.district != null ? item.district : "";
        finalCity = item.city != null ? item.city : "";

        // Di chuyển camera đến vị trí chọn
        if (mapboxMap != null) {
            CameraOptions options = new CameraOptions.Builder()
                    .center(Point.fromLngLat(item.lon, item.lat))
                    .zoom(16.0)
                    .build();
            mapboxMap.setCamera(options);
        }
    }

    // --- SỬA LẠI HÀM NÀY ĐỂ TRÁNH NULL KHI GEOCODER LỖI ---
    private void getAddressFromCoordinates(double lat, double lng) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                // Lấy tối đa 1 địa chỉ
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    // Lấy dòng địa chỉ đầy đủ (thường là chính xác nhất)
                    String fullAddr = addr.getAddressLine(0);

                    runOnUiThread(() -> {
                        isUserInteracting = false;

                        // Cập nhật UI
                        if (fullAddr != null) {
                            etSearchAddress.setText(fullAddr);
                            finalAddressName = fullAddr;
                        } else {
                            etSearchAddress.setText("");
                            finalAddressName = "";
                        }

                        // Parse các thành phần địa chỉ
                        if (addr.getThoroughfare() != null) {
                            finalStreet = addr.getThoroughfare();
                            if (addr.getSubThoroughfare() != null) {
                                finalStreet = addr.getSubThoroughfare() + " " + finalStreet;
                            }
                        } else {
                            // Nếu không có tên đường riêng, dùng địa chỉ full
                            finalStreet = finalAddressName;
                        }

                        // Kiểm tra null cho các trường khác
                        finalWard = addr.getSubLocality() != null ? addr.getSubLocality() : "";
                        finalDistrict = addr.getSubAdminArea() != null ? addr.getSubAdminArea() : "";
                        finalCity = addr.getAdminArea() != null ? addr.getAdminArea() : "";
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupClickEvents() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnSaveAddress != null) btnSaveAddress.setOnClickListener(v -> {

            String currentText = etSearchAddress.getText().toString().trim();

            if (finalAddressName == null || finalAddressName.isEmpty()) {
                finalAddressName = currentText;
            }
            if (finalStreet == null || finalStreet.isEmpty()) {
                finalStreet = currentText;
            }

            if (finalAddressName == null || finalAddressName.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn một địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent result = new Intent();
            result.putExtra("fullAddress", finalAddressName);
            result.putExtra("selectedAddress", finalAddressName);

            result.putExtra("street", finalStreet != null ? finalStreet : "");
            result.putExtra("ward", finalWard != null ? finalWard : "");
            result.putExtra("district", finalDistrict != null ? finalDistrict : "");
            result.putExtra("city", finalCity != null ? finalCity : "");

            setResult(RESULT_OK, result);
            finish();
        });

        if (fabMyLocation != null) fabMyLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();
                moveToUserLocation();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
            }
        });

        if (btnSearch != null) btnSearch.setOnClickListener(v -> {
            String query = etSearchAddress.getText().toString().trim();
            if(!query.isEmpty()) {
                searchAndMoveToFirstResult(query);
            }
        });
    }

    private void moveToUserLocation() {
        if (mapView == null) return;
        LocationComponentPlugin locationPlugin = LocationComponentUtils.getLocationComponent(mapView);
        if (!locationPlugin.getEnabled()) {
            locationPlugin.setEnabled(true);
        }

        OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
            @Override
            public void onIndicatorPositionChanged(@NonNull Point point) {
                mapView.getMapboxMap().setCamera(
                        new CameraOptions.Builder()
                                .center(point)
                                .zoom(15.0)
                                .build()
                );
                locationPlugin.removeOnIndicatorPositionChangedListener(this);
                getAddressFromCoordinates(point.latitude(), point.longitude());
            }
        };

        locationPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}