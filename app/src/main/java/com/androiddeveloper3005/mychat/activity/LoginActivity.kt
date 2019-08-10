package com.androiddeveloper3005.mychat.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.androiddeveloper3005.mychat.R
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*


class LoginActivity : AppCompatActivity() , View.OnClickListener{

    private lateinit var auth : FirebaseAuth
    private lateinit var email_etx : EditText
    private lateinit var password_etx : EditText
    private lateinit var login_btn : Button
    private lateinit var registration_txt : TextView
    private lateinit var forgot_password_txt : TextView
    private lateinit var mAuthStateListener : FirebaseAuth.AuthStateListener
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //firebase
        auth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            if (auth.getCurrentUser() != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        };

        //view
        setContentView(R.layout.activity_login)
        email_etx = findViewById(R.id.email_edit_text)
        password_etx = findViewById(R.id.password_edit_text)
        forgot_password_txt = findViewById(R.id.forgot_password_txt)
        registration_txt = findViewById(R.id.registration_txt)
        login_btn = findViewById(R.id.login_btn)
        progressBar = findViewById(R.id.progressBar)
        login_btn.setOnClickListener(this)
        registration_txt.setOnClickListener(this)
        forgot_password_txt.setOnClickListener(this)




    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(mAuthStateListener)
    }

    override fun onClick(view : View?) {
        if (login_btn == view){
            progressBar.visibility = View.VISIBLE
            login()


        }else if (forgot_password_txt == view ){

        }else if (registration_txt == view){
            var intent = Intent(this,RegistrationActivity :: class.java)
            startActivity(intent)

        }

    }
    companion object{
        var TAG = "LoginActivity"
    }


    private fun login() {
        var email = email_etx.text.toString()
        var password = password_etx.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this){
                    task ->
                if (task.isSuccessful){
                    progressBar.visibility = View.INVISIBLE
                    var intent = Intent(this,MainActivity :: class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK )
                    startActivity(intent)

                }else{
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_LONG).show()
                }
            }
        }else{
            Toast.makeText(this, "You Need To Provide All Field.",
                Toast.LENGTH_LONG).show()

        }


    }
}