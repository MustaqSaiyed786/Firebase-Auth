package com.example.firebaseauth

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_google_sign_in.*

class GoogleSignInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var main_layout: LinearLayout
    lateinit var imgUser: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_sign_in)

        signInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)
        disconnectButton.setOnClickListener(this)
        main_layout = findViewById(R.id.main_layout)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        imgUser = findViewById(R.id.imgUser)
        auth = FirebaseAuth.getInstance()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.displayName)
                tvDisplayName.text = "Display Name:- ${account.displayName}"
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.email)
                tvEmail.text = "Email Address:- ${account.email}"
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.familyName)
                tvFamilyName.text = "Family Name:- ${account.familyName}"
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.givenName)
                tvGivenName.text = "Given Name:- ${account.givenName}"

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                updateUI(null)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithCredential:success")
                val user = auth.currentUser
                updateUI(user)
            } else if (task.isCanceled) {
                status.text = "Cancel Request"
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    private fun signOut() {
        auth.signOut()
        hideView(tvDisplayName, tvEmail, tvFamilyName, tvGivenName)
        googleSignInClient.signOut();
        updateUI(null)
    }

    private fun revokeAccess() {
        auth.signOut()
        hideView(tvDisplayName, tvEmail, tvFamilyName, tvGivenName)
        googleSignInClient.signOut()
        updateUI(null)
    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            showView(tvDisplayName, tvEmail, tvFamilyName, tvGivenName)
            status.text = user.email
            detail.text = user.uid
            signInButton.visibility = View.GONE
            signOutAndDisconnect.visibility = View.VISIBLE
        } else {
            status.text = "Signed Out"
            detail.text = null
            signInButton.visibility = View.VISIBLE
            signOutAndDisconnect.visibility = View.GONE
        }
    }

    override fun onClick(id: View?) {
        when (id!!.id) {
            R.id.signInButton -> signIn()
            R.id.signOutButton -> signOut()
            R.id.disconnectButton -> revokeAccess()
        }
    }

    fun hideView(vararg view: View) {
        for (v in view) {
            v.isVisible = false
        }
    }

    fun showView(vararg view: View) {
        for (v in view) {
            v.isVisible = true
        }
    }

    companion object {
        val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}