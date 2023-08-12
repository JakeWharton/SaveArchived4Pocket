package com.jakewharton.sa4p.db

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

object InstantColumnAdapter : ColumnAdapter<Instant, Long> {
	override fun decode(databaseValue: Long) = Instant.fromEpochMilliseconds(databaseValue)
	override fun encode(value: Instant) = value.toEpochMilliseconds()
}
