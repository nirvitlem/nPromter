package com.example.nvpromter

import android.R
import android.content.Context
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
import android.widget.Scroller



class ScrollTextView   : androidx.appcompat.widget.AppCompatTextView {

    constructor (context: Context) :super(context)
    {
        setSingleLine()
        ellipsize = null
        marqueeRepeatLimit=1;
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }
    constructor(context: Context?, attrs: AttributeSet?): super (context!!, attrs )
    {
        setSingleLine()
        marqueeRepeatLimit=1;
        ellipsize = null
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int):super(context!!,attrs,defStyle)
    {
        setSingleLine()
        marqueeRepeatLimit=1;
        ellipsize = null
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }



    // scrolling feature
    private var mSlr: Scroller? = null
    private var TextList:MutableList<String> = ArrayList()
    private var LineIndex :Int = 0;
    private var mXPaused = 0

    // whether it's being paused
    private var mPaused = true


    private var mScrollSpeed = 250f //Added speed for same scrolling speed regardless of text
    private var BTLTime :Int = 0;


    protected override fun onDetachedFromWindow() {
        removeGlobalListener()
        super.onDetachedFromWindow()
    }

    fun setSpeed(mScrollSpeedValue : Float)
    {
        mScrollSpeed = mScrollSpeedValue
    }

    fun setTimeBTLines(TimeBTLines : Int)
    {
       BTLTime = TimeBTLines

    }

    fun setText(mtextValue : String)
    {
        text = mtextValue
    }

    /**
     * begin to scroll the text from the original position to specfic text
     */
    fun startScroll() {
        splitTexttoLines()
        val needsScrolling: Boolean = checkIfNeedsScrolling(TextList[LineIndex].toString())
        // begin from the middle
        mXPaused = -1 * (width / 2)
        // assume it's paused
        mPaused = true
        if (needsScrolling) {
            resumeScroll(TextList[0].toString())
        } else {
            pauseScroll()
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
    fun resumeScroll(Line:String) {
        if (!mPaused) return

        // Do not know why it would not scroll sometimes
        // if setHorizontallyScrolling is called in constructor.
        setHorizontallyScrolling(true)

        // use LinearInterpolator for steady scrolling
        mSlr = Scroller(this.context, LinearInterpolator())
        setScroller(mSlr)
        val scrollingLen = calculateScrollingLen(Line)
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
        for(line in text.toString().split("\n")) {
            TextList.add(line.toString())
        }
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
            if (TextList.size==0)
            {
                if LineIndex
                LineIndex++
                startScroll()
            }
            startScroll()
        }
    }

    fun isPaused(): Boolean {
        return mPaused
    }
}