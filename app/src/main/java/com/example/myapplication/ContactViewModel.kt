package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ContactItemViewModel(private val dbHelper: DBHelper) : ViewModel() {
    private val _contact = MutableLiveData<EditContact>()
    val contact: LiveData<EditContact> get() = _contact

    private val _phoneList = MutableLiveData<List<EditPhoneNumber>>()
    val phoneList: LiveData<List<EditPhoneNumber>> get() = _phoneList

    init {
        // Initialize with data from DBHelper
    }

    fun getContact(id: String): Contact? {
        var temp = dbHelper.getContactFromId(id)
        _contact.value = EditContact(temp.id, temp.name)
        return temp
    }

    fun getPhone(contact_id: String): List<ContactPhoneNumber>? {
        var temp = dbHelper.allPhoneNumbersOf(contact_id)
        for (t in temp){
            _phoneList.value = _phoneList.value?.plus(EditPhoneNumber(t.id, t.contact_id, t.phoneNumber))
        }
        return temp
    }

    fun UpdateContactName(item: EditContact) {
        viewModelScope.launch {
            dbHelper.updateContactName(item.id.toString(), item.name)
            getContact(item.id.toString())
        }
    }

    fun UpdateContactPhone(item: EditPhoneNumber) {
        viewModelScope.launch {
            dbHelper.updateContactNumber(item.id, item.phoneNumber)
            getPhone(item.contact_id.toString())
            //Log.d("ContactViewModel", "Updated data - Phone List: ${item.phoneNumber}")
        }
    }
}

class ContactViewModel(private val dbHelper: DBHelper) : ViewModel() {

    private val _contactList = MutableLiveData<List<Contact>>()
    val contactList: LiveData<List<Contact>> get() = _contactList

    private val _phoneList = MutableLiveData<List<ContactPhoneNumber>>()
    val phoneList: LiveData<List<ContactPhoneNumber>> get() = _phoneList

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
            dbHelper.addContactData(item.name, item.id.toString())
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

