package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.request.SummaryResponseDto
import io.dodn.springboot.core.api.controller.v1.response.EmotionTagsResponseDto
import io.dodn.springboot.core.domain.worry.AiFeedback
import io.dodn.springboot.core.domain.worry.dto.AiFeedbackResponseDto
import io.dodn.springboot.core.domain.worry.dto.CreateAiFeedbackRequestDto
import io.dodn.springboot.core.domain.worry.dto.CreateConvoWorryRequestDto
import io.dodn.springboot.core.domain.worry.dto.CreateLetterWorryRequestDto
import io.dodn.springboot.core.domain.worry.dto.WorryResponseDto
import io.dodn.springboot.core.domain.worry.WorryService
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for worry-related endpoints
 */
@RestController
@RequestMapping("/api/worries")
class WorryController(
    private val worryService: WorryService,
) {
    @PostMapping("/letter")
    fun createLetterWorry(@RequestBody request: CreateLetterWorryRequestDto): ApiResponse<Map<String, Long>> {
        val worry = worryService.createLetterWorry(request.toWorry())
        return ApiResponse.success(mapOf("worryId" to worry.id))
    }

    @PostMapping("/convo")
    fun createConvoWorry(@RequestBody request: CreateConvoWorryRequestDto): ApiResponse<Map<String, Long>> {
        val worry = worryService.createConvoWorry(request.toWorry())
        return ApiResponse.success(mapOf("worryId" to worry.id))
    }

    @GetMapping("/{worryId}")
    fun getWorry(@PathVariable worryId: Long): ApiResponse<WorryResponseDto> {
        val worry = worryService.getWorry(worryId)
        return ApiResponse.success(WorryResponseDto.from(worry))
    }

    @PostMapping("/{worryId}/ai-feedback")
    fun createAiFeedback(
        @PathVariable worryId: Long,
        @RequestBody request: CreateAiFeedbackRequestDto?,
    ): ApiResponse<AiFeedbackResponseDto> {
        val aiFeedback = if (request != null) {
            // Manual feedback provided
            worryService.createAiFeedback(
                worryId,
                AiFeedback(
                    feedback = request.feedback,
                    tone = request.tone,
                    tags = request.tags ?: emptyList(),
                ),
            )
        } else {
            // Auto-generate feedback using AI
            worryService.requestAiFeedback(worryId)
        }

        return ApiResponse.success(AiFeedbackResponseDto.from(aiFeedback))
    }

    @GetMapping("/{worryId}/summary")
    fun getWorrySummary(@PathVariable worryId: Long): ApiResponse<SummaryResponseDto> {
        val summary = worryService.generateSummary(worryId)
        return ApiResponse.success(SummaryResponseDto(summary))
    }

    @GetMapping("/{worryId}/emotion-tags")
    fun getWorryEmotionTags(@PathVariable worryId: Long): ApiResponse<EmotionTagsResponseDto> {
        val tags = worryService.extractEmotionTags(worryId)
        return ApiResponse.success(EmotionTagsResponseDto(tags))
    }

    @PostMapping("/{worryId}/save")
    fun saveWorry(@PathVariable worryId: Long): ApiResponse<Map<String, String>> {
        // Just verify the worry exists
        worryService.getWorry(worryId)
        return ApiResponse.success(mapOf("status" to "saved"))
    }
}

