package com.example.firebaseauth


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onListner(this)
        initView()

    }

    private fun initView() {

    }

    private fun onListner(mainActivity: MainActivity) {
        btnMobileOtp.setOnClickListener(mainActivity)
        btnGoogleLogin.setOnClickListener(mainActivity)
        btnFacebookLogin.setOnClickListener(mainActivity)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {

            R.id.btnMobileOtp -> {
                startActivity(Intent(this,MobileOtpActivity::class.java))
            }
            R.id.btnGoogleLogin -> {
                customToast("Google")
            }
            R.id.btnFacebookLogin -> {
                customToast("Facebook")
            }
        }
    }

    fun customToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}