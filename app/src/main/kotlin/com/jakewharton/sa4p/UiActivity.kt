package com.jakewharton.sa4p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.jakewharton.sa4p.presenter.MainPresenter
import com.jakewharton.sa4p.ui.MainUi

class UiActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)

		val app = application as Sa4pApp
		val authQueries = app.db.authQueries
		val urlsQueries = app.db.urlsQueries
		val syncManager = app.sync

		setContent {
			MainUi(MainPresenter(urlsQueries, authQueries, syncManager))
		}
	}

	// TODO Intent API to handle OAuth completion
}
