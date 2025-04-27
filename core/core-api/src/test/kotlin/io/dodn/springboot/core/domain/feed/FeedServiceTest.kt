package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.UnitTest
import io.dodn.springboot.core.domain.feed.empathy.EmpathyCounter
import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryStorage
import io.dodn.springboot.core.support.error.CoreException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime
import java.util.Collections

class FeedServiceTest : UnitTest() {
    private lateinit var feedStorage: FeedStorage
    private lateinit var worryStorage: WorryStorage
    private lateinit var empathyCounter: EmpathyCounter
    private lateinit var feedService: FeedService

    @BeforeEach
    fun setUp() {
        feedStorage = mockk(relaxed = true)
        worryStorage = mockk(relaxed = true)
        empathyCounter = mockk(relaxed = true)
        feedService = FeedService(feedStorage, worryStorage, empathyCounter)
    }

    @Test
    fun `should create feed by worry when user is owner`() {
        // given
        val userId = 1L
        val userDetails = createUserDetails(userId)
        val worryId = 10L
        val feedbackId = 20L

        val mockWorry = Worry(
            id = worryId,
            userId = userId,
            mode = WorryMode.LETTER,
            emotion = "Happy",
            category = "Work",
            content = "Test content",
            lastMessageOrder = 0,
        )

        val mockFeed = createMockFeed(1L, userId, mockWorry)

        every { worryStorage.getWorry(worryId) } returns mockWorry
        every { feedStorage.shareWorry(worryId, feedbackId) } returns mockFeed

        // when
        val result = feedService.createFeedByWorry(userDetails, worryId, feedbackId)

        // then
        assertThat(result).isEqualTo(mockFeed)
        verify { feedStorage.shareWorry(worryId, feedbackId) }
    }

    @Test
    fun `should throw exception when creating feed and user is not owner`() {
        // given
        val userId = 1L
        val ownerId = 2L // Different from userId
        val userDetails = createUserDetails(userId)
        val worryId = 10L
        val feedbackId = 20L

        val mockWorry = Worry(
            id = worryId,
            userId = ownerId, // Different from userId
            mode = WorryMode.LETTER,
            emotion = "Happy",
            category = "Work",
            content = "Test content",
            lastMessageOrder = 0,
        )

        every { worryStorage.getWorry(worryId) } returns mockWorry

        // when & then
        assertThrows<CoreException> {
            feedService.createFeedByWorry(userDetails, worryId, feedbackId)
        }
    }

    @Test
    fun `should delete feed when user is owner`() {
        // given
        val userId = 1L
        val userDetails = createUserDetails(userId)
        val feedId = 10L

        val mockFeed = createMockFeed(
            feedId,
            userId,
            createMockWorry(100L, userId),
        )

        every { feedStorage.getFeed(feedId) } returns mockFeed

        // when
        feedService.deleteFeed(userDetails, feedId)

        // then
        verify { feedStorage.deleteFeed(feedId) }
    }

    @Test
    fun `should delete feed when user is admin`() {
        // given
        val userId = 1L
        val ownerId = 2L // Different from userId
        val userDetails = createAdminUserDetails(userId)
        val feedId = 10L

        val mockFeed = createMockFeed(
            feedId,
            ownerId, // Different from userId
            createMockWorry(100L, ownerId),
        )

        every { feedStorage.getFeed(feedId) } returns mockFeed

        // when
        feedService.deleteFeed(userDetails, feedId)

        // then
        verify { feedStorage.deleteFeed(feedId) }
    }

    @Test
    fun `should throw exception when deleting feed and user is not owner or admin`() {
        // given
        val userId = 1L
        val ownerId = 2L // Different from userId
        val userDetails = createUserDetails(userId)
        val feedId = 10L

        val mockFeed = createMockFeed(
            feedId,
            ownerId, // Different from userId
            createMockWorry(100L, ownerId),
        )

        every { feedStorage.getFeed(feedId) } returns mockFeed

        // when & then
        assertThrows<CoreException> {
            feedService.deleteFeed(userDetails, feedId)
        }
    }

    @Test
    fun `should get feeds without filtering`() {
        // given
        val mockFeed1 = createMockFeed(
            1L,
            10L,
            createMockWorry(100L, 10L, "Happy"),
        )
        val mockFeed2 = createMockFeed(
            2L,
            11L,
            createMockWorry(101L, 11L, "Sad"),
        )

        every { feedStorage.getAllFeeds() } returns listOf(mockFeed1, mockFeed2)

        // when
        val results = feedService.getFeeds()

        // then
        assertThat(results).hasSize(2)
        assertThat(results).containsExactly(mockFeed1, mockFeed2)
    }

    @Test
    fun `should filter feeds by emotion`() {
        // given
        val mockFeed1 = createMockFeed(
            1L,
            10L,
            createMockWorry(100L, 10L, "Happy"),
        )
        val mockFeed2 = createMockFeed(
            2L,
            11L,
            createMockWorry(101L, 11L, "Sad"),
        )

        every { feedStorage.getAllFeeds() } returns listOf(mockFeed1, mockFeed2)

        // when
        val results = feedService.getFeeds(emotion = "Happy")

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo(1L)
    }

