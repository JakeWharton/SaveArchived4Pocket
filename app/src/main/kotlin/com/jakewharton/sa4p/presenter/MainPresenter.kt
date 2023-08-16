package com.jakewharton.sa4p.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.jakewharton.sa4p.db.AuthQueries
import com.jakewharton.sa4p.db.Pending
import com.jakewharton.sa4p.db.UrlsQueries
import com.jakewharton.sa4p.sync.SyncManager
import com.jakewharton.sa4p.sync.SyncManager.State
import com.jakewharton.sa4p.sync.SyncManager.Tokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainPresenter(
	urlsQueries: UrlsQueries,
	authQueries: AuthQueries,
	syncManager: SyncManager,
): MainModel {
	var isLoggingOut by remember {
		mutableStateOf(false)
	}

	val _credentials by remember {
		authQueries.credentials()
			.asFlow()
			.mapToOneOrNull(Dispatchers.IO)
	}.collectAsState(initial = null)
	val credentials = _credentials

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
			Authenticated(
				isLoggingOut = isLoggingOut,
				username = "JakeWharton",
				onSignOut = {
					isLoggingOut = true
					// TODO cancel work manager
					scope.launch(Dispatchers.IO) {
						try {
							authQueries.clear_credentials()
							// TODO error handling
						} finally {
							isLoggingOut = false
						}
					}
				},
				onSyncNow = {
					syncManager.sync(Tokens(credentials.consumer_key, credentials.access_token))
				},
			)
		} else {
			Unauthenticated(
				onStartAuthentication = {
					authQueries.update("sup", "sup", "JakeWharton")
					// TODO launch oauth https://getpocket.com/developer/docs/authentication
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
	val onStartAuthentication: () -> Unit,
) : Authentication

data class Authenticated(
	val isLoggingOut: Boolean,
	val username: String,
	val onSignOut: () -> Unit,
	/** Request to perform a sync, if one is not already running. */
	val onSyncNow: () -> Unit,
) : Authentication
