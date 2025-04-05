package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.memorai.R;

public class SecurityQuestionFragment extends Fragment {

    private EditText answerEditText;
    private Button submitButton;

    public SecurityQuestionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_security_question, container, false);

        answerEditText = rootView.findViewById(R.id.answer_edit_text);
        submitButton = rootView.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(view -> {
            String answer = answerEditText.getText().toString();
            if (isAnswerCorrect(answer)) {
                openPrivateAlbumFragment();  // Được phép truy cập vào album
            } else {
                Toast.makeText(getContext(), "Câu trả lời không chính xác", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private boolean isAnswerCorrect(String answer) {
        return answer.equals("YourAnswer");  // Ví dụ đơn giản
    }

    private void openPrivateAlbumFragment() {
        // Mở PrivateAlbumFragment sau khi xác thực thành công
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new PrivateAlbumFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
