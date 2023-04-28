package com.example.reservation_system.ui.act_hizmet_veren.profile_page

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseFragment
import com.example.reservation_system.databinding.HizmetVerenProfilePageLayoutBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso


class HizmetVerenProfilePage : BaseFragment(){
    private lateinit var binding: HizmetVerenProfilePageLayoutBinding
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var userId : String
    private val TAG = "HizmetVerenProfilePage"
    private lateinit var nameSurname : String
    private lateinit var phoneNo : String
    private lateinit var profilUri : String

    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var imageUri: Uri? = null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.hizmet_veren_profile_page_layout, container, false)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userId = firebaseAuth.currentUser!!.uid
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        getInfo()

        binding.changeProfilPhoto.setOnClickListener {
            changeProfilPhoto()
        }
        binding.profilChangePassword.setOnClickListener {
            changePassword()
        }
        binding.profilSaveButton.setOnClickListener {
            changeInfo()
        }
        binding.profilLogout.setOnClickListener {
            logOut()
        }
        return binding.root
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage && data != null && data.data != null) {
            imageUri = data.data
            filePath = imageUri
            binding.userProfilImage.setImageURI(imageUri)
        }
    }
    private fun addUploadRecordToDb(uri: String){
        firebaseFirestore.collection("users").document(userId).update("profilImage",uri)
    }

    private fun uploadImage() : Boolean{
        if(filePath != null){
            val ref = storageReference?.child("profileImages/$userId")
            val uploadTask = ref?.putFile(filePath!!)

            uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    addUploadRecordToDb(downloadUri.toString())
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
            return true
        }
        else
            return false
    }
    private fun changeInfo() {
        var temp = false
        if(nameSurname != binding.profilNameSurname.editText?.text.toString()){
            if(binding.profilNameSurname.editText?.text.toString() != "") {
                changeNameSurname(binding.profilNameSurname.editText?.text.toString())
                nameSurname = binding.profilNameSurname.editText?.text.toString()
                temp = true
            }
            else
                createSnackBar(binding.root,getString(R.string.eksik_alanlari__doldur), Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
        }
        if(phoneNo != binding.profilTelNo.editText?.text.toString()){
            if(binding.profilTelNo.editText?.text.toString() != "") {
                changeTelNo(binding.profilTelNo.editText?.text.toString())
                phoneNo = binding.profilTelNo.editText?.text.toString()
                temp = true
            }
            else
                createSnackBar(binding.root,getString(R.string.eksik_alanlari__doldur), Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
        }
        if (uploadImage()){
            temp = true
        }
        if(temp) {
            createSnackBar(binding.root, getString(R.string.basarili), Snackbar.LENGTH_SHORT, "#107163", Color.WHITE)
            temp = false
        }
    }

    private fun changeNameSurname(newNameSurname : String) {
        firebaseFirestore.collection("users").document(userId).update("nameSurname",newNameSurname)
    }

    private fun changeTelNo(newPhoneNo : String) {
        firebaseFirestore.collection("users").document(userId).update("phoneNo",newPhoneNo)
    }

    private fun changePassword(){
        firebaseAuth!!.sendPasswordResetEmail(firebaseAuth.currentUser!!.email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Check email to reset your password!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Fail to send reset password email!", Toast.LENGTH_SHORT).show()
                    }
                }
    }
    private fun getInfo() {
        val docRef = firebaseFirestore.collection("users").document(userId)
        docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        nameSurname = document.get("nameSurname").toString()
                        phoneNo = document.get("phoneNo").toString()
                        profilUri = document.get("profilImage").toString()
                        binding.profilNameSurname.editText?.setText(nameSurname)
                        binding.profilTelNo.editText?.setText(phoneNo)
                        setProfilImage(profilUri)
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
    }
    private fun setProfilImage(profilUri:String) {
        if (profilUri == "empty") {
            binding.userProfilImage.setImageResource(R.drawable.profile_page)
        } else {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(profilUri)
            ref.downloadUrl.addOnSuccessListener { Uri ->
                val imageURL = Uri.toString()
                Picasso.get().load(imageURL).into(binding.userProfilImage)
            }
        }
    }
}