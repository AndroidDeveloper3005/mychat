package com.androiddeveloper3005.mychat.activity

import android.Manifest
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import com.theartofdev.edmodo.cropper.CropImage
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.storage.UploadTask
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_registration.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class RegistrationActivity : AppCompatActivity() , View.OnClickListener {
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseRef : DatabaseReference
    private lateinit var storeageRef : StorageReference
    private lateinit var name_etx : EditText
    private lateinit var phone_etx : EditText
    private lateinit var email_etx : EditText
    private lateinit var password_etx : EditText
    private lateinit var registration_btn : Button
    private lateinit var have_account_txt : TextView
    private lateinit var progressBar : ProgressBar
    private lateinit var user_image : CircleImageView
    private lateinit var bitmap : Bitmap
    private lateinit var thumb_bitmap : Bitmap
    private lateinit var thump_filepath : File
    private  var mImageUri  : Uri? = null
    private lateinit var resultUri  : Uri
    private lateinit var username  : String
    private lateinit var user_phone  : String
    private lateinit var user_email  : String
    private lateinit var downloadUri  : String
    private lateinit var uid : String


    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        //firebase
        auth = FirebaseAuth.getInstance()
        storeageRef = FirebaseStorage.getInstance().getReference()
        databaseRef = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH)

        //view
        name_etx = findViewById(R.id.name_edit_text)
        phone_etx = findViewById(R.id.phone_edit_text)
        email_etx = findViewById(R.id.email_edit_text)
        password_etx = findViewById(R.id.password_edit_text)
        registration_btn = findViewById(R.id.registration_btn)
        have_account_txt  = findViewById(R.id.login_txt)
        user_image = findViewById(R.id.profile_image)
        progressBar = findViewById(R.id.progressBar)

        //onclick
        have_account_txt.setOnClickListener(this)
        user_image.setOnClickListener(this)
        registration_btn.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        if (view == registration_btn){
            progressBar.visibility = View.VISIBLE
            registrationWithEmailAndPassword()

        }else if(view == user_image){

            var galleryIntent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.setType("image/*")

            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //permision array
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permission, IMAGE_PICK_CODE)

            } else {
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), IMAGE_PICK_CODE);

            }






        }else if(view == have_account_txt){
            var intent = Intent(this,LoginActivity :: class.java)
            startActivity(intent)

        }

    }

    private fun registrationWithEmailAndPassword() {
        var name = name_etx.text.toString()
        var phone = phone_etx.text.toString()
        var email = email_etx.text.toString()
        var password = password_etx.text.toString()
        username = name
        user_phone = phone
        user_email = email

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone)
            && mImageUri != null){

            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task : Task<AuthResult> ->
                if (task.isSuccessful){
                    storeInfo()

                    var intent = Intent(this,MainActivity :: class.java)
                    startActivity(intent)

                }else{
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_LONG).show()

                }

            }
        }else{
            Toast.makeText(this, "You Need To Provide All Field.",
                Toast.LENGTH_LONG).show()

        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null) {

            mImageUri = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, mImageUri)
                profile_image.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            CropImage.activity(mImageUri)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result : CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK){
                resultUri = result.getUri()
                thump_filepath = File(resultUri.getPath());
                try {
                    thumb_bitmap = Compressor(this)
                        .setMaxWidth(300)
                        .setMaxHeight(300)
                        .setQuality(50)
                        .compressToBitmap(thump_filepath)

                }catch (e : IOException){
                    e.printStackTrace()

                }
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                 val error : Exception = result.getError();
            }

        }




    }

    private fun storeInfo() {
        uid = auth.currentUser!!.uid

        val byteArrayOutputStream  = ByteArrayOutputStream()
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,60,byteArrayOutputStream)
        val  thumb_byte = byteArrayOutputStream.toByteArray()
        val filePath : StorageReference = storeageRef.child(Constans.USER_IMAGE_STOREAGE_PATH).child(uid)
        val uploadTask : UploadTask = filePath.putBytes(thumb_byte)
        uploadTask.addOnSuccessListener {
                taskSnapshot  ->
            downloadUri = taskSnapshot.downloadUrl!!.toString()

            var userMap = HashMap<String,String>()
            userMap.put("name",username)
            userMap.put("email",user_email)
            userMap.put("phone",user_phone)
            userMap.put("image",downloadUri)
            userMap.put("uid",uid)
            userMap.put("status","Hi There,I Am Using Chat me App")

            //store data
            databaseRef.child(uid).setValue(userMap)
            progressBar.visibility = View.INVISIBLE


        }
    }
}
