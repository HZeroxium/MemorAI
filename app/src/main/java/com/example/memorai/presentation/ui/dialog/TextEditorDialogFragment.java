package com.example.memorai.presentation.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorai.R;
import com.example.memorai.presentation.ui.adapter.ColorPickerAdapter;

import java.util.Arrays;
import java.util.List;

public class TextEditorDialogFragment extends DialogFragment {

    public interface TextEditorListener {
        void onDone(String inputText, int colorCode);
    }

    private TextEditorListener textEditorListener;
    private EditText editText;
    private TextView addTextButton;
    private RecyclerView colorPickerRecyclerView;
    private int selectedColor = Color.WHITE;

    public static TextEditorDialogFragment show() {
        return new TextEditorDialogFragment();
    }

    public void setOnTextEditorListener(TextEditorListener listener) {
        this.textEditorListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_edit_add_text, container, false);

        editText = view.findViewById(R.id.add_text_edit_text);
        addTextButton = view.findViewById(R.id.add_text_done_tv);
        colorPickerRecyclerView = view.findViewById(R.id.add_text_color_picker_recycler_layout);

        setupColorPicker();
        setupListeners();

        return view;
    }

    private void setupColorPicker() {
        List<Integer> colors = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.WHITE, Color.BLACK);
        ColorPickerAdapter adapter = new ColorPickerAdapter(requireContext(), colors);
        adapter.setOnColorPickerClickListener(color -> {
            // Xử lý sự kiện chọn màu
            selectedColor = color;
            editText.setTextColor(color);
        });
        colorPickerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        colorPickerRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        addTextButton.setOnClickListener(v -> {
            String inputText = editText.getText().toString().trim();
            if (!inputText.isEmpty() && textEditorListener != null) {
                textEditorListener.onDone(inputText, selectedColor);
            }

            // Đóng bàn phím trước khi đóng hộp thoại
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }

            // Trì hoãn dismiss để văn bản kịp thêm lên ảnh
            editText.postDelayed(this::dismiss, 200);
        });



        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                addTextButton.setEnabled(s.length() > 0);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gán style trong suốt cho DialogFragment
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TransparentDialogTheme);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
