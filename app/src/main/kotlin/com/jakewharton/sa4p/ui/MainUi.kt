package com.jakewharton.sa4p.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.jakewharton.sa4p.presenter.MainModel

@Composable
fun MainUi(model: MainModel) {
	Theme {
		Column {
			AuthenticationHeader(model)
			PendingUrls(model)
		}
	}
}
