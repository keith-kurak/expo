package expo.modules.imagepicker.contracts

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import expo.modules.imagepicker.ImagePickerOptions
import expo.modules.imagepicker.activities.CameraActivity
import expo.modules.imagepicker.toMediaType

/**
 * A [ActivityResultContract] to start [CameraActivity]
 */
internal class CameraActivityContract(
  private val contentResolver: ContentResolver,
) : ActivityResultContract<ImagePickerOptions, ImagePickerContractResult>() {
  override fun createIntent(context: Context, input: ImagePickerOptions): Intent =
    Intent(context, CameraActivity::class.java)
      .putExtra(OPTIONS_KEY, input)

  override fun parseResult(resultCode: Int, intent: Intent?): ImagePickerContractResult =
    if (resultCode == Activity.RESULT_CANCELED) {
      ImagePickerContractResult.Cancelled()
    } else {
      val uri = intent?.data ?: throw IllegalStateException("No data available in Intent")
      val type = uri.toMediaType(contentResolver)

      ImagePickerContractResult.Success(type to uri)
    }
}
