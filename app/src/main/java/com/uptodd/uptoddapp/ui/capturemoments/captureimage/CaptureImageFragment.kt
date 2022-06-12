package com.uptodd.uptoddapp.ui.capturemoments.captureimage


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.*
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentCaptureImageBinding
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.Timestamp
import java.util.*


//class CaptureImageFragment : Fragment(), SurfaceHolder.Callback {

/*private lateinit var binding:FragmentCaptureImageBinding

private var camera: Camera?=null
private lateinit var surfaceHolder: SurfaceHolder
private lateinit var pictureCallback: Camera.PictureCallback

private var pictureBitmap: Bitmap?=null

private var fileName:String?=null

private var cameraType:String="back"
private var flash:Boolean=false

override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {

    binding= DataBindingUtil.inflate(
        inflater,
        R.layout.fragment_capture_image,
        container,
        false
    )
    binding.lifecycleOwner = this

    surfaceHolder=binding.surfaceView.holder
    surfaceHolder.addCallback(this)
    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

    binding.buttonCapture.setOnClickListener { onClickCapture() }

    if (Camera.getNumberOfCameras() > 1) {
        binding.imageButtonFlipCamera.visibility = View.VISIBLE
    }
    if (activity?.packageManager!!.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_FLASH
        )) {
        binding.imageButtonFlash.visibility = View.GONE
    }

    pictureCallback=Camera.PictureCallback { bytes: ByteArray, camera: Camera ->
        onPictureTaken(bytes, camera)
    }

    binding.imageButtonFlash.setOnClickListener{onClickFlash()}

    return binding.root
}

override fun surfaceCreated(holder: SurfaceHolder) {
    Log.d("div", "CaptureImageFragment L57 Surface Created")
    try{camera= Camera.open()}
    catch (e: Exception){ Log.d("div", "CaptureImageFragment L59 Error $e")}

    /*val displayMetrics = DisplayMetrics()
    activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
    val height = displayMetrics.heightPixels
    val width = displayMetrics.widthPixels
    Log.d("div","CaptureImageFragment L70 $height $width")*/


    val parameters=camera!!.parameters

    val allSizes: List<Camera.Size> = parameters.supportedPictureSizes
    var size: Camera.Size = allSizes[0]
    for (i in allSizes.indices)
        if (allSizes[i].width > size.width && allSizes[i].height>size.height) size = allSizes[i]
    parameters.setPictureSize(size.width, size.height)

    parameters.previewFrameRate=30

    val allPreviewSizes: List<Camera.Size> = parameters.supportedPreviewSizes
    var previewSize: Camera.Size = allPreviewSizes[0]
    for (i in allPreviewSizes.indices)
        if (allPreviewSizes[i].width >previewSize.width && allPreviewSizes[i].height>previewSize.height) previewSize = allPreviewSizes[i]
    parameters.setPreviewSize(previewSize.width, previewSize.height)
    Log.d(
        "div",
        "CaptureImageFragment L87 ${size.width} ${size.height} ${previewSize.width} ${previewSize.height}"
    )
    camera!!.parameters=parameters
    camera!!.setDisplayOrientation(90)

    try{
        camera!!.setPreviewDisplay(surfaceHolder)
        camera!!.startPreview()
    }
    catch (e: IOException){
        Log.d("div", "CaptureImageFragment L65 Error $e")
    }
}

override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    Log.d("div", "CaptureImageFragment L76 Surface Changed")
}

override fun surfaceDestroyed(holder: SurfaceHolder) {
    Log.d("div", "CaptureImageFragment L80 Surface Destroyed")
    camera!!.stopPreview()
    camera!!.release()
    camera=null
}

private fun onClickCapture()
{
    camera!!.takePicture(null, null, pictureCallback)
}

