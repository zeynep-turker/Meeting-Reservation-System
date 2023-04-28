package com.example.reservation_system.ui.act_hizmet_alan.home_page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reservation_system.R
import com.example.reservation_system.databinding.HizmetAlanHomePageLayoutBinding
import com.example.reservation_system.model.Category
import com.example.reservation_system.notification.MyFirebaseMessagingService
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class HizmetAlanHomePage : Fragment() {
    private lateinit var query : Query
    private lateinit var adapter: FirestoreRecyclerAdapter<Category, CategoryViewHolder>
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private val language = "tr"
    private lateinit var binding: HizmetAlanHomePageLayoutBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.hizmet_alan_home_page_layout, container, false)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        controlNotification()
        getUserNameSurname()
        fetch()
        val layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        binding.categoryRecyclerView.adapter = adapter
        binding.categoryRecyclerView.layoutManager = layoutManager

        binding.searchImage.setOnClickListener {
            val name = binding.searchName.text.toString()
            if(name.isNotEmpty()){
                val intent = Intent(context, HizmetAlanPeopleAsName::class.java)
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
            .addOnFailureListener { _ ->
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

    private fun fetch(){
        query = firebaseFirestore.collection("categories").document("jobs").collection(language)

        val options: FirestoreRecyclerOptions<Category> = FirestoreRecyclerOptions.Builder<Category>()
            .setQuery(query, Category::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<Category, CategoryViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.category_card_view_layout, parent, false)
                return CategoryViewHolder(view)
            }

            override fun onBindViewHolder(holder: CategoryViewHolder, position: Int, model: Category) {
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(model.getterIconUrl())
                ref.downloadUrl.addOnSuccessListener {Uri->
                    val imageURL = Uri.toString()
                    Picasso.get().load(imageURL).into(holder.image)
                }
                holder.setCategoryName(model.name)
                holder.categoryCard.setOnClickListener {
                    val intent = Intent(context, PeopleAsCategory::class.java)
                    intent.putExtra("category",model.name)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        }
    }
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.category_icon)
        val categoryName : TextView = itemView.findViewById(R.id.category_name)
        val categoryCard : CardView = itemView.findViewById(R.id.category_card)
        fun setImage(url : String){
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            ref.downloadUrl.addOnSuccessListener {Uri->
                val imageURL = Uri.toString()
                Picasso.get().load(imageURL).into(image)
            }
        }
        fun setCategoryName(text : String){
            categoryName.text = text
        }
    }
    private fun controlNotification(){
        val ref = firebaseFirestore.collection("notifications")
                .whereEqualTo("to",firebaseAuth.currentUser.uid)
                .whereEqualTo("deleteHizmetAlan",false)
                .whereEqualTo("gone",false)

        ref.addSnapshotListener(object : EventListener<QuerySnapshot>
        {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                for (newNotif in value!!.documents){
                    val controlNotification = MyFirebaseMessagingService()
                    controlNotification.showNotification(requireContext(),newNotif["from"].toString(),newNotif["message"].toString())
                    firebaseFirestore.collection("notifications").document(newNotif["notificationId"].toString()).update("gone",true)
                }
            }
        })


    }

}

