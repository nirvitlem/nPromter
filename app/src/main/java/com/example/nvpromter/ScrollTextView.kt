package com.example.nvpromter

import android.R
import android.content.Context
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
import android.widget.Scroller



class ScrollTextView   : androidx.appcompat.widget.AppCompatTextView {
    companion object {
        @JvmStatic
        var WaitTime : Int= 2000
    }

    constructor (context: Context) :super(context)
    {
        setSingleLine()
        ellipsize = null
        marqueeRepeatLimit=1
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }
    constructor(context: Context?, attrs: AttributeSet?): super (context!!, attrs )
    {
        setSingleLine()
        marqueeRepeatLimit=1
        ellipsize = null
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int):super(context!!,attrs,defStyle)
    {
        setSingleLine()
        marqueeRepeatLimit=1
        ellipsize = null
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }



    // scrolling feature
    private var mSlr: Scroller? = null
    private var TextList:MutableList<String> = ArrayList()
    private var LineIndex :Int = 0
    private var mXPaused = 0

    // whether it's being paused
    private var mPaused = true


    private var mScrollSpeed = 250f //Added speed for same scrolling speed regardless of text
    private var BTLTime :Int = 2000



    override fun onDetachedFromWindow() {
        removeGlobalListener()
        super.onDetachedFromWindow()
    }

    fun setSpeed(mScrollSpeedValue : Float)
    {
        mScrollSpeed = mScrollSpeedValue
    }

    fun setPauseStartTime(TextSetStartTime : Int)
    {
        WaitTime = TextSetStartTime
    }

    fun setTimeBTLines(TimeBTLines : Int)
    {
       BTLTime = TimeBTLines

    }

    fun setTextToShow(mtextValue : String)
    {
        text = mtextValue
        splitTexttoLines()
        Log.d("setTextToShow-LineIndex",LineIndex.toString())
    }

    /**
     * begin to scroll the text from the original position to specfic text
     */
    fun startScroll() {
        Thread.sleep(WaitTime.toLong())
        this.text=TextList[LineIndex].toString();
        Log.d("startScroll-TextList[LineIndex",TextList[LineIndex].toString())
        Log.d("startScroll-LineIndex",LineIndex.toString())
        val needsScrolling: Boolean = checkIfNeedsScrolling(TextList[LineIndex].toString())
        // begin from the middle
        mXPaused = 1 //* (width / 2)
        // assume it's paused
        mPaused = true
        if (needsScrolling) {
            Log.d("startScroll-needsScrolling",needsScrolling.toString())
            resumeScroll()
        } else {
           Log.d("startScroll-needsScrolling",needsScrolling.toString())
           Thread.sleep(BTLTime.toLong())
           resumeScroll()
        //pauseScroll()
        }
        removeGlobalListener()

    }

    /**
     * Removing global listener
     */
    @Synchronized
    private fun removeGlobalListener() {
        try {
            if (onGlobalLayoutListener != null) viewTreeObserver.removeOnGlobalLayoutListener(
                onGlobalLayoutListener
            )
            onGlobalLayoutListener = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Waiting for layout to initiate
     */
    private var onGlobalLayoutListener: OnGlobalLayoutListener? =      OnGlobalLayoutListener { //startScroll()
             }

    /**
     * Checking if we need scrolling
     */
    private fun checkIfNeedsScrolling(Line:String): Boolean {
        measure(0, 0)
        val textViewWidth = width
        if (textViewWidth == 0) return false
        val textWidth = getTextLength(Line).toFloat()
        return textWidth > textViewWidth
    }

    /**
     * resume the scroll from the pausing point
     */
    fun resumeScroll() {
        Log.d("resumeScroll-mPaused",mPaused.toString())
        if (!mPaused) return
        Log.d("resumeScroll-TextList[LineIndex",TextList[LineIndex].toString())
        // Do not know why it would not scroll sometimes
        // if setHorizontallyScrolling is called in constructor.
        setHorizontallyScrolling(true)
        // use LinearInterpolator for steady scrolling
        mSlr = Scroller(this.context, LinearInterpolator())
        setScroller(mSlr)
        val scrollingLen = calculateScrollingLen(TextList[LineIndex].toString())
        val distance = scrollingLen - (width + mXPaused)
        //check distance for next line
        val duration = (1000f * distance / mScrollSpeed).toInt()
        visibility = VISIBLE
        mSlr!!.startScroll(mXPaused, 0, distance, 0, duration)
        invalidate()
        mPaused = false
    }

    /**
     * calculate the scrolling length of the text in pixel
     *
     * @return the scrolling length in pixels
     */
    private fun calculateScrollingLen(Line:String): Int {
        val length = getTextLength(Line)
        return length + width
    }

    /**
     * calculate the lines  of the text
     *
     * @return the scrolling length in pixels
     */
    private fun splitTexttoLines()  {
        TextList.clear()
        for(line in text.toString().split("\n")) {
            TextList.add(line.toString())
        }
        Log.d("splitTexttoLines",TextList.size.toString())
    }

    private fun getTextLength(Line:String): Int {
        val tp: TextPaint = paint
        var rect: Rect? = Rect()
        val strTxt: String = Line.toString()
        tp.getTextBounds(strTxt, 0, strTxt.length, rect)
        val length: Int = rect!!.width()
        rect = null
        return length
    }

    /**
     * pause scrolling the text
     */
    fun pauseScroll() {
        Log.d("pauseScroll-mPaused", mPaused.toString())
        if (null == mSlr) return
        if (mPaused) return
        mPaused = true

        // abortAnimation sets the current X to be the final X,
        // and sets isFinished to be true
        // so current position shall be saved
        mXPaused = mSlr!!.currX
        mSlr!!.abortAnimation()

    }

    /*
 * override the computeScroll to restart scrolling when finished so as that
 * the text is scrolled forever
 */   override fun computeScroll() {
        super.computeScroll()
        if (null == mSlr) return
        if (mSlr!!.isFinished && !mPaused) {
            if(LineIndex!=TextList.size) {
                LineIndex++
                Log.d("computeScroll-LineIndex", LineIndex.toString())
                startScroll()
            }
            else
            {
                mPaused=true
                pauseScroll()
                LineIndex=0
            }


        }
    }

    fun isPaused(): Boolean {
        return mPaused
    }
}