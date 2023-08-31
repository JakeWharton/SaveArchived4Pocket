package com.jakewharton.sa4p.ui

import app.cash.paparazzi.DeviceConfig.Companion.NEXUS_4
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.jakewharton.sa4p.db.Pending
import com.jakewharton.sa4p.db.Urls
import com.jakewharton.sa4p.presenter.Authenticated
import com.jakewharton.sa4p.presenter.MainModel
import com.jakewharton.sa4p.presenter.Unauthenticated
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class MainUiTest(
	@TestParameter nightMode: NightMode,
) {
	@get:Rule val paparazzi = Paparazzi(
		deviceConfig = NEXUS_4.copy(
			nightMode = nightMode,
		),
	)

	@Test fun auth() {
		paparazzi.snapshot("unauthenticated") {
			MainUi(
				defaultModel.copy(
					authentication = Unauthenticated(
						isAuthenticating = false,
						onStartAuthentication = {},
					),
				),
			)
		}
		paparazzi.snapshot("authenticating") {
			MainUi(
				defaultModel.copy(
					authentication = Unauthenticated(
						isAuthenticating = true,
						onStartAuthentication = {},
					),
				),
			)
		}
		paparazzi.snapshot("authenticated") {
			MainUi(
				defaultModel.copy(
					authentication = Authenticated(
						isLoggingOut = false,
						username = "JakeWharton",
						onSignOut = {},
						onSyncNow = {},
					),
				),
			)
		}
		// TODO This state can only be tested by interacting with the UI layer. Clicking the
		//  authenticated username displays the log out button entirely at the UI layer and those
		//  are the ones which disable themselves with isLoggingOut.
		// paparazzi.snapshot("logging-out") {
		//   MainUi(defaultModel.copy(
		//     authentication = Authenticated(
		//       isLoggingOut = true,
		//       username = "JakeWharton",
		//       onSignOut = {},
		//       onSyncNow = {},
		//     ),
		//   ))
		// }
	}

	@Test fun sync() {
		paparazzi.snapshot("idle") {
			MainUi(
				defaultModel.copy(
					syncRunning = false,
				),
			)
		}
		paparazzi.snapshot("synchronizing") {
			MainUi(
				defaultModel.copy(
					syncRunning = true,
				),
			)
		}
	}

	@Test fun urls() {
		paparazzi.snapshot("empty") {
			MainUi(
				defaultModel.copy(
					pendingUrls = emptyList(),
				),
			)
		}
		paparazzi.snapshot("one") {
			MainUi(
				defaultModel.copy(
					pendingUrls = listOf(
						urlOf("https://example.com"),
					),
				),
			)
		}
		paparazzi.snapshot("many") {
			MainUi(
				defaultModel.copy(
					pendingUrls = listOf(
						urlOf("https://jakewharton.com/smaller-apks-with-resource-optimization/"),
						urlOf("https://blog.jetbrains.com/platform/2023/08/wayland-support/"),
						urlOf("https://publicobject.com/2019/06/10/value-objects-service-objects-and-glue/"),
						urlOf("https://developer.android.com/topic/libraries/view-binding"),
						urlOf("https://waitbutwhy.com/2014/05/life-weeks.html"),
						urlOf("https://code.cash.app/the-state-of-managing-state-with-compose"),
						urlOf("https://android-developers.googleblog.com/2018/02/introducing-android-ktx-even-sweeter.html"),
						urlOf("https://vorpus.org/blog/notes-on-structured-concurrency-or-go-statement-considered-harmful/"),
					),
				),
			)
		}
	}

	private val defaultModel = MainModel(
		authentication = Authenticated(
			isLoggingOut = false,
			username = "JakeWharton",
			onSignOut = {},
			onSyncNow = {},
		),
		pendingUrls = emptyList(),
		syncRunning = false,
	)

	private var nextId = 1L
	private fun urlOf(url: String): Pending {
		return Pending(
			id = Urls.Id(nextId++),
			url = url,
			added = Instant.DISTANT_FUTURE, // Currently unused at UI layer.
		)
	}
}
