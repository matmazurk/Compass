package com.mat.compass

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // if device has no gyroscope, app can't operate
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
            MaterialDialog(this).show {
                title(R.string.no_gyroscope_title)
                message(R.string.no_gyroscope_message)
                positiveButton(R.string.ok) { dialog ->
                    dialog.dismiss()
                    this@MainActivity.finish()
                }
                setOnDismissListener {
                    this@MainActivity.finish()
                }
            }
        }
    }
}
