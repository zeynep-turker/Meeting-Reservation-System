package com.example.reservation_system.ui.act_hizmet_veren.reservations.reservation_requests

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.HizmetVerenReservationRequestsLayoutBinding
import com.example.reservation_system.model.Randevu
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ReservationRequests : BaseActivity<HizmetVerenReservationRequestsLayoutBinding>() {
    private lateinit var query : Query
    private lateinit var adapter: FirestoreRecyclerAdapter<Randevu, RandevuTalepleriViewHolder>
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hizmet_veren_reservation_requests_layout)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        fetch()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.hizmetVerenRandevuTalepleri.adapter = adapter
        binding.hizmetVerenRandevuTalepleri.layoutManager = layoutManager
    }
    override fun getLayout() = R.layout.hizmet_veren_reservation_requests_layout

    private fun fetch() {

        query = firebaseFirestore.collection("reservations")
                .whereEqualTo("hizmetVeren", firebaseAuth.currentUser?.uid)
                .whereEqualTo("status","talep")

        val options: FirestoreRecyclerOptions<Randevu> = FirestoreRecyclerOptions.Builder<Randevu>()
                .setQuery(query, Randevu::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<Randevu, RandevuTalepleriViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandevuTalepleriViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.hizmet_veren_reservation_request_card_view_layout, parent, false)
                return RandevuTalepleriViewHolder(view)
            }

            override fun onBindViewHolder(holder: RandevuTalepleriViewHolder, position: Int, model: Randevu) {
                getUserInfo(model.hizmetAlan,holder,model.id)
                holder.date.text = model.date
                holder.hour.text = model.hour
                holder.pmOrAm.text = model.pmOrAm
            }
        }
    }

    private fun getUserInfo(id:String,holder: RandevuTalepleriViewHolder, reservationId:String){
        firebaseFirestore.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nameSurname = document.get("nameSurname").toString()
                        val profilImage = document.get("profilImage").toString()
                        val telNo = document.get("phoneNo").toString()

                        holder.personName.text = nameSurname
                        holder.setImage(profilImage)
                        holder.ara.setOnClickListener {
                            if(telNo.isNotEmpty()) {
                                val intent = Intent()
                                intent.action = Intent.ACTION_DIAL // Action for what intent called for
                                intent.data = Uri.parse("tel: $telNo") // Data with intent respective action on intent
                                startActivity(intent)
                            }
                        }
                        holder.randevuOlustur.setOnClickListener {
                            createReservation(reservationId)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                }
    }

    private fun createReservation(id:String){
        val intent = Intent(this, CreateReservation::class.java)
        intent.putExtra("reservationId",id)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
    class RandevuTalepleriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.hizmet_alan_profil_image)
        val personName : TextView = itemView.findViewById(R.id.hizmet_alan_name)
        val date : TextView = itemView.findViewById(R.id.talep_edilen_tarih)
        val hour : TextView = itemView.findViewById(R.id.talep_edilen_saat)
        val pmOrAm : TextView = itemView.findViewById(R.id.talep_edilen_pmOrAm)
        val ara : Button = itemView.findViewById(R.id.hizmet_alan_ara)
        val randevuOlustur : Button = itemView.findViewById(R.id.hizmet_alan_randevu_olustur)


        fun setImage(url : String){
            if (url == "empty"){
                image.setImageResource(R.drawable.profile_page)
            }
            else {
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                ref.downloadUrl.addOnSuccessListener { Uri ->
                    val imageURL = Uri.toString()
                    Picasso.get().load(imageURL).into(image)
                }
            }
        }
        fun setPersonName(text : String){
            personName.text = text
        }
        fun setDate(text : String){
            date.text = text
        }
    }
}