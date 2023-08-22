package com.jakewharton.sa4p.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jakewharton.sa4p.db.Pending
import com.jakewharton.sa4p.db.Urls
import kotlinx.datetime.Instant

@Composable
fun PendingUrls(urls: List<Pending>) {
	if (urls.isEmpty()) {
		ListItem(headlineContent = { Text("No URLs to sync!") })
	} else {
		LazyColumn {
			itemsIndexed(urls) { index, pending ->
				if (index > 0) {
					Divider()
				}
				ListItem(
					headlineContent = {
						Text(
							text = pending.url,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)
					},
				)
			}
		}
	}
}

@Preview
@Composable
fun PendingUrlsEmptyPreview() {
	PendingUrls(urls = emptyList())
}

@Preview
@Composable
fun PendingUrlsPopulatedPreview() {
	PendingUrls(
		urls = listOf(
			Pending(
				id = Urls.Id(0),
				url = "https://example.com/ball.php",
				added = Instant.parse("2017-06-11T00:00:00.000Z"),
			),
			Pending(
				id = Urls.Id(0),
				url = "https://www.example.com/bell/army.html",
				added = Instant.parse("2017-06-11T00:00:00.000Z"),
			),
			Pending(
				id = Urls.Id(0),
				url = "https://www.example.com/",
				added = Instant.parse("2017-06-11T00:00:00.000Z"),
			),
			Pending(
				id = Urls.Id(0),
				url = "https://activity.example.com/airplane.html?back=bag",
				added = Instant.parse("2017-06-11T00:00:00.000Z"),
			),
			Pending(
				id = Urls.Id(0),
				url = "https://attraction.example.com/box/bat",
				added = Instant.parse("2017-06-11T00:00:00.000Z"),
			),
		),
	)
}
