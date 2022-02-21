package com.projects.wallet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.projects.wallet.facerecognition.FaceDetectionActivity
import com.projects.wallet.prefs.IPrefsManager
import com.projects.wallet.prefs.PrefsManager
import com.projects.wallet.utils.showToast
import kotlinx.android.synthetic.main.activity_recognize_face.*
import kotlinx.android.synthetic.main.bottomsheet_password.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor

class RecognizeFaceActivity : AppCompatActivity() {

    private lateinit var executor: Executor

    private val REQUEST_CAMERA_PERMISSION = 101

    lateinit var prefManager: IPrefsManager

    var imageNumber = 0

    lateinit var takePhoto: ActivityResultLauncher<Void>
    lateinit var secondPhoto: ActivityResultLauncher<Void>
    lateinit var thirdPhoto: ActivityResultLauncher<Void>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognize_face)
        prefManager = providePrefsManager(provideSharePreference(this))
        executor = ContextCompat.getMainExecutor(this)
        takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            savePhotoToInternalStorage(UUID.randomUUID().toString(), it)
            imageNumber++
            prefManager.setInt("NUMBER", imageNumber)
            application.showToast("2 image remaining", Toast.LENGTH_LONG)
            secondPhoto.launch(null, null)
        }

        secondPhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            savePhotoToInternalStorage(UUID.randomUUID().toString(), it)
            imageNumber++
            prefManager.setInt("NUMBER", imageNumber)
            application.showToast("1 image remaining", Toast.LENGTH_LONG)
            thirdPhoto.launch(null, null)
        }

        thirdPhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            savePhotoToInternalStorage(UUID.randomUUID().toString(), it)
            Toast.makeText(this, "Images saved successfully", Toast.LENGTH_SHORT).show()
            prefManager.setBool("PHOTOS", true)
             val intent = Intent(this@RecognizeFaceActivity, FaceDetectionActivity::class.java)
             startActivityForResult(intent, 200)
        }

        if(!prefManager.getBool("PHOTOS")){
            tv_recognize.visibility = View.VISIBLE
            btn_post.text = "Register New face"
        }else{
           tv_recognize.visibility = View.GONE
            btn_post.visibility = View.GONE
        }

        btn_post.setOnClickListener {
            if(prefManager.getBool("PHOTOS")){
                val intent = Intent(this@RecognizeFaceActivity, FaceDetectionActivity::class.java)
                startActivityForResult(intent, 200)

            }else{
                takePhoto.launch(null, null)
            }
        }


        iv_face.setOnClickListener {
            if(prefManager.getBool("PHOTOS")){
                val intent = Intent(this@RecognizeFaceActivity, FaceDetectionActivity::class.java)
                startActivityForResult(intent, 200)
            }else{
                Toast.makeText(this,"Please register your face before authenticaing",Toast.LENGTH_SHORT).show()
            }
        }
        if(!prefManager.getBool("PHOTOS")){
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val files = filesDir.listFiles()
                    files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                        deletePhotoFromInternalStorage(it.name)
                    } ?: listOf()
                }
            }
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
            Log.d("PermissionGranted", "Premis")
        } else {
            //application.showToast("Plz take three selfie to add the face data.", Toast.LENGTH_LONG)
            //takePhoto.launch(null, null)

            Log.d("SETUP", "FROM PERMISSION")
        }

    }


    fun provideSharePreference(context: Context): SharedPreferences {


        val sharedPreferences = getSharedPreferences("", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        return sharedPreferences


    }

    fun providePrefsManager(pref: SharedPreferences): IPrefsManager = PrefsManager(pref)

    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return try {
            openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            Log.d("Detected","200")

            tv_recognize.visibility = View.GONE
            btn_post.visibility = View.GONE

            Toast.makeText(
                this, "Detected",
                Toast.LENGTH_LONG
            ).show();
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                val alertDialog = AlertDialog.Builder(this).apply {
                    setTitle("Camera Permission")
                    setMessage("The app couldn't function without the camera permission.")
                    setCancelable(false)
                    setPositiveButton("ALLOW") { dialog, which ->
                        dialog.dismiss()
                        ActivityCompat.requestPermissions(
                            this@RecognizeFaceActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION
                        )
                    }
                    setNegativeButton("CLOSE") { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    create()
                }
                alertDialog.show()
            }
        }
    }

    private fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return try {
            deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

