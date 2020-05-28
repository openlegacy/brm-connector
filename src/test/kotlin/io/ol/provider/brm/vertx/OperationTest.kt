package io.ol.provider.brm.vertx

import io.ol.core.rpc.RpcRequest
import io.ol.provider.brm.entity.EntityPlaceholder
import io.ol.provider.brm.operation.PlaceholderOperation
import io.vertx.junit5.Timeout
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KLogging
import openlegacy.test.utils.ConnectivityUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
class OperationTest : AbstractTest() {

  companion object : KLogging() {
    @BeforeAll
    @JvmStatic
    fun init() {
      ConnectivityUtils.checkTcpConnection("localhost", 11960, 5000)
    }
  }

  @Test
  @Timeout(value = 15, timeUnit = TimeUnit.SECONDS)
  fun `placeholder`(testContext: VertxTestContext) {
    CoroutineScope(vertx.dispatcher()).launch {
      // GIVEN
      var inputEntity = EntityPlaceholder()
      val operation = PlaceholderOperation(inputEntity)
      val rpcRequest = RpcRequest(
        operation = operation
      )
      // WHEN
      val response = rpcConnection.invoke(request = rpcRequest)
      val responseEntity = response.body!! as EntityPlaceholder
      // THEN
      assertThat(response.statusCode).isEqualTo(200)

      testContext.completeNow()
    }.invokeOnCompletion { it?.run { testContext.failNow(it) } }
  }
}
