package com.silverhetch.artemis

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.silverhetch.aura.AuraActivity

/**
 * Uri Activity
 */
class UriInputActivity : AuraActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputView = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(this)
            .setView(inputView)
            .setPositiveButton(R.string.app_confirm) { _, _ ->
                try {
                    setResult(RESULT_OK, Intent().apply {
                        data = Uri.parse(inputView.text.toString())
                    })
                } catch (e: Exception) {
                    setResult(RESULT_CANCELED)
                }
                finish()
            }.setOnCancelListener {
                setResult(RESULT_CANCELED)
                finish()
            }.setNegativeButton(R.string.app_cancel) { _, _ ->
                setResult(RESULT_CANCELED)
                finish()
            }.show()
    }
}