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
                PText!!.pauseScroll();
                PText=null
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
        PText!!.setTextToShow(TextToshow?.text.toString())
      //  PText!!.setSpeed(TextSpeed?.text.toString().toFloat())
      //  PText!!.setTimeBTLines(TextTospeedbtl?.text.toString().toInt())
        PText!!.setTextColor(Color.RED);
        PText?.startScroll();
    }


}