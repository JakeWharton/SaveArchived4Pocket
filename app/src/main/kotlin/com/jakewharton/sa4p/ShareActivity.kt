package com.jakewharton.sa4p

import android.app.Activity
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.work.Constraints
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
import com.jakewharton.sa4p.sync.SyncWorker
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.datetime.Clock

class ShareActivity : Activity() {
	private val scope = MainScope()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val intent = intent
		if (intent.type == "text/plain") {
			val data = intent.getStringExtra(EXTRA_TEXT)
			if (data != null) {
				// TODO Try to validate it's a URL? Is this our problem? Does Pocket tell us?
				saveUrl(data)
			} else {
				// TODO log no text
				finish()
			}
		} else {
			// TODO log wrong type
			finish()
		}
	}

	private fun saveUrl(url: String) {
		val app = application as Sa4pApp
		val appScope = app.scope
		val db = app.db
		val work = app.work
		val clock: Clock = Clock.System

		// Persist in the app scope so even if this activity is killed we do not lose user data.
		val dbDeferred = appScope.async(Dispatchers.IO) {
			db.urlsQueries.add(url, clock.now())
			db.credentialsQueries.get().executeAsOneOrNull()
		}
		val timeout = scope.launch(start = UNDISPATCHED) {
			delay(250.milliseconds)
		}

		scope.launch(start = UNDISPATCHED) {
			// Race the DB persist and credential lookup against a 250ms timer.
			val showProgress = select {
				dbDeferred.onAwait { false }
				timeout.onJoin { true }
			}

			// If the DB operation took more than 250 milliseconds, display a progress spinner to
			// acknowledge receipt of the user's share intent. We display this for a minimum of 250ms
			// so as to not flash it imperceptibly on screen and allow the animation to finish.
			if (showProgress) {
				// TODO show progress spinner animation
				delay(250.milliseconds)
			}

			// If the DB operation completed in time, or it completed in the 250ms progress spinner
			// display, this call will be instant. Otherwise we will suspend until it completes.
			val auth = dbDeferred.await()
			if (auth != null) {
				val inputData = SyncWorker.createData(auth.access_token)
				val constraints = Constraints(requiredNetworkType = CONNECTED)
				work.enqueue(
					OneTimeWorkRequestBuilder<SyncWorker>()
						.setConstraints(constraints)
						.setInputData(inputData)
						.build(),
				)
			}

			// TODO transition from progress spinner, if displayed
			// TODO Good looking toast, not this crap.
			// TODO Indicate if you are not authenticated and offer a button
			Toast.makeText(this@ShareActivity, "URL saved!", LENGTH_SHORT).show()
			finish()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		scope.cancel()
	}
}
