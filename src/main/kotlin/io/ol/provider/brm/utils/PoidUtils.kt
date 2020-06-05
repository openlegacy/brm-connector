package io.ol.provider.brm.utils

import com.portal.pcm.Poid
import io.vertx.core.json.JsonObject

object PoidUtils {
  /** default values are obtained from [com.portal.pcm.Poid.NULL_POID] */
  const val DEFAULT_DB: Long = 0
  const val DEFAULT_ID: Long = 0
  const val DEFAULT_TYPE: String = "/"
  const val DEFAULT_REV: Int = 0

  /** must match to the corresponding field names in the source code of [io.ol.provider.brm.OlPoid] POJO class */
  private const val DB_FIELD_NAME: String = "databaseNumber"
  private const val ID_FIELD_NAME: String = "objectId"
  private const val TYPE_FIELD_NAME: String = "objectType"
  private const val REV_FIELD_NAME: String = "objectRevision"

  fun initPoidFromJson(input: JsonObject?): Poid {
    if (input == null) {
      return Poid.NULL_POID
    }
    return Poid(
      input.getLong(DB_FIELD_NAME) ?: DEFAULT_DB,
      input.getLong(ID_FIELD_NAME) ?: DEFAULT_ID,
      input.getString(TYPE_FIELD_NAME) ?: DEFAULT_TYPE,
      input.getInteger(REV_FIELD_NAME) ?: DEFAULT_REV
    )
  }

  fun getPoidProperty(input: Poid?, key: String): Any? {
    if (input == null) {
      return null
    }
    return when (key) {
      DB_FIELD_NAME -> input.db
      ID_FIELD_NAME -> input.id
      TYPE_FIELD_NAME -> input.type
      REV_FIELD_NAME -> input.rev
      else -> null
    }
  }
}
