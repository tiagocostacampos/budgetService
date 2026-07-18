package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class SelectedService(
    val serviceId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("serviceId", serviceId)
        obj.put("name", name)
        obj.put("description", description)
        obj.put("price", price)
        obj.put("quantity", quantity)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): SelectedService {
            return SelectedService(
                serviceId = obj.getLong("serviceId"),
                name = obj.getString("name"),
                description = obj.optString("description", ""),
                price = obj.getDouble("price"),
                quantity = obj.getInt("quantity")
            )
        }
    }
}

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val clientEmail: String = "",
    val clientAddress: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val discount: Double = 0.0,
    val addition: Double = 0.0,
    val totalAmount: Double,
    val status: String = "Pendente", // Pendente, Aprovado, Concluído, Cancelado
    val servicesJson: String // Serialized JSONArray of SelectedService
) {
    fun getSelectedServices(): List<SelectedService> {
        val list = mutableListOf<SelectedService>()
        if (servicesJson.isEmpty()) return list
        try {
            val arr = JSONArray(servicesJson)
            for (i in 0 until arr.length()) {
                list.add(SelectedService.fromJsonObject(arr.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
