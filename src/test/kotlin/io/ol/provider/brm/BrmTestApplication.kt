package io.ol.provider.brm

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class BrmTestApplication {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(BrmTestApplication::class.java, *args)
    }
  }
}
