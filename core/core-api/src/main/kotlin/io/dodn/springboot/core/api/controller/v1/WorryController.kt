package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.request.CreateConvoWorryRequest
import io.dodn.springboot.core.api.controller.v1.request.CreateFeedbackRequest
import io.dodn.springboot.core.api.controller.v1.request.CreateLetterWorryRequest
import io.dodn.springboot.core.api.controller.v1.request.SummaryResponse
import io.dodn.springboot.core.api.controller.v1.response.EmotionTagsResponse
import io.dodn.springboot.core.api.controller.v1.response.FeedbackResponse
import io.dodn.springboot.core.api.controller.v1.response.WorryResponse
import io.dodn.springboot.core.domain.worry.Feedback
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
    fun createLetterWorry(@RequestBody request: CreateLetterWorryRequest): ApiResponse<Map<String, Long>> {
        val worry = worryService.createLetterWorry(request.toWorry())
        return ApiResponse.success(mapOf("worryId" to worry.id))
    }

    @PostMapping("/convo")
    fun createConvoWorry(@RequestBody request: CreateConvoWorryRequest): ApiResponse<Map<String, Long>> {
        val worry = worryService.createConvoWorry(request.toWorry())
        return ApiResponse.success(mapOf("worryId" to worry.id))
    }

    @GetMapping("/{worryId}")
    fun getWorry(@PathVariable worryId: Long): ApiResponse<WorryResponse> {
        val worry = worryService.getWorry(worryId)
        return ApiResponse.success(WorryResponse.from(worry))
    }

    @PostMapping("/{worryId}/feedback")
    fun createFeedback(
        @PathVariable worryId: Long,
        @RequestBody request: CreateFeedbackRequest?,
    ): ApiResponse<FeedbackResponse> {
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

        return ApiResponse.success(FeedbackResponse.from(feedback))
    }

    @GetMapping("/{worryId}/summary")
    fun getWorrySummary(@PathVariable worryId: Long): ApiResponse<SummaryResponse> {
        val summary = worryService.generateSummary(worryId)
        return ApiResponse.success(SummaryResponse(summary))
    }

    @GetMapping("/{worryId}/emotion-tags")
    fun getWorryEmotionTags(@PathVariable worryId: Long): ApiResponse<EmotionTagsResponse> {
        val tags = worryService.extractEmotionTags(worryId)
        return ApiResponse.success(EmotionTagsResponse(tags))
    }

    @PostMapping("/{worryId}/save")
    fun saveWorry(@PathVariable worryId: Long): ApiResponse<Map<String, String>> {
        // Just verify the worry exists
        worryService.getWorry(worryId)
        return ApiResponse.success(mapOf("status" to "saved"))
    }
}
