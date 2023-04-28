package com.example.reservation_system.ui.act_user_management.act_login

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.LoginPageLayoutBinding
import com.example.reservation_system.ui.act_hizmet_alan.navigation_menu.HizmetAlanNavigationMenuPage
import com.example.reservation_system.ui.act_hizmet_veren.navigation_menu.HizmetVerenNavigationMenuPage
import com.example.reservation_system.ui.act_user_management.act_register.RegisterPage
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginPage : BaseActivity<LoginPageLayoutBinding>() {
    private lateinit var emailInput: String
    private lateinit var passwordInput: String
    private lateinit var mAuth:  FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page_layout)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.loginButton.setOnClickListener {
            controlInfo()
        }
        binding.registerPageId.setOnClickListener {
            updateUI<RegisterPage>()
        }
    }
    private fun controlInfo() {
        if(!validateEmail() or !validatePassword()){
            createSnackBar(binding.root,getString(R.string.eksik_alanlari__doldur), Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
            return
        }
        loginUser()

    }
    private fun loginUser() {

        if (!TextUtils.isEmpty(emailInput) && !TextUtils.isEmpty(passwordInput)) {
            mAuth?.let {
                it.signInWithEmailAndPassword(emailInput, passwordInput)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val sharedPreferences = this.getSharedPreferences(packageName, Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("userId", mAuth.currentUser?.uid).apply()
                                val userVerification: FirebaseUser? = mAuth!!.currentUser
                                if(userVerification!!.isEmailVerified) {
                                    /*FirebaseMessaging.getInstance().token.addOnSuccessListener {
                                        FirebaseService.token = it
                                        Toast.makeText(baseContext,it, Toast.LENGTH_LONG).show()
                                        FirebaseFirestore.getInstance().collection("users").document(mAuth.uid.toString()).update("token",it)
                                    }*/

                                    val docRef = db.collection("users").document(mAuth!!.uid.toString())
                                    docRef.get()
                                            .addOnSuccessListener { document ->
                                                if (document != null) {
                                                    val userType = document.get("userType").toString()
                                                    if (userType == "Hizmet Alan") {
                                                        updateUIwithFinish<HizmetAlanNavigationMenuPage>()
                                                    } else {
                                                        updateUIwithFinish<HizmetVerenNavigationMenuPage>()
                                                    }
                                                } else {
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                            }
                                    createSnackBar(binding.root, getString(R.string.basarili), Snackbar.LENGTH_SHORT, "#107163", Color.WHITE)
                                }
                                else{
                                    createSnackBar(binding.root,getString(R.string.bu_hesap_onaylanmamis), Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
                                }
                            } else {
                                createSnackBar(binding.root,getString(R.string.basarisiz), Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
                            }
                        }
            }
        } else {
            createSnackBar(binding.root,getString(R.string.basarisiz), Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
        }
    }
    private fun validateEmail(): Boolean {
        emailInput = binding.email.editText?.text.toString().trim()
        return if (emailInput.isEmpty()) {
            binding.email.error = getString(R.string.alan_bos_kalamaz)
            false
        } else {
            binding.email.error = null
            true
        }
    }
    private fun validatePassword(): Boolean {
        passwordInput = binding.password.editText?.text.toString().trim()
        return if (passwordInput.isEmpty()) {
            binding.password.error = getString(R.string.alan_bos_kalamaz)
            false
        } else {
            binding.password.error = null
            true
        }
    }
    override fun getLayout() = R.layout.login_page_layout
}