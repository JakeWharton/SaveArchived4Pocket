package com.jakewharton.sa4p.sync

import com.jakewharton.sa4p.db.UrlsQueries
import com.jakewharton.sa4p.net.AddRequest
import com.jakewharton.sa4p.net.PocketApi
import com.jakewharton.sa4p.net.SendArchiveAction
import com.jakewharton.sa4p.net.SendRequest
import com.jakewharton.sa4p.sync.SyncManager.State.Idle
import com.jakewharton.sa4p.sync.SyncManager.State.Running
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncManager(
	private val scope: CoroutineScope,
	private val urlsQueries: UrlsQueries,
	private val api: PocketApi,
	private val ioContext: CoroutineContext,
) {
	private val activeJob = MutableStateFlow<Job?>(null)

	enum class State {
		Idle,
		Running,
	}
	val state: StateFlow<State> get() = object : StateFlow<State> {
		override val replayCache: List<State> get() = listOf(value)
		override val value: State get() = if (activeJob.value == null) Idle else Running
		override suspend fun collect(collector: FlowCollector<State>): Nothing {
			activeJob.collect {
				collector.emit(if (it == null) Idle else Running)
			}
		}
	}

	fun sync(): Job {
		while (true) {
			// If there's an active Job, grab and return it.
			activeJob.value?.let { return it }

			// Create a new job and attempt to install it as the active one.
			val newJob = scope.launch(start = LAZY) {
				performSync()
			}
			if (activeJob.compareAndSet(null, newJob)) {
				newJob.start()
				return newJob
			}

			// Found non-null Job!? Must have raced someone else. Cancel and try again.
			newJob.cancel()
		}
	}

	private suspend fun performSync() {
		val pendingList = withContext(ioContext) {
			urlsQueries.pending().executeAsList()
		}

		// TODO should these be args?
		// TODO what happens if you log out during sync? cancel the scope?
		val consumerKey = TODO()
		val accessToken = TODO()

		for (pending in pendingList) {
			// TODO try/catch stuff

			val addResponse = api.add(
				AddRequest(consumerKey, accessToken, pending.url, pending.added),
			)

			val pocketId = addResponse.item.id
			withContext(ioContext) {
				urlsQueries.update_pocket_id(pending.id, pocketId)
			}

			val sendResponse = api.send(
				SendRequest(
					consumerKey,
					accessToken,
					listOf(
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