private fun onPictureTaken(bytes: ByteArray, camera: Camera)
{
    val bitmap:Bitmap=BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    pictureBitmap=Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, null, true)

    fileName=getFileName()
    Log.d("div", "CaptureImageFragment L77 $fileName $pictureBitmap")
    storePhotoToStorage(pictureBitmap, fileName)
    //this.camera!!.startPreview()
}

private fun getFileName(): String? {
    val dateFormat=SimpleDateFormat("yyyyMMdd_HHmmss")
    return dateFormat.format(Date())
}

private fun storePhotoToStorage(pictureBitmap: Bitmap?, fileName: String?) {

}

override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)

    val currentOrientation = resources.configuration.orientation
    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
        camera!!.setDisplayOrientation(0)
    } else {
        camera!!.setDisplayOrientation(90)
    }
}

private fun onClickFlash()
{
    if (camera != null) {
        try {
            val param: Camera.Parameters = camera!!.parameters
            param.flashMode = if (!flash) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = param
            flash = !flash
        } catch (e: java.lang.Exception) {
            Log.d("div","CaptureImageFragment L179 $e")
        }
    }
}
}*/


/*private lateinit var binding: FragmentCaptureImageBinding

private lateinit var surfaceView: SurfaceView
private lateinit var surfaceHolder: SurfaceHolder
private var camera: Camera? = null
private lateinit var flipCamera: ImageButton
private lateinit var flashCameraButton: ImageButton
private lateinit var captureImage: Button
private var cameraId = 0
private var flashmode = false
private var rotation = 0

private var cameraPermissinGranted:Boolean=false
private var storagePermissinGranted:Boolean=false

private val STORAGE_PERMISSION_REQUEST_CODE=0
private val CAMERA_PERMISSION_REQUEST_CODE=1

private var previousActivity:String?="Home"                          //Enter the previous activity here and get it through intent also
override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {

    (requireActivity() as AppCompatActivity?)?.supportActionBar?.title="Capture Moments"
    (requireActivity() as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true);
    setHasOptionsMenu(true)

    cameraPermissinGranted=hasCameraPermission()
    storagePermissinGranted=hasStoragePermission()

    if(cameraPermissinGranted && storagePermissinGranted) {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_capture_image,
            container,
            false
        )
        binding.lifecycleOwner = this

        val intent = activity?.intent
        previousActivity = intent!!.getStringExtra("Previous activity")

        cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        flipCamera = binding.imageButtonFlipCamera
        flashCameraButton = binding.imageButtonFlash
        captureImage = binding.buttonCapture
        surfaceView = binding.surfaceView
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
        flipCamera.setOnClickListener { flipCamera() }
        captureImage.setOnClickListener { takeImage() }
        flashCameraButton.setOnClickListener { flashOnButton() }
        binding.imageButtonGallery.setOnClickListener { onClickGallery() }
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Camera.getNumberOfCameras() > 1) {
            flipCamera.visibility = View.VISIBLE
        }
        if (!activity?.baseContext?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)!!) {
            flashCameraButton.visibility = View.GONE
        }

        DrawableCompat.setTint(binding.imageButtonGallery.drawable, Color.WHITE)
        DrawableCompat.setTint(binding.imageButtonFlipCamera.drawable, Color.WHITE)
        DrawableCompat.setTint(binding.imageButtonFlash.drawable, Color.WHITE)
    }
    else{
        Toast.makeText(activity,"Permission denied",Toast.LENGTH_LONG).show()
        activity?.fragmentManager?.popBackStack();
    }

    return binding.root
}


private fun hasStoragePermission():Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(requireActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
            return storagePermissinGranted;
        } else {
            return true;
        }
    } else {
        return true;
    }
}

private fun hasCameraPermission():Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(requireActivity(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf<String>(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            return cameraPermissinGranted;
        } else {
            return true;
        }
    } else {
        return true;
    }
}

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults.size > 1
        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE)
            cameraPermissinGranted=true
        if(requestCode==STORAGE_PERMISSION_REQUEST_CODE)
            storagePermissinGranted=true
    }
}

override fun surfaceCreated(holder: SurfaceHolder) {
    if (!openCamera(CameraInfo.CAMERA_FACING_BACK)) {
        alertCameraDialog()
    }
}

private fun openCamera(id: Int): Boolean {
    var result = false
    cameraId = id
    releaseCamera()
    try {
        camera = Camera.open(cameraId)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    if (camera != null) {
        try {
            setUpCamera(camera!!)
            camera!!.setErrorCallback { error, camera -> }
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
            result = false
            releaseCamera()
        }
    }
    return result
}

private fun setUpCamera(c: Camera) {
    val info = CameraInfo()
    Camera.getCameraInfo(cameraId, info)
    rotation = activity?.windowManager?.defaultDisplay!!.rotation
    var degree = 0
    when (rotation) {
        Surface.ROTATION_0 -> degree = 0
        Surface.ROTATION_90 -> degree = 90
        Surface.ROTATION_180 -> degree = 180
        Surface.ROTATION_270 -> degree = 270
        else -> {
        }
    }
    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
        rotation = (info.orientation + degree) % 330
        Log.d("div","CaptureImageFragement L364 $rotation ${info.orientation} $degree")
        rotation = (360 - rotation) % 360
        Log.d("div","CaptureImageFragement L364 $rotation ")
    } else {
        rotation = (info.orientation - degree + 360) % 360
        Log.d("div","CaptureImageFragement L364 $rotation ${info.orientation} $degree")
    }
    c.setDisplayOrientation(rotation)
    val params: Camera.Parameters = c.parameters

    params.jpegQuality = 100
    /*val allSizes: List<Camera.Size> = params.supportedPictureSizes
    var size: Camera.Size = allSizes[0]
    for (i in allSizes.indices)
        if ( allSizes[i].height>size.height) size = allSizes[i]
    params.setPictureSize(size.width, size.height)

    val allPreviewSizes: List<Camera.Size> = params.supportedPreviewSizes
    var previewSize: Camera.Size = allPreviewSizes[0]
    for (i in allPreviewSizes.indices)
        if (allPreviewSizes[i].height>previewSize.height) previewSize = allPreviewSizes[i]
    params.setPreviewSize(previewSize.width, previewSize.height)*/

    showFlashButton(params)
    val focusModes: List<String> = params.supportedFocusModes
    if (focusModes != null) {
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
        {
            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        }
    }
    params.setRotation(rotation)
}

