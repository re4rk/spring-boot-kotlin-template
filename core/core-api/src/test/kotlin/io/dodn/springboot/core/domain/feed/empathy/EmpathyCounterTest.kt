package io.dodn.springboot.core.domain.feed.empathy

import io.dodn.springboot.UnitTest
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyEntity
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmpathyCounterTest : UnitTest() {
    private lateinit var feedEmpathyRepository: FeedEmpathyRepository
    private lateinit var empathyCounter: EmpathyCounter

    @BeforeEach
    fun setUp() {
        feedEmpathyRepository = mockk(relaxed = true)
        empathyCounter = EmpathyCounter(feedEmpathyRepository)
    }

    @Test
    fun `should add empathy when user has not liked yet`() {
        // given
        val feedId = 1L
        val userId = 10L
        val expectedCount = 5L

        every { feedEmpathyRepository.findByFeedIdAndOwnerId(feedId, userId) } returns null
        every { feedEmpathyRepository.countByFeedId(feedId) } returns expectedCount
        every { feedEmpathyRepository.save(any<FeedEmpathyEntity>()) } returns FeedEmpathyEntity(feedId = feedId, ownerId = userId)

        // when
        val result = empathyCounter.addEmpathy(feedId, userId)

        // then
        assertThat(result).isEqualTo(expectedCount)
        verify { feedEmpathyRepository.save(any<FeedEmpathyEntity>()) }
        verify { feedEmpathyRepository.countByFeedId(feedId) }
    }

    @Test
    fun `should throw exception when adding empathy that already exists`() {
        // given
        val feedId = 1L
        val userId = 10L
        val existingEmpathy = FeedEmpathyEntity(feedId = feedId, ownerId = userId)

        every { feedEmpathyRepository.findByFeedIdAndOwnerId(feedId, userId) } returns existingEmpathy

        // when & then
        assertThrows<CoreException> {
            empathyCounter.addEmpathy(feedId, userId)
        }
    }

    @Test
    fun `should remove empathy when it exists`() {
        // given
        val feedId = 1L
        val userId = 10L
        val expectedCount = 4L
        val existingEmpathy = FeedEmpathyEntity(feedId = feedId, ownerId = userId)

        every { feedEmpathyRepository.findByFeedIdAndOwnerId(feedId, userId) } returns existingEmpathy
        every { feedEmpathyRepository.countByFeedId(feedId) } returns expectedCount

        // when
        val result = empathyCounter.removeEmpathy(feedId, userId)

        // then
        assertThat(result).isEqualTo(expectedCount)
        verify { feedEmpathyRepository.delete(existingEmpathy) }
        verify { feedEmpathyRepository.countByFeedId(feedId) }
    }

    @Test
    fun `should throw exception when removing empathy that doesn't exist`() {
        // given
        val feedId = 1L
        val userId = 10L

        every { feedEmpathyRepository.findByFeedIdAndOwnerId(feedId, userId) } returns null

        // when & then
        assertThrows<CoreException> {
            empathyCounter.removeEmpathy(feedId, userId)
        }
    }

    @Test
    fun `should get empathy count`() {
        // given
        val feedId = 1L
        val expectedCount = 10L

        every { feedEmpathyRepository.countByFeedId(feedId) } returns expectedCount

        // when
        val result = empathyCounter.getEmpathyCount(feedId)

        // then
        assertThat(result).isEqualTo(expectedCount)
        verify { feedEmpathyRepository.countByFeedId(feedId) }
    }
}
