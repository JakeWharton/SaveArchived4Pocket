package com.jakewharton.sa4p.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.jakewharton.sa4p.presenter.Authenticated
import com.jakewharton.sa4p.presenter.MainModel
import com.jakewharton.sa4p.presenter.Unauthenticated

@Suppress("ktlint:compose:modifier-missing-check") // BottomAppBar has its own modifier
@Composable
fun BottomBar(
	model: MainModel,
) {
	BottomAppBar(
		modifier = Modifier.wrapContentHeight(),
		actions = {
			when (val authentication = model.authentication) {
				is Authenticated -> {
					var confirm by remember { mutableStateOf(false) }
					if (confirm) {
						TextButton(
							onClick = { authentication.onSignOut() },
							enabled = !authentication.isLoggingOut,
						) {
							Icon(Filled.Check, contentDescription = "")
							Text(modifier = Modifier.padding(start = 4.dp), text = "Sign out")
						}
						TextButton(
							onClick = { confirm = false },
							enabled = !authentication.isLoggingOut,
						) {
							Icon(Filled.Clear, contentDescription = "")
							Text(modifier = Modifier.padding(start = 4.dp), text = "Cancel")
						}
						BackHandler {
							confirm = false
						}
					} else {
						TextButton(onClick = { confirm = true }) {
							Icon(Filled.AccountCircle, contentDescription = "")
							Text(modifier = Modifier.padding(start = 4.dp), text = authentication.username)
						}
					}
				}

				is Unauthenticated -> {
					TextButton(
						enabled = !authentication.isAuthenticating,
						onClick = { authentication.onStartAuthentication() },
					) {
						Icon(Filled.Person, contentDescription = "")
						Text(modifier = Modifier.padding(start = 4.dp), text = "Sign in")
					}
				}
			}
		},
		floatingActionButton = {
			if (model.authentication is Authenticated) {
				FloatingActionButton(
					onClick = {
						if (!model.syncRunning) {
							model.authentication.onSyncNow()
						}
					},
					containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
					elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
				) {
					val rotation = if (model.syncRunning) {
						val infiniteTransition = rememberInfiniteTransition()
						infiniteTransition.animateFloat(
							initialValue = 0F,
							targetValue = 360F,
							animationSpec = infiniteRepeatable(
								animation = tween(1000, easing = LinearEasing),
							),
						).value
					} else {
						0f
					}
					Icon(
						modifier = Modifier
							.rotate(rotation)
							// We cannot disable the FAB when sync is running, so try to visually replicate it
							// by dimming the icon.
							// TODO https://issuetracker.google.com/issues/243799938
							.alpha(if (model.syncRunning) 0.7f else 1f),
						imageVector = Filled.Refresh,
						contentDescription = "Synchronize URLs to Pocket",
					)
				}
			}
		},
	)
}
