package com.example.reservation_system.ui.act_user_management.act_register

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.widget.ListPopupWindow
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.RegisterPageLayoutBinding
import com.example.reservation_system.model.HizmetAlan
import com.example.reservation_system.model.HizmetVeren
import com.example.reservation_system.model.User
import com.example.reservation_system.ui.act_user_management.act_login.LoginPage
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegisterPage  : BaseActivity<RegisterPageLayoutBinding>() {
    private lateinit var nameSurnameInput: String
    private lateinit var emailInput: String
    private lateinit var passwordInput: String
    private lateinit var telephoneNumberInput : String
    private var isSend = false
    private var collection = "tr"
    private var document = "jobs"
    private val TAG = "RegisterPage"
    private var mAuth: FirebaseAuth? = null
    private lateinit var db:FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page_layout)
        db = FirebaseFirestore.getInstance()


        binding.registerButton.setOnClickListener {
            controlInfo()
        }
        binding.loginPageId.setOnClickListener {
            updateUI<LoginPage>()
        }
        setJobSpinnerList()

    }

    private fun controlInfo() {
        if (!validateNameSurname() or !validateEmail()  or !validatePassword() or !validateRadioGroup() or !validateTelNo())  {
            createSnackBar(binding.root,getString(R.string.eksik_alanlari__doldur), Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
            return
        }
        mAuth = FirebaseAuth.getInstance()
        mAuth!!
            .createUserWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener(this) {
                if (!it.isSuccessful) return@addOnCompleteListener
                val userId = mAuth!!.currentUser!!.uid
                val selectedOption: Int = binding.radioGroup.checkedRadioButtonId
                val radioButton = findViewById<RadioButton>(selectedOption)
                val job: String
                val user: User
                telephoneNumberInput = binding.ccp.selectedCountryCode + telephoneNumberInput
                if (radioButton.text.toString() == "Hizmet Veren") {
                    user = HizmetVeren()
                    user.init(nameSurnameInput, emailInput, telephoneNumberInput, passwordInput, userId, radioButton.text.toString(), "empty")
                    job = binding.spinnerJob.selectedItem.toString()
                    user.job = job
                } else {
                    user = HizmetAlan()
                    user.init(nameSurnameInput, emailInput, telephoneNumberInput, passwordInput, userId, radioButton.text.toString(), "empty")
                }
                db.collection("users").document(userId).set(user)
                sendVerificationLink()
            }
            .addOnFailureListener{
                Log.d("Main", "Failed to create user: ${it.message}")
                createSnackBar(binding.root,"Failed to create user: ${it.message}", Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
            }



    }
    private fun sendVerificationLink() {
        mAuth = FirebaseAuth.getInstance()
        mAuth!!.currentUser!!.sendEmailVerification()
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    val snackbar = Snackbar.make(binding.root,getString(R.string.dogrulama_kodunu_onaylayiniz), Snackbar.LENGTH_LONG)
                    snackbar.view.setBackgroundColor(Color.parseColor("#FFCB2B"))
                    snackbar.addCallback(object : Snackbar.Callback() {
                                override fun onDismissed(snackbar: Snackbar, event: Int) {
                                    updateUI<LoginPage>()
                                }
                            })
                    snackbar.show()

                })
                .addOnFailureListener(OnFailureListener {
                    createSnackBar(binding.root, getString(R.string.dogrulama_kodu_gonderilemedi) + it.message, Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
                })
    }
    @SuppressLint("ResourceType")
    private fun validateRadioGroup(): Boolean{
        val id: Int = binding.radioGroup.checkedRadioButtonId
        if (id!=-1){
        }else{
           return false
        }
        return true
    }
    private fun validateNameSurname(): Boolean {
        nameSurnameInput = binding.registerNameSurname.editText?.text.toString().trim()
        return if (nameSurnameInput.isEmpty()) {
            binding.registerNameSurname.error = getString(R.string.alan_bos_kalamaz)
            false
        } else {
            binding.registerNameSurname.error = null
            true
        }
    }
    private fun validateEmail(): Boolean {
        emailInput = binding.registerEmail.editText?.text.toString().trim()
        return if (emailInput.isEmpty()) {
            binding.registerEmail.error = getString(R.string.alan_bos_kalamaz)
            false
        } else {
            binding.registerEmail.error = null
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
    private fun validateTelNo(): Boolean {
        telephoneNumberInput = binding.registerTelNo.editText?.text.toString().trim()
        return if (telephoneNumberInput.isEmpty()) {
            binding.registerTelNo.error = getString(R.string.alan_bos_kalamaz)
            false
        } else {
            binding.registerTelNo.error = null
            true
        }
    }

    private fun setJobSpinnerList(){
        val array = arrayListOf<String>()
        FirebaseFirestore.getInstance().collection("categories").document(document).collection(collection)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val categoryName = document.getString("name").toString()
                    array.add(categoryName)
                }
                val adapter = ArrayAdapter<String>(this,R.layout.spinner_item, array)
                binding.spinnerJob.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
        val popup = binding.spinnerJob.javaClass.getDeclaredField("mPopup")
        popup.isAccessible = true
        val popupWindow : ListPopupWindow = popup.get(binding.spinnerJob) as ListPopupWindow
        popupWindow.height = 200
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioHizmetAlan -> {
                    binding.meslekSecLayout.visibility = View.GONE
                }
                R.id.radioHizmetVeren -> {
                    binding.meslekSecLayout.visibility = View.VISIBLE
                }
            }
        }
    }
    override fun getLayout() = R.layout.register_page_layout
}