package io.ol.provider.brm.customfields

import com.portal.pcm.FList
import com.portal.pcm.Field
import customfields.CFldOlCustomField
import io.ol.provider.brm.mock.BrmTestApplication
import io.ol.provider.brm.utils.FListUtils
import mu.KLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * How to add custom fields to the JAVA application - https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_cust_storable_classes.htm#BRMDR445
 *
 * Checks that custom fields specified in Infranet.properties and in a corresponding Java package are being loaded and correctly processed by BRM SDK.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [BrmTestApplication::class])
class CustomFieldsTest {

  companion object : KLogging()

  /**
   * Checks that custom fields specified in Infranet.properties and in a corresponding Java package are being loaded and correctly processed by BRM SDK.
   */
  @Test
  fun olCustomFieldLoadingTest() {
    // checks that original name of the field could ve correctly converted to a corresponding class name
    val customFieldClassConvertedName = FListUtils.convertFieldName("C_FLD_OL_CUSTOM_FIELD")
    val cutomFieldClassName = "CFldOlCustomField"
    Assertions.assertEquals(cutomFieldClassName, customFieldClassConvertedName)

    // loads custom field from the class name
    val fieldInstance = Field.fromName(cutomFieldClassName)
    Assertions.assertNotNull(fieldInstance)

    // ensures that it is the same instance of the custom class (BRM JAVA SDK re-uses field instances)
    Assertions.assertEquals(CFldOlCustomField.getInst(), fieldInstance)

    // checks that field instance could be safely added to the FList
    val flist = FList()
    flist.set(fieldInstance, "value")
  }
}
