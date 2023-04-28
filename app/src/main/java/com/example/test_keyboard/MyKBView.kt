package com.example.test_keyboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.toColorInt
import kotlin.math.max

class MyKBView : View {
    lateinit var buffer: Bitmap;

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
        Log.i(context.getString(R.string.my_ime), "onMeasure: in width: $widthMeasureSpec, height: $heightMeasureSpec")
        setMeasuredDimension(
            200, 200
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return;
        var canvasWidth = max(1, width);
        var canvasHeight = max(1, height);
        Log.i(context.getString(R.string.my_ime), "onDraw: width $canvasWidth height $canvasHeight")
        buffer = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        var mcanvas = Canvas(buffer)
        var paint = Paint()
        paint.color = 0x77FFFFFF.toInt()
        mcanvas.drawCircle((canvasWidth / 2).toFloat(), (canvasHeight / 2).toFloat(), 50f, paint)
        canvas.drawBitmap(buffer, 0f, 0f, null)
    }
}