private fun showFlashButton(params: Camera.Parameters) {
    val showFlash = (activity?.packageManager!!.hasSystemFeature(
        PackageManager.FEATURE_CAMERA_FLASH
    ) && params.flashMode != null
            && params.supportedFlashModes != null && params.supportedFocusModes
        .size > 1)
    flashCameraButton.visibility = if (showFlash) View.VISIBLE else View.INVISIBLE
}

private fun releaseCamera() {
    try {
        if (camera != null) {
            camera!!.setPreviewCallback(null)
            camera!!.setErrorCallback(null)
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("div", e.toString())
        camera = null
    }
}

override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
}

override fun surfaceDestroyed(holder: SurfaceHolder) {}

private fun takeImage() {
    if(camera!=null)
    {
        try {
            val param: Camera.Parameters = camera!!.parameters
            param.flashMode = if (flashmode) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = param
            if(flashmode)
                Thread.sleep(2000)
        }
        catch (e: Exception)
        {Log.d("div", "CaptureImageFragment L477 $e")}
    }
    camera!!.takePicture(null, null, object : PictureCallback {
        override fun onPictureTaken(data: ByteArray, camera: Camera) {
            try {
                var loadedImage: Bitmap? = null
                var rotatedBitmap: Bitmap? = null
                loadedImage = BitmapFactory.decodeByteArray(data, 0, data.size)

                val rotateMatrix = Matrix()
                val info = CameraInfo()
                Camera.getCameraInfo(cameraId, info)
                if(info.facing == CameraInfo.CAMERA_FACING_FRONT)
                    rotation-=180
                rotateMatrix.postRotate(rotation.toFloat())
                rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.width, loadedImage.height, rotateMatrix, false)
                saveToFile(rotatedBitmap)
            } catch (e: Exception) {
                if (camera != null) {
                    try {
                        val param: Camera.Parameters = camera.parameters
                        param.flashMode = Camera.Parameters.FLASH_MODE_OFF
                        camera.parameters = param
                    } catch (e: Exception) {
                        Log.d("div", "CaptureImageFragment L442 $e")
                    }
                }
                e.printStackTrace()
            }
        }
    })
    if(camera!=null)
    {
        try {
            val param: Camera.Parameters = camera!!.parameters
            param.flashMode =Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = param
        }
        catch (e: Exception)
        {Log.d("div", "CaptureImageFragment L477 $e")}
    }
}

