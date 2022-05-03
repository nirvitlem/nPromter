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
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }
    constructor(context: Context?, attrs: AttributeSet?): super (context!!, attrs )
    {
        setSingleLine()
        ellipsize = null
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int):super(context!!,attrs,defStyle)
    {
        setSingleLine()
        ellipsize = null
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }



    // scrolling feature
    private var mSlr: Scroller? = null

    // the X offset when paused
    private var mXPaused = 0

    // whether it's being paused
    private var mPaused = true

    private var mScrollSpeed = 250f //Added speed for same scrolling speed regardless of text


    protected override fun onDetachedFromWindow() {
        removeGlobalListener()
        super.onDetachedFromWindow()
    }

    fun setSpeed(mScrollSpeedValue : Float)
    {
        mScrollSpeed = mScrollSpeedValue
    }

    fun setText(mtextValue : String)
    {
        text = mtextValue
    }

    /**
     * begin to scroll the text from the original position
     */
    fun startScroll() {
        val needsScrolling: Boolean = checkIfNeedsScrolling()
        // begin from the middle
        mXPaused = -1 * (width / 2)
        // assume it's paused
        mPaused = true
        if (needsScrolling) {
            resumeScroll()
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
    private var onGlobalLayoutListener: OnGlobalLayoutListener? =
        OnGlobalLayoutListener { startScroll() }

    /**
     * Checking if we need scrolling
     */
    private fun checkIfNeedsScrolling(): Boolean {
        measure(0, 0)
        val textViewWidth = width
        if (textViewWidth == 0) return false
        val textWidth = getTextLength().toFloat()
        return textWidth > textViewWidth
    }

    /**
     * resume the scroll from the pausing point
     */
    fun resumeScroll() {
        if (!mPaused) return

        // Do not know why it would not scroll sometimes
        // if setHorizontallyScrolling is called in constructor.
        setHorizontallyScrolling(true)

        // use LinearInterpolator for steady scrolling
        mSlr = Scroller(this.context, LinearInterpolator())
        setScroller(mSlr)
        val scrollingLen = calculateScrollingLen()
        val distance = scrollingLen - (width + mXPaused)
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
    private fun calculateScrollingLen(): Int {
        val length = getTextLength()
        return length + width
    }

    private fun getTextLength(): Int {
        val tp: TextPaint = paint
        var rect: Rect? = Rect()
        val strTxt: String = text.toString()
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
            startScroll()
        }
    }

    fun isPaused(): Boolean {
        return mPaused
    }
}