package com.aghakhani.khaterrebaz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private int likeCount = 0;
    private int dislikeCount = 0;
    private int commentCount = 0;
    private StringBuilder comments = new StringBuilder();

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

        // Another Memory button click
        btnAnotherMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "خاطره جدید بارگذاری شد!", Toast.LENGTH_SHORT).show();
                // اینجا می‌تونی منطق عوض کردن خاطره رو اضافه کنی
            }
        });

        // Write Memory button click
        btnWriteMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "به زودی می‌تونی خاطره بنویسی!", Toast.LENGTH_SHORT).show();
                // اینجا می‌تونی به صفحه نوشتن خاطره بری
            }
        });

        // Like button click
        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount++;
                tvLikeCount.setText(String.valueOf(likeCount));
                Toast.makeText(MainActivity.this, "لایک شد!", Toast.LENGTH_SHORT).show();
            }
        });

        // Dislike button click
        ivDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeCount++;
                tvDislikeCount.setText(String.valueOf(dislikeCount));
                Toast.makeText(MainActivity.this, "دیس‌لایک شد!", Toast.LENGTH_SHORT).show();
            }
        });

        // Comment button click
        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llCommentInput.setVisibility(llCommentInput.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        // Submit comment button click
        btnSubmitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = etComment.getText().toString().trim();
                if (!comment.isEmpty()) {
                    commentCount++;
                    comments.append("کاربر: ").append(comment).append("\n");
                    tvCommentCount.setText(String.valueOf(commentCount));
                    tvCommentsList.setText(comments.toString());
                    etComment.setText("");
                    llCommentInput.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "کامنت ثبت شد!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "لطفاً کامنت بنویسید!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}