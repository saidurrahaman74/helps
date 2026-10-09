package com.srcreation.smarthelp

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Centralized App Navigator for managing screen transitions efficiently.
 * Optimized for activity reuse (SINGLE_TOP / REORDER_TO_FRONT) to reduce memory overhead.
 */
object AppNavigator {

    /**
     * Launch an Activity with optimized flags to prevent stacking multiple instances of the same Activity.
     */
    inline fun <reified T : Activity> navigateTo(
        context: Context,
        clearTop: Boolean = false,
        extras: Intent.() -> Unit = {}
    ) {
        val intent = Intent(context, T::class.java).apply {
            if (clearTop) {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            } else {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            extras()
        }
        context.startActivity(intent)
    }

    /**
     * Safely launch an Activity intent with single top flags.
     */
    fun launchIntent(context: Context, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        context.startActivity(intent)
    }
}
