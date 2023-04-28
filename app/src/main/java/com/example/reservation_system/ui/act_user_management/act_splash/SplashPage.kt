package com.example.reservation_system.ui.act_user_management.act_splash

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.SplashScreenLayoutBinding
import com.example.reservation_system.ui.act_hizmet_alan.navigation_menu.HizmetAlanNavigationMenuPage
import com.example.reservation_system.ui.act_hizmet_veren.navigation_menu.HizmetVerenNavigationMenuPage
import com.example.reservation_system.ui.act_user_management.act_login.LoginPage
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class SplashPage : BaseActivity<SplashScreenLayoutBinding>() {
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private val TAG = "SplashPage"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_layout)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        val sharedPreferences = this.getSharedPreferences(packageName, Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("userId"," ").toString()
        val isConnected = checkInternetConnection()
        if (!isConnected) {
            Snackbar.make(binding.root,"You don't have any internet connection. Please connect to the internet.",
                    Snackbar.LENGTH_LONG
            )
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar, event: Int) {
                    finish()
                }
            }).show()
        }
        else {
            Handler().postDelayed({
                if (id == " ") {
                    val intent = Intent(this, LoginPage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    firebaseFirestore.collection("users").document(id).get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                  val userType = document.get("userType").toString()
                                    if(userType == "Hizmet Veren")
                                        updateUIwithFinish<HizmetVerenNavigationMenuPage>()
                                    else
                                        updateUIwithFinish<HizmetAlanNavigationMenuPage>()
                                } else {
                                    Log.d(TAG, "No such document")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d(TAG, "get failed with ", exception)
                            }
                }
            }, 1000)
        }
    }

    override fun getLayout() = R.layout.splash_screen_layout

    fun checkInternetConnection(): Boolean {
        val cm =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // test for connection
        return if (cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isAvailable
                && cm.activeNetworkInfo!!.isConnected
        ) {
            true
        } else {
            Log.d("MyTest", "No Internet Connection")
            false
        }
    }


}