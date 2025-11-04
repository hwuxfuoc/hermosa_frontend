// Đổi tên file này thành EditCakeActivity.java
package com.example.demo;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LayoutEditItemCake extends AppCompatActivity {

    // Khai báo biến ở đây
    private RecyclerView recyclerView;
    // ✅ SỬA: Biến này phải là kiểu CheckboxAdapter (lớp Adapter bạn đã tạo)
    private CheckboxAdapter adapter;
    private List<CheckboxItem> checkboxList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thiết lập layout cho Activity này
        setContentView(R.layout.layout_edit_item_cake);

        // 1. Tìm RecyclerView từ layout
        recyclerView = findViewById(R.id.recycler_checkboxes);

        // 2. Thiết lập LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Chuẩn bị dữ liệu
        checkboxList = new ArrayList<>();
        checkboxList.add(new CheckboxItem("Ít ngọt", false));
        checkboxList.add(new CheckboxItem("Thêm trái cây", true));
        checkboxList.add(new CheckboxItem("Ghi lời chúc", false));

        // 4. Tạo và gán Adapter
        // ✅ SỬA: Tạo một đối tượng từ lớp CheckboxAdapter đã có
        adapter = new CheckboxAdapter(checkboxList);
        recyclerView.setAdapter(adapter);
    }
}
