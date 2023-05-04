package com.example.happy_places_app.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.happy_places_app.R
import com.example.happy_places_app.database.DatabaseHandler
import com.example.happy_places_app.databinding.ActivityAddHappyPlaceBinding
import com.example.happy_places_app.models.HappyPlaceModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    //private var toolBarAddPlace : Toolbar? = null

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var binding : ActivityAddHappyPlaceBinding? = null
    private var saveImageToInternalStorage : Uri? = null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0

    private var mHappyPlaceDetails : HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        //toolBarAddPlace = findViewById(R.id.toolbar_add_place)
        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener{
            onBackPressed()
        }

        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.google_maps_api_key))
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getParcelableExtra(
                MainActivity.EXTRA_PLACE_DETAILS
            ) as HappyPlaceModel?
        }


        dateSetListener = DatePickerDialog.OnDateSetListener {
                _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if (mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"

            binding!!.etTitle.setText(mHappyPlaceDetails!!.title)
            binding!!.etDescription.setText(mHappyPlaceDetails!!.description)
            binding!!.etDate.setText(mHappyPlaceDetails!!.date)
            binding!!.etLocation.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(
                mHappyPlaceDetails!!.image
            )

            binding!!.ivPlaceImage.setImageURI(saveImageToInternalStorage)

            binding!!.btnSave.text = "UPDATE"

        }

        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding!!.btnSave.setOnClickListener(this)
        binding!!.etLocation.setOnClickListener(this)
        binding!!.tvSelectCurrentLocation.setOnClickListener(this)
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItem = arrayOf("Select photo from Gallery",
                    "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItem){
                    _, which ->
                        when(which){
                            0 -> choosePhotoFromGallery()
                            1 -> takePhotoFromCamera()
                        }
                }
                pictureDialog.show()
            }
            R.id.btn_save -> {
                when{
                    binding!!.etTitle.text.isNullOrEmpty() ->  {
                        Toast.makeText(
                            this,
                            "please enter title",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding!!.etDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "please enter a description",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding!!.etLocation.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "please enter a location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    saveImageToInternalStorage == null ->{
                        Toast.makeText(
                            this,
                            "please select an image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else ->{
                        val happyPlaceModel = HappyPlaceModel(
                            if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            binding!!.etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding!!.etDescription.text.toString(),
                            binding!!.etDate.text.toString(),
                            binding!!.etLocation.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)

                        if (mHappyPlaceDetails == null){
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                            if (addHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }else{
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                            if (updateHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                }
                }

            }
            R.id.et_location -> {
                try {

                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )

                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)


                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {

                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // This will redirect you to settings from where you need to turn on the location provider.
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    // For Getting current location of user please have a look at below link for better understanding
                    // https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html
                    Dexter.withActivity(this)
                        .withPermissions(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {

                                    Toast.makeText(
                                        this@AddHappyPlaceActivity,
                                        "Location permission is granted. Now you can request for a current location.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationalDialogForPermissions()
                            }
                        }).onSameThread()
                        .check()
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY){
                if (data != null){
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.
                        getBitmap(this.contentResolver, contentURI)

                         saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)

                        Log.e("Saved image: ", "path :: $saveImageToInternalStorage")
                        binding!!.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Failed to load the Image from Gallery!",
                            Toast.LENGTH_LONG).show()

                    }
                }
            }else if (requestCode == CAMERA){
                val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap

                 saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)

                Log.e("Saved image: ", "path :: $saveImageToInternalStorage")

                binding!!.ivPlaceImage.setImageBitmap(thumbnail)
            }else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                binding!!.etLocation.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ).withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(
                    report: MultiplePermissionsReport?)
                {
                    if (report!!.areAllPermissionsGranted()){
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                        startActivityForResult(galleryIntent, CAMERA)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken)
                {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check();
    }

    private fun choosePhotoFromGallery(){
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(
                    report: MultiplePermissionsReport?)
                {
                    if (report!!.areAllPermissionsGranted()){
                        val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken)
                {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check();
    }

    private fun showRationalDialogForPermissions(){
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("" +
                    "It looks like you have turned off permission required" +
                    " for this feature. It can be enabled under the" +
                    " Applications Settings")
            .setPositiveButton("GO TO SETTING"){
                _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.show()

    }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}