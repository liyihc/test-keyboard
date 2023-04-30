package com.example.test_keyboard;

import android.app.Service;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;
import com.charleskorn.kaml.Yaml

public class MyIME : InputMethodService() {
    var DEBUG = true;
    private lateinit var keyboardView: MyKBView;
    private lateinit var keyboardLayout: MyKBView.KeyboardLayout;
    override fun onInitializeInterface() {
        super.onInitializeInterface()
        keyboardLayout = Yaml.default.decodeFromStream(
            MyKBView.KeyboardLayout.serializer(),
            resources.openRawResource(R.raw.kb_default)
        )
        if (DEBUG) {
            Log.i(getString(R.string.my_ime), "onInitializeInterface: ${keyboardLayout.rows.size}")
            for (row in keyboardLayout.rows)
                Log.i(getString(R.string.my_ime), "onInitializeInterface: ${row.toPrintString()}")
        }
    }

    override fun onCreateInputView(): View {
        Log.i("MyIME", "onCreateInputView: ")
        keyboardView = layoutInflater.inflate(R.layout.keyboard, null) as MyKBView;
        keyboardView.DEBUG = DEBUG
        keyboardView.layout = keyboardLayout
        return keyboardView;
    }
}
