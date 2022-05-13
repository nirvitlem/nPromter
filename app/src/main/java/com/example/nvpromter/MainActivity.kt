package com.example.nvpromter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.icu.text.SimpleDateFormat
import android.media.CamcorderProfile
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*

const val CAMERA_REQUEST_RESULT = 101

private val PERMISSION_REQUEST_CODE = 101;
private val TAG = "Permission";
private var bTnStatus :Boolean = true;
private var runningBackgroundThread : Boolean =false;
private var  surfaceTexture : SurfaceTexture? = null
private var previewSurface: Surface?=null
private var cameraCharacteristics : CameraCharacteristics?=null
private  var textureView: TextureView? = null
private var backgroundHandlerThread: HandlerThread?= null
private var backgroundHandler: Handler? = null
private  var cameraManager: CameraManager? = null
private  var cameraDevice: CameraDevice? = null
private  var captureRequestBuilder: CaptureRequest.Builder? = null
private  var cameraCaptureSession: CameraCaptureSession? = null
private  var imageReader: ImageReader? = null
private  var previewSize: Size? = null
private  var videoSize: Size? = null
private var shouldProceedWithOnResume: Boolean = true
private var CameraFront : Boolean = false
private var orientations : SparseIntArray = SparseIntArray(4).apply {
    append(Surface.ROTATION_0, 0)
    append(Surface.ROTATION_90, 90)
    append(Surface.ROTATION_180, 180)
    append(Surface.ROTATION_270, 270)
}

var cameraCount : Int = 0;
var cameraIndex :  Int = 0;
private var PMText: ScrollTextView? = null;


class MainActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        var ScrollTextViewObject: ScrollTextView? = null
    }



    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording: Boolean = false


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            makeRequest();


            textureView = findViewById<TextureView>(R.id.tVcamera_preview)
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraCount= cameraManager!!.cameraIdList.size

            val btn_camera = findViewById<Button>(R.id.CameraB)
            // set on-click listener
            btn_camera.setOnClickListener {

                if (cameraIndex == (cameraCount )) cameraIndex = 0;
                setupCamera()
                connectCamera()
                cameraIndex += 1;

            }
            // get reference to button
            val btn_click = findViewById<Button>(R.id.button_capture)
            // set on-click listener
            btn_click.setOnClickListener {
                if (bTnStatus) {
                    btn_click.text = "Stop"
                    bTnStatus = false;
                    mediaRecorder = MediaRecorder()
                    setupMediaRecorder()
                    startRecording()
                } else {
                    btn_click.text = "Capture"
                    bTnStatus = true;
                    mediaRecorder.stop()
                    mediaRecorder.reset()
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
            if (!wasCameraPermissionWasGiven()) {
               requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_RESULT)
            }
        }



    private fun makeRequest() {

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


    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView!!.isAvailable && shouldProceedWithOnResume) {
            setupCamera()
        } else if (!textureView!!.isAvailable){
            textureView!!.surfaceTextureListener = surfaceTextureListener
        }
        shouldProceedWithOnResume = !shouldProceedWithOnResume
    }

    private fun setupCamera() {
        Log.d("setupCamera-cameraIndex", cameraIndex.toString())
        // val cameraIds: Array<String> = cameraManager.cameraIdList

        // for (id in cameraIds) {
        if ( surfaceTexture!=null)
        {
         //   if (!surfaceTexture!!.isReleased ) surfaceTexture!!.release()
        }

        if ( cameraDevice!= null)
        {
            cameraDevice!!.close()
            cameraDevice = null;
        }
        cameraCharacteristics = cameraManager?.getCameraCharacteristics(cameraIndex.toString())

        //If we want to choose the rear facing camera instead of the front facing one
        CameraFront = cameraCharacteristics?.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT

        val streamConfigurationMap: StreamConfigurationMap? = cameraCharacteristics!!.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        if (streamConfigurationMap != null) {
            previewSize =
                cameraCharacteristics!!.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                    .getOutputSizes(
                        ImageFormat.JPEG
                    ).maxByOrNull { it.height * it.width }!!
            videoSize =
                cameraCharacteristics!!.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                    .getOutputSizes(MediaRecorder::class.java)
                    .maxByOrNull { it.height * it.width }!!
            imageReader = ImageReader.newInstance(
                previewSize!!.width,
                previewSize!!.height,
                ImageFormat.JPEG,
                1
            )
            imageReader!!.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
            //    }
        }
    }

    private fun takePhoto() {
        captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder?.addTarget(imageReader!!.surface)
        val rotation = windowManager.defaultDisplay.rotation
        captureRequestBuilder!!.set(CaptureRequest.JPEG_ORIENTATION, orientations.get(rotation))
        cameraCaptureSession!!.capture(captureRequestBuilder!!.build(), captureCallback, null)
    }

    @SuppressLint("MissingPermission")
    private fun connectCamera() {
        cameraManager!!.openCamera(cameraIndex.toString(), cameraStateCallback, backgroundHandler)
    }

    private fun setupMediaRecorder() {
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setVideoSize(videoSize!!.width, videoSize!!.height)
        mediaRecorder.setVideoFrameRate(30)
        if (!CameraFront) {
            // Back
            mediaRecorder.setOrientationHint(90);
        } else {
            // Front
            mediaRecorder.setOrientationHint(270);
        }
        mediaRecorder.setOutputFile(createFile()?.absolutePath)
        mediaRecorder.setVideoEncodingBitRate(10_000_000)
        mediaRecorder.prepare()
    }

    private fun startRecording() {
        val surfaceTexture: SurfaceTexture? = textureView!!.surfaceTexture
        surfaceTexture?.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)
        val previewSurface: Surface = Surface(surfaceTexture)
        val recordingSurface = mediaRecorder.surface

       /* PMText = findViewById<ScrollTextView>(R.id.PtextView)
        PMText!!.setTextToShow(ScrollTextViewObject?.text.toString())
        PMText!!.setTextColor(Color.RED);
        PMText?.startScroll();*/

        captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequestBuilder!!.addTarget(previewSurface)
        captureRequestBuilder!!.addTarget(recordingSurface)

        cameraDevice!!.createCaptureSession(
            listOf(previewSurface, recordingSurface),
            captureStateVideoCallback,
            backgroundHandler
        )
    }

    /**
     * Surface Texture Listener
     */

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        @SuppressLint("MissingPermission")
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            if (wasCameraPermissionWasGiven()) {
                setupCamera()
                connectCamera()
                if (cameraIndex == (cameraCount + 1)) cameraIndex = 0;
                cameraIndex++
            }
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {

        }
    }

    /**
     * Camera State Callbacks
     */

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d("cameraStateCallback-onOpened",camera.id.toString() )
            cameraDevice = camera
            surfaceTexture = textureView!!.surfaceTexture
            surfaceTexture?.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)
            previewSurface = Surface(surfaceTexture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(previewSurface!!)

            cameraDevice!!.createCaptureSession(listOf(previewSurface, imageReader!!.surface), captureStateCallback, null)
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Log.d("cameraStateCallback-onDisconnected",cameraDevice.id.toString() )
        }

        override fun onClosed(camera: CameraDevice) {
            super.onClosed(camera)
            Log.d("cameraStateCallback-onClosed",camera.id.toString() )
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            val errorMsg = when(error) {
                ERROR_CAMERA_DEVICE -> "Fatal (device)"
                ERROR_CAMERA_DISABLED -> "Device policy"
                ERROR_CAMERA_IN_USE -> "Camera in use"
                ERROR_CAMERA_SERVICE -> "Fatal (service)"
                ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                else -> "Unknown"
            }
            Log.e(TAG, "Error when trying to connect camera $errorMsg")
            Log.d("cameraStateCallback-onError", "Error when trying to connect camera $errorMsg " + cameraDevice.id.toString())
        }
    }

    /**
     * Background Thread
     */
    private fun startBackgroundThread() {
        Log.d("startBackgroundThread-cameraIndex",cameraIndex.toString())
        runningBackgroundThread = true;
        backgroundHandlerThread = HandlerThread("CameraVideoThread")
        backgroundHandlerThread!!.start()
        backgroundHandler = Handler(backgroundHandlerThread!!.looper)
    }

    private fun stopBackgroundThread() {
        Log.d("stopBackgroundThread-cameraIndex",cameraIndex.toString())
        runningBackgroundThread = false
        backgroundHandlerThread!!.quitSafely()
        backgroundHandlerThread!!.join()
    }

    /**
     * Capture State Callback
     */

    private val captureStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.d("captureStateCallback-onConfigureFailed", session.device.id.toString())
        }
        override fun onConfigured(session: CameraCaptureSession) {
            Log.d("captureStateCallback-onConfigured", session.device.id.toString())
            cameraCaptureSession = session

            cameraCaptureSession!!.setRepeatingRequest(
                captureRequestBuilder!!.build(),
                null,
                backgroundHandler
            )
        }
    }

    private val captureStateVideoCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.d("captureStateVideoCallback-onConfigureFailed", session.device.id.toString())
            Log.e(TAG, "Configuration failed")
        }
        override fun onConfigured(session: CameraCaptureSession) {
            Log.d("captureStateVideoCallback-onConfigured", session.device.id.toString())
            cameraCaptureSession = session
            captureRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureResult.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
            try {
                cameraCaptureSession!!.setRepeatingRequest(
                    captureRequestBuilder!!.build(), null,
                    backgroundHandler
                )
                mediaRecorder.start()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                Log.e(TAG, "Failed to start camera preview because it couldn't access the camera")
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Capture Callback
     */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) {
            Log.d("captureCallback-onCaptureStarted", session.device.id.toString())
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            Log.d("captureCallback-onCaptureProgressed", session.device.id.toString())
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            Log.d("captureCallback-onCaptureCompleted", session.device.id.toString())
        }
    }

    /**
     * ImageAvailable Listener
     */
    val onImageAvailableListener = object: ImageReader.OnImageAvailableListener{
        override fun onImageAvailable(reader: ImageReader) {
            Toast.makeText(this@MainActivity, "Photo Taken!", Toast.LENGTH_SHORT).show()
            val image: Image = reader.acquireLatestImage()
            image.close()
        }
    }

    /**
     * File Creation
     */

    private fun createFile(): File? {
        val sdf = java.text.SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "nPromter"
        )
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory")
                    return null
                }
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return  File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
    }

    private fun wasCameraPermissionWasGiven() : Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            return true
        }

        return false
    }
    /** Create a file Uri for saving an image or video */



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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