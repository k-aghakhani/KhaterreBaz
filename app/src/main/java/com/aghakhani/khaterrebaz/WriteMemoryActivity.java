package com.aghakhani.khaterrebaz;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WriteMemoryActivity extends AppCompatActivity {

    private static final String API_URL = "https://rasfam.ir/khaterrebaz/api.php";
    private static final String TAG = "KhaterreBaz";
    private static final int TIMEOUT_MS = 15000;
    private static final int MAX_RETRIES = 3;
    private static final String PREFS_NAME = "KhaterreBazPrefs";
    private static final String KEY_USER_ID = "user_id";

    private RequestQueue queue;
    private SharedPreferences sharedPreferences;
    private String userId;
    private Typeface vazirRegular;
    private Typeface vazirBold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_memory);

        // Load Vazir fonts
        vazirRegular = Typeface.createFromAsset(getAssets(), "fonts/Vazir-Regular.ttf");
        vazirBold = Typeface.createFromAsset(getAssets(), "fonts/Vazir-Bold.ttf");

        // Initialize SharedPreferences and userId
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = sharedPreferences.getString(KEY_USER_ID, null);
        if (userId == null) {
            Toast.makeText(this, "خطا: کاربر شناسایی نشد!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        TextView tvWriteMemoryTitle = findViewById(R.id.tv_write_memory_title);
        EditText etMemoryTitle = findViewById(R.id.et_memory_title);
        EditText etMemoryDescription = findViewById(R.id.et_memory_description);
        Button btnSubmitMemory = findViewById(R.id.btn_submit_memory);

        // Apply fonts
        tvWriteMemoryTitle.setTypeface(vazirBold);
        etMemoryTitle.setTypeface(vazirRegular);
        etMemoryDescription.setTypeface(vazirRegular);
        btnSubmitMemory.setTypeface(vazirRegular);

        // Initialize Volley
        queue = Volley.newRequestQueue(this);

        // Submit memory button click
        btnSubmitMemory.setOnClickListener(v -> {
            String title = etMemoryTitle.getText().toString().trim();
            String description = etMemoryDescription.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(WriteMemoryActivity.this, "لطفاً عنوان و توضیحات خاطره را وارد کنید!", Toast.LENGTH_SHORT).show();
            } else {
                submitMemory(title, description, etMemoryTitle, etMemoryDescription);
            }
        });
    }

    private void submitMemory(String title, String description, EditText etMemoryTitle, EditText etMemoryDescription) {
        StringRequest request = new StringRequest(Request.Method.POST, API_URL,
                response -> {
                    try {
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                Toast.makeText(WriteMemoryActivity.this, "خاطره با موفقیت ثبت شد!", Toast.LENGTH_SHORT).show();
                                etMemoryTitle.setText("");
                                etMemoryDescription.setText("");
                                finish(); // Close the activity and return to MainActivity
                            } else {
                                Toast.makeText(WriteMemoryActivity.this, "خطا: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(WriteMemoryActivity.this, "پاسخ نامعتبر از سرور!", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(WriteMemoryActivity.this, "خطا در پردازش پاسخ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(WriteMemoryActivity.this, "خطا در شبکه: " + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "add_memory");
                params.put("user_id", userId);
                params.put("title", title);
                params.put("description", description);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }
}