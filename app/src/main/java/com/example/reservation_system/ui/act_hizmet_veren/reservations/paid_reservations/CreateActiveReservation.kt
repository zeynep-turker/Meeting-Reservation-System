package com.example.reservation_system.ui.act_hizmet_veren.reservations.paid_reservations

import android.graphics.Color
import android.os.Bundle
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.CreateActiveReservationLayoutBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class CreateActiveReservation : BaseActivity<CreateActiveReservationLayoutBinding>() {
    lateinit var reservationId : String
    private lateinit var firebaseFirestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_active_reservation_layout)
        reservationId = intent.extras?.getString("reservationId").toString()
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding.createActiveReservationButton.setOnClickListener {
            createMeeting()
        }

    }
    override fun getLayout() = R.layout.create_active_reservation_layout

    private fun createMeeting(){
        val meetingId = binding.meetingId.text.toString()
        val meetingPassword = binding.meetingPassword.text.toString()
        if (controlMeetingId() && controlMeetingPassword()) {
            val updates = hashMapOf<String, Any>(
                    "meetingId" to meetingId,
                    "meetingPassword" to meetingPassword,
                    "status" to "aktif")
            val path = "reservations/$reservationId"
            firebaseFirestore.document(path).update(updates)

            val snackbar = Snackbar.make(binding.root,getString(R.string.basarili), Snackbar.LENGTH_SHORT)
            snackbar.view.setBackgroundColor(Color.parseColor("#107163"))
            snackbar.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar, event: Int) {
                    finish()
                }
            })
            snackbar.show()

        }
        else {
            createSnackBar(binding.root, getString(R.string.eksik_alanlari__doldur), Snackbar.LENGTH_SHORT, "#dc3545", Color.WHITE)
        }
    }
    private fun controlMeetingId() : Boolean{
        if(binding.meetingId.text.isEmpty()) return false
        return true
    }
    private fun controlMeetingPassword() : Boolean{
        if(binding.meetingPassword.text.isEmpty()) return false
        return true
    }

}