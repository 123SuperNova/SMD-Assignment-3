package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.Serializable
import kotlin.properties.Delegates

class EditContactActivity : ComponentActivity() {
    private var contactID by Delegates.notNull<Long>()

    var imageUriState  = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myApplication = application as MyApplication

        contactID = intent.serializable("key") ?: 0

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

    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }

    override fun onDestroy() {
        super.onDestroy()
        val myApplication = application as MyApplication
        myApplication.contactViewModel.getContact2(contactID.toString())
        myApplication.contactViewModel.getPhone2(contactID.toString())
    }

    val REQUEST_IMAGE_GET = 1

    @SuppressLint("QueryPermissionsNeeded")
    fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            val fullPhotoUri: Uri? = data?.data
            val myApplication = application as MyApplication
            if ((fullPhotoUri != Uri.EMPTY) and (fullPhotoUri != null)) {
                if (myApplication.contactViewModel.contact.value?.id != null) {
                    myApplication.contactViewModel.tempUpdateContactName(
                        EditContact(
                            myApplication.contactViewModel.contact.value!!.id,
                            myApplication.contactViewModel.contact.value!!.name,
                            fullPhotoUri
                        )
                    )
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

            val singlePhotoPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = {uri->
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    if (uri != null) {
                        editContactActivity.contentResolver.takePersistableUriPermission(uri, flag)
                    }
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

            LazyColumn (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(6.0f)
                    .padding(8.dp)
            ) {
                item{
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .size(256.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable {
                                if (contact?.photoUri == Uri.EMPTY) {
                                    // Open the gallery to select an image
                                    if (Build.VERSION.SDK_INT > 30) {
                                        singlePhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }else{
                                        selectImage()
                                    }
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
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(Color.White)
                                .focusRequester(focusRequester)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
                    .weight(1f)
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
                        contact?.let { viewModel.updateDBContactName(it) }
                        for (pN in phoneList) {
                            viewModel.updateDBContactPhone(pN)
                        }
                        mToast(editContactActivity, "Contact Updated.")
                        editContactActivity.finish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Save",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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
        ) {
            OutlinedTextField(
                value = contactPhoneNumber.phoneNumber,
                singleLine = true,
                onValueChange = {
                    viewModel.tempUpdateContactPhone(
                        EditPhoneNumber(
                            contactPhoneNumber.id,
                            contactPhoneNumber.contact_id,
                            it
                        )
                    ).toString()
                },
                //label = { Text(contactPhoneNumber.phoneNumber) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
    }
}
