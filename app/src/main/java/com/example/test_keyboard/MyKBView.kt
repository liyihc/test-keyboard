package com.example.test_keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.lang.StringBuilder
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class MyKBView : View {
    var DEBUG = true
    lateinit var buffer: Bitmap;
    lateinit var layout: KeyboardLayout;
    val pos: MutableList<Pair<Float, List<Pair<Float, Key>>>> = mutableListOf()

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
        setMeasuredDimension( //        这里传入的宽会很大，但不要怕，我们直接传一个最大的值回去，这样在实际onDraw的时候会帮我们获取需要的宽度的
            widthMeasureSpec, ceil(layout.height).toInt()
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
        val keyWidth = (canvasWidth - layout.padding.horizontal) / maxRowLength
        var y = layout.padding.top.toDouble();
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.CENTER
        val fontMax = min(
            (keyWidth - layout.keyPadding.horizontal) * 0.8,
            layout.rowHeight / 2.0
        )
        pos.clear()
        for (row in layout.rows) {
            val top = y;
            val bottom = y + (row.height ?: layout.rowHeight);
            var (x, keyWidths) = row.getKeyWidths(maxRowLength.toDouble())
            val posRow = mutableListOf<Float>()
            for ((key, width) in row.keys.zip(keyWidths)) {
                val left = layout.padding.left + x * keyWidth
                val right = left + width * keyWidth
                val keyPadding = key.padding ?: row.keyPadding ?: layout.keyPadding
//                if (DEBUG)
//                    Log.i(
//                        context.getString(R.string.my_ime),
//                        "onDraw: ${key.text ?: key.value} l:$left t:$top"
//                    )
                paint.color = when (key.type) {
                    KeyType.KEY -> 0xFF515151.toInt()
                    KeyType.FUNC -> 0xFF8c3d30.toInt()
                }
                mcanvas.drawRoundRect(
                    (left + keyPadding.left).toFloat(),
                    (top + keyPadding.top).toFloat(),
                    (right - keyPadding.right).toFloat(),
                    (bottom - keyPadding.bottom).toFloat(),
                    10f,
                    10f,
                    paint
                )
                paint.color = 0xFFBBBBBB.toInt()
                val text = key.shownText
                paint.textSize =
                    min(
                        ((keyWidth * width - keyPadding.horizontal) * 0.8 / (text.length.toDouble()
                            .pow(0.6))), fontMax
                    ).toFloat()
                mcanvas.drawText(
                    text.uppercase(),
                    (left + keyWidth * width / 2).toFloat(),
                    (top + layout.rowHeight / 2 + paint.textSize * 3 / 8).toFloat(),
                    paint
                )
                posRow.add(right.toFloat())
                x += width
            }
            y += row.height ?: layout.rowHeight
            pos.add(Pair(y.toFloat(), posRow.zip(row.keys)))
        }

        canvas.drawBitmap(buffer, 0f, 0f, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (DEBUG)
            Log.i(
                context.getString(R.string.my_ime),
                "onSizeChanged: new-size: w $w h $h old-size: w $w h $h"
            )
        super.onSizeChanged(w, h, oldw, oldh)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        if (DEBUG)
            Log.i(context.getString(R.string.my_ime), "onTouchEvent: ${event.x} ${event.y}")
        var find: Key? = null
        for ((bottom, row) in pos) {
            if (bottom < event.y) continue
            for ((right, key) in row) {
                if (right < event.x) continue
                find = key
                break
            }
            if (find == null) find = row.last().second
            if (DEBUG)
                Log.i(
                    context.getString(R.string.my_ime),
                    "onTouchEvent: Touch ${find.shownText}"
                )

            break
        }

//        return true;
        return super.onTouchEvent(event)
    }

//    override fun onHoverEvent(event: MotionEvent?): Boolean {
////        类似鼠标略过吧
//        Log.i(context.getString(R.string.my_ime), "onHoverEvent: ${event.toString()}")
//        return super.onHoverEvent(event)
//    }

    @Serializable
    class KeyboardLayout(
        var name: String = "",
        @SerialName("row_height") var rowHeight: Int = 130,
        var padding: Padding = Padding(),
        @SerialName("key_padding") var keyPadding: Padding = Padding(10, 10, 10, 10),
        var rows: List<Row> = emptyList()
    ) {
        val height: Double
            get() = padding.vertical +
                    (rows.sumOf { it.height ?: rowHeight }).toDouble()
        val maxRowLength = rows.maxOfOrNull { it.keys.size } ?: 1
    }

    @Serializable
    class Row(
        var height: Int? = null,
        var keys: List<Key> = emptyList(),
        @SerialName("key_padding")
        var keyPadding: Padding? = null
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

        private fun countExpandKey(): Int = keys.count { it.keyWidthRatio == null }

        fun getKeyWidths(keyCount: Double): Pair<Double, List<Double>> {
            val currentWidth =
                keys.sumOf { it.keyWidthRatio ?: 0.0 }
            val expandKeyCount = countExpandKey()
            val expandKeyWidth =
                (keyCount - currentWidth) / (if (expandKeyCount == 0) 2 else expandKeyCount)
            return if (expandKeyCount > 0) {
                Pair(
                    0.0,
                    keys.map { it.keyWidthRatio ?: expandKeyWidth })
            } else {
                Pair(
                    expandKeyWidth,
                    keys.map { it.keyWidthRatio!! }
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

        val shownText: String
            get() = text ?: value ?: "-";

    };

    @Serializable
    class Padding(
        val top: Int = 30, val left: Int = 30, val right: Int = 30, val bottom: Int = 30
    ) {
        val vertical: Int get() = top + bottom;
        val horizontal: Int get() = left + right;
    };

    @Serializable
    enum class KeyType() {
        @SerialName("key")
        KEY,

        @SerialName("func")
        FUNC,
    }

    interface OnKeyboardActionListener {
        fun onPress();
        fun onRelease();
        fun swipeLeft();
        fun swipeRight();
        fun swipeUp();
        fun swipeDown();
    }
}