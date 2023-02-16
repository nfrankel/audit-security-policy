package ch.frankel.blog.consumers

import com.hazelcast.core.Hazelcast
import com.hazelcast.jet.cdc.ChangeRecord
import com.hazelcast.jet.cdc.postgres.PostgresCdcSources
import com.hazelcast.jet.pipeline.Pipeline
import com.hazelcast.jet.pipeline.SinkBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

fun main() {
    val source = PostgresCdcSources.postgres("source")
        .setDatabaseAddress(System.getenv("DATABASE_HOST") ?: "localhost")
        .setDatabasePort((System.getenv("DATABASE_PORT") ?: "5432").toInt())
        .setDatabaseUser(System.getenv("DATABASE_USER") ?: "postgres")
        .setDatabasePassword(System.getenv("DATABASE_PASSWORD") ?: "postgres")
        .setDatabaseName(System.getenv("DATABASE_NAME") ?: "postgres")
        .setTableWhitelist("secure.account")
        .setLogicalDecodingPlugIn("pgoutput")
        .build()

    val pipeline = Pipeline.create().apply {
        readFrom(source).withoutTimestamps().writeTo(consumerSink)
    }
    val hz = Hazelcast.bootstrappedInstance()
    hz.jet.newJob(pipeline)
}

private val consumerSink = SinkBuilder.sinkBuilder("consumerSink") {
    val client = OkHttpClient()
    val requestBuilder = Request.Builder()
        .url("${System.getenv("APISIX_ADMIN_URL")}/apisix/admin/consumers")
        .addHeader("Content-Type", "application/json")
        .addHeader("X-API-KEY", System.getenv("APISIX_ADMIN_API_KEY"))
    client to requestBuilder
}.receiveFn { clientRequestBuilderPair, item: ChangeRecord ->
    val (client, requestBuilder) = clientRequestBuilderPair
    val map = item.value().toMap()
    val request = requestBuilder
        .put(map.toJsonPayload().toRequestBody())
        .build()
    client.newCall(request).execute()
}.build()

private fun Map<String, Any>.toJsonPayload() = """
        {
          "username": "${this["id"]}",
          "plugins": {
            "key-auth": {
              "key": "${this["password"]}"
            }
          }
        }
    """.trimIndent()