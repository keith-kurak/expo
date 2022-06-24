package expo.modules.imagepicker.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import expo.modules.core.utilities.ifNull
import expo.modules.imagepicker.ImagePickerOptions
import expo.modules.imagepicker.MediaType
import expo.modules.imagepicker.contracts.CropImageContract
import expo.modules.imagepicker.contracts.ERROR_KEY
import expo.modules.imagepicker.contracts.ImageLibraryContract
import expo.modules.imagepicker.contracts.ImagePickerContractResult
import expo.modules.imagepicker.contracts.OPTIONS_KEY

class ImageLibraryActivity : ComponentActivity() {
  private lateinit var options: ImagePickerOptions

  private val cropperLauncher = registerForActivityResult(CropImageContract(contentResolver, options)) { result ->
    when (result) {
      is ImagePickerContractResult.Cancelled -> finishCancelled()
      is ImagePickerContractResult.Success -> finishWithUri(result.data.second)
    }
  }

  private val imageLibraryLauncher = registerForActivityResult(ImageLibraryContract(contentResolver)) { result ->
    when (result) {
      is ImagePickerContractResult.Cancelled -> finishCancelled()
      is ImagePickerContractResult.Success -> {
        if (result.data.first == MediaType.VIDEO || !options.allowsEditing) {
          finishWithUri(result.data.second)
        } else {
          cropperLauncher.launch(result.data.second)
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    options = intent.getSerializableExtra(OPTIONS_KEY).ifNull {
      return finishWithMissingOptionsError()
    } as ImagePickerOptions
    imageLibraryLauncher.launch(options.toImageLibraryContractOptions())
  }

  private fun finishWithMissingOptionsError() {
    setResult(RESULT_CANCELED, Intent().apply { putExtra(ERROR_KEY,  "No picking options available") })
    finish()
  }

  private fun finishCancelled() {
    setResult(RESULT_CANCELED, Intent().apply {  })
    finish()
  }

  private fun finishWithUri(uri: Uri) {
    setResult(RESULT_OK, Intent().apply { data = uri })
    finish()
  }
}
