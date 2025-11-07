package com.example.demo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.demo.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PaymentMethodBottomSheetFragment extends BottomSheetDialogFragment {

    private TextView optionMomo, optionCash, optionVNPay;
    private PaymentMethodListener mListener;

    // 1. Định nghĩa Interface để gửi dữ liệu về Activity
    public interface PaymentMethodListener {
        void onPaymentMethodSelected(String paymentMethod);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_payment_method, container, false);

        optionMomo = view.findViewById(R.id.optionMomo);
        optionCash = view.findViewById(R.id.optionCash);
        optionVNPay = view.findViewById(R.id.optionVNPay);

        // 2. Xử lý click
        optionMomo.setOnClickListener(v -> {
            mListener.onPaymentMethodSelected("Momo"); // Gửi chữ "Momo" về
            dismiss(); // Đóng BottomSheet
        });

        optionCash.setOnClickListener(v -> {
            mListener.onPaymentMethodSelected("Cash"); // Gửi chữ "Cash" về
            dismiss();
        });

        optionVNPay.setOnClickListener(v -> {
            mListener.onPaymentMethodSelected("VNPay"); // Gửi chữ "VNPay" về
            dismiss();
        });

        return view;
    }

    // 3. Gắn Listener với Activity khi BottomSheet được mở
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // context ở đây chính là ConfirmOrderActivity
            mListener = (PaymentMethodListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " phải implement PaymentMethodListener");
        }
    }
}