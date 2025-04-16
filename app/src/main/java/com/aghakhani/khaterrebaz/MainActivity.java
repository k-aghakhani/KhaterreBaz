package com.aghakhani.khaterrebaz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        Button btnAnotherMemory = findViewById(R.id.btn_another_memory);
        Button btnWriteMemory = findViewById(R.id.btn_write_memory);
        Button btnDecades = findViewById(R.id.btn_decades);

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

        // Decades button click
        btnDecades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DecadesActivity.class);
                startActivity(intent);
            }
        });
    }
}