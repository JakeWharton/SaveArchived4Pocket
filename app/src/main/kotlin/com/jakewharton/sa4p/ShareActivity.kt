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
import kotlinx.datetime.Clock

class ShareActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val app = application as Sa4pApp
		val db = app.db
		val work = app.work
		val clock: Clock = Clock.System

		val intent = intent
		if (intent.type == "text/plain") {
			val data = intent.getStringExtra(EXTRA_TEXT)
			if (data != null) {
				// TODO Do this all asynchronously.
				//  If it succeeds within nice toast pop-in animation then show success.
				//  Else show progress spinner for at least 250ms and then success.
				db.urlsQueries.add(data, clock.now())
				val auth = db.authQueries.credentials().executeAsOneOrNull()
				if (auth != null) {
					val inputData = SyncWorker.createData(auth.consumer_key, auth.access_token)
					val constraints = Constraints(requiredNetworkType = CONNECTED)
					work.enqueue(
						OneTimeWorkRequestBuilder<SyncWorker>()
							.setConstraints(constraints)
							.setInputData(inputData)
							.build(),
					)
				}

				// TODO Good looking toast, not this crap.
				Toast.makeText(this, "URL saved!", LENGTH_SHORT).show()
			}
		}

		finish()
	}
}
