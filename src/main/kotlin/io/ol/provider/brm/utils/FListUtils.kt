package io.ol.provider.brm.utils

import com.portal.pcm.Field
import org.openlegacy.core.utils.StringUtil

/**
 * Class which holds utility methods to work with FList and its fields
 */
object FListUtils {
  /**
   * Returns instance of the corresponding FList field based on the provided original field name from the FList sources.
   *
   * For example: for the original name "PIN_FLD_FIELD_NAME" it will return an instance of a corresponding BRM SDK class FldFieldName.
   */
  fun getFlistField(originalFieldName: String): Field? {
    val flistFieldClassName = convertFieldName(originalFieldName)
    return Field.fromName(flistFieldClassName)
  }

  /**
   * Converts an original field name to a naming format which used in BRM SDK for fields classes.
   *
   * From the documentation - https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#BRMDR772
   *
   * Field names follow the Java class-naming conventions and use mixed case without the underscores.
   * The Java field classes in the Java PCM package use the C #define name without the PIN_ prefix.
   * For example, PIN_FLD_NAME_INFO in C becomes FldNameInfo in Java.
   */
  fun convertFieldName(originalFieldName: String): String {
    return StringUtil.toCamelCaseClassName(originalFieldName.substringAfter("PIN_"))
  }
}
