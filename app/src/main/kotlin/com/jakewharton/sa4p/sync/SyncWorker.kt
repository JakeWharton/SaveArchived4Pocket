package com.jakewharton.sa4p.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jakewharton.sa4p.Sa4pApp
import kotlinx.coroutines.CancellationException

class SyncWorker(
	appContext: Context,
	workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
	override suspend fun doWork(): Result {
		val sync = (applicationContext as Sa4pApp).sync
		val job = sync.sync()
		try {
			job.join()
		} catch (_: CancellationException) {
			// Allow propagation.
		} catch (e: Throwable) {
			return Result.retry()
		}
		return Result.success()
	}
}
