package io.ol.provider.brm

import org.openlegacy.core.model.legacy.type.LegacyType

/**
 * Documentation - https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_data_types.htm#CHDFCAJF
 */
interface BrmLegacyTypes {

  /**
   * Represented in RPC Entity as [io.ol.provider.brm.OlPoid]
   */
  class POID : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_POID"
    }
  }

  /**
   * Represented in RPC Entity as [java.lang.Integer]
   */
  class INT : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_INT"
    }
  }

  /**
   * Represented in RPC Entity as [java.lang.Integer]
   */
  class ENUM : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_ENUM"
    }
  }

  /**
   * Represented in RPC Entity as [java.math.BigDecimal]
   */
  class DECIMAL : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_DECIMAL"
    }
  }

  /**
   * Represented in RPC Entity as [java.lang.String]
   */
  class STR : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_STR"
    }
  }

  /**
   * Represented in RPC Entity as byte[] (byte array)
   */
  class BINSTR : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_BINSTR"
    }
  }

  /**
   * Represented in RPC Entity as [java.util.Date]
   */
  class TSTAMP : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_TSTAMP"
    }
  }

  /**
   * Represented in RPC Entity as [java.util.List]
   */
  class ARRAY : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_ARRAY"
    }
  }

  /**
   * Represented in RPC Entity as Java POJO class (i.e. RpcPart)
   */
  class SUBSTRUCT : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_SUBSTRUCT"
    }
  }

  /**
   * Represented in RPC Entity as byte[] (byte array)
   */
  class BUF : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_BUF"
    }
  }

  /**
   * Currently not used, assuming that in case of an error a PortalContext instance will throw an exception [com.portal.pcm.EBufException]
   */
  class ERRBUF : LegacyType {
    override fun alias(): String {
      return "PIN_FLDT_ERRBUF"
    }
  }
}
