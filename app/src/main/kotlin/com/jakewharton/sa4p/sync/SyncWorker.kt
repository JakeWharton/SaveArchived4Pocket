package com.jakewharton.sa4p.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.jakewharton.sa4p.Sa4pApp
import com.jakewharton.sa4p.sync.SyncManager.Tokens
import kotlinx.coroutines.CancellationException

class SyncWorker(
	appContext: Context,
	workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
	override suspend fun doWork(): Result {
		val sync = (applicationContext as Sa4pApp).sync
		val tokens = Tokens(
			consumerKey = inputData.getString(ConsumerKey)!!,
			accessToken = inputData.getString(AccessToken)!!,
		)
		val job = sync.sync(tokens)
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
		private const val ConsumerKey = "consumer_key"
		private const val AccessToken = "access_token"
		fun createData(consumerKey: String, accessToken: String): Data = workDataOf(
			ConsumerKey to consumerKey,
			AccessToken to accessToken,
		)
	}
}
