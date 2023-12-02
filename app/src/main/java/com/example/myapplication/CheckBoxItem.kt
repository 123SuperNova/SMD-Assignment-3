package com.example.myapplication

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@Composable
fun ContactListItemWithCheckbox(
    contact: Contact,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // Use remember or mutableStateOf to manage the checkbox state
    var checked by remember { mutableStateOf(isChecked) }
    checked = isChecked

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable {
                checked = !checked
                onCheckedChange(checked)
            }
            .padding(16.dp)
    ) {
        // Checkbox
        Checkbox(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange(it)
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Image Holder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            // You can load the contact image here
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
            modifier = Modifier
                .weight(1f)

        )
    }
}
