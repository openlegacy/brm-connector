package io.ol.provider.brm

import io.ol.provider.brm.utils.PoidUtils
import org.openlegacy.core.annotations.rpc.RpcPart

/**
 * OpenLegacy re-usable POJO class (i.e. RpcPart) which holds all required metadata about BRM Portal Object ID i.e. POID.
 * Allows to be serialized/deserialized to/from JSON format and used in auto-generated Entity and Operation Helper classes (in contrast to the original [com.portal.pcm.Poid] class)
 *
 * The POID data type identifies a storable object in the BRM database.
 * Each storable object has a unique POID in BRM. The POID is being used to locate a storable object in the database.
 *
 * Documentation - https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_data_types.htm#BRMDR205
 */
@RpcPart
class OlPoid @JvmOverloads constructor(
  /**
   * An arbitrary 64-bit number assigned to a particular BRM database by the BRM system administrator.
   * Each database has a unique database number that is stored in each object in that database.
   * This number must be used by all programs, CMs, and DMs accessing that database.
   * Decimal dotted notation is used for the database number: 0.0.0.x, where x is the database number, such as 1057.
   */
  var databaseNumber: Long = PoidUtils.DEFAULT_DB,
  /**
   * The class to which the object belongs, for example, /event and /service. Default value is only /.
   */
  var objectType: String = PoidUtils.DEFAULT_TYPE,
  /**
   * A unique 64-bit number assigned to each object.
   * Once assigned, the ID is never changed or re-used. The ID is a 64-bit number to accommodate the large number of objects that can exist within a single database.
   * The ID is guaranteed to be unique within a given database, not across databases.
   */
  var objectId: Long = PoidUtils.DEFAULT_ID,
  /**
   * Revision number.
   * This value is being incremented automatically by the BRM system each time the object is being updated.
   * The user cannot change this value directly.
   */
  var objectRevision: Int = PoidUtils.DEFAULT_REV
)
