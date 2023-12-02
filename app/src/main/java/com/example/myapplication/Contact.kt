package com.example.myapplication

import android.net.Uri

data class Contact (val id: Long = 0, val name: String, val photoUri: Uri?) {
}
data class EditContact (var id: Long = 0, var name: String, var photoUri: Uri?) {
}
data class ContactPhoneNumber(val id: String,val contact_id: Long = 0, val phoneNumber: String){}

data class EditPhoneNumber(var id: String, var contact_id: Long = 0, var phoneNumber: String){
}