private fun flipCamera() {
    val id =
        (if (cameraId == CameraInfo.CAMERA_FACING_BACK) CameraInfo.CAMERA_FACING_FRONT else CameraInfo.CAMERA_FACING_BACK)
    if (!openCamera(id)) {
        alertCameraDialog()
    }
    DrawableCompat.setTint(binding.imageButtonFlipCamera.drawable, Color.WHITE)
}

private fun alertCameraDialog() {
    val dialog: AlertDialog.Builder = createAlert(
        requireActivity(),
        "Camera info", "Error in opening camera"
    )
    dialog.setNegativeButton("OK",
        DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
    dialog.show()
}

private fun createAlert(context: Context, title: String?, message: String): AlertDialog.Builder {
    val dialog: AlertDialog.Builder = AlertDialog.Builder(
        ContextThemeWrapper(
            context,
            android.R.style.Theme_Holo_Light_Dialog
        )
    )
    dialog.setIcon(R.drawable.app_icon)
    if (title != null) dialog.setTitle(title) else dialog.setTitle("Information")
    dialog.setMessage(message)
    dialog.setCancelable(false)
    return dialog
}

private fun flashOnButton() {
    if (camera != null) {
        try {
            /*val param: Camera.Parameters = camera!!.parameters
            param.flashMode = if (!flashmode) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = param*/
            flashmode = !flashmode
            if(flashmode)
                binding.imageButtonFlash.setImageResource(R.drawable.ic_baseline_flash_on_24)
            else
                binding.imageButtonFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
        } catch (e: Exception) {
            Log.d("div", "CaptureImageFragment L477 $e")
        }
    }
    DrawableCompat.setTint(binding.imageButtonFlash.drawable, Color.WHITE)
}

private fun onClickGallery()
{
    val pickPhoto = Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI)
    startActivityForResult(pickPhoto, 1)
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    //super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == RESULT_OK && data != null && data.data!=null) {
        val selectedImage = data.data
        val bitmap:Bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImage)
        saveToFile(bitmap)


    }
    else
        Toast.makeText(activity,"Failed to choose image",Toast.LENGTH_LONG)
}

