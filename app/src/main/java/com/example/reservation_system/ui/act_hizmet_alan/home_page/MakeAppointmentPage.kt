package com.example.reservation_system.ui.act_hizmet_alan.home_page

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.MakeAppointmentLayoutBinding
import com.example.reservation_system.model.Randevu
import com.example.reservation_system.notification.MyFirebaseMessagingService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import kotlinx.android.synthetic.main.user_evaluating_layout.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class MakeAppointmentPage : BaseActivity<MakeAppointmentLayoutBinding>(){
    lateinit var userId : String
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private var choosenDay = ""
    private var temp1 = false
    private var temp2 = false
    private lateinit var docId : String
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.make_appointment_layout)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userId = intent.extras?.getString("userId").toString()

        getUserInfo()
        setCalendar()

        binding.morningHoursGroup.setOnCheckedChangeListener { group, checkedId ->
            val id: Int = binding.eveningHoursGroup.checkedRadioButtonId
            if(id != -1 && temp1) {
                binding.eveningHoursGroup.clearCheck()
                temp1 = false
            }
            temp2 = true
        }
        binding.eveningHoursGroup.setOnCheckedChangeListener { group, checkedId ->
            val id: Int = binding.morningHoursGroup.checkedRadioButtonId
            val id2: Int = binding.eveningHoursGroup.checkedRadioButtonId

            if(id != -1 && temp2) {
                binding.morningHoursGroup.clearCheck()
                temp2 = false
            }
            temp1 = true
        }
        binding.makeAppointment.setOnClickListener {
            controlValues()
        }
        binding.ratingUserButton.setOnClickListener {
            ratingUser()
        }
        setRatingbuttonVisibility()






    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setRatingbuttonVisibility(){
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val date = formatter.format(now).toString()
        firebaseFirestore.collection("reservations")
                .whereEqualTo("hizmetVeren",userId)
                .whereEqualTo("hizmetAlan",firebaseAuth.uid)
                .whereEqualTo("rate",true)
                .get()
                .addOnSuccessListener { document ->
                    if(document.size() == 0) {
                        firebaseFirestore.collection("reservations")
                                .whereEqualTo("hizmetVeren", userId)
                                .whereEqualTo("hizmetAlan", firebaseAuth.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    for(doc in document){
                                        val date2 = doc["date"].toString()
                                        val localDate1 = LocalDate.parse(date2, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                        val localDate2 = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                        val long1 = localDate1.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
                                        val long2 = localDate2.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond

                                        if(long1 < long2){
                                            docId = doc.id
                                            binding.ratingUserButton.visibility = View.VISIBLE
                                            return@addOnSuccessListener
                                        }
                                    }
                                }
                                .addOnFailureListener { _ ->
                                }
                    }
                }
                .addOnFailureListener { _ ->
                }
    }

    private fun ratingUser(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.user_evaluating_layout, null)

        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).show()

        mBuilder.degerlendir_button.setOnClickListener {

            val rating = mBuilder.user_rate.rating
            Log.d("num", rating.toString())
            firebaseFirestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener {
                        var ratingNumber = it["ratingNumber"] as Double
                        var ratingUserNumber = it["ratingUserNumber"] as Long
                        ratingUserNumber++
                        val newRatingNumber = (ratingNumber+rating) / ratingUserNumber

                        val updates = hashMapOf<String, Any>(
                                "ratingNumber" to newRatingNumber,
                                "ratingUserNumber" to ratingUserNumber)
                        firebaseFirestore.collection("users").document(userId).update(updates)
                        firebaseFirestore.collection("reservations").document(docId).update("rate",true)
                        Toast.makeText(this,"Değerlendirildi",Toast.LENGTH_SHORT).show()
                        binding.ratingUserButton.visibility = View.INVISIBLE
                        mBuilder.dismiss()

                    }
                    .addOnFailureListener {

                    }


            mBuilder.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun controlValues() {
        if (!controlDate() || !controlHour()){
            createSnackBar(binding.root,getString(R.string.eksik_alanlari__doldur), Snackbar.LENGTH_SHORT,"#dc3545", Color.WHITE)
        }
        else{
            createReservationRequest()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createReservationRequest() {
        val hour : String
        val pmOrAm : String
        if(temp1){
            val eveningId: Int = binding.eveningHoursGroup.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(eveningId)
            hour = radioButton.text.toString()
            pmOrAm = "PM"
        }
        else{
            val morningId: Int = binding.morningHoursGroup.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(morningId)
            hour = radioButton.text.toString()
            pmOrAm = "AM"
        }

        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val date = formatter.format(now).toString()

        val reservation = Randevu()
        reservation.hizmetAlan = firebaseAuth.currentUser!!.uid
        reservation.hizmetVeren = userId
        reservation.date = choosenDay
        reservation.hour = hour
        reservation.pmOrAm = pmOrAm
        reservation.status = "talep"
        reservation.takenDate = date
        firebaseFirestore.collection("reservations").add(reservation)
                .addOnSuccessListener { documentReference ->
                    documentReference.update("id",documentReference.id)
                    val notif = MyFirebaseMessagingService()
                    notif.createNotification(firebaseAuth.currentUser!!.uid,userId,"Yeni bir randevu talebiniz var.",documentReference.id)
        }
        createSnackBar(binding.root, getString(R.string.basarili), Snackbar.LENGTH_SHORT, "#107163", Color.WHITE)


    }


    private fun controlDate() : Boolean{
        if (choosenDay != "") return true
        return false
    }
    private fun controlHour() : Boolean{
        val eveningId: Int = binding.eveningHoursGroup.checkedRadioButtonId
        val morningId: Int = binding.morningHoursGroup.checkedRadioButtonId
        if(eveningId != -1 && temp1) {
            val radioButton = findViewById<RadioButton>(eveningId)
            return true
        }
        else if(morningId != -1 && temp2) {
            val radioButton = findViewById<RadioButton>(morningId)
            return true
        }
        return false
    }


    private fun getUserInfo() {
        firebaseFirestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val nameSurname = document.get("nameSurname").toString()
                    val job = document.get("job").toString()
                    val imageUrl = document.get("profilImage").toString()
                    val ratingNumber = document.get("ratingNumber") as Double
                    binding.userName.text = nameSurname
                    binding.userJob.text = job
                    binding.userRate.rating = ratingNumber.toFloat()
                    setProfilImage(imageUrl)
                }
            }
            .addOnFailureListener { _ ->
            }
    }

    private fun setCalendar() {
        val startDate: Calendar = Calendar.getInstance()
        startDate.add(Calendar.DAY_OF_WEEK, 6)
        val endDate: Calendar =  Calendar.getInstance()
        endDate.add(Calendar.DAY_OF_WEEK,6)
        endDate.add(Calendar.DAY_OF_WEEK, 14)

        val horizontalCalendar =
            HorizontalCalendar.Builder(this, binding.days.id)
                .range(startDate, endDate)
                .defaultSelectedDate(startDate)
                .datesNumberOnScreen(5)
                .build()

        horizontalCalendar.calendarListener = object : HorizontalCalendarListener() {
            @SuppressLint("SimpleDateFormat")
            override fun onDateSelected(date: Calendar, position: Int) {
                val c = Calendar.getInstance()
                c.time = date.time
                c.add(Calendar.DATE, 1)
                date.time = c.time
                val dateFormat = SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
                val date = dateFormat.parse(date.time.toString())
                val formatter = SimpleDateFormat("dd-MM-yyyy")
                choosenDay = formatter.format(date)
            }
        }
    }
    private fun setProfilImage(profilUri:String){
        if (profilUri == "empty"){
            binding.personProfilImage.setImageResource(R.drawable.profile_page)
        }
        else {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(profilUri)
            ref.downloadUrl.addOnSuccessListener { Uri ->
                val imageURL = Uri.toString()
                Picasso.get().load(imageURL).into(binding.personProfilImage)
            }
        }
    }
    override fun getLayout() = R.layout.make_appointment_layout
}