package expo.modules.imagepicker.contracts

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageActivity
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.options
import expo.modules.imagepicker.ImagePickerOptions
import expo.modules.imagepicker.MediaType
import expo.modules.imagepicker.copyExifData
import expo.modules.imagepicker.createOutputFile
import expo.modules.imagepicker.toBitmapCompressFormat
import expo.modules.imagepicker.toImageFileExtension
import kotlinx.coroutines.runBlocking

internal class CropImageContract(
  private val contentResolver: ContentResolver,
  private val options: ImagePickerOptions,
  /**
   * for [IMAGE_LIBRARY] we need to create a new file as up to this point we've been operating on the original media asset
   * for [CAMERA] we do not have to do it as it's already been created at the beginning of the picking process
   */
  private val needCreateNewFile: Boolean = false
) : ActivityResultContract<Uri, ImagePickerContractResult>() {
  override fun createIntent(context: Context, input: Uri) = Intent(context, CropImageActivity::class.java).apply {
    val mediaType = expo.modules.imagepicker.getType(context.contentResolver, input)
    val compressFormat = mediaType.toBitmapCompressFormat()

    val outputUri: Uri = if (needCreateNewFile) {
      createOutputFile(context.cacheDir, compressFormat.toImageFileExtension()).toUri()
    } else {
      input
    }

    val options = options(input) {
      setOutputCompressFormat(compressFormat)
      setOutputCompressQuality((this@CropImageContract.options.quality * 100).toInt())
      setOutputUri(outputUri)

      this@CropImageContract.options.aspect?.let { (x, y) ->
        setAspectRatio(x, y)
        setFixAspectRatio(true)
        setInitialCropWindowPaddingRatio(0f)
      }

      cropImageOptions.validate()
    }

    putExtra(
      CropImage.CROP_IMAGE_EXTRA_BUNDLE,
      bundleOf(
        CropImage.CROP_IMAGE_EXTRA_SOURCE to options.uri,
        CropImage.CROP_IMAGE_EXTRA_OPTIONS to options.cropImageOptions
      )
    )
  }

  override fun parseResult(resultCode: Int, intent: Intent?): ImagePickerContractResult {
    val result = intent?.getParcelableExtra<CropImage.ActivityResult?>(CropImage.CROP_IMAGE_EXTRA_RESULT)
    if (resultCode == Activity.RESULT_CANCELED || result == null) {
      return ImagePickerContractResult.Cancelled()
    }
    val targetUri = requireNotNull(result.uriContent)
    val sourceUri = requireNotNull(result.originalUri)
    runBlocking { copyExifData(sourceUri, targetUri.toFile(), contentResolver) }
    return ImagePickerContractResult.Success(MediaType.IMAGE to targetUri)
  }
}
