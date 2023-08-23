package com.jakewharton.sa4p.presenter

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jakewharton.sa4p.auth.AuthManager
import com.jakewharton.sa4p.db.Pending
import com.jakewharton.sa4p.db.UrlsQueries
import com.jakewharton.sa4p.sync.SyncManager
import com.jakewharton.sa4p.sync.SyncManager.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainPresenter(
	syncManager: SyncManager,
	authManager: AuthManager,
	urlsQueries: UrlsQueries,
): MainModel {
	val credentials = remember { authManager.credentials }
		.collectAsState()
		.value

	val pending by remember {
		urlsQueries.pending()
			.asFlow()
			.mapToList(Dispatchers.IO)
	}.collectAsState(initial = emptyList())

	val syncState by remember {
		syncManager.state
	}.collectAsState()

	val scope = rememberCoroutineScope()

	return MainModel(
		authentication = if (credentials != null) {
			var isLoggingOut by remember {
				mutableStateOf(false)
			}
			Authenticated(
				isLoggingOut = isLoggingOut,
				username = "JakeWharton",
				onSignOut = {
					isLoggingOut = true
					// TODO cancel work manager
					scope.launch {
						try {
							authManager.clearAuthentication()
							// TODO error handling
						} finally {
							isLoggingOut = false
						}
					}
				},
				onSyncNow = {
					syncManager.sync(credentials.accessToken)
				},
			)
		} else {
			val context = LocalContext.current
			var isAuthenticating by remember {
				mutableStateOf(false)
			}
			Unauthenticated(
				isAuthenticating = isAuthenticating,
				onStartAuthentication = {
					isAuthenticating = true
					scope.launch {
						val uri = try {
							authManager.initiateAuthentication()
						} finally {
							isAuthenticating = false
						}
						context.startActivity(Intent(ACTION_VIEW, uri))
					}
				},
			)
		},
		syncRunning = syncState != State.Idle,
		pendingUrls = pending,
	)
}

data class MainModel(
	val authentication: Authentication,
	val pendingUrls: List<Pending>,
	val syncRunning: Boolean,
)

sealed interface Authentication

data class Unauthenticated(
	val isAuthenticating: Boolean,
	val onStartAuthentication: () -> Unit,
) : Authentication

data class Authenticated(
	val isLoggingOut: Boolean,
	val username: String,
	val onSignOut: () -> Unit,
	/** Request to perform a sync, if one is not already running. */
	val onSyncNow: () -> Unit,
) : Authentication
