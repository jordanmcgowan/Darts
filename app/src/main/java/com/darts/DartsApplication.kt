package com.darts

import android.app.Application
import android.content.res.Configuration
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DartsApplication : Application() {
  // Called when the application is starting, before any other application objects have been created.
  // Overriding this method is totally optional!
  override fun onCreate() {
    super.onCreate()

    SoLoader.init(this, false)

    if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
      val client = AndroidFlipperClient.getInstance(this)
      client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
      client.addPlugin(DatabasesFlipperPlugin(this))
      client.start()
    }
  }

  // Called by the system when the device configuration changes while your component is running.
  // Overriding this method is totally optional!
  override fun onConfigurationChanged (newConfig : Configuration) {
    super.onConfigurationChanged(newConfig)
  }

  // This is called when the overall system is running low on memory,
  // and would like actively running processes to tighten their belts.
  // Overriding this method is totally optional!
  override fun onLowMemory() {
    super.onLowMemory()
  }
}