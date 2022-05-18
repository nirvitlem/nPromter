package com.example.nvpromter

import android.content.Context
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
import android.widget.Scroller


class ScrollTextView   : androidx.appcompat.widget.AppCompatTextView {

    interface MyScrollTextViewListener {
        fun onFinishScroll(status: Boolean?)
    }

    var listener: MyScrollTextViewListener? = null;

    fun setCustomObjectListener(listener: MyScrollTextViewListener?) {
        this.listener = listener
    }

    companion object {
        @JvmStatic
        var WaitTime : Int= 8
        var endScroll : Boolean= false
        var TextToShow : String =""
    }

    constructor (context: Context) :super(context)
    {
        setSingleLine()
        LineIndex = 0
        ellipsize = null
        marqueeRepeatLimit=1
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }
    constructor(context: Context?, attrs: AttributeSet?): super (context!!, attrs )
    {
        setSingleLine()
        LineIndex = 0
        marqueeRepeatLimit=1
        ellipsize = null
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int):super(context!!,attrs,defStyle)
    {
        setSingleLine()
        LineIndex = 0
        marqueeRepeatLimit=1
        ellipsize = null
        visibility = VISIBLE
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener) //added listener check
    }



    // scrolling feature
    private var mSlr: Scroller? = null
    private var LineIndex :Int = 0
    private var mXPaused = 0


    // whether it's being paused



    private var mScrollSpeed = 5 //Added speed for same scrolling speed regardless of text
    private var BTLTime :Int = 2



    override fun onDetachedFromWindow() {
        removeGlobalListener()
        super.onDetachedFromWindow()
    }

    fun setSpeed(mScrollSpeedValue : Int)
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
        Log.d("setTextToShow-LineIndex",LineIndex.toString())
    }

    fun startScroll() {
        try {
            endScroll=false
            var textWidth: Float = this.getPaint().measureText("M")
            var howMnayCharfortextwidth: Float = this.measuredWidth / textWidth
            var stringforLine: String = "";
            var StringForStart : String = "";
            var StringForEnd : String = "";
            var HowManyLines : Int = this.text.toString().split("\n").size
            for (i in 0 until howMnayCharfortextwidth.toInt()) {
                stringforLine += " "
            }
            for (i in 0 until WaitTime.toInt()) {
                StringForStart += stringforLine
            }
            StringForEnd+= stringforLine
            for (i in 0 until BTLTime.toInt()) {
                StringForEnd += stringforLine
            }
            endScroll = false
            var line : String = this.text.toString()
            line = line.replace("\n", stringforLine, true)
            this.text = StringForStart + line
            this.text = this.text.toString() + StringForEnd
            mXPaused = -10
            setHorizontallyScrolling(true)
            // use LinearInterpolator for steady scrolling
            mSlr = Scroller(this.context, LinearInterpolator())
            setScroller(mSlr)
            val scrollingLen = calculateScrollingLen(this.text.toString())
            var distance : Int = 0;
            distance = scrollingLen + textWidth.toInt()
            Log.d("resumeScroll-distance",distance.toString())
            //check distance for next line
            val duration = (mScrollSpeed*1000 * HowManyLines).toInt()
            visibility = VISIBLE
            Log.d("resumeScroll-TextList[mXPaused",mXPaused.toString())
            mSlr!!.startScroll(mXPaused, 0, distance, 0, duration)
            invalidate()
            removeGlobalListener()
        } catch (exc: Exception) {
            Log.d("startScroll-Exception", exc.message.toString())
            return
        }
    }

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
     * calculate the scrolling length of the text in pixel
     *
     * @return the scrolling length in pixels
     */
    private fun calculateScrollingLen(Line:String): Int {
        val length = getTextLength(Line)
        Log.d("calculateScrollingLen",(length + width).toString())
        return length + width
    }

    override fun onScrollChanged(x: Int, y: Int, oldX: Int, oldY: Int) {

        super.onScrollChanged(x, y, oldX, oldY)
    }

    private fun getTextLength(Line:String): Int {
        val tp: TextPaint = paint
        var rect: Rect? = Rect()
        val strTxt: String = Line.toString()
        tp.getTextBounds(strTxt, 0, strTxt.length, rect)
        val length: Int = rect!!.width()
        return length
    }



    /*
 * override the computeScroll to restart scrolling when finished so as that
 * the text is scrolled forever
 */   override fun computeScroll() {
        super.computeScroll()
        if (null == mSlr) return
        if (mSlr!!.isFinished && endScroll==false )
        {
            endScroll = true
            if (listener != null)  listener!!.onFinishScroll(true)

            Log.d("computeScroll mSlr!!.isFinished",endScroll.toString())

        }

    }

    fun isEndScrolling(): Boolean {
        return endScroll
    }
}