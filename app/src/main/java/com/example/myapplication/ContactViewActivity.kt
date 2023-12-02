package com.example.myapplication

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.Serializable

class ContactViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val i = intent

        val contactID = i.serializable<Long>("key")

        val myApplication = application as MyApplication


        setContent {
            MyApplicationTheme {
                //val contactList by viewModel.contactList.observeAsState(emptyList())
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactPage(
                        this,
                        contactID.toString(),
                        myApplication.contactViewModel
                    )
                }
            }
        }
    }

    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPage(
    contactViewActivity: ContactViewActivity,
    contactId: String,
    viewModel: ContactViewModel
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        viewModel.getContact2(contactId)
        viewModel.getPhone2(contactId)
        val contact by viewModel.contact.observeAsState()
        val phoneList by viewModel.currphoneList.observeAsState(emptyList())
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(onClick = {
                        // Go back to Main Page
                        contactViewActivity.finish()
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                    Text("Contact Profile")
                    IconButton(onClick = {
                        // Go back to Main Page
                        // Create an explicit intent to start SecondActivity
                        val intent = Intent(contactViewActivity, EditContactActivity::class.java)

                        // Add any extra data you want to pass to SecondActivity
                        viewModel.contact.value?.let { intent.putExtra("key", it.id) }

                        // Start SecondActivity
                        contactViewActivity.startActivity(intent)
                    }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Contact")
                    }
                }
            },
            actions = {
                // If you have additional actions, you can add them here
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(256.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            // You can load the contact image here
            if (contact?.photoUri == Uri.EMPTY){
                Icon(imageVector = Icons.Default.Person,
                    contentDescription = "Profile Pic",
                    tint = Color.White,
                    modifier = Modifier
                        .size(256.dp))
            }
            else{
                AsyncImage(
                    model = contact?.photoUri,
                    contentDescription = "Profile Pic",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }

        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(8.dp)
        ) {
            // Contact Name

            Spacer(modifier = Modifier.height(16.dp))
            if (contact != null) {
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                )
                {
                    item {
                        Text(
                            text = contact!!.name,
                            fontSize = 40.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(phoneList) { phoneNum ->
                    PhoneNumItem(contactPhoneNumber = ContactPhoneNumber(phoneNum.id,phoneNum.contact_id, phoneNum.phoneNumber))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PhoneNumItem(contactPhoneNumber: ContactPhoneNumber){
    val ctx = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(8.dp)
    ){
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .weight(0.45f)
                .padding(4.dp)
        ) {
            IconButton(onClick = {
                // Open dialer with number
                val u = Uri.parse("tel:" + contactPhoneNumber.phoneNumber)
                // Create the intent and set the data for the
                // intent as the phone number.
                val i = Intent(Intent.ACTION_DIAL, u)
                try {
                    // Launch the Phone app's dialer with a phone
                    // number to dial a call.
                    ctx.startActivity(i)
                } catch (s: SecurityException) {
                    // show() method display the toast with
                    // exception message.
                    mToast(ctx, "An error occurred")
                }
            }) {
                Icon(imageVector = Icons.Filled.Phone, contentDescription = "Dial", tint = Color.Black)
            }
            IconButton(onClick = {
                // Open SMS app with pre-populated number
                val phoneNumber = contactPhoneNumber.phoneNumber
                val smsUri = Uri.parse("smsto:$phoneNumber")

                // Create the intent and set the data for the
                // intent as the phone number.
                val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)

                try {
                    // Launch the SMS app with the pre-populated
                    // phone number.
                    ctx.startActivity(smsIntent)
                } catch (e: ActivityNotFoundException) {
                    // Handle the case where there is no SMS app
                    // on the device or no activity to handle the intent.
                    Toast.makeText(ctx, "No SMS app found", Toast.LENGTH_LONG).show()
                }
            }) {
                Icon(imageVector = Icons.Filled.Message, contentDescription = "Message", tint = Color.Black)
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp)
        ) {
            item{
                Text(
                    text = contactPhoneNumber.phoneNumber,
                    fontSize = 24.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    MyApplicationTheme {
        //Greeting("Android")
    }
}