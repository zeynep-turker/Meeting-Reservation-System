package com.example.reservation_system.ui.act_hizmet_veren.home_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.PeopleAsNameLayoutBinding
import com.example.reservation_system.model.HizmetVeren
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class HizmetVerenPeopleAsName : BaseActivity<PeopleAsNameLayoutBinding>(){
    private lateinit var searchName : String
    private lateinit var query : Query
    private lateinit var adapter: FirestoreRecyclerAdapter<HizmetVeren, PeopleViewHolder>
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.people_as_category_layout)
        firebaseFirestore = FirebaseFirestore.getInstance()
        searchName = intent.extras?.getString("name").toString()
        firebaseAuth = FirebaseAuth.getInstance()
        fetch()
        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.peopleAsName.adapter = adapter
        binding.peopleAsName.layoutManager = manager
    }
    override fun getLayout() = R.layout.people_as_name_layout

    private fun fetch() {
        query = firebaseFirestore.collection("users")
                .whereEqualTo("userType","Hizmet Alan")
                .whereGreaterThan("nameSurname", searchName)
                .whereLessThan("nameSurname","$searchName~")



        val options: FirestoreRecyclerOptions<HizmetVeren> = FirestoreRecyclerOptions.Builder<HizmetVeren>()
                .setQuery(query, HizmetVeren::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<HizmetVeren, PeopleViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.people_as_category_card_view_layout, parent, false)
                return PeopleViewHolder(view)
            }

            override fun onBindViewHolder(holder: PeopleViewHolder, position: Int, model: HizmetVeren) {
                if (model.getterProfilImage() == "empty"){
                    holder.personImage.setImageResource(R.drawable.profile_page)
                }
                else {
                    holder.setImage(model.getterProfilImage())
                }
                holder.setPersonName(model.nameSurname)
                holder.personJob.visibility = View.GONE
                holder.starLayout.visibility = View.GONE
            }
        }
    }
    class PeopleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val personImage : ImageView = itemView.findViewById(R.id.person_profil_image)
        val personName : TextView = itemView.findViewById(R.id.person_name)
        val personJob : TextView = itemView.findViewById(R.id.person_job)
        val starLayout : LinearLayout = itemView.findViewById(R.id.star_layout)
        fun setImage(url : String){
            if(url != null && url != "" && url != " " && url != "empty") {
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                ref.downloadUrl.addOnSuccessListener { Uri ->
                    val imageURL = Uri.toString()
                    Picasso.get().load(imageURL).into(personImage)
                }
            }
        }
        fun setPersonName(text : String) {
            personName.text = text
        }
        fun setPersonjon(text : String){
            personJob.text = text
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
}