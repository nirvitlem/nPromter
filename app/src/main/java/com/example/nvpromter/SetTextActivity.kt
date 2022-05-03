package com.example.nvpromter

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import java.lang.reflect.Field

private var bTnStatus :Boolean = true;
private var PText: ScrollTextView? = null;
var TextToshow : TextView?=null
var TextSpeed: TextView?=null
var TextTospeedbtl: TextView?=null
var TextTostartaftertime: TextView?=null
var TextTostopCapture: TextView?=null

class SetTextActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_text)

        TextToshow = findViewById(R.id.TextPad) as TextView
        TextSpeed = findViewById(R.id.textSetSpeed) as TextView
        TextTospeedbtl = findViewById(R.id.textSetTimeBetweenLines) as TextView
        TextTostartaftertime = findViewById(R.id.TextSetStartTime) as TextView
        TextTostopCapture = findViewById(R.id.TextSetSTopCapture) as TextView

        // get reference to button
        val btn_click = findViewById(R.id.Pbutton) as Button
        // set on-click listener
        btn_click.setOnClickListener {
            if (bTnStatus)
            {
                btn_click.text="Stop"
                bTnStatus = false;
                StartPriview()
            }
            else
            {
                btn_click.text="Preview"
                bTnStatus = true;
                PText=null;
            }
        }

        // get reference to button
        val btn_close = findViewById(R.id.BClose) as Button
        // set on-click listener
        btn_close.setOnClickListener {
            finish();
        }
    }

    fun StartPriview()
    {
        PText = findViewById(R.id.PvtextView) as ScrollTextView
        PText!!.setText(TextToshow?.text)
        PText!!.setSpeed(TextSpeed.toString().toFloat())
        PText!!.setTextColor(Color.RED);
        PText?.startScroll();
    }

    fun setMarqueeSpeed(tv: TextView?, speed: Float) {
        if (tv != null) {
            try {
                var f: Field? = null
                f = if (tv is AppCompatTextView) {
                    tv.javaClass.superclass.getDeclaredField("mMarquee")
                } else {
                    tv.javaClass.getDeclaredField("mMarquee")
                }
                if (f != null) {
                    f.setAccessible(true)
                    val marquee: Any = f.get(tv)
                    if (marquee != null) {
                        var scrollSpeedFieldName = "mScrollUnit"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            scrollSpeedFieldName = "mPixelsPerSecond"
                        }
                        val mf: Field = marquee.javaClass.getDeclaredField(scrollSpeedFieldName)
                        mf.setAccessible(true)
                        mf.setFloat(marquee, speed)
                    }
                } else {
                    Log.e("Marquee", "mMarquee object is null.")
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}