package com.jakewharton.sa4p.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.jakewharton.sa4p.presenter.MainModel

@Composable
fun PendingUrls(model: MainModel) {
	if (model.pendingUrls.isEmpty()) {
		Text("No URLs to sync!")
	} else {
		LazyColumn {
			items(model.pendingUrls) { pending ->
				Text(pending.url)
			}
		}
	}
}
