package com.androiddeveloper3005.mychat.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_registration.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class SettingActivity : AppCompatActivity() ,View.OnClickListener{


    private lateinit var auth : FirebaseAuth
    private lateinit var databaseRef : DatabaseReference
    private lateinit var storeageRef : StorageReference
    private val TAG = "SettingActivity"
    private lateinit var  name_txt : TextView
    private lateinit var  status_txt : TextView
    private lateinit var  image : CircleImageView
    private lateinit var  change_image_btn : Button
    private lateinit var  change_status_btn : Button
    private lateinit var dialog : ProgressDialog
    private lateinit var dialog_change_status_alert : ProgressDialog
    private lateinit var dialog_change_image_alert : ProgressDialog
    private lateinit var bitmap : Bitmap
    private lateinit var thumb_bitmap : Bitmap
    private lateinit var thump_filepath : File
    private  var mImageUri  : Uri? = null
    private lateinit var resultUri  : Uri
    private lateinit var downloadUri  : String
    private lateinit var uid : String
    private lateinit var change_image : CircleImageView



    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        //view
        name_txt = findViewById(R.id.name_txt)
        status_txt = findViewById(R.id.status_txt)
        change_image_btn = findViewById(R.id.change_image_btn)
        change_status_btn = findViewById(R.id.change_status_btn)
        image = findViewById(R.id.profile_image)

        //listener
        change_status_btn.setOnClickListener(this)
        change_image_btn.setOnClickListener(this)

        storeageRef = FirebaseStorage.getInstance().getReference()



        //dialog
        dialog = ProgressDialog(this)
        dialog.setTitle("User Info")
        dialog.setMessage("Please wait while we check your credentials.");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        //firebase
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser!!.uid.toString()
        databaseRef = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH).child(uid)
        val listenner = object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                var name = dataSnapshot!!.child(Constans.NAME).getValue().toString()
                var status = dataSnapshot!!.child(Constans.STATUS).getValue().toString()
                var image_uri = dataSnapshot!!.child(Constans.IMAGE).getValue().toString()
                name_txt.setText(name)
                status_txt.setText(status)
                Picasso.get().load(image_uri)
                    .placeholder(R.drawable.no_image)
                    .into(image);

                dialog.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                Log.w(TAG, "loadPost:onCancelled", databaseError?.toException())
            }
        }

        databaseRef.addValueEventListener(listenner)
    }

    override fun onClick(view: View?) {
        if (view == change_image_btn){
            val mBuilder : AlertDialog.Builder = AlertDialog.Builder(this)
            val mView : View = layoutInflater.inflate(R.layout.change_image,null)
            change_image = mView.findViewById(R.id.change_image)
            val upload_image_btn : Button = mView.findViewById(R.id.change_btn)
            mBuilder.setView(mView)
            val dialog : AlertDialog = mBuilder.create()
            dialog.show()

            //get inage
            change_image.setOnClickListener{
                var galleryIntent: Intent = Intent(Intent.ACTION_GET_CONTENT)
                galleryIntent.setType("image/*")

                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //permision array
                    val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(this, permission, SettingActivity.IMAGE_PICK_CODE)

                } else {
                    startActivityForResult(
                        Intent.createChooser(galleryIntent, "Select Picture"),
                        SettingActivity.IMAGE_PICK_CODE
                    );

                }


            }

            upload_image_btn.setOnClickListener{
                //dialog
                dialog_change_image_alert = ProgressDialog(this)
                dialog_change_image_alert.setTitle("Changing Image")
                dialog_change_image_alert.setMessage("Please wait while we check your credentials.");
                dialog_change_image_alert.setCanceledOnTouchOutside(false);
                dialog_change_image_alert.show();
                val byteArrayOutputStream  = ByteArrayOutputStream()
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,60,byteArrayOutputStream)
                val  thumb_byte = byteArrayOutputStream.toByteArray()
                storeageRef.child(Constans.USER_IMAGE_STOREAGE_PATH).child(uid).delete().addOnSuccessListener {
                    task ->
                    Toast.makeText(applicationContext,"Image Removed.",Toast.LENGTH_SHORT).show()
                }
                val filePath : StorageReference = storeageRef.child(Constans.USER_IMAGE_STOREAGE_PATH).child(uid)
                val uploadTask : UploadTask = filePath.putBytes(thumb_byte)
                uploadTask.addOnSuccessListener {
                        taskSnapshot  ->
                    downloadUri = taskSnapshot.downloadUrl!!.toString()
                    databaseRef.child(Constans.IMAGE).setValue(downloadUri)
                    dialog_change_image_alert.dismiss()


                }




            }


        }else if (view == change_status_btn){

            val mBuilder : AlertDialog.Builder = AlertDialog.Builder(this)
            val mView : View = layoutInflater.inflate(R.layout.change_status,null)

            val status_etx: EditText = mView.findViewById(R.id.status_etx)
            val upload_status : Button = mView.findViewById(R.id.change_btn)

            mBuilder.setView(mView)
            val dialog : AlertDialog = mBuilder.create()
            dialog.show()
            upload_status.setOnClickListener{
                val status = status_etx.text.toString()
                dialog_change_status_alert = ProgressDialog(this)
                dialog_change_status_alert.setTitle("Saving Changes")
                dialog_change_status_alert.setMessage("Please wait while we save the changes");
                dialog_change_status_alert.setCanceledOnTouchOutside(false);
                dialog_change_status_alert.show();
                databaseRef.child(Constans.STATUS).setValue(status).addOnCompleteListener(){
                    task ->
                    if (task.isSuccessful){
                        dialog_change_status_alert.dismiss()

                    }else{
                        Toast.makeText(this, "There was some error in saving Changes.", Toast.LENGTH_LONG).show();
                    }
                }

            }


        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SettingActivity.IMAGE_PICK_CODE && data != null) {

            mImageUri = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, mImageUri)
                change_image.setImageBitmap(bitmap)
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
}
