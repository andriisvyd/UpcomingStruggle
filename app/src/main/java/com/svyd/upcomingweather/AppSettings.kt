package com.svyd.upcomingweather

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Opens this app's own settings page.
 *
 * The only way back from a refused location: the system dialog will not appear again, so the
 * permission has to be granted where the platform keeps it.
 */
fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
