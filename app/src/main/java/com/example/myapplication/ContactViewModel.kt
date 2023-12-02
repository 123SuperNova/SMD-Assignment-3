package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ContactItemViewModel(private val dbHelper: DBHelper) : ViewModel() {

}

class ContactViewModel(private val dbHelper: DBHelper) : ViewModel() {
    private val _contact = MutableLiveData<EditContact>()
    val contact: LiveData<EditContact> get() = _contact

    private val _currphoneList = MutableLiveData<List<EditPhoneNumber>>()
    val currphoneList: LiveData<List<EditPhoneNumber>> get() = _currphoneList

    private val _contactList = MutableLiveData<List<Contact>>()
    val contactList: LiveData<List<Contact>> get() = _contactList

    private val _phoneList = MutableLiveData<List<ContactPhoneNumber>>()
    val phoneList: LiveData<List<ContactPhoneNumber>> get() = _phoneList

    fun getContact2(id: String): Contact? {
        var temp = dbHelper.getContactFromId(id)
        _contact.value = EditContact(temp.id, temp.name, temp.photoUri)
        return temp
    }

    fun getPhone2(contact_id: String): List<ContactPhoneNumber>? {
        var temp = dbHelper.allPhoneNumbersOf(contact_id)
        _currphoneList.value = emptyList()
        for (t in temp){
            _currphoneList.value = (_currphoneList.value ?: emptyList()) + EditPhoneNumber(t.id, t.contact_id, t.phoneNumber)
        }
        return temp
    }

    fun tempUpdateContactName(updatedContact: EditContact) {
        _contact.value = updatedContact
    }

    fun tempUpdateContactPhone(updatedPhone: EditPhoneNumber) {
        // Find the position of the updated phone in the list and update it
        val updatedList = _currphoneList.value?.toMutableList() ?: mutableListOf()
        val index = updatedList.indexOfFirst { it.id == updatedPhone.id }
        if (index != -1) {
            updatedList[index] = updatedPhone
            _currphoneList.value = updatedList
        }
    }

    fun UpdateDBContactName(item: EditContact) {
        viewModelScope.launch {
            dbHelper.updateContactName(item.id.toString(), item.name)
            dbHelper.updateContactPhoto(item.id.toString(), item.photoUri.toString())
            refreshContactData()
        }
    }

    fun UpdateDBContactPhone(item: EditPhoneNumber) {
        viewModelScope.launch {
            dbHelper.updateContactNumber(item.id, item.phoneNumber)
            refreshPhoneData()
            //Log.d("ContactViewModel", "Updated data - Phone List: ${item.phoneNumber}")
        }
    }

    init {
        // Initialize with data from DBHelper
        refreshContactData()
        refreshPhoneData()
    }
    // Use coroutine scope for database operations

    fun refreshContactData() {
        viewModelScope.launch {
            _contactList.value = dbHelper.allUsers
            //Log.d("ContactViewModel", "Refreshed data - Contact List: ${_contactList.value}")
        }
    }

    fun refreshPhoneData() {
        viewModelScope.launch {
            _phoneList.value = dbHelper.allPhoneNumbers
            //Log.d("ContactViewModel", "Refreshed data - Phone List: ${_phoneList.value}")
        }
    }

    fun getContact(id: String): Contact? {
        return _contactList.value.orEmpty().firstOrNull { it.id == id.toLong() }
    }

    fun getPhone(contact_id: String): List<ContactPhoneNumber> {
        return _phoneList.value.orEmpty().filter { it.contact_id == contact_id.toLong() }
    }

    // Use coroutine scope for database operations
    fun addContact(item: Contact) {
        viewModelScope.launch {
            dbHelper.addContactData(item.name, item.id.toString(), item.photoUri.toString())
            refreshContactData()
        }
    }

    // Use coroutine scope for database operations
    fun addPhone(item: ContactPhoneNumber) {
        viewModelScope.launch {
            dbHelper.addContactNumberData(item.phoneNumber, item.contact_id.toString(),item.id.toString())
            refreshPhoneData()
        }
    }

    // Use coroutine scope for database operations
    fun removeAllContactsAndPhone(items: List<Contact>) {
        viewModelScope.launch {
            for (item in items) {
                dbHelper.deleteContactNumberData(item.id.toString())
                dbHelper.deleteContactData(item.id.toString())
            }
            refreshContactData()
        }
    }


}

