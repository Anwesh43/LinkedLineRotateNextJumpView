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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LRNJNode(var i : Int, val state : State = State()){

        private var next : LRNJNode? = null
        private var prev : LRNJNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = LRNJNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLRNJNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LRNJNode {
            var curr : LRNJNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LineRotateNextJump(var i : Int) {

        private var  curr : LRNJNode = LRNJNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LineRotateNextJumpView) {

        private val lrnj : LineRotateNextJump = LineRotateNextJump(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            lrnj.draw(canvas, paint)
            animator.animate {
                lrnj.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lrnj.startUpdating {
                animator.start()
            }
        }
    }
}