package com.example.nvpromter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.icu.text.SimpleDateFormat
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.util.*


private var mCamera: Camera? = null
private var mPreview: CameraPreview? = null
private val PERMISSION_REQUEST_CODE = 101;
private val TAG = "Permission";
private var bTnStatus :Boolean = true;
private  var recorder = MediaRecorder() ;
val MEDIA_TYPE_IMAGE = 1
val MEDIA_TYPE_VIDEO = 2
var cameraInfo :CameraInfo? = null;
var cameraCount : Int = 0;
var cameraIndex :  Int = 0;
var preview: FrameLayout? = null;
var CameraFront : Boolean =  false;


class MainActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        var Speed: Float = 5.0f
        @JvmStatic
        var TimeBetweenLines: Int = 1;
        @JvmStatic
        var TimeToStart: Int = 2;
        @JvmStatic
        var TimeToEndCapture: Int = 2;
    }
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeRequest();


        preview = findViewById(R.id.camera_preview)
        cameraInfo = CameraInfo()
        cameraCount = Camera.getNumberOfCameras()
        val btn_camera = findViewById(R.id.CameraB) as Button
        // set on-click listener
        btn_camera.setOnClickListener {

            if (cameraIndex== (cameraCount+1)) cameraIndex=0;
            getCameratoPreview(cameraIndex);
            cameraIndex += 1;

        }

        // get reference to button
        val btn_click = findViewById(R.id.button_capture) as Button
        // set on-click listener
        btn_click.setOnClickListener {
            if (bTnStatus)
            {
                btn_click.text="Stop"
                bTnStatus = false;
                StartRecord()
            }
            else
            {
                btn_click.text="Capture"
                bTnStatus = true;
                StopRecord();
            }
        }

        // get reference to button
        val btn_click_set_text = findViewById(R.id.SetText) as Button
        // set on-click listener
        btn_click_set_text.setOnClickListener {
            val intent = Intent(this, SetTextActivity::class.java).apply {
                //putExtra(EXTRA_MESSAGE, message)
            }
            startActivity(intent)
        }

    }

    /** A safe way to get an instance of the Camera object. */
    fun getCameraInstance(cAmeraid:Int): Camera? {
        return try {
            Camera.open(cAmeraid) // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    private fun getCameratoPreview(cameraId:Int) {

        mCamera = getCameraInstance(cameraId)
        Camera.getCameraInfo(cameraId, cameraInfo)
        CameraFront = cameraInfo?.facing == CameraInfo.CAMERA_FACING_FRONT;
        mCamera?.setDisplayOrientation(90)
        Log.d(TAG, "getCameratoPreview  cameraId: ${cameraId}")

        mPreview = mCamera?.let {
            // Create our Preview view

            CameraPreview(this, it)
        }

        // Set the Preview view as the content of our activity.
        mPreview?.also {
            preview?.removeView(it);

            preview?.addView(it)

        }
    }

    /*fun getFrontCameraId(): Int {
        var cameraCount = 0
        val cameraInfo = CameraInfo()
        cameraCount = Camera.getNumberOfCameras()
        for (camIdx in 0 until cameraCount) {
            Camera.getCameraInfo(camIdx, cameraInfo)
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                return camIdx
            } /*from  w  ww  .j  a va2 s . c  o m*/
        }
        return -1
    }*/

    private fun makeRequest() {
        /*   ActivityCompat.requestPermissions(this,
               arrayOf(Manifest.permission.BLUETOOTH_PRIVILEGED,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.BLUETOOTH,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_WIFI_STATE
               ,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
               PERMISSION_REQUEST_CODE
           )*/
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun StartRecord()
    {
        try {
            val PText = findViewById(R.id.PtextView) as TextView
            PText.isSelected=true;
            setMarqueeSpeed(PText, 10F)
            mCamera?.unlock();
            recorder.setCamera(mCamera)
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
            recorder.setProfile(CamcorderProfile.get(cameraIndex))
            if (!CameraFront) {
                // Back
                recorder.setOrientationHint(90);
            } else {
                // Front
                recorder.setOrientationHint(270);
            }
            recorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO));
            recorder.setPreviewDisplay(mPreview?.holder?.surface);
            recorder.prepare()
            recorder.start()
        }
     catch (e: IllegalStateException) {
        Log.d(TAG, "IllegalStateException preparing MediaRecorder: ${e.message}")
        StopRecord()

    } catch (e: IOException) {
        Log.d(TAG, "IOException preparing MediaRecorder: ${e.message}")
        StopRecord()

    }

    }

    private fun StopRecord()    {
        recorder.stop()
        recorder.reset()
        recorder.release()
        mCamera?.lock();
        mCamera?.stopPreview();
        mCamera?.release()


    }

    /** Create a file Uri for saving an image or video */
    private fun getOutputMediaFileUri(type: Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "nPromter"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory")
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
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

    override fun onRequestPermissionsResult(       requestCode: Int,        permissions: Array<String>, grantResults: IntArray    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

}