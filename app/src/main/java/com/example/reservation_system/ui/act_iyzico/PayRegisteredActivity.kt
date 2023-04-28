package com.example.reservation_system.ui.act_iyzico

import android.os.Bundle
import com.example.reservation_system.R
import com.example.reservation_system.core.BaseActivity
import com.example.reservation_system.databinding.PaymentPageLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iyzipay.Options
import com.iyzipay.model.Currency
import com.iyzipay.model.Locale
import com.iyzipay.model.SubMerchant
import com.iyzipay.model.SubMerchantType
import com.iyzipay.request.CreateSubMerchantRequest


class PayRegisteredActivity  : BaseActivity<PaymentPageLayoutBinding>() {
    private lateinit var reservationId : String
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_page_layout)
        reservationId = intent.extras?.getString("reservationId").toString()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        getReservationInfo()
        /*val options = Options()
        options.apiKey = "sandbox-k9vP74uYjifp3OuzL7oxOwswGK9uwg8T"
        options.secretKey = "sandbox-6wtey8DnF4VY3qc6sopv91jIX4bLzFcF"
        options.baseUrl = "https://sandbox-api.iyzipay.com"

        val request = CreateSubMerchantRequest()
        request.setLocale(Locale.TR.getValue());
        request.setConversationId("123456789");
        request.setSubMerchantExternalId("B49224");
        request.setSubMerchantType(SubMerchantType.PERSONAL.name);
        request.setAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");
        request.setContactName("John");
        request.setContactSurname("Doe");
        request.setEmail("email@submerchantemail.com");
        request.setGsmNumber("+905350000000");
        request.setName("John's market");
        request.setIban("TR180006200119000006672315");
        request.setIdentityNumber("31300864726");
        request.setCurrency(Currency.TRY.name);

        val subMerchant = SubMerchant.create(request, options)

        println(subMerchant)*/
    }

    override fun getLayout() = R.layout.payment_page_layout

    private fun getReservationInfo() {
        firebaseFirestore.collection("reservations").document(reservationId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val price = document.get("pay").toString() + "TL"
                        binding.reservationPrice.text = price.subSequence(1,price.length)
                    }
                }
                .addOnFailureListener { _ ->
                }
    }

}