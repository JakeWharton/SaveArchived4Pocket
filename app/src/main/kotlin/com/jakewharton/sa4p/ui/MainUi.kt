package com.jakewharton.sa4p.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jakewharton.sa4p.R
import com.jakewharton.sa4p.presenter.MainModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUi(model: MainModel) {
	Theme {
		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text(text = stringResource(id = R.string.app_name)) },
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
