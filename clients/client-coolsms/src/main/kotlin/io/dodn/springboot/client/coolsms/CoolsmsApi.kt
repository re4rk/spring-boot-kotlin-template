package io.dodn.springboot.client.coolsms

import io.dodn.springboot.client.coolsms.dto.SendSmsRequestDto
import io.dodn.springboot.client.coolsms.dto.SendSmsResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(value = "coolsms-api", url = "\${coolsms.api.url}")
internal interface CoolsmsApi {
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/messages/v4/send"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun sendSms(@RequestBody request: SendSmsRequestDto): SendSmsResponseDto
}
