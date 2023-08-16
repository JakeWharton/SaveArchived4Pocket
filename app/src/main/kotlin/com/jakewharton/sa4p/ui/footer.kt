package com.jakewharton.sa4p.ui

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
import androidx.compose.ui.unit.dp
import com.jakewharton.sa4p.presenter.Authenticated
import com.jakewharton.sa4p.presenter.MainModel
import com.jakewharton.sa4p.presenter.Unauthenticated

@Composable
fun BottomBar(model: MainModel) {
	BottomAppBar(
		modifier = Modifier.wrapContentHeight(),
		actions = {
			when (val authentication = model.authentication) {
				is Authenticated -> {
					var confirm by remember { mutableStateOf(false) }
					if (confirm) {
						TextButton(onClick = { authentication.onSignOut() }) {
							Icon(Filled.Check, contentDescription = "")
							Text(modifier = Modifier.padding(start = 4.dp), text = "Sign out")
						}
						TextButton(onClick = { confirm = false }) {
							Icon(Filled.Clear, contentDescription = "")
							Text(modifier = Modifier.padding(start = 4.dp), text = "Cancel")
						}
					} else {
						TextButton(onClick = { confirm = true }) {
							Icon(Filled.AccountCircle, contentDescription = "")
							Text(modifier = Modifier.padding(start = 4.dp), text = authentication.username)
						}
					}
				}

				is Unauthenticated -> {
					TextButton(onClick = { authentication.onStartAuthentication() }) {
						Icon(Filled.Person, contentDescription = "")
						Text(modifier = Modifier.padding(start = 4.dp), text = "Sign in")
					}
				}
			}
		},
		floatingActionButton = {
			if (model.authentication is Authenticated) {
				// TODO disable if synchronizing
				FloatingActionButton(
					onClick = { model.authentication.onSyncNow() },
					containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
					elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
				) {
					// TODO rotate if synchronizing
					Icon(Filled.Refresh, "Synchronize URLs to Pocket")
				}
			}
		},
	)
}
