package com.example.myapplication

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.ui.theme.MyApplicationTheme

class EditContactActivity : ComponentActivity() {
    lateinit var contact: Contact
    lateinit var contactPhoneList: List<ContactPhoneNumber>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val i = intent
        val contactID = i.getSerializableExtra("key")

        val myApplication = application as MyApplication

        contact = myApplication.contactViewModel.getContact(contactID.toString())!!

        contactPhoneList = myApplication.contactViewModel.getPhone(contactID.toString())!!

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditContactPage(
                        this,
                        contactID.toString(),
                        myApplication.contactItemViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactPage(
    editContactActivity: EditContactActivity,
    contactId: String,
    viewModel: ContactItemViewModel
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
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
                        editContactActivity.finish()
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                    Text("Edit Contact")
                    IconButton(onClick = {
                        // Go back to Main Page
                        editContactActivity.finish()
                    }) {
                        Icon(imageVector = Icons.Filled.SaveAlt, contentDescription = "Save Changes")
                    }
                }
            },
            actions = {
                // If you have additional actions, you can add them here
            }
        )
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val view = LocalView.current
        val viewTreeObserver = view.viewTreeObserver
        DisposableEffect(viewTreeObserver) {
            val listener = ViewTreeObserver.OnGlobalLayoutListener {
                val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
                // ... do anything you want here with `isKeyboardOpen`
                if (!isKeyboardOpen){
                    focusManager.clearFocus()
                }
            }

            viewTreeObserver.addOnGlobalLayoutListener(listener)
            onDispose {
                viewTreeObserver.removeOnGlobalLayoutListener(listener)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(256.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            // You can load the contact image here
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        val contact by viewModel.contact.observeAsState()
        val phoneList by viewModel.phoneList.observeAsState(emptyList())
        Log.d("ECA", "Contact: $contact PhoneList: $phoneList")

        // Contact Name
        Spacer(modifier = Modifier.height(16.dp))
        if (contact != null) {
            OutlinedTextField(
                value = contact!!.name,
                onValueChange = { viewModel.tempUpdateContactName(contact!!.copy(name = it)) },
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 30.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White)
                    .focusRequester(focusRequester)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn (
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(8.dp)
        ) {
            items(phoneList) { phoneNum ->
                EditPhoneNumItem(contactPhoneNumber = phoneNum, viewModel=viewModel, focusRequester = focusRequester)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    if (TextUtils.isEmpty(contact?.name)) {
                        mToast(editContactActivity, "Please enter Name.")
                        return@Button;
                    }
                    for (pN in phoneList) {
                        if (TextUtils.isEmpty(pN.phoneNumber)) {
                            mToast(editContactActivity, "Please enter Phone Number.")
                            return@Button;
                        }
                    }
                    Log.d("ECA", "newContact: $contact" +
                            " newPhoneL: $phoneList")
                    contact?.let { viewModel.UpdateDBContactName(it) }
                    for (pN in phoneList) {
                        viewModel.UpdateDBContactPhone(pN)
                    }
                    mToast(editContactActivity, "Contact Updated.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun EditPhoneNumItem(
    contactPhoneNumber: EditPhoneNumber,
    viewModel: ContactItemViewModel,
    focusRequester: FocusRequester
){
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
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = contactPhoneNumber.phoneNumber,
                onValueChange = { viewModel.tempUpdateContactPhone(EditPhoneNumber(contactPhoneNumber.id, contactPhoneNumber.contact_id, it)).toString() },
                //label = { Text(contactPhoneNumber.phoneNumber) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .focusRequester(focusRequester)
            )

        }
    }
}