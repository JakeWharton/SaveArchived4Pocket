package com.jakewharton.sa4p

import android.app.Application
import android.util.Log
import androidx.work.WorkManager
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.jakewharton.sa4p.db.Database
import com.jakewharton.sa4p.db.InstantColumnAdapter
import com.jakewharton.sa4p.db.Urls
import com.jakewharton.sa4p.net.PocketApi
import com.jakewharton.sa4p.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import retrofit2.Retrofit
import retrofit2.create

class Sa4pApp : Application() {
	private val scope = MainScope()

	lateinit var db: Database
		private set
	lateinit var work: WorkManager
		private set
	lateinit var sync: SyncManager
		private set

	override fun onCreate() {
		super.onCreate()

		val dbDriver = AndroidSqliteDriver(
			schema = Database.Schema,
			context = this,
			name = "sa4p.db",
		)
		db = Database(
			driver = dbDriver,
			urlsAdapter = Urls.Adapter(
				addedAdapter = InstantColumnAdapter,
			),
		)

		work = WorkManager.getInstance(this)

		val client = OkHttpClient.Builder()
			.apply {
				if (BuildConfig.DEBUG) {
					addNetworkInterceptor(
						HttpLoggingInterceptor { Log.d("HTTP", it) }
							.apply { setLevel(BASIC) },
					)
				}
			}
			.build()

		val json = Json {
			prettyPrint = BuildConfig.DEBUG
			ignoreUnknownKeys = true
		}

		val api = Retrofit.Builder()
			.baseUrl("https://getpocket.com/")
			.client(client)
			.validateEagerly(BuildConfig.DEBUG)
			.addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
			.build()
			.create<PocketApi>()

		sync = SyncManager(
			scope = scope,
			urlsQueries = db.urlsQueries,
			api = api,
			ioContext = Dispatchers.IO,
		)
	}
}
