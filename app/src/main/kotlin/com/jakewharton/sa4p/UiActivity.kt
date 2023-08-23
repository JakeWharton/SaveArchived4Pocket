package com.jakewharton.sa4p

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.jakewharton.sa4p.presenter.MainPresenter
import com.jakewharton.sa4p.ui.MainUi
import kotlinx.coroutines.launch

class UiActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)

		val app = application as Sa4pApp
		val db = app.db
		val syncManager = app.sync
		val authManager = app.auth

		setContent {
			MainUi(MainPresenter(syncManager, authManager, db.urlsQueries))
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		if (intent.action == ACTION_VIEW) {
			val app = application as Sa4pApp
			val authManager = app.auth
			app.scope.launch {
				authManager.completeAuthentication()
			}
		}
	}
}
