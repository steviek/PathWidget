package com.sixbynine.transit.path.widget

import androidx.work.WorkManager
import androidx.work.await
import com.google.common.truth.Truth.assertThat
import com.sixbynine.transit.path.util.runBlockingTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class WidgetRefreshWorkerSchedulerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var workerScheduler: WidgetRefreshWorkerScheduler

    @Inject
    lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `schedulePeriodicRefresh() enqueues a single work request`() = runBlockingTest {
        workerScheduler.schedulePeriodicRefresh()

        assertThat(workManager.getWorkInfosByTag(WORK_TAG).await()).hasSize(1)
    }

    @Test
    fun `schedulePeriodicRefresh() multiple times doesn't add more work`() = runBlockingTest {
        workerScheduler.schedulePeriodicRefresh()

        workerScheduler.schedulePeriodicRefresh()

        assertThat(workManager.getWorkInfosByTag(WORK_TAG).await()).hasSize(1)
    }

    @Test
    fun `cancelPeriodicRefresh() removes scheduled work`() = runBlockingTest {
        workerScheduler.schedulePeriodicRefresh()

        workerScheduler.cancelPeriodicRefresh()

        assertThat(workManager.getWorkInfosByTag(WORK_TAG).await()).isEmpty()
    }

    @Test
    fun `cancelPeriodicRefresh() without work scheduled is a no-op`() = runBlockingTest {
        workerScheduler.cancelPeriodicRefresh()

        assertThat(workManager.getWorkInfosByTag(WORK_TAG).await()).isEmpty()
    }

    private companion object {
        const val WORK_TAG = "path_widget_refresh"
    }
}