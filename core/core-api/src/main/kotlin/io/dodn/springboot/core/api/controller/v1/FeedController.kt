package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.response.EmpathyResponse
import io.dodn.springboot.core.api.controller.v1.response.FeedResponse
import io.dodn.springboot.core.api.controller.v1.response.FeedSummaryResponse
import io.dodn.springboot.core.domain.feed.FeedService
import io.dodn.springboot.core.support.auth.GominUserDetails
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
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

    @GetMapping("/owner/{ownerId}")
    fun getFeedByOwnerId(
        @PathVariable ownerId: Long,
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ApiResponse<List<FeedSummaryResponse>> {
        val feeds = feedService.getFeedByOwnerId(userDetails, ownerId)
        return ApiResponse.success(feeds.map { FeedSummaryResponse.from(it) })
    }

    @PostMapping("/worry/{worryId}/feedback/{feedbackId}")
    fun shareWorry(
        @PathVariable worryId: Long,
        @PathVariable feedbackId: Long,
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ApiResponse<FeedResponse> {
        val feed = feedService.createFeedByWorry(userDetails, worryId, feedbackId)
        return ApiResponse.success(FeedResponse.from(feed))
    }

    @DeleteMapping("/{feedId}")
    fun deleteFeed(
        @PathVariable feedId: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ApiResponse<Any> {
        feedService.deleteFeed(userDetails, feedId)
        return ApiResponse.success()
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
