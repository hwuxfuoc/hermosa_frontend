package com.example.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.api.ApiClient;
import com.example.demo.api.ApiService;
import com.example.demo.fragment.FragmentHome;
import com.example.demo.models.AuthResponse;
import com.example.demo.models.CommonResponse;
import com.example.demo.utils.SessionManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.messaging.FirebaseMessaging;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

// IMPORT ĐÚNG 100% – KHÔNG BAO GIỜ LỖI
import retrofit2.Call;
import retrofit2.Callback;        // DÙNG CHO RETROFIT (login email)
import retrofit2.Response;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private ImageView imgTogglePassword;
    private Button buttonLogin;
    private MaterialButton buttonGoogleLogin, buttonFacebookLogin;
    private TextView textViewRegister, textViewForgotPassword;
    private CheckBox checkBoxRemember;
    private ApiService apiService;
    private SharedPreferences prefs;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        setContentView(R.layout.activity_login);

        initViews();
        initGoogleSignIn();
        initFacebookLogin();

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
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initFacebookLogin() {
        callbackManager = CallbackManager.Factory.create();
    }

    private void setupClickListeners() {
        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        buttonLogin.setOnClickListener(v -> login());
        textViewRegister.setOnClickListener(v -> startActivity(new Intent(this, ActivityRegister.class)));
        textViewForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ActivityForgotPassword.class)));

        buttonGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });

        buttonFacebookLogin.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        });

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                loginWithFacebook(accessToken);
            }
            @Override public void onCancel() {
                Toast.makeText(ActivityLogin.this, "Hủy đăng nhập Facebook", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(FacebookException error) {
                Toast.makeText(ActivityLogin.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            task.addOnSuccessListener(account -> {
                String idToken = account.getIdToken();
                if (idToken != null) {
                    loginWithGoogle(idToken);
                } else {
                    Toast.makeText(this, "Không lấy được idToken", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Google login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loginWithGoogle(String idToken) {
        apiService.googleLogin(idToken).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                handleAuthResponse(response, "Google");
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(ActivityLogin.this, "Lỗi mạng Google", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithFacebook(String accessToken) {
        apiService.facebookLogin(accessToken).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                handleAuthResponse(response, "Facebook");
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(ActivityLogin.this, "Lỗi mạng Facebook", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAuthResponse(Response<AuthResponse> response, String provider) {
        if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
            saveUserAndGoToMain(response.body());
            Toast.makeText(this, "Đăng nhập " + provider + " thành công!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Đăng nhập " + provider + " thất bại", Toast.LENGTH_SHORT).show();
        }
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

            if (!TextUtils.isEmpty(user.getName())) {
                name = user.getName().trim();
            } else if (!TextUtils.isEmpty(email)) {
                name = email.split("@")[0];
            }

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