package com.example.reservation_system.core

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.reservation_system.ui.act_user_management.act_splash.SplashPage
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

abstract class BaseFragment : Fragment(){
    val PERMISSION_REQUEST_CODE: Int = 101
    val pickImage = 100

    fun logOut(){
        FirebaseAuth.getInstance().signOut()
        val sharedPreferences = requireContext().getSharedPreferences(context?.packageName,Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("userId").apply()
        finishAffinity(requireActivity())
        val intent = Intent(requireContext(), SplashPage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    fun createSnackBar(view: View, message : String, time:Int, backgroundColor:String, textColor:Int){
        val snackbar = Snackbar.make(view,message, time)
        snackbar.view.setBackgroundColor(Color.parseColor(backgroundColor))
        snackbar.setTextColor(textColor)
        snackbar.show()
    }
    fun checkPersmission(): Boolean {
        return (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ), PERMISSION_REQUEST_CODE)
    }
    fun openGalleryForImage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)
    }
    fun changeProfilPhoto() {
        if (checkPersmission()) openGalleryForImage() else requestPermission()
    }

}