package io.ol.provider.brm.utils

import com.portal.pcm.Field
import org.openlegacy.core.utils.StringUtil

object FListUtils {

  fun getFlistField(originalFieldName: String): Field? {
    val flistFieldClassName = convertFieldName(originalFieldName)
    return Field.fromName(flistFieldClassName)
  }

  fun convertFieldName(originalFieldName: String): String {
    return StringUtil.toCamelCaseClassName(originalFieldName.substringAfter("PIN_"))
  }
}
