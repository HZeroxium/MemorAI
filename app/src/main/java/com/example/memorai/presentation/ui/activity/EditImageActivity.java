package com.example.memorai.presentation.ui.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memorai.R;

public class EditImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_photo); // Đảm bảo đúng file XML của bạn

        // Ánh xạ RecyclerView
//        RecyclerView rvConstraintTools = findViewById(R.id.rvConstraintTools);
//
//        // Thiết lập LinearLayoutManager theo chiều ngang
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        rvConstraintTools.setLayoutManager(layoutManager);
    }
}
