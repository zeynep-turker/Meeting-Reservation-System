package com.example.reservation_system.ui.act_hizmet_veren.reservations.active_reservations

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.HizmetVerenActiveReservationsLayoutBinding
import com.example.reservation_system.model.Randevu
import com.example.reservation_system.ui.act_hizmet_veren.reservations.paid_reservations.CreateActiveReservation
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class ActiveReservations : BaseActivity<HizmetVerenActiveReservationsLayoutBinding>() {
    private lateinit var query : Query
    private lateinit var adapter: FirestoreRecyclerAdapter<Randevu, ActiveReservationsViewHolder>
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hizmet_veren_active_reservations_layout)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        fetch()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.hizmetVerenActiveReservations.adapter = adapter
        binding.hizmetVerenActiveReservations.layoutManager = layoutManager
    }
    override fun getLayout() = R.layout.hizmet_veren_active_reservations_layout

    private fun fetch() {
        query = firebaseFirestore.collection("reservations")
                .whereEqualTo("hizmetVeren", firebaseAuth.currentUser?.uid)
                .whereEqualTo("status","aktif")

        val options: FirestoreRecyclerOptions<Randevu> = FirestoreRecyclerOptions.Builder<Randevu>()
                .setQuery(query, Randevu::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<Randevu, ActiveReservationsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveReservationsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.hizmet_veren_active_reservation_card_view_layout, parent, false)
                return ActiveReservationsViewHolder(view)
            }

            override fun onBindViewHolder(holder: ActiveReservationsViewHolder, position: Int, model: Randevu) {
                getUserInfo(model.hizmetAlan,holder)
                holder.date.text =  model.date + " " + model.hour + " " + model.pmOrAm
                holder.meetingId.text = model.meetingId
                holder.meetingPassword.text = model.meetingPassword
                holder.randevuGuncelle.setOnClickListener {
                    val intent = Intent(baseContext, UpdateReservation::class.java)
                    intent.putExtra("reservationId",model.id)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        }
    }
    private fun getUserInfo(id:String,holder: ActiveReservationsViewHolder){
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
                                intent.action = Intent.ACTION_DIAL
                                intent.data = Uri.parse("tel: $telNo")
                                startActivity(intent)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                }
    }
    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
    class ActiveReservationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.hizmet_alan_profil_image)
        val personName : TextView = itemView.findViewById(R.id.hizmet_alan_name)
        val date : TextView = itemView.findViewById(R.id.randevu_tarih)
        val ara : Button = itemView.findViewById(R.id.hizmet_alan_ara)
        val randevuGuncelle : Button = itemView.findViewById(R.id.randevu_guncelle)
        val meetingId : TextView = itemView.findViewById(R.id.meeting_id_info)
        val meetingPassword : TextView = itemView.findViewById(R.id.meeting_password_info)

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