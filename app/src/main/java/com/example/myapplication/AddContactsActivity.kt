package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.UUID

class AddContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val myApplication = application as MyApplication
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AddContactsPage(
                        this,
                        contactList = myApplication.contactViewModel.contactList.value.orEmpty(),
                        onAddSelectedContacts = {selectedContacts ->
                            for (newContact in selectedContacts) {
                                myApplication.contactViewModel.addContact(newContact)
                                val phoneNumbers = getContactPhoneNumbers(newContact.id.toString())
                                for (pN in phoneNumbers) {
                                    myApplication.contactViewModel.addPhone(pN)
                                }
                            }
                            mToast(this, "Contacts Added")
                        }
                    )
                }
            }
        }
    }

    private fun getContactPhoneNumbers(contactId: String): List<ContactPhoneNumber> {
        val phoneNumbers = mutableListOf<ContactPhoneNumber>()
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        phoneCursor?.use { phCursor ->
            val phoneNumberIndex = phCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (phCursor.moveToNext()) {
                val phoneNumber = phCursor.getString(phoneNumberIndex)
                phoneNumbers.add(ContactPhoneNumber(generateUniqueId()?:"",contactId.toLong(), phoneNumber))
            }
        }

        return phoneNumbers
    }

    fun fetchContactNames() : ArrayList<Contact> {
        // Use the ContentResolver to query the contacts
        var allContacts = ArrayList<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        // Check if the cursor is not null and move to the first entry
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val contactIdIndex = it.getColumnIndex(ContactsContract.Contacts._ID)

                // Iterate over the cursor to get contact names
                do {
                    val displayName = it.getString(displayNameIndex)
                    val contactId = it.getString(contactIdIndex)

                    allContacts.add(Contact(contactId.toLong(), displayName, Uri.EMPTY))
                } while (it.moveToNext())
            }
        }

        return allContacts
    }

    private fun generateUniqueId(): String? {
        return UUID.randomUUID().toString()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactsPage(
    addContactsActivity: AddContactsActivity,
    contactList: List<Contact>,
    onAddSelectedContacts: (List<Contact>) -> Unit
) {
    // Filter out the contacts in contactList
    val allContacts = addContactsActivity.fetchContactNames()
    val contacts = allContacts.filterNot { contact ->
        contactList.any { it.id == contact.id }
    }

    // Use remember or mutableStateOf to manage the new contact list
    var newContactList by remember { mutableStateOf(emptyList<Contact>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Add New Contacts")
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    // Go back to Main Page
                    addContactsActivity.finish()
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                // If you have additional actions, you can add them here
            }
        )

        // Contacts List
        LazyColumn (
            modifier = Modifier
                .padding(8.dp)
        ) {
            items(contacts) { contact ->
                ContactListItemWithCheckbox(
                    contact = contact,
                    isChecked = newContactList.contains(contact),
                    onCheckedChange = { isChecked ->
                        // Handle checkbox state change
                        newContactList = if (isChecked) {
                            newContactList + contact
                        } else {
                            newContactList - contact
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Add Selected Contacts Button
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    // Handle button click
                    onAddSelectedContacts(newContactList)
                    addContactsActivity.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(text = "Add Selected Contacts")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    MyApplicationTheme {
        //Greeting("Android")
    }
}