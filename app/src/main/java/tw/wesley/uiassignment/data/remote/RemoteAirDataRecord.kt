package tw.wesley.uiassignment.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteAirDataRecord(
    @Json(name = "siteid") val siteId: String,
    @Json(name = "sitename") val siteName: String,
    @Json(name = "county") val county: String,
    @Json(name = "pm2.5") val pm25: String,
    @Json(name = "status") val status: String
)

