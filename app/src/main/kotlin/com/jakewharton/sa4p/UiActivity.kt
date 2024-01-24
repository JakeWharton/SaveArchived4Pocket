package com.jakewharton.sa4p

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jakewharton.sa4p.presenter.mainPresenter
import com.jakewharton.sa4p.ui.MainUi
import kotlinx.coroutines.launch

class UiActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		enableEdgeToEdge(
			statusBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) {
				// App bar is inverted, so dark icons in dark mode and light icons in light mode.
				(it.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_NO
			},
		)
		// Fix for three-button nav not properly going edge-to-edge.
		//  TODO https://issuetracker.google.com/issues/298296168
		window.setFlags(FLAG_LAYOUT_NO_LIMITS, FLAG_LAYOUT_NO_LIMITS)

		val app = application as Sa4pApp
		val db = app.db
		val syncManager = app.sync
		val authManager = app.auth

		setContent {
			MainUi(mainPresenter(syncManager, authManager, db.urlsQueries))
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
