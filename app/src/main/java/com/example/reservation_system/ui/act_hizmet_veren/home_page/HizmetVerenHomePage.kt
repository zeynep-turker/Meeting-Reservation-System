package com.example.reservation_system.ui.act_hizmet_veren.home_page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.reservation_system.R
import com.example.reservation_system.databinding.HizmetVerenHomePageLayoutBinding
import com.example.reservation_system.notification.MyFirebaseMessagingService
import com.example.reservation_system.ui.act_hizmet_veren.reservations.active_reservations.ActiveReservations
import com.example.reservation_system.ui.act_hizmet_veren.reservations.paid_reservations.PaidReservations
import com.example.reservation_system.ui.act_hizmet_veren.reservations.reservation_requests.ReservationRequests
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class HizmetVerenHomePage : Fragment() {
    private lateinit var binding: HizmetVerenHomePageLayoutBinding
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.hizmet_veren_home_page_layout, container, false)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        controlNotification()
        getUserNameSurname()

        binding.randevuTalepleri.setOnClickListener {
            val intent = Intent(context, ReservationRequests::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        binding.odemesiYapilmisRandevular.setOnClickListener {
            val intent = Intent(context, PaidReservations::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        binding.aktifRandevular.setOnClickListener {
            val intent = Intent(context, ActiveReservations::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        binding.searchImage.setOnClickListener {
            val name = binding.searchName.text.toString()
            if(name.isNotEmpty()){
                val intent = Intent(context, HizmetVerenPeopleAsName::class.java)
                intent.putExtra("name",name)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }

        return binding.root
    }
    private fun getUserNameSurname(){
        val id = firebaseAuth.currentUser!!.uid
        firebaseFirestore.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nameSurname = document.get("nameSurname").toString()
                        binding.userNameText.text = nameSurname
                    }
                }
                .addOnFailureListener { exception ->
                }

    }
    private fun controlNotification(){
        val ref = firebaseFirestore.collection("notifications")
                .whereEqualTo("to",firebaseAuth.currentUser.uid)
                .whereEqualTo("gone",false)

        ref.addSnapshotListener(object : EventListener<QuerySnapshot>
        {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                for (newNotif in value!!.documents){
                    val controlNotification = MyFirebaseMessagingService()
                    firebaseFirestore.collection("users").document(newNotif["from"].toString()).get().addOnSuccessListener {
                        controlNotification.showNotification(requireContext(),it["nameSurname"].toString(),newNotif["text"].toString())
                        firebaseFirestore.collection("notifications").document(newNotif["notificationId"].toString()).update("gone",true)
                    }


                }
            }
        })


    }

}