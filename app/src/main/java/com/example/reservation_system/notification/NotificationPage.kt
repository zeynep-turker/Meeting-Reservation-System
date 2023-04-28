package com.example.reservation_system.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reservation_system.R
import com.example.reservation_system.databinding.NotificationPageLayoutBinding
import com.example.reservation_system.model.Notification
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.notification_card_view.*

class NotificationPage : Fragment() {
    private lateinit var query : Query
    private lateinit var adapter: FirestoreRecyclerAdapter<Notification, NotificationViewHolder>
    private lateinit var binding: NotificationPageLayoutBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.notification_page_layout, container, false)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fetch()
        val manager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.notificationRecyclerView.adapter = adapter
        binding.notificationRecyclerView.layoutManager = manager
        return binding.root

    }


    private fun fetch() {
        query = db.collection("notifications")
                .whereEqualTo("to",mAuth.currentUser.uid)
                .orderBy("date",Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<Notification> = FirestoreRecyclerOptions.Builder<Notification>()
                .setQuery(query, Notification::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<Notification, NotificationViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.notification_card_view, parent, false)
                return NotificationViewHolder(view)
            }

            override fun onBindViewHolder(holder: NotificationViewHolder, position: Int, model: Notification) {
                holder.message.text = model.text
                holder.date.text = model.date
                holder.delete_notif.setOnClickListener {
                    db.collection("notifications").document(model.notificationId).delete()
                }
                db.collection("users").document(model.from).get().addOnSuccessListener {
                    holder.fromWho.text = it["nameSurname"].toString()
                }
            }
        }
    }
    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fromWho : TextView = itemView.findViewById(R.id.fromWho)
        val message : TextView = itemView.findViewById(R.id.message)
        val date : TextView = itemView.findViewById(R.id.notif_date)
        val delete_notif : Button = itemView.findViewById(R.id.delete_notification)

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