package com.yida.dialogchainhelper.dialogfragment;


import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.yida.dialogchainhelper.R;

public class DialogFragment5 extends DialogFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(getContext());
        textView.setWidth(300);
        textView.setHeight(300);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        textView.setTextColor( ContextCompat.getColor(getContext(), R.color.black));
        textView.setText("我是DialogFragment:5");
        Log.e("yida","onCreateView");
        return textView;
    }

}
