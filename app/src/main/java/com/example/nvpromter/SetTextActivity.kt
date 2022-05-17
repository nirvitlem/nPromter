package com.example.nvpromter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

private var bTnStatus :Boolean = true;
private var PText: ScrollTextView? = null;
private var TextToshow : TextView?=null
private var TextSpeed: TextView?=null
private var TextTospeedbtl: TextView?=null
private var TextTostartaftertime: TextView?=null
private var TextTostopCapture: TextView?=null
private var  btn_click : Button? = null

class SetTextActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_text)

        TextToshow = findViewById<TextView>(R.id.TextPad)
        TextSpeed = findViewById<TextView>(R.id.textSetSpeed)
        TextTospeedbtl = findViewById<TextView>(R.id.textSetTimeBetweenLines)
        TextTostartaftertime = findViewById<TextView>(R.id.TextSetStartTime)
        TextTostopCapture = findViewById<TextView>(R.id.TextSetSTopCapture)
        PText = findViewById<ScrollTextView>(R.id.PvtextView)


        PText!!.setCustomObjectListener( object : ScrollTextView.MyScrollTextViewListener {
            override fun onFinishScroll(status: Boolean?)
            {
                if (status == true) {
                    btn_click!!.text = "Preview"
                    bTnStatus = true;
                }
            }

        })


        // get reference to button
        btn_click = findViewById<Button>(R.id.Pbutton)
        // set on-click listener
        btn_click?.setOnClickListener {
            if (bTnStatus)
            {
                btn_click!!.text="Stop"
                bTnStatus = false;
                StartPriview()
            }
            else
            {
                btn_click!!.text="Preview"
                bTnStatus = true;
                //PText=null
            }
        }

        // get reference to button
        val btn_close = findViewById(R.id.BClose) as Button
        // set on-click listener
        btn_close.setOnClickListener {

            PText!!.setTextToShow(TextToshow?.text.toString())
            ScrollTextView.TextToShow = TextToshow?.text.toString();
            PText!!.setSpeed(TextSpeed?.text.toString().toInt())
            PText!!.setTimeBTLines(TextTospeedbtl?.text.toString().toInt())
            PText!!.setPauseStartTime(TextTostartaftertime?.text.toString().toInt())
            ScrollTextView.WaitTime =  TextTostopCapture?.text.toString().toInt()
            PText!!.setTextColor(Color.RED);
            MainActivity.ScrollTextViewObject = PText
            finish();
        }
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun StartPriview()
    {

        hideKeyboard()

        PText!!.setTextToShow(TextToshow?.text.toString())
        PText!!.setSpeed(TextSpeed?.text.toString().toInt())
        PText!!.setTimeBTLines(TextTospeedbtl?.text.toString().toInt())
        PText!!.setPauseStartTime(TextTostartaftertime?.text.toString().toInt())
        ScrollTextView.WaitTime =  TextTostopCapture?.text.toString().toInt()
        PText!!.setTextColor(Color.RED);
        PText?.startScroll();

    }


}