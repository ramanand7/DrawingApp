package com.example.drawingapp__

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawinView(context : Context, attrs : AttributeSet) : View(context,attrs) {

    private var mDrawPath : CustomPath ?= null
    private var mCanvasBitmap : Bitmap? =null
    private var mDarwPaint : Paint?=null
    private var mCanvasPaint : Paint? =null
    private var mBrushSize : Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas :Canvas?=null
    private val mPaths = ArrayList<CustomPath>()

    // toundo step 1
    private val mUndoPaths =  ArrayList<CustomPath>()


    init {
        setUpDrawing()
    }

    fun onClickUndo(){
        if (mPaths.size>0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate()
        }
    }

    private fun setUpDrawing(){

        mDarwPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDarwPaint!!.color = color
        mDarwPaint!!.style = Paint.Style.STROKE
        mDarwPaint!!.strokeJoin=Paint.Join.ROUND
        mDarwPaint!!.strokeCap =Paint.Cap.ROUND
        mCanvasPaint=Paint(Paint.DITHER_FLAG)
        mBrushSize = 10.toFloat()
    }


    //set canvas bitamap
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        for (path in mPaths){
           mDarwPaint!!.strokeWidth = path.brushThickness
           mDarwPaint!!.color =path.color
           canvas.drawPath(path,mDarwPaint!!)
        }

        if (!mDrawPath!!.isEmpty){
            mDarwPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDarwPaint!!.color = mDrawPath!!.color

            canvas.drawPath(mDrawPath!!,mDarwPaint!!)

        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
       val toucx = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mDrawPath!!.color =color
                mDrawPath!!.brushThickness =mBrushSize
                mDrawPath!!.moveTo(toucx!!,touchY!!)
            }
            MotionEvent.ACTION_MOVE->{
                mDrawPath!!.lineTo(toucx!!,touchY!!)
            }

            MotionEvent.ACTION_UP->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color,mBrushSize)
            }
            else->return false

        }
        invalidate()
     return true
    }

    public fun setSizeForBrush(newSize : Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP
        ,newSize,resources.displayMetrics)
       mDarwPaint!!.strokeWidth =mBrushSize
    }

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        mDarwPaint!!.color = color
    }

    internal  inner class CustomPath(var color: Int,var brushThickness :Float
    ): Path() {
    }


}