private fun saveToFile(bitmap:Bitmap)
{
    var imageFile: File? = null
    try {
        val state: String = Environment.getExternalStorageState()
        var folder: File? = null
        if (state.contains(Environment.MEDIA_MOUNTED)) {
            folder = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/UpToddCards")
        } else {
            folder = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/UpToddCards")
        }
        var success = true
        if (!folder.exists()) {
            success = folder.mkdirs()
        }
        if (success) {
            val date = Date()
            imageFile = File(
                folder.absolutePath + File.separator + Timestamp(date.time).toString().toString() + "Image.jpg")
            imageFile.createNewFile()
        } else {
            Toast.makeText(activity, "Image Not saved", Toast.LENGTH_SHORT).show()
            return
        }
        val ostream = ByteArrayOutputStream()

        bitmap.compress(CompressFormat.JPEG, 100, ostream)
        val fout = FileOutputStream(imageFile)
        fout.write(ostream.toByteArray())
        fout.close()
        val values = ContentValues()
        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
        activity?.contentResolver?.insert(Images.Media.EXTERNAL_CONTENT_URI, values)
        Log.d("div","CaptureImageFragment L536 path= ${imageFile.absolutePath}")


        if(// previousActivity=="Home"
        true ) {
            val bundle=Bundle()
            bundle.putString("imagePath",imageFile.absolutePath)

            view?.findNavController()?.navigate(R.id.action_captureImageFragment_to_selectTypeFragment, bundle)
        }
        else {
            val bundle=Bundle()
            bundle.putString("imagePath",imageFile.absolutePath)
            bundle.putString("photoType","Parent")                          //Enter the photoType here
            view?.findNavController()?.navigate(R.id.action_captureImageFragment_to_generateCardFragment,bundle)
        }
    }
    catch(e:Exception)
    {Log.d("div","CaptureImageFragment L538 $e")}
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
        android.R.id.home -> {
            activity?.onBackPressed()
            return true
        }
    }
    return super.onOptionsItemSelected(item)
}
}*/

class CaptureImageFragment : Fragment() {
    private lateinit var binding: FragmentCaptureImageBinding

    private val FLASH_OFF = 0
    private val FLASH_ON = 1
    private val FLASH_AUTO = 2
    private var cameraFacing = true           //true for back and false for front
    private var flashMode = 0

    private val FOLDER_NAME = "UpToddImages"
    private val DEFAULT_CARD_CATEGORY =
        "Tasks"                   //Enter the photoType here if the previous page was not home page

    private var cameraPermissinGranted: Boolean = false
    private var storagePermissinGranted: Boolean = false

    private val STORAGE_PERMISSION_REQUEST_CODE = 0
    private val CAMERA_PERMISSION_REQUEST_CODE = 1

    private var previousActivity: String? =
        "Home"              //Enter the previous activity here and get it through intent also

