package com.aghakhani.khaterrebaz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "https://rasfam.ir/khaterrebaz/api.php";
    private static final String TAG = "KhaterreBaz";
    private static final int TIMEOUT_MS = 15000;
    private static final int MAX_RETRIES = 3;
    private static final String PREFS_NAME = "KhaterreBazPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "user_id";

    private RequestQueue queue;
    private int currentMemoryId;
    private ImageView ivMemoryImage;
    private TextView tvMemoryText;
    private TextView tvMemoryDesc;
    private TextView tvLikeCount;
    private TextView tvDislikeCount;
    private TextView tvCommentCount;
    private TextView tvCommentsList;
    private TextView tvAppTitle;
    private Button btnPreviousMemory;
    private SharedPreferences sharedPreferences;
    private String username;
    private String userId;
    private LinearLayout llCommentsContainer;

    private Typeface vazirRegular;
    private Typeface vazirBold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vazirRegular = Typeface.createFromAsset(getAssets(), "fonts/Vazir-Regular.ttf");
        vazirBold = Typeface.createFromAsset(getAssets(), "fonts/Vazir-Bold.ttf");

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        userId = sharedPreferences.getString(KEY_USER_ID, null);
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USER_ID, userId);
            editor.apply();
            Log.d(TAG, "Generated new user_id: " + userId);
        } else {
            Log.d(TAG, "Loaded existing user_id: " + userId);
        }

        username = sharedPreferences.getString(KEY_USERNAME, null);

        tvAppTitle = findViewById(R.id.tv_app_title);
        btnPreviousMemory = findViewById(R.id.btn_previous_memory);
        Button btnAnotherMemory = findViewById(R.id.btn_another_memory);
        Button btnWriteMemory = findViewById(R.id.btn_write_memory);
        ImageView ivLike = findViewById(R.id.iv_like);
        tvLikeCount = findViewById(R.id.tv_like_count);
        ImageView ivDislike = findViewById(R.id.iv_dislike);
        tvDislikeCount = findViewById(R.id.tv_dislike_count);
        ImageView ivComment = findViewById(R.id.iv_comment);
        tvCommentCount = findViewById(R.id.tv_comment_count);
        ImageView ivReportMemory = findViewById(R.id.iv_report_memory);
        LinearLayout llCommentInput = findViewById(R.id.ll_comment_input);
        EditText etComment = findViewById(R.id.et_comment);
        Button btnSubmitComment = findViewById(R.id.btn_submit_comment);
        tvCommentsList = findViewById(R.id.tv_comments_list);
        llCommentsContainer = findViewById(R.id.ll_comments_container);
        tvMemoryText = findViewById(R.id.tv_memory_text);
        tvMemoryDesc = findViewById(R.id.tv_memory_desc);
        ivMemoryImage = findViewById(R.id.iv_memory_image);

        tvAppTitle.setTypeface(vazirBold);
        tvMemoryText.setTypeface(vazirRegular);
        tvMemoryDesc.setTypeface(vazirRegular);
        tvLikeCount.setTypeface(vazirRegular);
        tvDislikeCount.setTypeface(vazirRegular);
        tvCommentCount.setTypeface(vazirRegular);
        tvCommentsList.setTypeface(vazirRegular);
        etComment.setTypeface(vazirRegular);
        btnSubmitComment.setTypeface(vazirRegular);
        btnAnotherMemory.setTypeface(vazirRegular);
        btnPreviousMemory.setTypeface(vazirRegular);
        btnWriteMemory.setTypeface(vazirRegular);

        queue = Volley.newRequestQueue(this);
        Picasso.get().setLoggingEnabled(true);

        checkInternetConnection();

        btnPreviousMemory.setOnClickListener(v -> {
            if (currentMemoryId > 1) {
                loadMemory("prev");
            } else {
                Toast.makeText(MainActivity.this, "این اولین خاطره است!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAnotherMemory.setOnClickListener(v -> loadMemory("next"));

        btnWriteMemory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WriteMemoryActivity.class);
            startActivity(intent);
        });

        ivLike.setOnClickListener(v -> addLikeDislike(1, tvLikeCount, tvDislikeCount));

        ivDislike.setOnClickListener(v -> addLikeDislike(0, tvLikeCount, tvDislikeCount));

        ivComment.setOnClickListener(v -> {
            llCommentInput.setVisibility(llCommentInput.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            if (llCommentInput.getVisibility() == View.VISIBLE) {
                loadComments();
            }
        });

        ivReportMemory.setOnClickListener(v -> showReportDialog("memory", currentMemoryId, null));

        btnSubmitComment.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            if (!comment.isEmpty()) {
                if (username == null) {
                    showUsernameDialog(comment, etComment, llCommentInput);
                } else {
                    addComment(comment, etComment, llCommentInput);
                }
            } else {
                Toast.makeText(MainActivity.this, "لطفاً کامنت بنویسید!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUsernameDialog(String comment, EditText etComment, LinearLayout llCommentInput) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView titleView = new TextView(this);
        titleView.setText("نام کاربری");
        titleView.setTypeface(vazirBold);
        titleView.setTextSize(20);
        titleView.setPadding(40, 40, 40, 20);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        builder.setCustomTitle(titleView);

        final EditText input = new EditText(this);
        input.setHint("نام خود را وارد کنید (اختیاری)");
        input.setTypeface(vazirRegular);
        input.setPadding(40, 20, 40, 20);
        builder.setView(input);

        builder.setPositiveButton("تایید", (dialog, which) -> {
            String enteredName = input.getText().toString().trim();
            username = enteredName.isEmpty() ? "کاربر مهمان" : enteredName;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, username);
            editor.apply();
            addComment(comment, etComment, llCommentInput);
        });

        builder.setNegativeButton("رد کردن", (dialog, which) -> {
            username = "کاربر مهمان";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, username);
            editor.apply();
            addComment(comment, etComment, llCommentInput);
        });

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (positiveButton != null) {
            positiveButton.setTypeface(vazirRegular);
        }
        if (negativeButton != null) {
            negativeButton.setTypeface(vazirRegular);
        }
    }

    private void showReportDialog(String type, int memoryId, Integer commentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView titleView = new TextView(this);
        titleView.setText("گزارش تخلف");
        titleView.setTypeface(vazirBold);
        titleView.setTextSize(20);
        titleView.setPadding(40, 40, 40, 20);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        builder.setCustomTitle(titleView);

        final EditText input = new EditText(this);
        input.setHint("دلیل گزارش را بنویسید (مثلاً: محتوای نامناسب)");
        input.setTypeface(vazirRegular);
        input.setPadding(40, 20, 40, 20);
        input.setMinLines(3);
        input.setGravity(View.TEXT_ALIGNMENT_VIEW_START);
        builder.setView(input);

        builder.setPositiveButton("ارسال", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(MainActivity.this, "لطفاً دلیل گزارش را وارد کنید!", Toast.LENGTH_SHORT).show();
            } else {
                submitReport(type, memoryId, commentId, reason);
            }
        });

        builder.setNegativeButton("لغو", null);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (positiveButton != null) {
            positiveButton.setTypeface(vazirRegular);
        }
        if (negativeButton != null) {
            negativeButton.setTypeface(vazirRegular);
        }
    }

    private void submitReport(String type, int memoryId, Integer commentId, String reason) {
        StringRequest request = new StringRequest(Request.Method.POST, API_URL,
                response -> {
                    try {
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                Toast.makeText(MainActivity.this, "گزارش با موفقیت ثبت شد!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "خطا: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "پاسخ نامعتبر از سرور!", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "خطا در پردازش پاسخ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "خطا در شبکه: " + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "report_content");
                params.put("user_id", userId);
                if (type.equals("memory")) {
                    params.put("memory_id", String.valueOf(memoryId));
                } else if (type.equals("comment")) {
                    params.put("comment_id", String.valueOf(commentId));
                }
                params.put("reason", reason);
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

    private void checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            currentMemoryId = 0;
            loadMemory("next");
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            TextView titleView = new TextView(this);
            titleView.setText("عدم اتصال به اینترنت");
            titleView.setTypeface(vazirBold);
            titleView.setTextSize(20);
            titleView.setPadding(40, 40, 40, 20);
            titleView.setTextColor(getResources().getColor(android.R.color.black));
            builder.setCustomTitle(titleView);

            TextView messageView = new TextView(this);
            messageView.setText("اتصال به اینترنت برقرار نیست. لطفاً اتصال خود را بررسی کنید و دوباره امتحان کنید.");
            messageView.setTypeface(vazirRegular);
            messageView.setTextSize(16);
            messageView.setPadding(40, 20, 40, 20);
            messageView.setTextColor(getResources().getColor(android.R.color.black));
            builder.setView(messageView);

            builder.setPositiveButton("تلاش مجدد", (dialog, which) -> checkInternetConnection());
            builder.setNegativeButton("خروج", (dialog, which) -> finish());
            builder.setCancelable(true);

            AlertDialog dialog = builder.create();
            dialog.show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (positiveButton != null) {
                positiveButton.setTypeface(vazirRegular);
            }
            if (negativeButton != null) {
                negativeButton.setTypeface(vazirRegular);
            }
        }
    }

    private void loadMemory(String direction) {
        String url = API_URL + "?action=get_memory&current_id=" + currentMemoryId + "&direction=" + direction;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject data = response.getJSONObject("data");
                            currentMemoryId = data.getInt("id");
                            tvMemoryText.setText(data.getString("title"));
                            tvMemoryDesc.setText(data.getString("description"));
                            tvLikeCount.setText(String.valueOf(data.getInt("like_count")));
                            tvDislikeCount.setText(String.valueOf(data.getInt("dislike_count")));
                            tvCommentCount.setText(String.valueOf(data.getInt("comment_count")));

                            String imageUrl = data.optString("image_url", "");
                            Log.d(TAG, "Loading image from URL: " + imageUrl);
                            if (!imageUrl.isEmpty()) {
                                Picasso.get()
                                        .load(imageUrl)
                                        .resize(600, 400)
                                        .centerCrop()
                                        .placeholder(R.drawable.sample_memory)
                                        .error(R.drawable.sample_memory)
                                        .into(ivMemoryImage, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d(TAG, "Image loaded successfully: " + imageUrl);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Log.e(TAG, "Failed to load image: " + e.getMessage(), e);
                                                Toast.makeText(MainActivity.this, "خطا در لود تصویر: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                ivMemoryImage.setImageResource(R.drawable.sample_memory);
                                            }
                                        });
                            } else {
                                Log.w(TAG, "Image URL is empty");
                                ivMemoryImage.setImageResource(R.drawable.sample_memory);
                            }

                            loadComments();
                            Log.d(TAG, "Memory loaded: " + data.toString());

                            btnPreviousMemory.setEnabled(currentMemoryId > 1);
                        } else {
                            if (response.getString("message").equals("No memories found")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                                TextView titleView = new TextView(this);
                                titleView.setText("پایان خاطره‌ها");
                                titleView.setTypeface(vazirBold);
                                titleView.setTextSize(20);
                                titleView.setPadding(40, 40, 40, 20);
                                titleView.setTextColor(getResources().getColor(android.R.color.black));
                                builder.setCustomTitle(titleView);

                                TextView messageView = new TextView(this);
                                messageView.setText("خاطره جدیدی وجود نداره، لطفاً بعداً سر بزن! می‌خوای از اول شروع کنی؟");
                                messageView.setTypeface(vazirRegular);
                                messageView.setTextSize(16);
                                messageView.setPadding(40, 20, 40, 20);
                                messageView.setTextColor(getResources().getColor(android.R.color.black));
                                builder.setView(messageView);

                                builder.setPositiveButton("بله، از اول", (dialog, which) -> {
                                    currentMemoryId = 0;
                                    loadMemory("next");
                                });
                                builder.setNegativeButton("خیر", null);
                                builder.setCancelable(true);

                                AlertDialog dialog = builder.create();
                                dialog.show();

                                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                                if (positiveButton != null) {
                                    positiveButton.setTypeface(vazirRegular);
                                }
                                if (negativeButton != null) {
                                    negativeButton.setTypeface(vazirRegular);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Error: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Server error: " + response.toString());
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Parse error: " + e.getMessage(), e);
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Network error: " + error.toString(), error);
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

    private void addLikeDislike(int isLike, TextView tvLikeCount, TextView tvDislikeCount) {
        StringRequest request = new StringRequest(Request.Method.POST, API_URL,
                response -> {
                    Log.d(TAG, "Raw response: " + response);
                    try {
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                loadMemory("stay");
                                Toast.makeText(MainActivity.this, isLike == 1 ? "لایک شد!" : "دیس‌لایک شد!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Like/Dislike success: " + response);
                            } else {
                                Toast.makeText(MainActivity.this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Server error: " + jsonResponse.toString());
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid response from server: " + response, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Invalid JSON: " + response);
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Parse error: " + e.getMessage(), e);
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Network error: " + error.toString(), error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "add_like");
                params.put("user_id", userId);
                params.put("memory_id", String.valueOf(currentMemoryId));
                params.put("is_like", String.valueOf(isLike));
                Log.d(TAG, "Sending params: " + params.toString());
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

    private void addComment(String comment, EditText etComment, LinearLayout llCommentInput) {
        StringRequest request = new StringRequest(Request.Method.POST, API_URL,
                response -> {
                    Log.d(TAG, "Raw response: " + response);
                    try {
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                loadMemory("stay");
                                etComment.setText("");
                                llCommentInput.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "کامنت ثبت شد!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Comment success: " + response);
                            } else {
                                Toast.makeText(MainActivity.this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Server error: " + jsonResponse.toString());
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid response from server: " + response, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Invalid JSON: " + response);
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Parse error: " + e.getMessage(), e);
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Network error: " + error.toString(), error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "add_comment");
                params.put("user_id", userId);
                params.put("memory_id", String.valueOf(currentMemoryId));
                params.put("comment_text", comment);
                params.put("username", username); // Send the username to the server
                Log.d(TAG, "Sending params: " + params.toString());
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

    private void loadComments() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API_URL + "?action=get_comments&memory_id=" + currentMemoryId, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray comments = response.getJSONArray("data");
                            llCommentsContainer.removeAllViews(); // Clear previous comments

                            for (int i = 0; i < comments.length(); i++) {
                                JSONObject comment = comments.getJSONObject(i);
                                int commentId = comment.getInt("id");
                                String commentUsername = comment.getString("username"); // Get username from API response
                                String commentText = commentUsername + ": " + comment.getString("comment_text");

                                // Create a layout for each comment
                                LinearLayout commentLayout = new LinearLayout(MainActivity.this);
                                commentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                commentLayout.setOrientation(LinearLayout.HORIZONTAL);
                                commentLayout.setPadding(0, 8, 0, 8);

                                // Comment text
                                TextView tvComment = new TextView(MainActivity.this);
                                tvComment.setLayoutParams(new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        3));
                                tvComment.setText(commentText);
                                tvComment.setTextColor(getResources().getColor(R.color.dark_gray));
                                tvComment.setTextSize(14);
                                tvComment.setTypeface(vazirRegular);
                                commentLayout.addView(tvComment);

                                // Report button for comment
                                ImageView ivReportComment = new ImageView(MainActivity.this);
                                ivReportComment.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                ivReportComment.setImageResource(R.drawable.ic_report);
                                ivReportComment.setPadding(8, 0, 8, 0);
                                ivReportComment.setContentDescription("گزارش تخلف کامنت");
                                ivReportComment.setOnClickListener(v -> showReportDialog("comment", currentMemoryId, commentId));
                                commentLayout.addView(ivReportComment);

                                llCommentsContainer.addView(commentLayout);
                            }
                            Log.d(TAG, "Comments loaded: " + response.toString());
                        } else {
                            Toast.makeText(MainActivity.this, "Error: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Server error: " + response.toString());
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Parse error: " + e.getMessage(), e);
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Network error: " + error.toString(), error);
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }
}