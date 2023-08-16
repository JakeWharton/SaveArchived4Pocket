package com.jakewharton.sa4p.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jakewharton.sa4p.presenter.MainModel

@Composable
fun MainUi(model: MainModel) {
	Theme {
		Scaffold(
			bottomBar = {
				BottomBar(model)
			},
		) {
			Box(modifier = Modifier.padding(it)) {
				PendingUrls(model)
			}
		}
	}
}
