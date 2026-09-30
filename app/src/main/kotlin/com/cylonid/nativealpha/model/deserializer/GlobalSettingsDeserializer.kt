package com.cylonid.nativealpha.model.deserializer

import com.cylonid.nativealpha.model.GlobalSettings
import com.cylonid.nativealpha.model.WebApp
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.NullPointerException
import java.lang.reflect.Type

class GlobalSettingsDeserializer : JsonDeserializer<GlobalSettings> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): GlobalSettings {
        try {
            val obj = json.asJsonObject
            val globalWebApp =
                context.deserialize<WebApp>(obj.get("globalWebApp"), WebApp::class.java)
            val settings = Gson().fromJson(obj, GlobalSettings::class.java)
            settings.globalWebApp = globalWebApp

            return settings
        } catch (e: NullPointerException) {
            return GlobalSettings()
        }
    }
}
