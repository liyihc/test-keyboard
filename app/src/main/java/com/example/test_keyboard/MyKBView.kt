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
        ctx, attrs, defStyleAttr, defStyleRes
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
        val canvasWidth = max(1, width);
        val canvasHeight = max(1, height);
        if (DEBUG) Log.i(
            context.getString(R.string.my_ime), "onDraw: width $canvasWidth height $canvasHeight"
        )
        buffer = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        val mcanvas = Canvas(buffer)
        val paint = Paint()
        paint.color = 0xFF000000.toInt() // background
        mcanvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), paint)

        val maxRowLength = layout.maxRowLength
        val keyWidth = (canvasWidth - layout.padding.left - layout.padding.right) / maxRowLength
        paint.color = 0xFF515151.toInt()
        var y = layout.padding.top.toDouble();
        for (row in layout.rows) {
            val top = y;
            val bottom = y + (row.height ?: layout.rowHeight);
            var (x, keyWidths) = row.getKeyWidths(maxRowLength.toDouble())
            for ((key, width) in row.keys.zip(keyWidths)) {
                val left = layout.padding.left + x * keyWidth
                val right = left + width * keyWidth
                if (DEBUG)
                    Log.i(
                        context.getString(R.string.my_ime),
                        "onDraw: ${key.text ?: key.value} l:$left t:$top"
                    )
                mcanvas.drawRoundRect(
                    (left + (key.padding?.left ?: layout.keyPadding.left)).toFloat(),
                    (top + (key.padding?.top ?: layout.keyPadding.top)).toFloat(),
                    (right - (key.padding?.right ?: layout.keyPadding.right)).toFloat(),
                    (bottom - (key.padding?.bottom ?: layout.keyPadding.bottom)).toFloat(),
                    7f,
                    7f,
                    paint
                )
                x += width
            }
            y += row.height ?: layout.rowHeight
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
        @SerialName("row_height") var rowHeight: Int = 130,
        var padding: Padding = Padding(),
        @SerialName("key_padding") var keyPadding: Padding = Padding(10, 10, 10, 10),
        var rows: List<Row> = emptyList()
    ) {
        val height: Double
            get() = padding.top + padding.bottom +
                    (rows.sumOf { row -> row.height ?: rowHeight }).toDouble()
        val maxRowLength = rows.maxOfOrNull { row -> row.keys.size } ?: 1
    }

    @Serializable
    class Row(
        var height: Int? = null, var keys: List<Key> = emptyList()
    ) {
        fun toPrintString(): String {
            val text = StringBuilder()
            for (key in keys) {
                when (key.type) {
                    KeyType.KEY -> text.append(key.text ?: key.value)

                    KeyType.FUNC -> text.append(key.text ?: key.value)
                }
                text.append(' ')
            }
            return text.trim().toString()
        }

        fun countExpandKey(): Int = keys.count { key -> key.keyWidthRatio == null }

        fun getKeyWidths(keyCount: Double): Pair<Double, List<Double>> {
            val currentWidth =
                keys.sumOf { key -> key.keyWidthRatio ?: 0.0 }
            val expandKeyCount = countExpandKey()
            val expandKeyWidth =
                (keyCount - currentWidth) / (if (expandKeyCount == 0) 2 else expandKeyCount)
            return if (expandKeyCount > 0) {
                Pair(
                    0.0,
                    keys.map { key -> key.keyWidthRatio ?: expandKeyWidth })
            } else {
                Pair(
                    expandKeyWidth,
                    keys.map { key -> key.keyWidthRatio!! }
                )
            }
        }
    }

    @Serializable
    class Key(
        val type: KeyType,
        val text: String? = null,
        val value: String? = null,
        @SerialName("key_width") val _keyWidthRatio: Double? = null,
        val padding: Padding? = null
    ) {
        val keyWidthRatio: Double?
            get() = _keyWidthRatio ?: if (type == KeyType.KEY) 1.0 else null;

    };

    @Serializable
    class Padding(
        val top: Int = 30, val left: Int = 30, val right: Int = 30, val bottom: Int = 30
    );

    @Serializable
    enum class KeyType() {
        @SerialName("key")
        KEY,

        @SerialName("func")
        FUNC,
    }
}