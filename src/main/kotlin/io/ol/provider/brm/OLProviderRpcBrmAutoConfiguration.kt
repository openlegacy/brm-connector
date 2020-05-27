package io.ol.provider.brm

import io.ol.provider.brm.config.BrmProviderConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [BrmProviderConfiguration::class])
class OLProviderRpcBrmAutoConfiguration
