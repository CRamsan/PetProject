package com.cramsan.petproject.download

import androidx.lifecycle.Observer
import com.cramsan.framework.logging.EventLogger
import com.cramsan.framework.test.TestBase
import com.cramsan.framework.thread.ThreadUtil
import com.cramsan.framework.userevents.UserEvents
import com.cramsan.petproject.PetProjectApplication
import com.cramsan.petproject.appcore.provider.ModelProviderInterface
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class DownloadCatalogViewModelTest : TestBase() {

    lateinit var application: PetProjectApplication
    lateinit var modelProvider: ModelProviderInterface
    lateinit var testDispatcher: CoroutineDispatcher
    lateinit var viewModel: DownloadCatalogViewModel

    lateinit var observer: Observer<Boolean>

    override fun setupTest() {
        application = mockk(relaxed = true)
        modelProvider = mockk(relaxed = true)
        EventLogger.setInstance(mockk(relaxed = true))
        ThreadUtil.setInstance(mockk(relaxed = true))
        UserEvents.setInstance(mockk(relaxed = true))
        testDispatcher = TestCoroutineDispatcher()
        observer = mockk(relaxed = true)

        viewModel = DownloadCatalogViewModel(application, modelProvider, testDispatcher, dispatcherProvider, mockk())
        viewModel.observableIsDownloadComplete.observeForever(observer)
    }

    @Test
    fun testIsCatalogReadyInitialState() = runBlockingTest {
        assertEquals(false, viewModel.observableIsDownloadComplete.value)
        assertFalse(viewModel.isCatalogReady())
        assertEquals(false, viewModel.observableIsDownloadComplete.value)
    }

    @Test
    fun testIsCatalogReadyOnceDownloaded() = runBlockingTest {
        every { modelProvider.isCatalogAvailable(any()) } returns true

        assertEquals(false, viewModel.observableIsDownloadComplete.value)
        assertTrue(viewModel.isCatalogReady())
        assertEquals(true, viewModel.observableIsDownloadComplete.value)
    }

    @Test
    fun testDownloadCatalogOnBackground() = runBlockingTest {
        every { modelProvider.isCatalogAvailable(any()) } returns false
        coEvery { modelProvider.getPlantsWithToxicity(any(), any()) } returns emptyList()

        assertEquals(false, viewModel.observableIsDownloadComplete.value)
        viewModel.downloadCatalog()

        advanceUntilIdle()

        verify { observer.onChanged(true) }
        assertEquals(true, viewModel.observableIsDownloadComplete.value)
    }
}
