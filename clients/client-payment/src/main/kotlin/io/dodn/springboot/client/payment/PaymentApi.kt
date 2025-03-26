package io.dodn.springboot.client.payment

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(value = "payment-api", url = "\${payment.api.url}")
internal interface PaymentApi {
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/api/v1/payments"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun processPayment(@RequestBody request: PaymentRequestDto): PaymentResponseDto

    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/api/v1/payments/{paymentId}/cancel"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun cancelPayment(@PathVariable paymentId: String): PaymentResponseDto
}
