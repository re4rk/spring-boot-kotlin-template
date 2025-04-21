package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.ExampleData

data class ExampleRequest(
    val data: String,
) {
    fun toExampleData(): ExampleData {
        return ExampleData(data, data)
    }
}
