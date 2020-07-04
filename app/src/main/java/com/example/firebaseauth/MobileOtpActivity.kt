package com.example.firebaseauth

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import kotlinx.android.synthetic.main.activity_mobile_otp.*
import java.util.concurrent.TimeUnit


class MobileOtpActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "MobileAuthActivity"
    private lateinit var auth: FirebaseAuth
    private var mVerificationInProgress = false
    private var mCallbacks: OnVerificationStateChangedCallbacks? = null
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_otp)
        onlistner(this)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            disableViews(buttonSignOutButton)
        }

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                verificationInProgress = false
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                verificationInProgress = false

                if (e is FirebaseAuthInvalidCredentialsException) {
                    fieldPhoneNumber.error = "Invalid Mobile Number"
                } else if (e is FirebaseTooManyRequestsException) {
                    customToast("Quota exceeded.")
                }

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
            }

        }
    }

    override fun onStart() {
        super.onStart()
        if (verificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(fieldPhoneNumber.text.toString())
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        mCallbacks?.let {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91$phoneNumber",  // Phone number to verify
                60,  // Timeout duration
                TimeUnit.SECONDS,  // Unit of timeout
                this,  // Activity (for callback
                it
            )
        } // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
        mVerificationInProgress = true
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = fieldPhoneNumber.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            fieldPhoneNumber.error = "Invalid phone number."
            return false
        }

        return true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }


    private fun resendVerificationCode(
        phoneNumber: String,
        resendToken: PhoneAuthProvider.ForceResendingToken
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91$phoneNumber",
            60,
            TimeUnit.SECONDS,
            this,
            mCallbacks!!,
            resendToken
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                // redirect to activity
            } else {
                if (it.exception is FirebaseAuthInvalidCredentialsException) {
                    fieldVerificationCode.error = "Invalid code."
                }
            }
        }
    }

    private fun signOut() {
        auth.signOut()
    }

    private fun invisible(vararg view: View) {

        for (v in view) {
            v.isVisible = false
        }

    }

    private fun visible(vararg view: View) {
        for (v in view) {
            v.isVisible = true
        }
    }

    private fun disableViews(vararg view: View) {
        for (v in view) {
            v.isEnabled = false
        }

    }

    private fun enableViews(vararg view: View) {
        for (v in view) {
            v.isEnabled = true
        }
    }


    private fun onlistner(mobileOtpActivity: MobileOtpActivity) {
        buttonStartVerification.setOnClickListener(mobileOtpActivity)
        buttonVerifyPhone.setOnClickListener(mobileOtpActivity)
        buttonResend.setOnClickListener(mobileOtpActivity)
        buttonSignOutButton.setOnClickListener(mobileOtpActivity)
    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.buttonStartVerification -> {
                if (!validatePhoneNumber()) {
                    return
                }
                startPhoneNumberVerification(fieldPhoneNumber.text.toString())
            }
            R.id.buttonVerifyPhone -> {
                val code = fieldVerificationCode.text.toString()
                if (TextUtils.isEmpty(code)) {
                    fieldVerificationCode.error = "Cannot be empty."
                    return
                }

                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
            R.id.buttonResend -> resendVerificationCode(
                fieldPhoneNumber.text.toString(),
                resendToken
            )
            R.id.buttonSignOutButton -> signOut()
        }
    }

    fun customToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}