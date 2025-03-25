package io.dodn.springboot.client.coolsms.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
internal data class SendSmsRequestDto(
    @field:NotBlank
    val apiKey: String,
    
    @field:NotBlank
    val apiSecret: String,
    
    @field:NotBlank
    val to: String,
    
    @field:NotBlank
    val from: String,
    
    @field:NotBlank
    val text: String,
    
    @field:NotBlank
    val type: String, // "SMS", "LMS", "MMS"
    
    val subject: String? = null,
    val imageId: String? = null,
)