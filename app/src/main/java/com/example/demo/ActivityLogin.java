/*
package com.example.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.AuthResponse;
import com.example.demo.models.CommonResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;


import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private ImageView imgTogglePassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private TextView textViewForgotPassword; // ← THÊM DÒNG NÀY
    private CheckBox checkBoxRemember;
    private ApiService apiService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.edit_text_username);
        editTextPassword = findViewById(R.id.edit_text_password);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        buttonLogin = findViewById(R.id.button_login);
        textViewRegister = findViewById(R.id.text_view_register);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password); // ← THÊM DÒNG NÀY
        checkBoxRemember = findViewById(R.id.checkBoxRemember);

        apiService = ApiClient.getClient().create(ApiService.class);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Load nhớ đăng nhập
        if (prefs.getBoolean("REMEMBER_ME", false)) {
            editTextEmail.setText(prefs.getString("EMAIL", ""));
            editTextPassword.setText(prefs.getString("PASSWORD", ""));
            checkBoxRemember.setChecked(true);
        }

        // Hiện/ẩn mật khẩu
        imgTogglePassword.setOnClickListener(v -> {
            if (editTextPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                imgTogglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
        });

        buttonLogin.setOnClickListener(v -> login());
        textViewRegister.setOnClickListener(v -> startActivity(new Intent(this, ActivityRegister.class)));
        textViewForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ActivityForgotPassword.class)));
    }
    private void getFCMTokenAndSendToServer(String userId) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("FCM", "Lấy token thất bại", task.getException());
                return;
            }

            // 1. Lấy Token
            String token = task.getResult();
            Log.d("FCM", "Token: " + token);

            // 2. Gửi lên Server (Bạn cần thêm API này vào ApiService)
            sendTokenToBackend(userId, token);
        });
    }
    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("946081988458-c3h39d7hgefv3pdhf7d6qdja81avjjue.apps.googleusercontent.com")
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initFacebookLogin() {
        facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult);
            }

            @Override
            public void onCancel() {
                Toast.makeText(ActivityLogin.this, "Hủy đăng nhập Facebook", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(ActivityLogin.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendTokenToBackend(String userId, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("userID", userId);
        body.put("fcmToken", token);
        // body.put("os", "Android"); // Nếu backend cần biết hệ điều hành

        // Gọi API (Giả sử bạn đã thêm hàm saveFcmToken vào ApiService)
        apiService.saveFcmToken(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                Log.d("FCM", "Gửi token lên server thành công");
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e("FCM", "Lỗi gửi token: " + t.getMessage());
            }
        });
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Nhập mật khẩu");
            return;
        }

        buttonLogin.setEnabled(false);
        buttonLogin.setText("Đang đăng nhập...");

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Log.d("LOGIN", "Gửi: email=" + email);

        // DÙNG .login() – endpoint mới
        apiService.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Đăng nhập");

                Log.d("LOGIN", "Code: " + response.code());
                if (response.body() != null) {
                    Log.d("LOGIN", "Status: " + response.body().getStatus());
                    Log.d("LOGIN", "UserID: " + (response.body().getData() != null ? response.body().getData().getUserID() : "null"));
                }

                if (response.isSuccessful() && response.body() != null) {
                    if ("Success".equals(response.body().getStatus())) {
                        AuthResponse.User user = response.body().getData();
                        String userId = user != null ? user.getUserID() : "unknown";

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("EMAIL", email);
                        editor.putString("USER_ID", userId);
                        editor.putBoolean("IS_LOGGED_IN", true);
                        if (checkBoxRemember.isChecked()) {
                            editor.putString("PASSWORD", password);
                            editor.putBoolean("REMEMBER_ME", true);
                        }
                        editor.apply();
                        getFCMTokenAndSendToServer(userId);

                        Toast.makeText(ActivityLogin.this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ActivityLogin.this, MainActivity.class));
                        finish();
                        return;
                    }
                }

                Toast.makeText(ActivityLogin.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Đăng nhập");
                Log.e("LOGIN", "Lỗi: " + t.getMessage());
                Toast.makeText(ActivityLogin.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}*/
