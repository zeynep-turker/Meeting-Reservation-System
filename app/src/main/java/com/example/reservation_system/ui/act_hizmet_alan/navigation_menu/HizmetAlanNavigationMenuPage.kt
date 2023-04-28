package com.example.reservation_system.ui.act_hizmet_alan.navigation_menu

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.HizmetAlanNavMenuLayoutBinding
import com.example.reservation_system.notification.NotificationPage
import com.example.reservation_system.ui.act_hizmet_alan.home_page.HizmetAlanHomePage
import com.example.reservation_system.ui.act_hizmet_alan.profile_page.HizmetAlanProfilePage
import com.example.reservation_system.ui.act_hizmet_alan.reservations_page.ReservationsPage
import com.example.reservation_system.ui.act_user_management.act_login.LoginPage
import com.google.android.material.bottomnavigation.BottomNavigationView

class HizmetAlanNavigationMenuPage : BaseActivity<HizmetAlanNavMenuLayoutBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hizmet_alan_nav_menu_layout)

        binding.bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        loadFragment(HizmetAlanHomePage())
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

    override fun getLayout() = R.layout.hizmet_alan_nav_menu_layout
    private val mOnNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home_button -> loadFragment(HizmetAlanHomePage())
                    R.id.reservation_button -> loadFragment(ReservationsPage())
                    R.id.nofication_button -> loadFragment(NotificationPage())
                    R.id.profil_button -> loadFragment(HizmetAlanProfilePage())
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