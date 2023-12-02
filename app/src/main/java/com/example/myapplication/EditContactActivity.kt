package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme

class EditContactActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myApplication = application as MyApplication


        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditContactPage(
                        this,
                        myApplication.contactViewModel
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
    viewModel: ContactViewModel
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {

        val contact by viewModel.contact.observeAsState()
        val phoneList by viewModel.currphoneList.observeAsState(emptyList())
        Log.d("ECA", "Contact: $contact PhoneList: $phoneList")

        val singlePhotoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = {uri->
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                if (uri != null) {
                    editContactActivity.contentResolver.takePersistableUriPermission(uri, flag)
                }
                Log.d("ECA Photo", "$uri")
                if ((uri != Uri.EMPTY) and (uri != null)) {
                    if (contact?.id != null) {
                        viewModel.tempUpdateContactName(
                            EditContact(
                                contact!!.id,
                                contact!!.name,
                                uri
                            )
                        )
                    }
                }
            }
        )

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
                    }
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                    Text("Edit Contact")
                    Spacer(modifier = Modifier.padding(horizontal = 32.dp))
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
               .clickable {
                   if (contact?.photoUri == Uri.EMPTY) {
                       // Open the gallery to select an image
                       singlePhotoPicker.launch(
                           PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                       )
                   } else {
                       if (contact?.id != null) {
                           viewModel.tempUpdateContactName(
                               EditContact(
                                   contact!!.id.toLong(),
                                   contact!!.name.toString(),
                                   Uri.EMPTY
                               )
                           )
                       }
                       //imageUri = null;
                   }
               },
           contentAlignment = Alignment.Center
        ) {
            if (contact?.photoUri != Uri.EMPTY){
                AsyncImage(
                    model = contact?.photoUri,
                    contentDescription = "Profile Pic",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                Icon(imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Image",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(120.dp))
            }
            else{
                Icon(imageVector = Icons.Default.Add,
                    contentDescription = "Add Image",
                    tint = Color.White,
                    modifier = Modifier
                        .size(120.dp))
            }

        }


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
                    editContactActivity.finish()
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




fun openGallery(launcher: ActivityResultLauncher<String>) {
    launcher.launch("image/*")
}

@Composable
fun EditPhoneNumItem(
    contactPhoneNumber: EditPhoneNumber,
    viewModel: ContactViewModel,
    focusRequester: FocusRequester
){
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