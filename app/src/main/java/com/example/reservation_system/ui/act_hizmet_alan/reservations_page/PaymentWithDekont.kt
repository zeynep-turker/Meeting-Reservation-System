package com.example.reservation_system.ui.act_hizmet_alan.reservations_page

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.PaymentWithDekontLayoutBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.Continuation
import com.google.android.material.snackbar.Snackbar

class PaymentWithDekont : BaseActivity<PaymentWithDekontLayoutBinding>() {
    private lateinit var reservationId : String
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private var firebaseStorage : StorageReference? = null
    private var pdfUri : Uri? = null
    private lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_with_dekont_layout)
        reservationId = intent.extras?.getString("reservationId").toString()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance().reference
        userId = firebaseAuth.currentUser!!.uid
        getReservationInfo()
        binding.dekontYukleButonId.setOnClickListener {
            selectFile()
        }
        binding.dekontOnaylaButtonId.setOnClickListener {
            upload()
        }
    }
    override fun getLayout() = R.layout.payment_with_dekont_layout
    private fun selectFile(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            selectPdf()
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),9)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectPdf()
        }
        else{
            Toast.makeText(this,"please provide premission",Toast.LENGTH_SHORT).show()
        }
    }
    private fun selectPdf(){

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,86)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 86) {
                pdfUri = data!!.data
                binding.fileNameId.visibility = View.VISIBLE
            }
        }
        else{
            Toast.makeText(this,"Lütfen dekont dosyasını seçiniz..",Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun upload() : Boolean{
        if(pdfUri != null){
            binding.progressBar.visibility = View.VISIBLE
        val mReference = firebaseStorage?.child("dekonts/$reservationId")
            val uploadTask = mReference?.putFile(pdfUri!!)

            uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation mReference.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    addUploadRecordToDb(downloadUri.toString())
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
            return true
        }
        else
            binding.progressBar.visibility = View.GONE

        return false
    }

    private fun addUploadRecordToDb(uri: String){
        val updates = hashMapOf<String, Any>(
                "dekontUrl" to uri,
                "paymentMethod" to "dekont",
                "status" to "ödenmiş",
                "paid" to true
        )
        val path = "reservations/$reservationId"
        firebaseFirestore.document(path).update(updates)
        binding.progressBar.visibility = View.GONE

        val snackbar = Snackbar.make(binding.root,getString(R.string.basarili), Snackbar.LENGTH_SHORT)
        snackbar.view.setBackgroundColor(Color.parseColor("#107163"))
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(snackbar: Snackbar, event: Int) {
                finish()
            }
        })
        snackbar.show()
    }
    private fun getReservationInfo() {
        firebaseFirestore.collection("reservations").document(reservationId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val accountName = document.get("accountName").toString()
                        val iban = document.get("hizmetVerenIban").toString()
                        val lastPaymentDate = document.get("lastPaymentDate").toString()
                        val price = document.get("pay").toString() + "TL"
                        binding.odenecekMiktarId.text = price.subSequence(1,price.length)
                        binding.hesapAdiId.text = accountName
                        binding.ibanId.text = iban
                        binding.sonOdemeTarihiId.text = lastPaymentDate
                    }
                }
                .addOnFailureListener { _ ->
                }
    }
}