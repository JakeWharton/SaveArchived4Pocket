package com.jakewharton.sa4p.ui

import android.content.res.Resources.Theme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jakewharton.sa4p.R
import com.jakewharton.sa4p.presenter.MainModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUi(model: MainModel) {
	val systemUiController = rememberSystemUiController()

	Theme {
		val appBarColor = MaterialTheme.colorScheme.primary

		LaunchedEffect(systemUiController) {
			systemUiController.setSystemBarsColor(
				color = Color.Transparent,
				darkIcons = appBarColor.luminance() > 0.5f,
			)
		}

		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text(text = stringResource(id = R.string.ui_activity_name)) },
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = MaterialTheme.colorScheme.primary,
						titleContentColor = MaterialTheme.colorScheme.onPrimary,
					),
				)
			},
			bottomBar = {
				BottomBar(model)
			},
		) {
			Box(modifier = Modifier.padding(it)) {
				PendingUrls(model.pendingUrls)
			}
		}
	}
}
