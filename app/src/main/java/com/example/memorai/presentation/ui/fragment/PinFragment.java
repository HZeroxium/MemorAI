package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.memorai.R;

public class PinFragment extends Fragment {

    private EditText pinEditText;

    public PinFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pin, container, false);
        pinEditText = rootView.findViewById(R.id.pin_edit_text);
        Button submitButton = rootView.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(view -> {
            String pin = pinEditText.getText().toString();
            if (isPinValid(pin)) {
                openPrivateAlbumFragment();  // Nếu đúng, mở PrivateAlbumFragment
            } else {
                Toast.makeText(getContext(), "PIN không chính xác", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private boolean isPinValid(String pin) {
        return pin.equals("1234");  // Ví dụ đơn giản
    }

    private void openPrivateAlbumFragment() {
        // Chuyển sang fragment PrivateAlbumFragment khi xác thực thành công
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new PrivateAlbumFragment())
                .addToBackStack(null)
                .commit();
    }
}
