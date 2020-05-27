package io.ol.provider.brm

/**
 * These constants are used in the RPC entities as keys for action properties, e.g.
 * `@ActionProperty(name = BrmConstants.CONSTANT_NAME, value = "value")`
 */
object BrmConstants {

  private val BRM_CONSTANTS: Collection<String> = listOf()

  fun isConstant(value: String): Boolean {
    return BRM_CONSTANTS.contains(value)
  }
}
