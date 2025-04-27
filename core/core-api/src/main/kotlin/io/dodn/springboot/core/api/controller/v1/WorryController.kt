package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.aspect.CheckWorryAccess
import io.dodn.springboot.core.api.controller.v1.request.AddWorryMessageRequest
import io.dodn.springboot.core.api.controller.v1.request.CreateConvoWorryRequest
import io.dodn.springboot.core.api.controller.v1.request.CreateFeedbackResponse
import io.dodn.springboot.core.api.controller.v1.request.CreateLetterWorryRequest
import io.dodn.springboot.core.api.controller.v1.request.SummaryResponse
import io.dodn.springboot.core.api.controller.v1.response.WorryResponse
import io.dodn.springboot.core.domain.worry.MessageRole
import io.dodn.springboot.core.domain.worry.WorryService
import io.dodn.springboot.core.support.auth.GominUserDetails
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * Controller for worry-related endpoints
 */
@RestController
@RequestMapping("/api/v1/worries")
class WorryController(
    private val worryService: WorryService,
) {
    @PostMapping("/letter")
    fun createLetterWorry(
        @RequestBody request: CreateLetterWorryRequest,
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ApiResponse<Map<String, Long>> {
        val worry = worryService.createWorry(request.toWorry(userDetails.id))
        return ApiResponse.success(mapOf("worryId" to worry.id))
    }

    @PostMapping("/convo")
    fun createConvoWorry(
        @RequestBody request: CreateConvoWorryRequest,
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ApiResponse<Map<String, Long>> {
        val worry = worryService.createWorry(request.toWorry(userDetails.id))
        return ApiResponse.success(mapOf("worryId" to worry.id))
    }

    @GetMapping("/{worryId}")
    @CheckWorryAccess(permission = "VIEW")
    fun getWorry(
        @PathVariable worryId: Long,
    ): ApiResponse<WorryResponse> {
        val worry = worryService.getWorry(worryId)
        return ApiResponse.success(WorryResponse.from(worry))
    }

    @PostMapping("/{worryId}/message")
    @CheckWorryAccess(permission = "EDIT")
    fun addWorryMessage(
        @PathVariable worryId: Long,
        @RequestBody request: AddWorryMessageRequest,
    ): ApiResponse<WorryResponse> {
        worryService.addWorryMessage(worryId = worryId, role = MessageRole.USER, content = request.message)
        return ApiResponse.success(WorryResponse.from(worryService.getWorry(worryId)))
    }

    @PostMapping("/{worryId}/feedback")
    @CheckWorryAccess(permission = "EDIT")
    fun requestFeedback(
        @PathVariable worryId: Long,
    ): ApiResponse<CreateFeedbackResponse> {
        val feedback = worryService.requestFeedback(worryId)

        return ApiResponse.success(CreateFeedbackResponse.from(feedback))
    }

    @PostMapping("/{worryId}/streaming-feedback")
    @CheckWorryAccess(permission = "EDIT")
    fun requestStreamingFeedback(@PathVariable worryId: Long): SseEmitter {
        // SseEmitter 인스턴스 생성 (타임아웃 5분)
        val emitter = SseEmitter(300000L)

        // 비동기적으로 처리하여 즉시 응답
        worryService.requestStreamingFeedback(
            worryId,
            onChunk = { partialResponse ->
                try {
                    emitter.send(SseEmitter.event().name("chunk").data(partialResponse))
                } catch (e: Exception) {
                    println("Error sending chunk: ${e.message}")
                }
            },
            // 완료 콜백
            onComplete = { fullResponse ->
                try {
                    emitter.send(SseEmitter.event().name("processing").data("Analyzing emotions and tone..."))

                    val worryMessage = worryService.addWorryMessage(worryId, MessageRole.AI, fullResponse)

                    emitter.send(SseEmitter.event().name("complete").data(worryMessage))

                    emitter.complete()
                } catch (e: Exception) {
                    emitter.send(SseEmitter.event().name("error").data("Error processing feedback: ${e.message}"))
                    emitter.completeWithError(e)
                }
            },
        )

        return emitter
    }

    @GetMapping("/{worryId}/summary")
    @CheckWorryAccess(permission = "VIEW")
    fun getWorrySummary(@PathVariable worryId: Long): ApiResponse<SummaryResponse> {
        val summary = worryService.generateSummary(worryId)
        return ApiResponse.success(SummaryResponse(summary))
    }
}
