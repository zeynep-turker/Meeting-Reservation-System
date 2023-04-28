package com.example.reservation_system.ui.act_hizmet_veren.reservations.paid_reservations

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.HizmetVerenPaidReservationsLayoutBinding
import com.example.reservation_system.model.Randevu
import com.example.reservation_system.ui.act_hizmet_alan.reservations_page.PaymentWithDekont
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso


class PaidReservations : BaseActivity<HizmetVerenPaidReservationsLayoutBinding>(){
    private lateinit var query : Query
    private lateinit var adapter: FirestoreRecyclerAdapter<Randevu, PaidReservationsViewHolder>
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var firebaseStorage : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hizmet_veren_reservation_requests_layout)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance().reference

        fetch()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.hizmetVerenPaidReservations.adapter = adapter
        binding.hizmetVerenPaidReservations.layoutManager = layoutManager
    }
    override fun getLayout() = R.layout.hizmet_veren_paid_reservations_layout

    private fun fetch() {
        query = firebaseFirestore.collection("reservations")
                .whereEqualTo("hizmetVeren", firebaseAuth.currentUser?.uid)
                .whereEqualTo("status","ödenmiş")

        val options: FirestoreRecyclerOptions<Randevu> = FirestoreRecyclerOptions.Builder<Randevu>()
                .setQuery(query, Randevu::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<Randevu, PaidReservationsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaidReservationsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.hizmet_veren_paid_reservation_card_view_layout, parent, false)
                return PaidReservationsViewHolder(view)
            }

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: PaidReservationsViewHolder, position: Int, model: Randevu) {
                getUserInfo(model.hizmetAlan,holder)
                holder.date.text =  model.date + " " + model.hour + " " + model.pmOrAm
                if(model.paymentMethod == "dekont"){
                    holder.paymentMethodText.text = " Dekont ile ödendi"
                    holder.dekont.visibility = View.VISIBLE
                }else{
                    holder.paymentMethodText.text =  "Kredi kartı ile ödendi"
                }
                holder.dekont.setOnClickListener {
                    downloadPdfFile(model.id)
                }
                holder.toplantiEkle.setOnClickListener {
                    val intent = Intent(baseContext, CreateActiveReservation::class.java)
                    intent.putExtra("reservationId",model.id)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        }
    }

    fun downloadFile(context: Context, fileName: String, fileExtension: String, destinationDirectory: String?, url: String): Long {
        val downloadmanager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri: Uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension)
        return downloadmanager.enqueue(request)
    }
    private fun downloadPdfFile(id: String) {
        firebaseStorage.child("dekonts/$id").downloadUrl.addOnSuccessListener {
            val url = it.toString()
            downloadFile(applicationContext,id,".pdf",Environment.DIRECTORY_DOWNLOADS,url)
        }.addOnFailureListener {
        }
    }

    private fun getUserInfo(id:String,holder: PaidReservationsViewHolder){
        firebaseFirestore.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nameSurname = document.get("nameSurname").toString()
                        val profilImage = document.get("profilImage").toString()

                        holder.personName.text = nameSurname
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
    class PaidReservationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.hizmet_alan_profil_image)
        val personName : TextView = itemView.findViewById(R.id.hizmet_alan_name)
        val date : TextView = itemView.findViewById(R.id.randevu_tarih)
        val dekont : Button = itemView.findViewById(R.id.hizmet_alan_dekont)
        val toplantiEkle : Button = itemView.findViewById(R.id.hizmet_alan_toplanti_ekle)
        val paymentMethodText : TextView = itemView.findViewById(R.id.payment_method_text_id)

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