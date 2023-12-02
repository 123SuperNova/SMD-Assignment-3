package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity(){
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if the READ_CONTACTS permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted

        } else {
            // Permission is not granted, request it
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
        setContent {
            MyApplicationTheme {
                val myApplication = application as MyApplication
                MainPage(
                    this,
                    myApplication.contactViewModel
                )
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // PERMISSION GRANTED
        } else {
            // PERMISSION NOT GRANTED
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    mainActivity: MainActivity,
    viewModel: ContactViewModel
)
{
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    )
    {
        // Top App Bar
        //val contactList by viewModel.contactList.collectAsState()
        TopAppBar(
            title = { Text("Contacts App") },
            actions = {
                // Delete Contacts Button
                IconButton(onClick = {
                    // Create an explicit intent to start SecondActivity
                    val intent = Intent(mainActivity, DeleteContactsActivity::class.java)

                    // Add any extra data you want to pass to SecondActivity
                    //intent.putExtra("key", "Hello from MainActivity!")

                    // Start SecondActivity
                    mainActivity.startActivity(intent)
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
                // Add Contacts Button
                IconButton(onClick = {
                    // Create an explicit intent to start SecondActivity
                    val intent = Intent(mainActivity, AddContactsActivity::class.java)

                    // Add any extra data you want to pass to SecondActivity
                    //intent.putExtra("key", "Hello from MainActivity!")

                    // Start SecondActivity
                    mainActivity.startActivity(intent)
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        )

        val contactList by viewModel.contactList.observeAsState(emptyList())

        // Contacts List
        LazyColumn (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (contactList.isEmpty()) {
                item {
                    Text(
                        text = "There are no contacts yet,\nclick the + button to add new contacts.",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            } else {
                items(contactList) { contact ->
                    ContactListItem(contact = contact) {
                        // Handle item click and navigate to ContactPage
                        // Create an explicit intent to start SecondActivity
                        val intent = Intent(mainActivity, ContactViewActivity::class.java)

                        // Add any extra data you want to pass to SecondActivity
                        intent.putExtra("key", contact.id)

                        // Start SecondActivity
                        mainActivity.startActivity(intent)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ContactListItem(contact: Contact, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Image Holder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            // You can load the contact image here
            Log.d("MainAPhoto:", "photouri: ${contact.photoUri}")
            if (contact.photoUri == Uri.EMPTY){
                Icon(imageVector = Icons.Default.Person,
                    contentDescription = "Profile Pic",
                    tint = Color.White,
                    modifier = Modifier
                        .size(56.dp))
            }
            else{
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = "Profile Pic",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Contact Name
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = contact.name,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Person Icon
        Icon(imageVector = Icons.Default.Info, contentDescription = "More info", tint= Color.LightGray)
    }
}

// Function to generate a Toast
public fun mToast(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

@Preview(showBackground = true)
@Composable
fun ContactsAppPreview() {

    MyApplicationTheme {

    }
}