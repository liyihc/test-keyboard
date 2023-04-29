package com.example.test_keyboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.lang.StringBuilder
import kotlin.math.ceil
import kotlin.math.max

class MyKBView : View {
    var DEBUG = true
    lateinit var buffer: Bitmap;
    lateinit var layout: KeyboardLayout;

    constructor(ctx: Context) : super(ctx) {}
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {}
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        ctx,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (DEBUG) Log.i(
            context.getString(R.string.my_ime),
            "onMeasure: in width: $widthMeasureSpec, height: $heightMeasureSpec"
        )
//        这里传入的宽会很大，但不要怕，我们直接传一个最大的值回去，这样在实际onDraw的时候会帮我们获取需要的宽度的
        setMeasuredDimension(
            widthMeasureSpec, ceil(layout.height).toInt() // need to get dpi here
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return;
        var canvasWidth = max(1, width);
        var canvasHeight = max(1, height);
        if (DEBUG) Log.i(
            context.getString(R.string.my_ime),
            "onDraw: width $canvasWidth height $canvasHeight"
        )
        buffer = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        var mcanvas = Canvas(buffer)
        var paint = Paint()
        paint.color = 0xFF000000.toInt() // background
        mcanvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), paint)

        val maxRowLength = layout.rows.map { row -> row.keys.size }.maxOrNull() ?: 1
        val keyWidth = canvasWidth / maxRowLength
        val halfPadding = 10f
        paint.color = 0xFF515151.toInt() // background
        for ((row_ind, row) in layout.rows.withIndex()) {
            val leftPadding = halfPadding*2 + ((maxRowLength - row.keys.size) * keyWidth) / 2
            // 分情况考虑，无功能键，有功能键，有spacer，等等
            for ((key_ind, key) in row.keys.withIndex()) {
                mcanvas.drawRoundRect(
                    (leftPadding + halfPadding + key_ind * keyWidth).toFloat(),
                    (halfPadding * 3 + row_ind * 130).toFloat(),
                    ((key_ind + 1) * keyWidth + leftPadding - halfPadding).toFloat(),
                    ((row_ind + 1) * 130 + halfPadding),
                    7f, 7f, paint
                )

            }
        }

        canvas.drawBitmap(buffer, 0f, 0f, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (DEBUG) Log.i(
            context.getString(R.string.my_ime),
            "onSizeChanged: new-size: w $w h $h old-size: w $w h $h"
        )
        super.onSizeChanged(w, h, oldw, oldh)
    }

    @Serializable
    class KeyboardLayout(
        var name: String = "",
        var rows: List<Row> = emptyList()
    ) {
        val height: Double
            get() = rows.size * 130.0 + 60.0 // 行130，padding 30
    }

    @Serializable
    class Row(
        var keys: List<Key> = emptyList()
    ) {
        fun toPrintString(): String {
            val text = StringBuilder()
            for (key in keys) {
                when (key.type) {
                    KeyType.KEY ->
                        text.append(key.text ?: key.value)

                    KeyType.FUNC ->
                        text.append(key.text ?: key.value)

                    KeyType.SPACER ->
                        text.append(key.text ?: "-")
                }
                text.append(' ')
            }
            return text.trim().toString()
        }
    }

    @Serializable
    class Key(
        val type: KeyType,
        val text: String? = null,
        val value: String? = null
    );

    @Serializable
    enum class KeyType() {
        @SerialName("key")
        KEY,

        @SerialName("func")
        FUNC,

        @SerialName("spacer")
        SPACER;
    }
}