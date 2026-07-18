package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object CepHelper {

    data class CepResponse(
        val cep: String,
        val logradouro: String,
        val complemento: String,
        val bairro: String,
        val localidade: String,
        val uf: String,
        val erro: Boolean = false
    ) {
        fun toFormattedAddress(): String {
            val parts = mutableListOf<String>()
            if (logradouro.isNotBlank()) parts.add(logradouro)
            if (bairro.isNotBlank()) parts.add(bairro)
            if (localidade.isNotBlank()) {
                if (uf.isNotBlank()) {
                    parts.add("$localidade - $uf")
                } else {
                    parts.add(localidade)
                }
            }
            return parts.joinToString(", ")
        }
    }

    suspend fun fetchAddressByCep(cep: String): CepResponse? = withContext(Dispatchers.IO) {
        val sanitizedCep = cep.replace(Regex("[^0-9]"), "")
        if (sanitizedCep.length != 8) {
            return@withContext null
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://viacep.com.br/ws/$sanitizedCep/json/")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.doInput = true

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val json = JSONObject(response.toString())
                if (json.has("erro") && json.getBoolean("erro")) {
                    return@withContext CepResponse("", "", "", "", "", "", true)
                }

                return@withContext CepResponse(
                    cep = json.optString("cep", ""),
                    logradouro = json.optString("logradouro", ""),
                    complemento = json.optString("complemento", ""),
                    bairro = json.optString("bairro", ""),
                    localidade = json.optString("localidade", ""),
                    uf = json.optString("uf", "")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return@withContext null
    }
}
