package expo.modules.imagepicker.contracts

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import expo.modules.imagepicker.ImagePickerOptions
import expo.modules.imagepicker.activities.ImageLibraryActivity
import expo.modules.imagepicker.toMediaType

/**
 * An [androidx.activity.result.contract.ActivityResultContract] to start [ImageLibraryActivity]
 */
internal class ImageLibraryActivityContract(
  private val contentResolver: ContentResolver,
) : ActivityResultContract<ImagePickerOptions, ImagePickerContractResult>() {
  override fun createIntent(context: Context, input: ImagePickerOptions) =
    Intent(context, ImageLibraryActivity::class.java)
      .putExtra(OPTIONS_KEY, input)

  override fun parseResult(resultCode: Int, intent: Intent?) =
    if (resultCode == Activity.RESULT_CANCELED) {
      ImagePickerContractResult.Cancelled()
    } else {
      val uri = requireNotNull(requireNotNull(intent).data)
      val type = uri.toMediaType(contentResolver)

      ImagePickerContractResult.Success(type to uri)
    }
}