package com.example.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.models.AuthResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private ImageView imgTogglePassword;
    private Button buttonLogin, buttonGoogleLogin, buttonFacebookLogin;
    private TextView textViewRegister, textViewForgotPassword;
    private CheckBox checkBoxRemember;
    private ApiService apiService;
    private SharedPreferences prefs;

    // Google
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    // Facebook
    private CallbackManager facebookCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initGoogleSignIn();
        initFacebookLogin();

        // Load remember me
        SessionManager.loadRememberMe(this, editTextEmail, editTextPassword, checkBoxRemember);

        setupClickListeners();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.edit_text_username);
        editTextPassword = findViewById(R.id.edit_text_password);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        buttonLogin = findViewById(R.id.button_login);
        buttonGoogleLogin = findViewById(R.id.button_google_login);
        buttonFacebookLogin = findViewById(R.id.button_facebook_login);
        textViewRegister = findViewById(R.id.text_view_register);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        checkBoxRemember = findViewById(R.id.checkBoxRemember);

        apiService = ApiClient.getClient().create(ApiService.class);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("946081988458-c3h39d7hgefv3pdhf7d6qdja81avjjue.apps.googleusercontent.com")
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initFacebookLogin() {
        facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult);
            }

            @Override
            public void onCancel() {
                Toast.makeText(ActivityLogin.this, "Hủy đăng nhập Facebook", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(ActivityLogin.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        buttonLogin.setOnClickListener(v -> login());
        textViewRegister.setOnClickListener(v -> startActivity(new Intent(this, ActivityRegister.class)));
        textViewForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ActivityForgotPassword.class)));

        buttonGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        buttonFacebookLogin.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        });
    }

    private void togglePasswordVisibility() {
        if (editTextPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            imgTogglePassword.setImageResource(R.drawable.ic_eye_open);
        } else {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imgTogglePassword.setImageResource(R.drawable.ic_eye_closed);
        }
        editTextPassword.setSelection(editTextPassword.getText().length());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void handleFacebookAccessToken(LoginResult loginResult) {
        com.facebook.GraphRequest request = com.facebook.GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                (object, response) -> {
                    if (object == null) {
                        runOnUiThread(() -> Toast.makeText(this, "Lỗi lấy dữ liệu Facebook", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    try {
                        String email = object.optString("email");
                        String name = object.optString("name", "Facebook User");

                        if (TextUtils.isEmpty(email)) {
                            String id = object.optString("id", "unknown");
                            email = id + "@facebook.com";
                        }
                        if (TextUtils.isEmpty(name)) name = "Facebook User";

                        loginWithSocial(email, name, "Facebook");

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Lỗi xử lý dữ liệu Facebook", Toast.LENGTH_SHORT).show();
                            Log.e("FB_LOGIN", "Error: " + e.getMessage());
                        });
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Google
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String email = account.getEmail();
                    String name = account.getDisplayName();
                    if (TextUtils.isEmpty(email)) email = account.getId() + "@google.com";
                    if (TextUtils.isEmpty(name)) name = "Google User";
                    loginWithSocial(email, name, "Google");
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google login failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Nhập mật khẩu");
            return;
        }

        buttonLogin.setEnabled(false);
        buttonLogin.setText("Đang đăng nhập...");

        final ActivityLogin that = this;

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Đăng nhập");

                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    SessionManager.saveRememberMe(
                            that,
                            email,
                            password,
                            checkBoxRemember.isChecked()
                    );

                    saveUserAndGoToMain(response.body());
                } else {
                    Toast.makeText(that, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Đăng nhập");
                Toast.makeText(that, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithSocial(String email, String name, String provider) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("name", name);
        body.put("provider", provider);

        apiService.socialLogin(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    saveUserAndGoToMain(response.body());
                    Toast.makeText(ActivityLogin.this, "Đăng nhập " + provider + " thành công!", Toast.LENGTH_LONG).show();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(ActivityLogin.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(ActivityLogin.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ĐÃ FIX CRASH 100% TẠI ĐÂY
    private void saveUserAndGoToMain(AuthResponse authResponse) {
        try {
            AuthResponse.User user = authResponse.getData();
            if (user == null) {
                Toast.makeText(this, "Lỗi: Không nhận được thông tin người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = "unknown";
            if (!TextUtils.isEmpty(user.getUserID())) {
                userId = user.getUserID();
            } else if (!TextUtils.isEmpty(user.getId())) {
                userId = user.getId();
            }

            String email = TextUtils.isEmpty(user.getEmail()) ? "" : user.getEmail();
            String name = "Khách";

            // CHỈ DÙNG getName() VÀ getEmail() – VÌ CLASS USER CHỈ CÓ 2 FIELD NÀY!
            if (!TextUtils.isEmpty(user.getName())) {
                name = user.getName().trim();
            } else if (!TextUtils.isEmpty(email)) {
                name = email.split("@")[0];
            }

            // LƯU BẰNG SESSION MANAGER CHUẨN
            SessionManager.saveUserSession(
                    this,
                    userId,
                    name,
                    "",
                    ""
            );

            getFCMTokenAndSend(userId);

            startActivity(new Intent(this, MainActivity.class));
            finish();

        } catch (Exception e) {
            Log.e("LOGIN", "Lỗi lưu user", e);
            Toast.makeText(this, "Lỗi hệ thống", Toast.LENGTH_LONG).show();
        }
    }

    private void getFCMTokenAndSend(String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token != null && !token.isEmpty()) {
                        sendTokenToBackend(userId, token);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FCM", "Lấy FCM token thất bại", e);
                    // Không crash, chỉ log → vẫn vào MainActivity bình thường
                });
    }

    private void sendTokenToBackend(String userId, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("userID", userId);
        body.put("fcmToken", token);

        apiService.saveFcmToken(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                Log.d("FCM", "Gửi FCM token thành công");
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e("FCM", "Gửi FCM thất bại (không ảnh hưởng app): " + t.getMessage());
            }
        });
    }
}