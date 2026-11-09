package com.worthour.ui.home

import android.content.Context
import androidx.annotation.DrawableRes

object SubscriptionLogoMapper {
    /**
     * Attempts to find a matching drawable resource for the subscription service name.
     * Returns null if no specific drawable asset exists in res/drawable.
     */
    @DrawableRes
    fun getLogoResId(context: Context, name: String): Int? {
        val normalized = name.lowercase().replace(" ", "").replace("+", "")
        val drawableName = when {
            normalized.contains("netflix") -> "ic_logo_netflix"
            normalized.contains("youtube") -> "ic_logo_youtube"
            normalized.contains("spotify") -> "ic_logo_spotify"
            normalized.contains("disney") -> "ic_logo_disney"
            else -> "ic_logo_$normalized"
        }

        return try {
            val field = com.worthour.R.drawable::class.java.getField(drawableName)
            val resId = field.getInt(null)
            if (resId != 0) resId else null
        } catch (e: Throwable) {
            null
        }
    }
}
