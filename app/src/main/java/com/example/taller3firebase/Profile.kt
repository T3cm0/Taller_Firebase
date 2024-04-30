package com.example.taller3firebase

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Profile : AppCompatActivity() {

    private lateinit var photoUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        val btnCamera = findViewById<Button>(R.id.camBtn)
        btnCamera.setOnClickListener {
            permisoCamara()
        }
        val btnGallery = findViewById<Button>(R.id.gallerybtn)
        btnGallery.setOnClickListener {
            permisoGaleria()
        }

    }
    fun permisoCamara(){
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePic()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Necesita permiso de camara", Toast.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    Permission.MY_PERMISSION_REQUEST_CAMERA)
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    Permission.MY_PERMISSION_REQUEST_CAMERA)
            }
        }
    }
    fun permisoGaleria(){
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                selectPhoto()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                Toast.makeText(this, "La aplicación necesita acceso a la galería para seleccionar fotos", Toast.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Permission.MY_PERMISSION_REQUEST_GALLERY
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Permission.MY_PERMISSION_REQUEST_GALLERY
                )
            }
        }
    }
    fun selectPhoto () {
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val pickImage = Intent(Intent.ACTION_PICK)
            pickImage.type = "image/*"
            startActivityForResult(pickImage, Permission.IMAGE_PICKER_REQUEST)
        } else {
            Toast.makeText(this, "No hay permiso de galeria", Toast.LENGTH_SHORT).show()
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                Permission.MY_PERMISSION_REQUEST_GALLERY)
        }
    }
    fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Imagen_${System.currentTimeMillis()}")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera") // <-- Change this line
        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            try {
                val outStream = resolver.openOutputStream(uri)
                if (outStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                }
                outStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return uri
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            Permission.IMAGE_PICKER_REQUEST ->{
                if(resultCode == Activity.RESULT_OK){
                    try {
                        //Logica de seleccion de imagen
                        val selectedImageUri = data!!.data
                        if(data.data != null){
                            val imageView = findViewById<ImageView>(R.id.photo)
                            imageView.setImageURI(selectedImageUri)
                        }
                    } catch (e: FileNotFoundException){
                        e.printStackTrace()
                    }
                }
            }
            Permission.REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Load the full-quality image into ImageView
                    val imageView = findViewById<ImageView>(R.id.photo)
                    imageView.setImageURI(photoUri)
                }
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Permission.MY_PERMISSION_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takePic()
                    Toast.makeText(this, "Permiso de camara concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de camara negado", Toast.LENGTH_SHORT).show()
                }
            }
            Permission.MY_PERMISSION_REQUEST_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectPhoto()
                    Toast.makeText(this, "Permiso de galería concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
    private fun takePic() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    null
                }
                photoFile?.also {
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.myapp.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, Permission.REQUEST_IMAGE_CAPTURE)
                }
            } else {
                Toast.makeText(this, "No hay una cámara disponible en este dispositivo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No hay permiso de cámara", Toast.LENGTH_SHORT).show()
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), Permission.MY_PERMISSION_REQUEST_CAMERA)
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
}