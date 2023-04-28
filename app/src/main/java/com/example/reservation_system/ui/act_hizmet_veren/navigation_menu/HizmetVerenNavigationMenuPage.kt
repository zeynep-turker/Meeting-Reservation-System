package com.example.reservation_system.ui.act_hizmet_veren.navigation_menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.HizmetVerenNavMenuLayoutBinding
import com.example.reservation_system.notification.NotificationPage
import com.example.reservation_system.ui.act_hizmet_alan.home_page.HizmetAlanHomePage
import com.example.reservation_system.ui.act_hizmet_veren.home_page.HizmetVerenHomePage
import com.example.reservation_system.ui.act_hizmet_veren.profile_page.HizmetVerenProfilePage
import com.google.android.material.bottomnavigation.BottomNavigationView

class HizmetVerenNavigationMenuPage : BaseActivity<HizmetVerenNavMenuLayoutBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hizmet_veren_nav_menu_layout)

        binding.bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        loadFragment(HizmetVerenHomePage())
    }
    override fun onBackPressed() {
        val fragmentList: List<*> = supportFragmentManager.fragments
        var handled = false
        for (f in fragmentList) {
            if (f is HizmetAlanHomePage){
                finish()
            }
            else {
                binding.bottomNavigation.selectedItemId = R.id.home_button
                loadFragment(HizmetAlanHomePage())
                handled = true
                if (handled) {
                    break
                }
            }
        }
        if (!handled) {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("gelddiiiii","*********")
        if (intent != null) {
                val data = intent.getStringExtra("data");

                if (data != null) {
                    loadFragment(NotificationPage())
                    binding.bottomNavigation.selectedItemId = R.id.nofication_button
                }
            }
    }
    override fun getLayout() = R.layout.hizmet_veren_nav_menu_layout
    private val mOnNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home_button -> loadFragment(HizmetVerenHomePage())
                    R.id.nofication_button -> loadFragment(NotificationPage())
                    R.id.profil_button -> loadFragment(HizmetVerenProfilePage())
                }
                true
    }
    private fun loadFragment(fragment: Fragment?) { //fragmentlarımızı çağırdığımız fonksiyon
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragment?.let { transaction.replace(R.id.mainFrameLayout, it) }
        transaction.addToBackStack(null)
        transaction.commit()
    }
}