# hermosa_frontend

## 1. Prerequisites (Yêu cầu hệ thống)
Để chạy được ứng dụng này, bạn cần:
* **Android Studio:** Phiên bản mới nhất (Recommended: Koala/Ladybug...).
* **JDK:** Java Development Kit 17 hoặc mới hơn.
* **Internet:** Bắt buộc (để kết nối với Server Online).

## 2. Server Information (Thông tin Server)
Backend đã được deploy và đang chạy online, không cần cài đặt local.
* **Base URL:** `http://34.151.64.207/`
* **Status:** Online (Active).

## 3. Installation & Run (Cài đặt và Chạy)
Do server đã online, bạn chỉ cần thực hiện các bước sau để chạy App:

1.  **Clone Project:**
    ```bash
    git clone https://github.com/hwuxfuoc/hermosa_frontend.git
    cd hermosa_frontend/
    git checkout demo-fix
    git pull
    ```
    
2.  **Open in Android Studio:**
    * Mở Android Studio -> File -> Open -> Chọn thư mục vừa clone.
3.  **Check Configuration (Quan trọng):**
    * Mở file: `com/example/demo/api/ApiClient.java`.
    * Đảm bảo `BASE_URL` đang trỏ về: `http://34.151.64.207/`.
4.  **Sync & Run:**
    * Nhấn **Sync Project with Gradle Files**.
    * Chọn máy ảo (Emulator) hoặc thiết bị thật.
    * Nhấn nút **Run** (Play icon).

## 4. Sử dụng ứng dụng
