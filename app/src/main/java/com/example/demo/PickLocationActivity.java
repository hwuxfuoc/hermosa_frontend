package com.example.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;

    public class PickLocationActivity extends AppCompatActivity {

        private MapView mapView;
        private MapboxMap mapboxMap;
        private EditText etSearchAddress; // Thanh tìm kiếm
        private MaterialButton btnSaveAddress; // Nút Lưu dưới đáy
        private ImageView btnBack;
        private FloatingActionButton fabMyLocation;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.fragment_map_picker); // Tên file XML của bạn

            // 1. Ánh xạ View
            mapView = findViewById(R.id.mapView);
            etSearchAddress = findViewById(R.id.etSearchAddress);
            btnSaveAddress = findViewById(R.id.btnSaveAddress);
            btnBack = findViewById(R.id.btnBack);
            Button btnSearch = findViewById(R.id.button); // Nút Tìm trong thanh search

            // 2. Cấu hình Mapbox
            if (mapView != null) {
                mapboxMap = mapView.getMapboxMap();
                // Load giao diện bản đồ đường phố
                mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, style -> {
                    // Map đã load xong
                });

                // Set vị trí mặc định (Ví dụ: TP.HCM)
                CameraOptions cameraOptions = new CameraOptions.Builder()
                        .center(Point.fromLngLat(106.6297, 10.8231))
                        .zoom(14.0)
                        .build();
                mapboxMap.setCamera(cameraOptions);
            }

            // 3. Sự kiện nút Back
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
            if (fabMyLocation != null) {
                fabMyLocation.setOnClickListener(v -> {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Code để camera zoom về vị trí (cần xử lý thêm Location provider nếu muốn chính xác tuyệt đối)
                        Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();
                        // Để đơn giản, Mapbox tự hiển thị puck, ta chỉ cần bật nó lên
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
                    }
                });
            }
            // 4. Sự kiện nút Lưu
            if (btnSaveAddress != null) {
                btnSaveAddress.setOnClickListener(v -> {
                    String addressResult = "";

                    // Ưu tiên 1: Lấy text người dùng nhập ở ô tìm kiếm
                    if (etSearchAddress != null && !etSearchAddress.getText().toString().isEmpty()) {
                        addressResult = etSearchAddress.getText().toString();
                    }
                    // Ưu tiên 2: Lấy tọa độ tâm bản đồ (nơi đặt cái ghim)
                    else if (mapboxMap != null) {
                        Point center = mapboxMap.getCameraState().getCenter();
                        // Vì chưa có API chuyển đổi tên đường, ta tạm trả về tọa độ
                        addressResult = String.format("Vị trí ghim: %.4f, %.4f", center.latitude(), center.longitude());
                    }

                    if (addressResult.isEmpty()) {
                        Toast.makeText(this, "Vui lòng chọn hoặc nhập địa chỉ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Trả kết quả về màn hình trước
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedAddress", addressResult);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }

            // 5. Sự kiện nút Tìm (Giả lập)
            if (btnSearch != null) {
                btnSearch.setOnClickListener(v -> {
                    Toast.makeText(this, "Chức năng tìm kiếm cần API Search", Toast.LENGTH_SHORT).show();
                    // Ở đây bạn có thể code thêm logic gọi API Mapbox Search nếu muốn
                });
            }
        }
        private void enableLocationComponent(Style style) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Xin quyền nếu chưa có
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
                return;
            }

            // Lấy plugin Location
            LocationComponentPlugin locationPlugin = LocationComponentUtils.getLocationComponent(mapView);

            // Kích hoạt
            locationPlugin.setEnabled(true);

            // Tùy chỉnh hiển thị (Puck mặc định là chấm xanh)
            // Bạn có thể đổi icon nếu muốn:
            // locationPlugin.setLocationPuck(new LocationPuck2D(null, ContextCompat.getDrawable(this, R.drawable.ic_my_location_puck)));

            // Chế độ camera đi theo người dùng (Optional)
            locationPlugin.setPulsingEnabled(true);
        }

        // Xử lý kết quả xin quyền
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == 111 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mapboxMap != null) {
                    mapboxMap.getStyle(this::enableLocationComponent);
                }
            }
        }
        // Các hàm vòng đời bắt buộc của Mapbox

        @Override
        protected void onStart() {
            super.onStart();
            if (mapView != null) mapView.onStart();
        }

        @Override
        protected void onStop() {
            super.onStop();
            if (mapView != null) mapView.onStop();
        }

        @Override
        public void onLowMemory() {
            super.onLowMemory();
            if (mapView != null) mapView.onLowMemory();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (mapView != null) mapView.onDestroy();
        }
    }