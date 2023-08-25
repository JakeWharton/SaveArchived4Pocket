package com.jakewharton.sa4p.net

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PocketApi {
	@POST("v3/add")
	@Headers("X-Accept: application/json")
	suspend fun add(@Body body: AddRequest): AddResponse

	@POST("v3/send")
	@Headers("X-Accept: application/json")
	suspend fun send(@Body body: SendRequest): SendResponse

	@POST("v3/oauth/request")
	@Headers("X-Accept: application/json")
	suspend fun oauthRequest(@Body body: OauthRequestRequest): OauthRequestResponse

	@POST("v3/oauth/authorize")
	@Headers("X-Accept: application/json")
	suspend fun oauthAuthorize(@Body body: OauthAuthorizeRequest): OauthAuthorizeResponse
}

@Serializable
data class OauthRequestRequest(
	@SerialName("consumer_key") val consumerKey: String,
	@SerialName("redirect_uri") val redirectUri: String,
)

@Serializable
data class OauthRequestResponse(
	val code: String,
)

@Serializable
data class OauthAuthorizeRequest(
	@SerialName("consumer_key") val consumerKey: String,
	val code: String,
)

@Serializable
data class OauthAuthorizeResponse(
	@SerialName("access_token") val accessToken: String,
	val username: String,
)

@Serializable
data class AddRequest(
	@SerialName("consumer_key") val consumerKey: String,
	@SerialName("access_token") val accessToken: String,
	val url: String,
	@Serializable(UnixTimeSerializer::class)
	@SerialName("time") val timestamp: Instant,
)

@Serializable
data class AddResponse(
	@Serializable(StatusSerializer::class)
	@SerialName("status") val success: Boolean,
	val item: Item,
) {
	@Serializable
	data class Item(
		@SerialName("item_id") val id: String,
	)
}

@Serializable
data class SendRequest(
	@SerialName("consumer_key") val consumerKey: String,
	@SerialName("access_token") val accessToken: String,
	val actions: List<SendAction>,
)

@Serializable
sealed interface SendAction

@Serializable
@SerialName("archive")
data class SendArchiveAction(
	@SerialName("item_id") val itemId: String,
	@Serializable(UnixTimeSerializer::class)
	@SerialName("time") val timestamp: Instant,
) : SendAction

@Serializable
data class SendResponse(
	@Serializable(StatusSerializer::class)
	@SerialName("status") val success: Boolean,
	@SerialName("action_results") val results: List<Boolean>,
)

private object StatusSerializer : KSerializer<Boolean> {
	override val descriptor get() = Int.serializer().descriptor

	override fun deserialize(decoder: Decoder): Boolean {
		return when (val value = decoder.decodeInt()) {
			0 -> false
			1 -> true
			else -> throw IllegalStateException("Unknown status value: $value")
		}
	}

	override fun serialize(encoder: Encoder, value: Boolean) {
		encoder.encodeInt(if (value) 1 else 0)
	}
}

private object UnixTimeSerializer : KSerializer<Instant> {
	override val descriptor get() = Long.serializer().descriptor

	override fun deserialize(decoder: Decoder): Instant {
		val value = decoder.decodeLong()
		return Instant.fromEpochSeconds(value)
	}

	override fun serialize(encoder: Encoder, value: Instant) {
		encoder.encodeLong(value.epochSeconds)
	}
}
