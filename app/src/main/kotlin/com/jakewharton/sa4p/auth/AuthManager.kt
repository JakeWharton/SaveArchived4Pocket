package com.jakewharton.sa4p.auth

import android.net.Uri
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.jakewharton.sa4p.BuildConfig
import com.jakewharton.sa4p.db.CredentialsQueries
import com.jakewharton.sa4p.db.OauthQueries
import com.jakewharton.sa4p.net.OauthAuthorizeRequest
import com.jakewharton.sa4p.net.OauthRequestRequest
import com.jakewharton.sa4p.net.PocketApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class AuthManager(
	scope: CoroutineScope,
	private val credentialsQueries: CredentialsQueries,
	private val oauthQueries: OauthQueries,
	private val api: PocketApi,
) {
	val credentials: StateFlow<Credentials?> = credentialsQueries
		.get(::Credentials)
		.asFlow()
		.mapToOneOrNull(scope.coroutineContext)
		.stateIn(scope, started = Eagerly, initialValue = null)

	sealed interface Authentication
	data object Unauthenticated : Authentication
	data object InProgress : Authentication

	data class Credentials(
		val accessToken: String,
		val username: String,
	) : Authentication

	suspend fun clearAuthentication() {
		withContext(Dispatchers.IO) {
			credentialsQueries.clear()
		}
	}

	// TODO Error handling
	suspend fun initiateAuthentication(): Uri {
		val response = api.oauthRequest(
			OauthRequestRequest(
				consumerKey = BuildConfig.POCKET_CONSUMER_KEY.ifEmpty {
					throw IllegalStateException("No Pocket consumer key available")
				},
				redirectUri = RedirectUri,
			),
		)
		withContext(Dispatchers.IO) {
			oauthQueries.update(response.code)
		}
		return Uri.Builder()
			.scheme("https")
			.authority("getpocket.com")
			.path("/auth/authorize")
			.appendQueryParameter("request_token", response.code)
			.appendQueryParameter("redirect_uri", RedirectUri)
			.build()
	}

	suspend fun completeAuthentication() {
		val code = withContext(Dispatchers.IO) {
			oauthQueries.get().executeAsOneOrNull()
		} ?: return

		val response = api.oauthAuthorize(
			OauthAuthorizeRequest(
				consumerKey = BuildConfig.POCKET_CONSUMER_KEY,
				code = code,
			),
		)

		withContext(Dispatchers.IO) {
			oauthQueries.clear()
			credentialsQueries.update(response.accessToken, response.username)
		}
	}
}

private const val RedirectUri = "pocketapp108648:authorizationFinished"
