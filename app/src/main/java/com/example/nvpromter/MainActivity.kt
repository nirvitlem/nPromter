package com.example.nvpromter

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.icu.text.SimpleDateFormat
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException
import java.util.*


private var mCamera: Camera? = null
private var mPreview: CameraPreview? = null
private val PERMISSION_REQUEST_CODE = 101;
private val TAG = "Permission";
private var bTnStatus :Boolean = true;
private  var recorder = MediaRecorder() ;
val MEDIA_TYPE_IMAGE = 1
val MEDIA_TYPE_VIDEO = 2




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeRequest();

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
        // Create an instance of Camera
        mCamera = getCameraInstance()

        mPreview = mCamera?.let {
            // Create our Preview view
            CameraPreview(this, it)
        }

        // Set the Preview view as the content of our activity.
        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
        }
    }

    /** A safe way to get an instance of the Camera object. */
    fun getCameraInstance(): Camera? {
        return try {
            Camera.open(getFrontCameraId()) // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }
    fun getFrontCameraId(): Int {
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
    }

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
            mCamera?.unlock();
            recorder.setCamera(mCamera)
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
            recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
            recorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO));
            recorder.setPreviewDisplay(mPreview?.holder?.surface);
        /*    recorder?.apply {
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)
            }*/
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

    private fun StopRecord()
    {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
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