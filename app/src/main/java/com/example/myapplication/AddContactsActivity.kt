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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                        myApplication.contactViewModel
                    )
                }
            }
        }
    }

    fun fetchContactNames(): ArrayList<Contact> {
        // Use the ContentResolver to query the contacts
        val allContacts = ArrayList<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        // Check if the cursor is not null and move to the first entry
        cursor?.use {
            val displayNameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val contactIdIndex = it.getColumnIndex(ContactsContract.Contacts._ID)

            while (it.moveToNext()) {
                val displayName = it.getString(displayNameIndex)
                val contactId = it.getString(contactIdIndex)

                // Check if displayName is not null before adding to the list
                if ((displayName != null) and (contactId != null)) {
                    allContacts.add(Contact(contactId.toLong(), displayName, Uri.EMPTY))
                }
            }
        }

        return allContacts
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactsPage(
    addContactsActivity: AddContactsActivity,
    viewModel: ContactViewModel
) {
    // Filter out the contacts in contactList
    var counter by remember { mutableIntStateOf(0) }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList())}
    val contactList by viewModel.contactList.observeAsState(emptyList())

    LaunchedEffect(counter) {
        launch {
            withContext(Dispatchers.IO){
                val allContacts = addContactsActivity.fetchContactNames()
                contacts = allContacts.filterNot { contact ->
                    contactList.any { it.id == contact.id }
                }
            }
        }
    }
    DisposableEffect(counter) {
        onDispose {
            // Cleanup or side effect when composable is disposed
            counter = 0
        }
    }

    // Use remember or mutableStateOf to manage the new contact list
    var newContactList by remember { mutableStateOf(emptyList<Contact>()) }
    var checked by remember { mutableStateOf(false) }

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add New Contacts")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = "All")
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                checked = it
                                newContactList = if (it) {
                                    contacts
                                } else {
                                    emptyList()
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .weight(6f)
        ) {
            if (contacts.isEmpty()){
                item{
                    Text(
                        text = "There are no new contacts to add.",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .padding(16.dp)
                    )
                }
            }
            else {
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
                            checked = contacts == newContactList
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Add Selected Contacts Button
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
                .weight(1f)
        ) {

            val composableScope = rememberCoroutineScope()
            var isAddingContacts by remember { mutableStateOf(false) }
            Button(
                enabled = newContactList.isNotEmpty() && !isAddingContacts,
                onClick = {
                    // Handle button click
                    composableScope.launch {
                        try {
                            withContext(Dispatchers.Main) {
                                isAddingContacts = true
                            }
                            withContext(Dispatchers.IO) {
                                viewModel.addAllContactsAndPhone(
                                    addContactsActivity.contentResolver,
                                    newContactList
                                )
                                //delay(100)
                            }
                        } finally {
                            // This block is executed even if there's an exception
                            withContext(Dispatchers.Main) {
                                mToast(addContactsActivity, "Contact(s) Added")
                                addContactsActivity.finish()
                                isAddingContacts = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isAddingContacts) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(30.dp)
                    )
                } else {
                    Text(
                        text = "Add Selected Contacts",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
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