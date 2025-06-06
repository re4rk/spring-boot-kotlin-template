package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.request.ExampleRequest
import io.dodn.springboot.core.api.controller.v1.response.ExampleResponse
import io.dodn.springboot.core.domain.ExampleData
import io.dodn.springboot.core.domain.ExampleService
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ExampleController(
    val exampleExampleService: ExampleService,
) {
    @GetMapping("/get/{exampleValue}")
    fun exampleGet(
        @PathVariable exampleValue: String,
        @RequestParam exampleParam: String,
    ): ApiResponse<ExampleResponse> {
        val result = exampleExampleService.processExample(ExampleData(exampleValue, exampleParam))
        return ApiResponse.success(ExampleResponse(result.data))
    }

    @PostMapping("/post")
    fun examplePost(
        @RequestBody request: ExampleRequest,
    ): ApiResponse<ExampleResponse> {
        val result = exampleExampleService.processExample(request.toExampleData())
        return ApiResponse.success(ExampleResponse(result.data))
    }
}
