package com.example.reservation_system.core

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<DB : ViewDataBinding>() : AppCompatActivity(){
    @LayoutRes
    abstract fun getLayout(): Int

    val binding by lazy {
        DataBindingUtil.setContentView(this, getLayout()) as DB
    }

    inline fun <reified T : Activity> updateUIwithFinish() {
        val intent = Intent(this, T::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    inline fun <reified T : Activity> updateUI() {
        val intent = Intent(this, T::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    fun createSnackBar(view: View, message : String, time:Int, backgroundColor:String, textColor:Int){
        val snackbar = Snackbar.make(view,message, time)
        snackbar.view.setBackgroundColor(Color.parseColor(backgroundColor))
        snackbar.setTextColor(textColor)
        snackbar.show()
    }
    fun createSnackBarwithFinish(view: View, message : String, time:Int, backgroundColor:String, textColor:Int){
        val snackbar = Snackbar.make(view,message, time)
        snackbar.view.setBackgroundColor(Color.parseColor(backgroundColor))
        snackbar.setTextColor(textColor)
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(snackbar: Snackbar, event: Int) {
                finish()
            }
        }).show()
    }

}
