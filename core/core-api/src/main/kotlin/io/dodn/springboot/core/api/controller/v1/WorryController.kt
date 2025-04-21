package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.request.SummaryResponseDto
import io.dodn.springboot.core.api.controller.v1.response.EmotionTagsResponseDto
import io.dodn.springboot.core.domain.worry.Feedback
import io.dodn.springboot.core.api.controller.v1.response.FeedbackResponseDto
import io.dodn.springboot.core.api.controller.v1.request.CreateFeedbackRequestDto
import io.dodn.springboot.core.api.controller.v1.request.CreateConvoWorryRequestDto
import io.dodn.springboot.core.api.controller.v1.request.CreateLetterWorryRequestDto
import io.dodn.springboot.core.api.controller.v1.response.WorryResponseDto
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
    fun createFeedback(
        @PathVariable worryId: Long,
        @RequestBody request: CreateFeedbackRequestDto?,
    ): ApiResponse<FeedbackResponseDto> {
        val feedback = if (request != null) {
            // Manual feedback provided
            worryService.createFeedback(
                worryId,
                Feedback(
                    content = request.feedback,
                    tone = request.tone,
                    tags = request.tags ?: emptyList(),
                ),
            )
        } else {
            // Auto-generate feedback using AI
            worryService.requestFeedback(worryId)
        }

        return ApiResponse.success(FeedbackResponseDto.from(feedback))
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

