package com.example.rotatelinenextjumpview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.content.Context
import android.app.Activity

val colors : Array<Int> = arrayOf(
    "",
    "",
    "",
    "",
    ""
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 2
val steps : Int = 2
val scGap : Float = 0.02f / (parts * steps + 1)
val strokeFactor : Float = 90f
val sizeFactor : Float = 3.9f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val deg : Float = 60f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawLineRotateNextJump(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts + 1)
    val sf2 : Float = sf.divideScale(1, parts + 1)
    val sf3 : Float = sf.divideScale(2, parts + 1)
    val sf21 : Float = sf2.divideScale(0, steps)
    val sf22 : Float = sf2.divideScale(1, steps)
    val sf31 : Float = sf3.divideScale(0, steps)
    val sf32 : Float = sf3.divideScale(1, steps)
    val x : Float = (w / 2 - size) * sf22 + (w / 2) * sf32
    val rot : Float = (deg / 2) * (sf21.sinify() + sf31.sinify())
    save()
    translate(size+ (w / 2 - size) * sf1.divideScale(1, parts), h / 2)

    for (j in 0..1) {
        save()
        rotate(rot * (1f - 2 * j))
        drawLine(0f, 0f, -size * sf1, 0f, paint)
        restore()
    }
    restore()
}

fun Canvas.drawLRNJNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawLineRotateNextJump(scale, w, h, paint)
}

class LineRotateNextJumpView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}