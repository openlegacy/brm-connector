package io.ol.provider.brm.config

import io.ol.core.properties.TimeoutProperties
import io.ol.provider.brm.properties.OLBrmProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BrmProviderConfiguration {

  @Bean
  @ConditionalOnMissingBean(OLBrmProperties::class)
  fun brmProperties(
    timeoutProperties: TimeoutProperties
  ): OLBrmProperties {
    return OLBrmProperties(timeoutProperties)
  }
}
