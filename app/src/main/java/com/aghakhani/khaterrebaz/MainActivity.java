package com.aghakhani.khaterrebaz;

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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "http://192.168.1.86/khaterrebaz/api.php"; // Updated IP
    private static final int USER_ID = 1; // Temporary user ID
    private static final String TAG = "KhaterreBaz";
    private static final int TIMEOUT_MS = 15000; // 15 seconds timeout
    private static final int MAX_RETRIES = 3; // Retry 3 times

    private RequestQueue queue;
    private int currentMemoryId;
    private ImageView ivMemoryImage; // Added for image loading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        Button btnAnotherMemory = findViewById(R.id.btn_another_memory);
        Button btnWriteMemory = findViewById(R.id.btn_write_memory);
        ImageView ivLike = findViewById(R.id.iv_like);
        TextView tvLikeCount = findViewById(R.id.tv_like_count);
        ImageView ivDislike = findViewById(R.id.iv_dislike);
        TextView tvDislikeCount = findViewById(R.id.tv_dislike_count);
        ImageView ivComment = findViewById(R.id.iv_comment);
        TextView tvCommentCount = findViewById(R.id.tv_comment_count);
        LinearLayout llCommentInput = findViewById(R.id.ll_comment_input);
        EditText etComment = findViewById(R.id.et_comment);
        Button btnSubmitComment = findViewById(R.id.btn_submit_comment);
        TextView tvCommentsList = findViewById(R.id.tv_comments_list);
        TextView tvMemoryText = findViewById(R.id.tv_memory_text);
        TextView tvMemoryDesc = findViewById(R.id.tv_memory_desc);
        ivMemoryImage = findViewById(R.id.iv_memory_image); // Initialize ImageView

        // Initialize Volley
        queue = Volley.newRequestQueue(this);

        // Load initial memory
        loadMemory(tvMemoryText, tvMemoryDesc, tvLikeCount, tvDislikeCount, tvCommentCount, tvCommentsList);

        // Another Memory button click
        btnAnotherMemory.setOnClickListener(v -> loadMemory(tvMemoryText, tvMemoryDesc, tvLikeCount, tvDislikeCount, tvCommentCount, tvCommentsList));

        // Write Memory button click
        btnWriteMemory.setOnClickListener(v -> Toast.makeText(MainActivity.this, "به زودی می‌تونی خاطره بنویسی!", Toast.LENGTH_SHORT).show());

        // Like button click
        ivLike.setOnClickListener(v -> addLikeDislike(1, tvLikeCount, tvDislikeCount));

        // Dislike button click
        ivDislike.setOnClickListener(v -> addLikeDislike(0, tvLikeCount, tvDislikeCount));

        // Comment button click
        ivComment.setOnClickListener(v -> {
            llCommentInput.setVisibility(llCommentInput.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            if (llCommentInput.getVisibility() == View.VISIBLE) {
                loadComments(tvCommentsList);
            }
        });

        // Submit comment button click
        btnSubmitComment.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            if (!comment.isEmpty()) {
                addComment(comment, tvCommentCount, tvCommentsList, etComment, llCommentInput);
            } else {
                Toast.makeText(MainActivity.this, "لطفاً کامنت بنویسید!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMemory(TextView tvMemoryText, TextView tvMemoryDesc, TextView tvLikeCount,
                            TextView tvDislikeCount, TextView tvCommentCount, TextView tvCommentsList) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API_URL + "?action=get_memory", null,
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

                            // Load image using Picasso
                            String imageUrl = data.optString("image_url", "");
                            if (!imageUrl.isEmpty()) {
                                Picasso.get()
                                        .load(imageUrl)
                                        .placeholder(R.drawable.sample_memory) // Placeholder image while loading
                                        .error(R.drawable.sample_memory) // Fallback image if loading fails
                                        .into(ivMemoryImage);
                            } else {
                                ivMemoryImage.setImageResource(R.drawable.sample_memory); // Fallback if no image URL
                            }

                            loadComments(tvCommentsList);
                            Log.d(TAG, "Memory loaded: " + data.toString());
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

        // Set timeout and retry policy
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
                    Log.d(TAG, "Raw response: " + response); // Log the raw response
                    try {
                        // Check if response is a valid JSON
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                loadMemory(findViewById(R.id.tv_memory_text), findViewById(R.id.tv_memory_desc),
                                        tvLikeCount, tvDislikeCount, findViewById(R.id.tv_comment_count), findViewById(R.id.tv_comments_list));
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
                params.put("user_id", String.valueOf(USER_ID));
                params.put("memory_id", String.valueOf(currentMemoryId));
                params.put("is_like", String.valueOf(isLike));
                Log.d(TAG, "Sending params: " + params.toString());
                return params;
            }
        };

        // Set timeout and retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

    private void addComment(String comment, TextView tvCommentCount, TextView tvCommentsList, EditText etComment, LinearLayout llCommentInput) {
        StringRequest request = new StringRequest(Request.Method.POST, API_URL,
                response -> {
                    Log.d(TAG, "Raw response: " + response); // Log the raw response
                    try {
                        // Check if response is a valid JSON
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                loadMemory(findViewById(R.id.tv_memory_text), findViewById(R.id.tv_memory_desc),
                                        findViewById(R.id.tv_like_count), findViewById(R.id.tv_dislike_count), tvCommentCount, tvCommentsList);
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
                params.put("user_id", String.valueOf(USER_ID));
                params.put("memory_id", String.valueOf(currentMemoryId));
                params.put("comment_text", comment);
                Log.d(TAG, "Sending params: " + params.toString());
                return params;
            }
        };

        // Set timeout and retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

    private void loadComments(TextView tvCommentsList) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API_URL + "?action=get_comments&memory_id=" + currentMemoryId, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray comments = response.getJSONArray("data");
                            StringBuilder commentText = new StringBuilder();
                            for (int i = 0; i < comments.length(); i++) {
                                JSONObject comment = comments.getJSONObject(i);
                                commentText.append("کاربر: ").append(comment.getString("comment_text")).append("\n");
                            }
                            tvCommentsList.setText(commentText.toString());
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

        // Set timeout and retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }
}