package com.example.myapplication

import android.os.Bundle
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class DeleteContactsActivity : ComponentActivity() {
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
                    DeleteContactsPage(
                        this,
                        myApplication.contactViewModel.contactList.value.orEmpty(),
                        onDeleteSelectedContacts = {selectedContacts ->
                            myApplication.contactViewModel.removeAllContactsAndPhone(selectedContacts)
                            mToast(this, "Contacts Deleted")
                        })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteContactsPage(
    deleteContactsActivity: DeleteContactsActivity,
    contactsList: List<Contact>,
    onDeleteSelectedContacts: (List<Contact>) -> Unit
) {
    var checked by remember { mutableStateOf(false) }
    var contactsToDelete by remember { mutableStateOf(emptyList<Contact>()) }

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
                    IconButton(onClick = {
                        // Go back to Main Page
                        deleteContactsActivity.finish()
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                    Text("Delete Contacts")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = "All")
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                checked = it
                                contactsToDelete = if (it) {
                                    contactsList
                                } else {
                                    emptyList()
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
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
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (contactsList.isEmpty()) {
                item {
                    Text(
                        text = "There are no contacts to delete.",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .padding(16.dp)
                    )
                }
            } else {
                items(contactsList) { contact ->
                    ContactListItemWithCheckbox(
                        contact = contact,
                        isChecked = contactsToDelete.contains(contact)
                    ) { isChecked ->
                        // Handle checkbox state change
                        contactsToDelete = if (isChecked) {
                            contactsToDelete + contact
                        } else {
                            contactsToDelete - contact
                        }
                        checked = contactsList == contactsToDelete
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Delete Button
            Button(
                onClick = {
                    // Handle button click
                    onDeleteSelectedContacts(contactsToDelete)
                    deleteContactsActivity.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Delete Selected Contacts",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        //Greeting("Android")
    }
}