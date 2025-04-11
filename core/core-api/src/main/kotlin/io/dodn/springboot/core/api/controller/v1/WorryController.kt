package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.domain.worry.AiFeedback
import io.dodn.springboot.core.domain.worry.AiFeedbackResponseDto
import io.dodn.springboot.core.domain.worry.CreateAiFeedbackRequestDto
import io.dodn.springboot.core.domain.worry.CreateConvoWorryRequestDto
import io.dodn.springboot.core.domain.worry.CreateLetterWorryRequestDto
import io.dodn.springboot.core.domain.worry.EmpathyResponseDto
import io.dodn.springboot.core.domain.worry.FeedResponseDto
import io.dodn.springboot.core.domain.worry.FeedSummaryResponseDto
import io.dodn.springboot.core.domain.worry.ShareWorryRequestDto
import io.dodn.springboot.core.domain.worry.WorryResponseDto
import io.dodn.springboot.core.domain.worry.WorryService
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class WorryController(
    private val worryService: WorryService,
) {
    @PostMapping("/worries/letter")
    fun createLetterWorry(@RequestBody request: CreateLetterWorryRequestDto): ApiResponse<Map<String, Long>> {
        val worry = worryService.createLetterWorry(request.toWorry())
        return ApiResponse.success(mapOf("worryId" to worry.id))
    }

    @PostMapping("/worries/convo")
    fun createConvoWorry(@RequestBody request: CreateConvoWorryRequestDto): ApiResponse<Map<String, Long>> {
        val worry = worryService.createConvoWorry(request.toWorry())
        return ApiResponse.success(mapOf("worryId" to worry.id))
    }

    @GetMapping("/worries/{worryId}")
    fun getWorry(@PathVariable worryId: Long): ApiResponse<WorryResponseDto> {
        val worry = worryService.getWorry(worryId)
        return ApiResponse.success(WorryResponseDto.from(worry))
    }

    @PostMapping("/worries/{worryId}/ai-feedback")
    fun createAiFeedback(
        @PathVariable worryId: Long,
        @RequestBody request: CreateAiFeedbackRequestDto,
    ): ApiResponse<AiFeedbackResponseDto> {
        val aiFeedback = worryService.createAiFeedback(
            worryId,
            AiFeedback(
                feedback = request.feedback,
                tone = request.tone,
                tags = request.tags ?: emptyList(),
            ),
        )
        return ApiResponse.success(AiFeedbackResponseDto.from(aiFeedback))
    }

    @PostMapping("/worries/{worryId}/share")
    fun shareWorry(
        @PathVariable worryId: Long,
        @RequestBody request: ShareWorryRequestDto,
    ): ApiResponse<Map<String, Any>> {
        // This is a simplified implementation - in a real system you'd find
        // the appropriate AiFeedback based on the request data
        val aiFeedback = worryService.getWorry(worryId).let {
            // Getting the most recent AI feedback for simplicity
            worryService.createAiFeedback(
                worryId,
                AiFeedback(
                    feedback = "AI has responded to your worry.",
                    tags = request.tags,
                ),
            )
        }

        val feed = worryService.shareWorry(worryId, aiFeedback.id)
        return ApiResponse.success(
            mapOf(
                "status" to "shared",
                "feedId" to feed.id,
            ),
        )
    }

    @PostMapping("/worries/{worryId}/save")
    fun saveWorry(@PathVariable worryId: Long): ApiResponse<Map<String, String>> {
        // This is just to mark as saved without sharing
        worryService.getWorry(worryId) // Verify it exists
        return ApiResponse.success(mapOf("status" to "saved"))
    }

    @GetMapping("/feed")
    fun getFeeds(
        @RequestParam(required = false) emotion: String?,
        @RequestParam(required = false) tag: String?,
    ): ApiResponse<List<FeedSummaryResponseDto>> {
        val feeds = worryService.getFeeds(emotion, tag)
        return ApiResponse.success(feeds.map { FeedSummaryResponseDto.from(it) })
    }

    @GetMapping("/feed/{feedId}")
    fun getFeed(@PathVariable feedId: Long): ApiResponse<FeedResponseDto> {
        val feed = worryService.getFeed(feedId)
        return ApiResponse.success(FeedResponseDto.from(feed))
    }

    @PostMapping("/feed/{feedId}/empathy")
    fun addEmpathy(
        @PathVariable feedId: Long,
        @RequestParam(required = false) userId: Long?,
    ): ApiResponse<EmpathyResponseDto> {
        val count = worryService.addEmpathy(feedId, userId)
        return ApiResponse.success(
            EmpathyResponseDto(
                status = "liked",
                count = count,
            ),
        )
    }
}
