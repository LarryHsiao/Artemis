package com.silverhetch.artemis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.silverhetch.artemis.devices.DeviceListActivity

/**
 * Launcher Activity.
 */
class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
        startActivity(Intent(this, DeviceListActivity::class.java))
    }
}
