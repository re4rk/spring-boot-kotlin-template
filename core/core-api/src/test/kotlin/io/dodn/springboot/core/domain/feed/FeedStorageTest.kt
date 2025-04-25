package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.UnitTest
import io.dodn.springboot.core.domain.feed.empathy.EmpathyCounter
import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryStorage
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.storage.db.core.BaseEntity
import io.dodn.springboot.storage.db.core.feed.FeedEntity
import io.dodn.springboot.storage.db.core.feed.FeedRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.Optional

class FeedStorageTest : UnitTest() {
    private lateinit var feedRepository: FeedRepository
    private lateinit var worryStorage: WorryStorage
    private lateinit var empathyCounter: EmpathyCounter
    private lateinit var feedStorage: FeedStorage

    @BeforeEach
    fun setUp() {
        feedRepository = mockk(relaxed = true)
        worryStorage = mockk(relaxed = true)
        empathyCounter = mockk(relaxed = true)
        feedStorage = FeedStorage(feedRepository, worryStorage, empathyCounter)
    }

    @Test
    fun `should share worry and create feed`() {
        // given
        val worryId = 1L
        val feedbackId = 2L
        val feedId = 3L
        val ownerId = 4L
        val sharedAt = LocalDateTime.now()

        val mockFeedEntity = FeedEntity(
            ownerId = ownerId,
            worryId = worryId,
            feedbackId = feedbackId,
            sharedAt = sharedAt,
        ).apply {
            val field = BaseEntity::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, feedId)
        }

        val mockWorry = Worry(
            id = worryId,
            userId = ownerId,
            mode = WorryMode.LETTER,
            emotion = "Happy",
            category = "Work",
            content = "Test content",
            lastStepOrder = 0,
        )

        every { feedRepository.save(any()) } returns mockFeedEntity
        every { worryStorage.getWorry(worryId) } returns mockWorry
        every { empathyCounter.getEmpathyCount(feedId) } returns 0L

        // when
        val result = feedStorage.shareWorry(worryId, feedbackId)

        // then
        assertThat(result.id).isEqualTo(feedId)
        assertThat(result.worry).isEqualTo(mockWorry)
        assertThat(result.empathyCount).isEqualTo(0L)
        verify { feedRepository.save(any()) }
    }

    @Test
    fun `should delete feed`() {
        // given
        val feedId = 1L
        val mockFeedEntity = mockk<FeedEntity>()

        every { feedRepository.findById(feedId) } returns Optional.of(mockFeedEntity)

        // when
        feedStorage.deleteFeed(feedId)

        // then
        verify { feedRepository.delete(mockFeedEntity) }
    }

    @Test
    fun `should throw exception when deleting non-existent feed`() {
        // given
        val feedId = 1L
        every { feedRepository.findById(feedId) } returns Optional.empty()

        // when & then
        assertThrows<CoreException> {
            feedStorage.deleteFeed(feedId)
        }
    }

    @Test
    fun `should get feed by id`() {
        // given
        val feedId = 1L
        val ownerId = 2L
        val worryId = 3L
        val feedbackId = 4L
        val sharedAt = LocalDateTime.now()

        val mockFeedEntity = FeedEntity(
            ownerId = ownerId,
            worryId = worryId,
            feedbackId = feedbackId,
            sharedAt = sharedAt,
        ).apply {
            val field = BaseEntity::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, feedId)
        }

        val mockWorry = Worry(
            id = worryId,
            userId = ownerId,
            mode = WorryMode.LETTER,
            emotion = "Happy",
            category = "Work",
            content = "Test content",
            lastStepOrder = 0,
        )
        every { feedRepository.findById(feedId) } returns Optional.of(mockFeedEntity)
        every { worryStorage.getWorry(worryId) } returns mockWorry
        every { empathyCounter.getEmpathyCount(feedId) } returns 5L

        // when
        val result = feedStorage.getFeed(feedId)

        // then
        assertThat(result.id).isEqualTo(feedId)
        assertThat(result.ownerId).isEqualTo(ownerId)
        assertThat(result.worry).isEqualTo(mockWorry)
        assertThat(result.empathyCount).isEqualTo(5L)
        assertThat(result.sharedAt).isEqualTo(sharedAt)
    }

    @Test
    fun `should throw exception when getting non-existent feed`() {
        // given
        val feedId = 1L
        every { feedRepository.findById(feedId) } returns Optional.empty()

        // when & then
        assertThrows<CoreException> {
            feedStorage.getFeed(feedId)
        }
    }

    @Test
    fun `should get feeds by owner id`() {
        // given
        val ownerId = 1L
        val feed1 = createMockFeedEntity(1L, ownerId, 10L, 20L)
        val feed2 = createMockFeedEntity(2L, ownerId, 11L, 21L)

        val mockWorry1 = createMockWorry(10L, ownerId)
        val mockWorry2 = createMockWorry(11L, ownerId)

        every { feedRepository.findByOwnerId(ownerId) } returns listOf(feed1, feed2)
        every { worryStorage.getWorry(10L) } returns mockWorry1
        every { worryStorage.getWorry(11L) } returns mockWorry2
        every { empathyCounter.getEmpathyCount(1L) } returns 5L
        every { empathyCounter.getEmpathyCount(2L) } returns 10L

        // when
        val results = feedStorage.getFeedByOwnerId(ownerId)

        // then
        assertThat(results).hasSize(2)
        assertThat(results[0].id).isEqualTo(1L)
        assertThat(results[1].id).isEqualTo(2L)
        assertThat(results[0].empathyCount).isEqualTo(5L)
        assertThat(results[1].empathyCount).isEqualTo(10L)
    }

    @Test
    fun `should get all feeds ordered by shared date`() {
        // given
        val feed1 = createMockFeedEntity(1L, 100L, 10L, 20L)
        val feed2 = createMockFeedEntity(2L, 101L, 11L, 21L)

        val mockWorry1 = createMockWorry(10L, 100L)
        val mockWorry2 = createMockWorry(11L, 101L)

        every { feedRepository.findAllByOrderBySharedAtDesc() } returns listOf(feed1, feed2)
        every { worryStorage.getWorry(10L) } returns mockWorry1
        every { worryStorage.getWorry(11L) } returns mockWorry2
        every { empathyCounter.getEmpathyCount(1L) } returns 5L
        every { empathyCounter.getEmpathyCount(2L) } returns 10L

        // when
        val results = feedStorage.getAllFeeds()

        // then
        assertThat(results).hasSize(2)
        assertThat(results[0].id).isEqualTo(1L)
        assertThat(results[1].id).isEqualTo(2L)
    }

    // Helper methods
    private fun createMockFeedEntity(id: Long, ownerId: Long, worryId: Long, feedbackId: Long): FeedEntity {
        return FeedEntity(
            ownerId = ownerId,
            worryId = worryId,
            feedbackId = feedbackId,
            sharedAt = LocalDateTime.now(),
        ).apply {
            val field = BaseEntity::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, id)
        }
    }

    private fun createMockWorry(id: Long, userId: Long): Worry {
        return Worry(
            id = id,
            userId = userId,
            mode = WorryMode.LETTER,
            emotion = "Happy",
            category = "Work",
            content = "Test content",
            lastStepOrder = 0,
        )
    }
}
