package com.example.reservation_system.ui.act_hizmet_alan.reservations_page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reservation_system.R
import com.example.reservation_system.databinding.HizmetAlanReservationsPageLayoutBinding
import com.example.reservation_system.model.Randevu
import com.example.reservation_system.ui.act_iyzico.PayRegisteredActivity
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ReservationsPage : Fragment(){
    private lateinit var binding: HizmetAlanReservationsPageLayoutBinding
    private lateinit var query : Query
    private lateinit var adapter: FirestoreRecyclerAdapter<Randevu,RandevuViewHolder>
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.hizmet_alan_reservations_page_layout, container, false)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        fetch()
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.hizmetAlanReservations.adapter = adapter
        binding.hizmetAlanReservations.layoutManager = layoutManager

        return binding.root
    }

    private fun fetch() {

        query = firebaseFirestore.collection("reservations")
                .whereEqualTo("hizmetAlan", firebaseAuth.currentUser?.uid)
                .whereIn("status", listOf("ödenmemiş","aktif","talep"))
                .orderBy("takenDate",Query.Direction.DESCENDING)


        val options: FirestoreRecyclerOptions<Randevu> = FirestoreRecyclerOptions.Builder<Randevu>()
                .setQuery(query, Randevu::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<Randevu, RandevuViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandevuViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.hizmet_alan_reservation_card_view_layout, parent, false)
                return RandevuViewHolder(view)
            }

            override fun onBindViewHolder(holder: RandevuViewHolder, position: Int, model: Randevu) {
                getUserInfo(model.hizmetVeren,holder)
                holder.date.text =  model.date
                holder.hour.text = model.hour
                holder.pmOrAm.text = model.pmOrAm
                holder.meetingId.text = model.meetingId
                holder.meetingPassword.text = model.meetingPassword
                if(model.status == "ödenmemiş"){
                    holder.kredi_karti.visibility = View.VISIBLE
                    holder.dekont.visibility = View.VISIBLE
                }
                else if(model.status == "aktif"){
                    holder.meetingIdLayout.visibility = View.VISIBLE
                    holder.meetingPasswordLayout.visibility = View.VISIBLE
                }
                holder.kredi_karti.setOnClickListener {
                    val intent = Intent(context, PayRegisteredActivity::class.java)
                    intent.putExtra("reservationId",model.id)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                holder.dekont.setOnClickListener {
                    val intent = Intent(context, PaymentWithDekont::class.java)
                    intent.putExtra("reservationId",model.id)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        }
    }

    private fun getUserInfo(id:String,holder: RandevuViewHolder){
        firebaseFirestore.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nameSurname = document.get("nameSurname").toString()
                        val job = document.get("job").toString()
                        val profilImage = document.get("profilImage").toString()

                        holder.personName.text = nameSurname
                        holder.personJob.text = job
                        holder.setImage(profilImage)
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
    class RandevuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.hizmet_veren_profil_image)
        val personName : TextView = itemView.findViewById(R.id.hizmet_veren_name)
        val personJob : TextView = itemView.findViewById(R.id.hizmet_veren_job)
        val date : TextView = itemView.findViewById(R.id.randevu_tarih)
        val hour : TextView = itemView.findViewById(R.id.randevu_saat)
        val pmOrAm : TextView = itemView.findViewById(R.id.randevu_pmOrAm)
        val kredi_karti : Button = itemView.findViewById(R.id.kredi_karti_ile_odeme)
        val dekont : Button = itemView.findViewById(R.id.dekont_yolla)
        val meetingIdLayout : LinearLayout = itemView.findViewById(R.id.meeting_id_layout)
        val meetingPasswordLayout : LinearLayout = itemView.findViewById(R.id.meeting_password_layout)
        val meetingId : TextView = itemView.findViewById(R.id.meeting_id_info)
        val meetingPassword : TextView = itemView.findViewById(R.id.meeting_password_info)

        fun setImage(url : String){
            if (url == "empty"){
                image.setImageResource(R.drawable.profile_page)
            }
            else {
                println("url**** $url")
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
        fun setPersonJob(text : String){
            personJob.text = text
        }
        fun setDate(text : String){
            date.text = text
        }
    }


}