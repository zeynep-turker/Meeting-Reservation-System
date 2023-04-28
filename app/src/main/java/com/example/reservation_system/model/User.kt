package com.example.reservation_system.model

open class User {
    fun init(nameSurname: String, email: String, phoneNo: String, password: String, id:String, userType:String, profilImage:String) {
        this.nameSurname = nameSurname
        this.email = email
        this.phoneNo = phoneNo
        this.password = password
        this.id = id
        this.userType = userType
        this.profilImage = profilImage
    }

    var nameSurname = ""
    var email = ""
    var phoneNo = ""
    var id = ""
    var userType = ""
    var password = ""
    var profilImage = ""

    fun getterProfilImage() : String {
        return profilImage
    }
}