    @Test
    fun `should filter feeds by tag`() {
        // given
        val mockFeed1 = createMockFeed(
            1L,
            10L,
            createMockWorry(100L, 10L),
        )
        val mockFeed2 = createMockFeed(
            2L,
            11L,
            createMockWorry(101L, 11L),
        )

        every { feedStorage.getAllFeeds() } returns listOf(mockFeed1, mockFeed2)

        // when
        val results = feedService.getFeeds(tag = "tag1")

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo(1L)
    }

    @Test
    fun `should get feed by id`() {
        // given
        val feedId = 1L
        val mockFeed = createMockFeed(
            feedId,
            10L,
            createMockWorry(100L, 10L),
        )

        every { feedStorage.getFeed(feedId) } returns mockFeed

        // when
        val result = feedService.getFeed(feedId)

        // then
        assertThat(result).isEqualTo(mockFeed)
    }

    @Test
    fun `should get feed by owner id when user is owner`() {
        // given
        val userId = 1L
        val userDetails = createUserDetails(userId)
        val mockFeed1 = createMockFeed(
            1L,
            userId,
            createMockWorry(100L, userId),
        )
        val mockFeed2 = createMockFeed(
            2L,
            userId,
            createMockWorry(101L, userId),
        )

        every { feedStorage.getFeedByOwnerId(userId) } returns listOf(mockFeed1, mockFeed2)

        // when
        val results = feedService.getFeedByOwnerId(userDetails, userId)

        // then
        assertThat(results).hasSize(2)
        assertThat(results).containsExactly(mockFeed1, mockFeed2)
    }

    @Test
    fun `should get feed by owner id when user is admin`() {
        // given
        val userId = 1L
        val ownerId = 2L // Different from userId
        val userDetails = createAdminUserDetails(userId)
        val mockFeed1 = createMockFeed(
            1L,
            ownerId,
            createMockWorry(100L, ownerId),
        )
        val mockFeed2 = createMockFeed(
            2L,
            ownerId,
            createMockWorry(101L, ownerId),
        )

        every { feedStorage.getFeedByOwnerId(ownerId) } returns listOf(mockFeed1, mockFeed2)

        // when
        val results = feedService.getFeedByOwnerId(userDetails, ownerId)

        // then
        assertThat(results).hasSize(2)
        assertThat(results).containsExactly(mockFeed1, mockFeed2)
    }

    @Test
    fun `should throw exception when getting feed by owner id and user is not owner or admin`() {
        // given
        val userId = 1L
        val ownerId = 2L // Different from userId
        val userDetails = createUserDetails(userId)

        // when & then
        assertThrows<CoreException> {
            feedService.getFeedByOwnerId(userDetails, ownerId)
        }
    }

    @Test
    fun `should add empathy`() {
        // given
        val feedId = 1L
        val userId = 10L
        val expectedCount = 5L
        val mockFeed = createMockFeed(
            feedId,
            20L,
            createMockWorry(100L, 20L),
        )

        every { feedStorage.getFeed(feedId) } returns mockFeed
        every { empathyCounter.addEmpathy(feedId, userId) } returns expectedCount

        // when
        val result = feedService.addEmpathy(feedId, userId)

        // then
        assertThat(result).isEqualTo(expectedCount)
    }

    @Test
    fun `should remove empathy`() {
        // given
        val feedId = 1L
        val userId = 10L
        val expectedCount = 4L

        every { empathyCounter.removeEmpathy(feedId, userId) } returns expectedCount

        // when
        val result = feedService.removeEmpathy(feedId, userId)

        // then
        assertThat(result).isEqualTo(expectedCount)
    }

    // Helper methods
    private fun createUserDetails(userId: Long): UserDetails {
        return User.builder()
            .username(userId.toString())
            .password("password")
            .authorities(Collections.emptyList())
            .build()
    }

    private fun createAdminUserDetails(userId: Long): UserDetails {
        val authorities = listOf<GrantedAuthority>(SimpleGrantedAuthority("ROLE_ADMIN"))
        return User.builder()
            .username(userId.toString())
            .password("password")
            .authorities(authorities)
            .build()
    }

    private fun createMockFeed(
        id: Long,
        ownerId: Long,
        worry: Worry,
        empathyCount: Long = 0L,
    ): Feed {
        return Feed(
            id = id,
            ownerId = ownerId,
            worry = worry,
            content = worry.content,
            empathyCount = empathyCount,
            sharedAt = LocalDateTime.now(),
        )
    }

    private fun createMockWorry(id: Long, userId: Long, emotion: String = "Happy"): Worry {
        return Worry(
            id = id,
            userId = userId,
            mode = WorryMode.LETTER,
            emotion = emotion,
            category = "Work",
            content = "Test content",
            lastMessageOrder = 0,
        )
    }
}
