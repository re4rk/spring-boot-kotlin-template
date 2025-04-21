package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.response.EmpathyResponse
import io.dodn.springboot.core.api.controller.v1.response.FeedResponse
import io.dodn.springboot.core.api.controller.v1.response.FeedSummaryResponse
import io.dodn.springboot.core.domain.feed.FeedService
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/feed")
class FeedController(
    private val feedService: FeedService,
) {
    @GetMapping
    fun getFeeds(
        @RequestParam(required = false) emotion: String?,
        @RequestParam(required = false) tag: String?,
    ): ApiResponse<List<FeedSummaryResponse>> {
        val feeds = feedService.getFeeds(emotion, tag)
        return ApiResponse.success(feeds.map { FeedSummaryResponse.from(it) })
    }

    @GetMapping("/{feedId}")
    fun getFeed(@PathVariable feedId: Long): ApiResponse<FeedResponse> {
        val feed = feedService.getFeed(feedId)
        return ApiResponse.success(FeedResponse.from(feed))
    }

    @PostMapping("/{feedId}/empathy")
    fun addEmpathy(
        @PathVariable feedId: Long,
        @RequestParam(required = false) userId: Long?,
    ): ApiResponse<EmpathyResponse> {
        val count = feedService.addEmpathy(feedId, userId)
        return ApiResponse.success(
            EmpathyResponse(
                status = "liked",
                count = count,
            ),
        )
    }
}
