package io.dodn.springboot.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class ClientOpenAiTestApplication

fun main(args: Array<String>) {
    runApplication<ClientOpenAiTestApplication>(*args)
}
