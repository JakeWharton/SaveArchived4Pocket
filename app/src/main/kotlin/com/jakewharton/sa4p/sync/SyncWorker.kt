package com.jakewharton.sa4p.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.jakewharton.sa4p.Sa4pApp
import kotlinx.coroutines.CancellationException

class SyncWorker(
	appContext: Context,
	workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
	override suspend fun doWork(): Result {
		val sync = (applicationContext as Sa4pApp).sync
		val accessToken = inputData.getString(ACCESS_TOKEN)!!
		val job = sync.sync(accessToken)
		try {
			job.join()
		} catch (_: CancellationException) {
			// Allow propagation.
		} catch (e: Throwable) {
			return Result.retry()
		}
		return Result.success()
	}

	companion object {
		private const val ACCESS_TOKEN = "access_token"
		fun createData(accessToken: String): Data = workDataOf(
			ACCESS_TOKEN to accessToken,
		)
	}
}
