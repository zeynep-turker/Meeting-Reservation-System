package com.example.reservation_system.ui.act_hizmet_alan.home_page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.PeopleAsCategoryLayoutBinding
import com.example.reservation_system.model.HizmetVeren
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class PeopleAsCategory : BaseActivity<PeopleAsCategoryLayoutBinding>(){
    private lateinit var category : String
    private lateinit var query : Query
    private lateinit var adapter: FirestoreRecyclerAdapter<HizmetVeren, PeopleViewHolder>
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.people_as_category_layout)
        firebaseFirestore = FirebaseFirestore.getInstance()
        category = intent.extras?.getString("category").toString()
        firebaseAuth = FirebaseAuth.getInstance()
        binding.job.text = category
        fetch()
        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.peopleAsCategory.adapter = adapter
        binding.peopleAsCategory.layoutManager = manager
    }
    override fun getLayout() = R.layout.people_as_category_layout

    private fun fetch() {
        query = firebaseFirestore.collection("users")
                .whereEqualTo("job", category)


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
                holder.setPersonjon(model.job)

                holder.userRatingNumber.text = model.ratingNumber.toString()
                if(model.ratingNumber == 0.0) holder.starLayout.visibility = View.INVISIBLE
                else holder.starLayout.visibility = View.VISIBLE

                holder.card.setOnClickListener {
                    val intent = Intent(baseContext, MakeAppointmentPage::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("userId",model.id)
                    startActivity(intent)
                }
            }
        }
    }
    class PeopleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val personImage : ImageView = itemView.findViewById(R.id.person_profil_image)
        val personName : TextView = itemView.findViewById(R.id.person_name)
        val personJob : TextView = itemView.findViewById(R.id.person_job)
        val card : CardView = itemView.findViewById(R.id.category_card)
        val starLayout : LinearLayout = itemView.findViewById(R.id.star_layout)
        val userRatingNumber : TextView = itemView.findViewById(R.id.user_rating_number)
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