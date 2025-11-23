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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private ImageView imgTogglePassword;
    private Button buttonLogin;
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

        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        // Init views
        editTextEmail = findViewById(R.id.edit_text_username);
        editTextPassword = findViewById(R.id.edit_text_password);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        buttonLogin = findViewById(R.id.button_login);
        textViewRegister = findViewById(R.id.text_view_register);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        checkBoxRemember = findViewById(R.id.checkBoxRemember);

        apiService = ApiClient.getClient().create(ApiService.class);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Facebook Init
        facebookCallbackManager = CallbackManager.Factory.create();

        // Load nhớ mật khẩu
        if (prefs.getBoolean("REMEMBER_ME", false)) {
            editTextEmail.setText(prefs.getString("EMAIL", ""));
            editTextPassword.setText(prefs.getString("PASSWORD", ""));
            checkBoxRemember.setChecked(true);
        }

        // Toggle hiện/ẩn mật khẩu
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

        // Click listeners
        buttonLogin.setOnClickListener(v -> login());
        textViewRegister.setOnClickListener(v -> startActivity(new Intent(this, ActivityRegister.class)));
        textViewForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ActivityForgotPassword.class)));

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.button_google_login).setOnClickListener(v -> signInWithGoogle());

        // Facebook Sign-In
        findViewById(R.id.button_facebook_login).setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(
                    ActivityLogin.this,
                    Arrays.asList("email", "public_profile")
            );
        });

        // Register Facebook callback
        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        (object, response) -> {
                            try {
                                String email = object.getString("email");
                                String name = object.getString("name");
                                loginWithSocial(email, name, "Facebook");
                            } catch (Exception e) {
                                Toast.makeText(ActivityLogin.this, "Lấy thông tin Facebook thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
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

        apiService.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Đăng nhập");

                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
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

                    Toast.makeText(ActivityLogin.this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ActivityLogin.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(ActivityLogin.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Đăng nhập");
                Toast.makeText(ActivityLogin.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook callback
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Google callback
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String email = account.getEmail();
                String name = account.getDisplayName();
                loginWithSocial(email, name, "Google");
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginWithSocial(String email, String name, String provider) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("name", name != null ? name : provider + " User");
        body.put("provider", provider);

        apiService.socialLogin(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && "Success".equals(response.body().getStatus())) {
                    AuthResponse.User user = response.body().getData();
                    String userId = user != null ? user.getUserID() : "unknown";

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("EMAIL", email);
                    editor.putString("USER_ID", userId);
                    editor.putString("USER_NAME", name);
                    editor.putBoolean("IS_LOGGED_IN", true);
                    editor.putString("LOGIN_METHOD", provider);
                    editor.apply();

                    Toast.makeText(ActivityLogin.this, "Đăng nhập với " + provider + " thành công!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ActivityLogin.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(ActivityLogin.this, "Lỗi server, thử lại sau", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(ActivityLogin.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}