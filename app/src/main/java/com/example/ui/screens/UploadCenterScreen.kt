package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UploadCenterScreen(
  onPublishPost: (
    postType: String,
    caption: String,
    subject: String,
    mediaUris: List<String>,
    hashtags: List<String>,
    locationTag: String,
    visibility: String,
  ) -> Unit,
  onPublishStory: (String, String, String, String, String, Boolean, String) -> Unit,
) {
  var postType by remember { mutableStateOf("educational") }
  var caption by remember { mutableStateOf("") }
  var subject by remember { mutableStateOf("Computer Science") }
  var hashtagsText by remember { mutableStateOf("#istudent #learning") }
  var locationTag by remember { mutableStateOf("") }
  var visibility by remember { mutableStateOf("public") }
  var selectedMediaUris by remember { mutableStateOf<List<String>>(emptyList()) }

  // Image Studio Editing Controls
  var rotationDeg by remember { mutableFloatStateOf(0f) }
  var brightness by remember { mutableFloatStateOf(0f) } // -100..100
  var contrast by remember { mutableFloatStateOf(1f) } // 0.5..1.8
  var saturation by remember { mutableFloatStateOf(1f) } // 0..2

  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      selectedMediaUris = uris.map { it.toString() }
    }
  }

  val videoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedMediaUris = listOf(uri.toString())
      postType = "reel"
    }
  }

  val typeOptions = listOf(
    "educational" to "Educational",
    "photo" to "Photo",
    "reel" to "Knowledge Reel",
    "note" to "Study Note",
    "text" to "Text Update",
    "story" to "24h Story",
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text("Upload & Creator Studio", style = MaterialTheme.typography.headlineSmall)
    Text(
      "Share photos, Knowledge Reels, collaborative study notes, or 24h stories with built-in editing controls.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      typeOptions.forEach { (key, label) ->
        FilterChip(
          selected = postType == key,
          onClick = { postType = key },
          label = { Text(label) },
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(
        onClick = {
          photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        modifier = Modifier.weight(1f).testTag("upload_pick_photos_button"),
      ) {
        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Pick Photos")
      }
      OutlinedButton(
        onClick = {
          videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        },
        modifier = Modifier.weight(1f).testTag("upload_pick_video_button"),
      ) {
        Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Pick Video")
      }
    }

    if (selectedMediaUris.isNotEmpty()) {
      val colorMatrix = remember(brightness, contrast, saturation) {
        val satMatrix = ColorMatrix().apply { setToSaturation(saturation) }
        val translate = (1f - contrast) * 128f + brightness
        val bcMatrix = ColorMatrix(
          floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f,
          )
        )
        satMatrix.timesAssign(bcMatrix)
        satMatrix
      }

      Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Media Preview (${selectedMediaUris.size} selected)", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = { rotationDeg = (rotationDeg + 90f) % 360f }) {
              Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate image")
            }
          }
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant),
          ) {
            AsyncImage(
              model = selectedMediaUris.first(),
              contentDescription = "Selected media preview",
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotationDeg },
              colorFilter = ColorFilter.colorMatrix(colorMatrix),
              contentScale = ContentScale.Crop,
            )
          }
          Text("Brightness", style = MaterialTheme.typography.labelSmall)
          Slider(value = brightness, onValueChange = { brightness = it }, valueRange = -60f..60f)
          Text("Contrast", style = MaterialTheme.typography.labelSmall)
          Slider(value = contrast, onValueChange = { contrast = it }, valueRange = 0.6f..1.6f)
          Text("Saturation", style = MaterialTheme.typography.labelSmall)
          Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..2f)
        }
      }
    }

    OutlinedTextField(
      value = caption,
      onValueChange = { caption = it },
      label = { Text("Caption, Study Notes, or Lesson Summary") },
      minLines = 3,
      modifier = Modifier.fillMaxWidth().testTag("upload_caption_input"),
    )

    OutlinedTextField(
      value = subject,
      onValueChange = { subject = it },
      label = { Text("Subject / Topic") },
      leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
      value = hashtagsText,
      onValueChange = { hashtagsText = it },
      label = { Text("Hashtags (space or comma separated)") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
      value = locationTag,
      onValueChange = { locationTag = it },
      label = { Text("Location Tag (Optional, e.g. Campus Library)") },
      leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Text("Privacy", style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf("public" to "Public", "followers" to "Followers", "private" to "Only Me").forEach { (key, label) ->
        FilterChip(
          selected = visibility == key,
          onClick = { visibility = key },
          label = { Text(label) },
        )
      }
    }

    Button(
      onClick = {
        val tags = hashtagsText.split(" ", ",")
          .map { it.trim().removePrefix("#") }
          .filter { it.isNotBlank() }
        if (postType == "story") {
          onPublishStory(
            if (selectedMediaUris.isNotEmpty()) "photo" else "text",
            caption,
            selectedMediaUris.firstOrNull() ?: "",
            "#4C1D95",
            if (visibility == "followers") "close_friends" else "public",
            false,
            "",
          )
        } else {
          onPublishPost(
            postType,
            caption,
            subject,
            selectedMediaUris,
            tags,
            locationTag,
            visibility,
          )
        }
        caption = ""
        selectedMediaUris = emptyList()
        rotationDeg = 0f
        brightness = 0f
        contrast = 1f
        saturation = 1f
      },
      enabled = caption.isNotBlank() || selectedMediaUris.isNotEmpty(),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("publish_content_button"),
    ) {
      Icon(Icons.Default.CloudUpload, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Publish to i student", style = MaterialTheme.typography.titleMedium)
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
