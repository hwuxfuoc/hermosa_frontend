package com.example.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.demo.R;

public class PaymentMethodBottomSheetFragment extends BottomSheetDialogFragment {

    public interface PaymentMethodListener {
        void onPaymentMethodSelected(String method);
    }

    private PaymentMethodListener listener;
    private TextView selectedOption = null; // Lưu option đang được chọn

    public void setPaymentMethodListener(PaymentMethodListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_payment_method, container, false);

        TextView optionMomo = view.findViewById(R.id.optionMomo);
        TextView optionCash = view.findViewById(R.id.optionCash);
        TextView optionVNPay = view.findViewById(R.id.optionVNPay);

        // Hàm xử lý chung khi chọn 1 option
        View.OnClickListener selectOption = v -> {
            TextView clicked = (TextView) v;

            // Bỏ chọn cái cũ
            if (selectedOption != null) {
                selectedOption.setBackgroundResource(R.drawable.payment_option_default);
                selectedOption.setTextColor(0xFF666666);
                selectedOption.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            // Chọn cái mới
            clicked.setBackgroundResource(R.drawable.payment_option_selected);
            clicked.setTextColor(0xFFA71317);
            clicked.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_tick_red, 0);

            selectedOption = clicked;
            String method = "";

            if (clicked.getId() == R.id.optionMomo) method = "momo";
            else if (clicked.getId() == R.id.optionCash) method = "cash";
            else if (clicked.getId() == R.id.optionVNPay) method = "vnpay";

            // Gọi callback để Activity biết
            if (listener != null) {
                listener.onPaymentMethodSelected(method);
            }

            view.postDelayed(this::dismiss, 300);
        };

        optionMomo.setOnClickListener(selectOption);
        optionCash.setOnClickListener(selectOption);
        optionVNPay.setOnClickListener(selectOption);

        return view;
    }
}