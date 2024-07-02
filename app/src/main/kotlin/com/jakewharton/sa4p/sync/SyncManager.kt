package com.jakewharton.sa4p.sync

import com.jakewharton.sa4p.BuildConfig
import com.jakewharton.sa4p.db.UrlsQueries
import com.jakewharton.sa4p.net.AddRequest
import com.jakewharton.sa4p.net.PocketApi
import com.jakewharton.sa4p.net.SendArchiveAction
import com.jakewharton.sa4p.net.SendRequest
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SyncManager(
	private val scope: CoroutineScope,
	private val urlsQueries: UrlsQueries,
	private val api: PocketApi,
	private val ioContext: CoroutineContext,
) {
	private val activeJob = AtomicReference<Job>()

	sealed interface State {
		data class Idle(val error: String? = null) : State
		data object Running : State
	}

	private val _state = MutableStateFlow<State>(State.Idle(null))
	val state: StateFlow<State> get() = _state

	fun sync(accessToken: String): Job {
		while (true) {
			// If there's an active Job, grab and return it.
			activeJob.get()?.let { return it }

			// Create a new job and attempt to install it as the active one.
			val newJob = scope.launch(start = LAZY) {
				_state.value = State.Running
				_state.value = try {
					performSync(accessToken)
					State.Idle()
				} catch (e: HttpException) {
					State.Idle(e.message)
				} catch (e: IOException) {
					State.Idle(e.message)
				} finally {
					activeJob.set(null)
				}
			}
			if (activeJob.compareAndSet(null, newJob)) {
				newJob.start()
				return newJob
			}

			// Found non-null Job!? Must have raced someone else. Cancel and try again.
			newJob.cancel()
		}
	}

	private suspend fun performSync(accessToken: String) {
		val pendingList = withContext(ioContext) {
			urlsQueries.pending().executeAsList()
		}

		for (pending in pendingList) {
			val addResponse = api.add(
				AddRequest(
					consumerKey = BuildConfig.POCKET_CONSUMER_KEY,
					accessToken = accessToken,
					url = pending.url,
					timestamp = pending.added,
				),
			)

			val pocketId = addResponse.item.id
			withContext(ioContext) {
				urlsQueries.update_pocket_id(pending.id, pocketId)
			}

			val sendResponse = api.send(
				SendRequest(
					consumerKey = BuildConfig.POCKET_CONSUMER_KEY,
					accessToken = accessToken,
					actions = listOf(
						SendArchiveAction(pocketId, pending.added),
					),
				),
			)

			if (sendResponse.success && sendResponse.results.single()) {
				withContext(ioContext) {
					urlsQueries.update_archived(pending.id)
				}
			}
		}
	}
}
