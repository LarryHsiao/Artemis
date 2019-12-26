package com.silverhetch.artemis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/**
 * Launcher Activity.
 */
class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
        startActivity(Intent(this, MediaListActivity::class.java))
    }
}
