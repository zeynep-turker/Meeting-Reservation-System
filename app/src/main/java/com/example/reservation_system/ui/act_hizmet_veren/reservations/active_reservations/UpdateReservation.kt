package com.example.reservation_system.ui.act_hizmet_veren.reservations.active_reservations

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.Toast
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.UpdateReservationLayoutBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.hizmet_veren_randevu_saatleri_dialog_layout.*
import java.util.*

class UpdateReservation : BaseActivity<UpdateReservationLayoutBinding>() {
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    lateinit var reservationId : String
    var hour = ""
    var pmOrAm = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_reservation_layout)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        reservationId = intent.extras?.getString("reservationId").toString()
        binding.updateReservationButton.setOnClickListener {
            update()
        }
        binding.updateRandevuTarihiEditText.setOnClickListener {
            setReservationDate()
        }
        binding.updateRandevuSaatiEditText.setOnClickListener {
            chooseHour()
        }
    }
    override fun getLayout() = R.layout.update_reservation_layout

    private fun update(){
        val date = binding.updateRandevuTarihiEditText.text.toString()
        val meetingId = binding.updateMeetingId.text.toString()
        val meetingPassword = binding.updateMeetingPassword.text.toString()

        if(controlMeetingId() && controlMeetingPassword() && controlReservationDate() && controlReservationHour()){
            val resHour = hour
            val resPmOrAm = pmOrAm
            val updates = hashMapOf<String, Any>(
                    "date" to date,
                    "hour" to resHour,
                    "pmOrAm" to resPmOrAm,
                    "meetingId" to meetingId,
                    "meetingPassword" to meetingPassword)
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
    private fun controlReservationHour() : Boolean{
        if(binding.updateRandevuSaatiEditText.text.isEmpty()){
            return false
        }
        return true
    }
    private fun controlReservationDate() : Boolean{
        if(binding.updateRandevuTarihiEditText.text.isEmpty() ){
            return false
        }
        return true
    }
    private fun controlMeetingId() : Boolean{
        if(binding.updateMeetingId.text.isEmpty()) return false
        return true
    }
    private fun controlMeetingPassword() : Boolean{
        if(binding.updateMeetingPassword.text.isEmpty()) return false
        return true
    }
    @SuppressLint("SetTextI18n")
    private fun chooseHour() {
        var temp1 = false
        var temp2 = false
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.hizmet_veren_randevu_saatleri_dialog_layout, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).show()
        mBuilder.choose_reservation_hour.setOnClickListener {
            if(temp1){
                val eveningId: Int = mBuilder.evening_hours_group.checkedRadioButtonId
                val radioButton = mBuilder.findViewById<RadioButton>(eveningId)
                hour = radioButton.text.toString()
                pmOrAm = "PM"
                binding.updateRandevuSaatiEditText.setText("$hour $pmOrAm")
                mBuilder.dismiss()
            }
            else if(temp2){
                val morningId: Int = mBuilder.morning_hours_group.checkedRadioButtonId
                val radioButton = mBuilder.findViewById<RadioButton>(morningId)
                hour = radioButton.text.toString()
                pmOrAm = "AM"
                binding.updateRandevuSaatiEditText.setText("$hour $pmOrAm")
                mBuilder.dismiss()
            }
            else{
                Toast.makeText(baseContext,getString(R.string.eksik_alanlari__doldur), Toast.LENGTH_SHORT).show()
            }

        }
        mBuilder.morning_hours_group.setOnCheckedChangeListener { group, checkedId ->
            val id: Int = mBuilder.evening_hours_group.checkedRadioButtonId
            if(id != -1 && temp1) {
                mBuilder.evening_hours_group.clearCheck()
                temp1 = false
            }
            temp2 = true
        }
        mBuilder.evening_hours_group.setOnCheckedChangeListener { group, checkedId ->
            val id: Int = mBuilder.morning_hours_group.checkedRadioButtonId
            if(id != -1 && temp2) {
                mBuilder.morning_hours_group.clearCheck()
                temp2 = false
            }
            temp1 = true
        }
    }
    @SuppressLint("SetTextI18n")
    private fun setReservationDate(){
        val calendar = Calendar.getInstance()
        val chosen_year = calendar.get(Calendar.YEAR)
        val chosen_month = calendar.get(Calendar.MONTH)
        val chosen_day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this@UpdateReservation, DatePickerDialog.OnDateSetListener
        { _, year, monthOfYear, dayOfMonth ->
            binding.updateRandevuTarihiEditText.setText("" +dayOfMonth + "-" + (monthOfYear+1) + "-" + year).toString()
        }, chosen_year, chosen_month, chosen_day)
        datePickerDialog.show()
    }
}