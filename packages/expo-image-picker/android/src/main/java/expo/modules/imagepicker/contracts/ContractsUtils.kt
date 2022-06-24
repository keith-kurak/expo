package expo.modules.imagepicker.contracts

import android.net.Uri
import expo.modules.imagepicker.MediaType

/**
 * Data required to be returned upon successful contract completion
 */
internal sealed class ImagePickerContractResult private constructor() {
  class Cancelled : ImagePickerContractResult()
  class Success(val data: Pair<MediaType, Uri>) : ImagePickerContractResult()
}

const val OPTIONS_KEY = "options"
const val ERROR_KEY = "error"
