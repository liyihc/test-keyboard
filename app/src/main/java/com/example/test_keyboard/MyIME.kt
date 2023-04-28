package com.example.test_keyboard;

import android.app.Service;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.View;

public class MyIME : InputMethodService() {
    private lateinit var keyboardView: MyKBView;
    override fun onCreateInputView(): View {
        Log.i("MyIME", "onCreateInputView: ")
        keyboardView = layoutInflater.inflate(R.layout.keyboard, null) as MyKBView;
        return keyboardView;
    }
}
