package com.jakewharton.sa4p.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.jakewharton.sa4p.presenter.Authenticated
import com.jakewharton.sa4p.presenter.MainModel
import com.jakewharton.sa4p.presenter.Unauthenticated

@Composable
fun AuthenticationHeader(model: MainModel) {
	Row {
		when (val authentication = model.authentication) {
			is Authenticated -> {
				Text(authentication.username)

				var confirm by remember { mutableStateOf(false) }
				if (confirm) {
					Button(onClick = authentication.onSignOut) {
						Text("Confirm")
					}
					Button(onClick = { confirm = false }) {
						Text("Cancel")
					}
				} else {
					Button(onClick = { confirm = true }) {
						Text("Sign out")
					}
				}
			}

			is Unauthenticated -> {
				Button(onClick = authentication.onStartAuthentication) {
					Text("Sign in")
				}
			}
		}
	}
}

@Preview(widthDp = 320)
@Composable
private fun PreviewUnauthenticated() {
	AuthenticationHeader(
		MainModel(
			authentication = Unauthenticated(
				onStartAuthentication = {},
			),
			pendingUrls = emptyList(),
			syncRunning = false,
			onSyncNow = {},
		),
	)
}

@Preview(widthDp = 320)
@Composable
private fun PreviewAuthenticated() {
	AuthenticationHeader(
		MainModel(
			authentication = Authenticated(
				isLoggingOut = false,
				username = "Shrek",
				onSignOut = {},
			),
			pendingUrls = emptyList(),
			syncRunning = false,
			onSyncNow = {},
		),
	)
}
