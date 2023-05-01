package com.example.test_keyboard;

import android.app.Service;
import android.inputmethodservice.InputMethodService;
import android.os.Build
import android.util.Log;
import android.view.KeyEvent
import android.view.View;
import android.view.inputmethod.InputMethodManager
import com.charleskorn.kaml.Yaml

public class MyIME : InputMethodService(), MyKBView.OnKeyboardActionListener {
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
        keyboardView.keyboardListener = this
        return keyboardView;
    }

    private fun keyDownUp(code: Int) {
        val ic = currentInputConnection
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }

    override fun onTmp(key: MyKBView.Key) {
        val ic = currentInputConnection
        val value = key.value ?: ""
        if (DEBUG)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.i(
                    getString(R.string.my_ime),
                    "onTmp: ic text ${ic.getSurroundingText(1000, 1000, 0)?.text}"
                )
            }
        when (key.type) {
            MyKBView.KeyType.KEY ->
                ic.commitText(value, value.length)

            MyKBView.KeyType.FUNC ->
                when (key.value) {
                    "space" ->
                        ic.commitText(" ", 1)

                    "backspace" ->
                        keyDownUp(KeyEvent.KEYCODE_DEL)

                    "switch-ime" ->
                        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
                }
        }
    }

    override fun onPress() {
        TODO("Not yet implemented")
    }

    override fun onRelease() {
        TODO("Not yet implemented")
    }

    override fun swipeLeft() {
        TODO("Not yet implemented")
    }

    override fun swipeRight() {
        TODO("Not yet implemented")
    }

    override fun swipeUp() {
        TODO("Not yet implemented")
    }

    override fun swipeDown() {
        TODO("Not yet implemented")
    }
}