    private val requiredBitmapSize: Int = 900
    private var onCap: OnCaptureListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        arguments?.let {
            previousActivity = it.getString("previous")
        }
//        val arguments=CaptureImageFragmentArgs.fromBundle(requireArguments())
//        previousActivity=arguments.pervious
        Log.d("div", "CaptureImageFragment L648 $previousActivity $arguments")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_capture_image,
            container,
            false
        )

        (activity as AppCompatActivity?)?.supportActionBar?.title =
            getString(R.string.capture_moments)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        binding.cameraView!!.setLifecycleOwner(this)

        binding.cameraView!!.addCameraListener(object : CameraListener() {
            override fun onCameraError(exception: CameraException) {
                Log.e("camera error", exception.localizedMessage)
            }

            override fun onCameraOpened(options: CameraOptions) {

            }

            override fun onOrientationChanged(orientation: Int) {

            }

        })


        val orientation = this.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // code for portrait mode
            CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
            binding.cameraView!!.setLifecycleOwner(this)
            binding.cameraView!!.mode = Mode.PICTURE
            checkFlashPresent()
            checkFrontCameraPresent()


            //cameraPermissinGranted = hasCameraPermission()
            hasStoragePermission()
            hasCameraPermission()


            if (storagePermissinGranted) {


                binding.imageButtonFlipCamera!!.setOnClickListener { onClickFlipCamera() }
                binding.buttonCapture!!.setOnClickListener { onClickCapture() }
                binding.imageButtonFlash!!.setOnClickListener { onClickFlash() }
                binding.imageButtonGallery!!.setOnClickListener { onClickGallery() }
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            } else {
                Toast.makeText(activity, getString(R.string.permission_denied), Toast.LENGTH_LONG)
                    .show()
                requireActivity().onBackPressed()
            }
        } else {
            binding.cameraView!!.mode = Mode.PICTURE
            binding.cameraView!!.setLifecycleOwner(viewLifecycleOwner)

            checkFlashPresent()
            checkFrontCameraPresent()
            // code for landscape mode
        }



        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCap = (activity as TodosListActivity)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (onCap != null)
                    onCap?.onCapturedAttach()
            }, 1000
        )
    }


    private fun hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            } else {
                storagePermissinGranted = true
            }
        } else {
            storagePermissinGranted = true
        }
        binding.cameraView!!.open()
    }

    private fun hasCameraPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
                return cameraPermissinGranted
            } else {
                return true
            }
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE)
                cameraPermissinGranted = true
            if (requestCode == STORAGE_PERMISSION_REQUEST_CODE)
                storagePermissinGranted = true
        }

    }

    private fun onClickGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImage = data.data
            val bitmap: Bitmap = Images.Media.getBitmap(
                activity?.contentResolver,
                selectedImage
            )
            saveToFile(bitmap)
        } else
            Toast.makeText(activity, getString(R.string.failed_to_choose_image), Toast.LENGTH_LONG)
    }

    private fun saveToFile(bitmap: Bitmap) {
        var imageFile: File? = null
        try {
            val state: String = Environment.getExternalStorageState()
            var folder: File? = null
            folder = File(
                requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    .toString() + "/UpTodd/$FOLDER_NAME"
            )
            var success = true
            if (!folder.exists()) {
                success = folder.mkdirs()
            }
            if (success) {
                val date = Date()
                imageFile = File(
                    folder.absolutePath + File.separator + Timestamp(date.time).toString() + "Image.jpg"
                )
                imageFile.createNewFile()
            } else {
                Toast.makeText(activity, getString(R.string.image_not_saved), Toast.LENGTH_SHORT)
                    .show()
                return
            }
            val ostream = ByteArrayOutputStream()

            bitmap.compress(CompressFormat.JPEG, 100, ostream)
            val fout = FileOutputStream(imageFile)
            fout.write(ostream.toByteArray())
            fout.close()
            val values = ContentValues()
            values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
            activity?.contentResolver?.insert(Images.Media.EXTERNAL_CONTENT_URI, values)
            Log.d("div", "CaptureImageFragment L536 path= ${imageFile.absolutePath}")

            navigate(imageFile.absolutePath)
        } catch (e: Exception) {
            Log.d("div", "CaptureImageFragment L538 $e")
        }
    }

    private fun onClickFlash() {
        flashMode = (flashMode + 1) % 3
        if (flashMode == FLASH_ON) {
            binding.cameraView!!.flash = Flash.ON
            binding.imageButtonFlash!!.setImageResource(R.drawable.ic_baseline_flash_on_24)
        } else if (flashMode == FLASH_OFF) {
            binding.cameraView!!.flash = Flash.OFF
            binding.imageButtonFlash!!.setImageResource(R.drawable.ic_baseline_flash_off_24)
        } else {
            binding.cameraView!!.flash = Flash.AUTO
            binding.imageButtonFlash!!.setImageResource(R.drawable.ic_baseline_flash_auto_24)
        }
    }

    private fun checkFlashPresent() {
        if (activity?.baseContext?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)!!) {
            binding.imageButtonFlash!!.visibility = View.VISIBLE
        } else {
            binding.imageButtonFlash!!.visibility = View.INVISIBLE
        }
    }

    private fun checkFrontCameraPresent() {
        if (activity?.baseContext?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)!!)
            binding.imageButtonFlipCamera!!.visibility = View.VISIBLE
        else
            binding.imageButtonFlipCamera!!.visibility = View.INVISIBLE
    }

    private fun onClickFlipCamera() {
        cameraFacing = !cameraFacing
        if (cameraFacing) {
            binding.cameraView!!.facing = Facing.BACK
        } else {
            binding.cameraView!!.facing = Facing.FRONT
        }
    }

    private fun onClickCapture() {
        binding.cameraView!!.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                // Picture was taken!
                // If planning to show a Bitmap, we will take care of
                // EXIF rotation and background threading for you...
                //result.toBitmap(maxWidth, maxHeight, callback)

                // If planning to save a file on a background thread,
                // just use toFile. Ensure you have permissions.

                //result.toFile(file, callback)

                // Access the raw data if needed.
                var data = result.data
                data = resolveRotationErrorAndRescaleImage(result.data, result.rotation)

                Log.d("div", "CaptureImageFragment L865 ${result.facing} ${result.rotation}")

                var imageFile: File? = null
                try {
                    val state: String = Environment.getExternalStorageState()
                    var folder: File? = null
                    folder =
                        File(
                            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                .toString() + "/UpTodd/$FOLDER_NAME"
                        )
                    var success = true
                    if (!folder.exists()) {
                        success = folder.mkdirs()
                    }
                    if (success) {
                        val date = Date()
                        imageFile = File(
                            folder.absolutePath + File.separator + Timestamp(date.time).toString() + "Image.jpg"
                        )
                        imageFile.createNewFile()
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.image_not_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val outputStream = FileOutputStream(imageFile.path)
                    outputStream.write(data)
                    outputStream.close()
                    Log.d("div", "CaptureImageFragment L852 Saved")
                    navigate(imageFile.absolutePath)
                } catch (e: IOException) {
                    Log.d("div", "CaptureImageFragment L854 $e")
                }
            }
        })
        binding.cameraView!!.takePicture()

    }

    private fun resolveRotationErrorAndRescaleImage(data: ByteArray, rotation: Int): ByteArray {
        val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)

        //To scale the image
        Log.d("div", "CaptureImageFragment L927 ${bmp.width} ${bmp.height}")
        val imageWidth =
            if (bmp.width < bmp.height) bmp.width else bmp.height       //Since it will be portrait photo, the lesser dimension will be width
        val imageHeight = if (bmp.width > bmp.height) bmp.width else bmp.height
        val scalingFactor: Float = requiredBitmapSize.toFloat() / imageWidth

        val scaledWidth: Float = imageWidth * scalingFactor
        val scaledHeight: Float = imageHeight * scalingFactor

        val scaledBitmap =
            Bitmap.createScaledBitmap(bmp, scaledWidth.toInt(), scaledHeight.toInt(), true)
        Log.d("div", "CaptureImageFragment L928 ${scaledBitmap.width} ${scaledBitmap.height}")

        //To rotate the bitmap
        val matrix = Matrix()
        matrix.postRotate((rotation).toFloat())
        val rotatedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
        Log.d("div", "CaptureImageFragment L928 ${rotatedBitmap.width} ${rotatedBitmap.height}")

        val stream = ByteArrayOutputStream()
        rotatedBitmap.compress(CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        rotatedBitmap.recycle()

        return byteArray
    }

    private fun navigate(path: String) {
        Log.d("div", "CaptureImageFragment L901 $previousActivity")
        if (previousActivity == "Home" || previousActivity == null) {
            val bundle = Bundle()
            bundle.putString("imagePath", path)

            try {
                findNavController().navigate(
                    R.id.action_captureImageFragment_to_selectTypeFragment,
                    bundle
                )
            } catch (e: Exception) {
                activity?.let {
                    Toast.makeText(it, "Please Try Again", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            try {
                val bundle = Bundle()
                bundle.putString("imagePath", path)
                bundle.putString("photoType", DEFAULT_CARD_CATEGORY)
                findNavController().navigate(
                    R.id.action_captureImageFragment_to_generateCardFragment,
                    bundle
                )
            } catch (e: Exception) {
                activity?.let {
                    Toast.makeText(it, "Please Try Again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    interface OnCaptureListener {
        fun onCapturedAttach()
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    /*override fun onResume() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        super.onResume()
    }

    override fun onPause() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        super.onPause()
    }*/
    /*override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE)
            Log.d("div","CaptureImageFragment L971 landscape $newConfig ")
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT)
            Log.d("div","CaptureImageFragment L973 portrait $newConfig ")

    }*/
}