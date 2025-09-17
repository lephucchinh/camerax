package com.example.camerax.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.camerax.databinding.ActivitySettingBinding
import com.example.camerax.utils.SaveDataCamera.Companion.folderName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    var zoomMode = MutableStateFlow<Boolean>(true)
    var mirrorMode = MutableStateFlow<Boolean>(false)

    var resultIntent = Intent()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObserver()
        setupUI()       // <- init filterAdapter ở đây trước
        setupListener() // <- sau đó mới gán cho RecyclerView
        setupData()
    }

    private fun setupObserver() {
        zoomMode.update { intent.getBooleanExtra(ZOOM_MODE, true) }
        mirrorMode.update { intent.getBooleanExtra(MIRROR_MODE, false) }
    }

    private fun setupUI() {

        binding.apply {
            zoomMode.onEach {
                swZoom.isChecked = it
            }.launchIn(lifecycleScope)

            mirrorMode.onEach {
                swMirrorMode.isChecked = it
            }.launchIn(lifecycleScope)
        }
    }

    fun showInputDialog(context: Context, onOk: (String) -> Unit) {
        val editText = EditText(context).apply {
            hint = "Nhập tên folder"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        AlertDialog.Builder(context)
            .setTitle("Nhập tên folder")
            .setView(editText)
            .setPositiveButton("OK") { dialog, _ ->
                val text = editText.text.toString()
                onOk(text)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupListener() {
        onBackPressedDispatcher.addCallback(this) {
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.apply {
            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            vCameraZoom.setOnClickListener {
                zoomMode.update { !it }
                resultIntent.putExtra(ZOOM_MODE, zoomMode.value)
            }
            vMirrorMode.setOnClickListener {
                mirrorMode.update { !it }
                resultIntent.putExtra(MIRROR_MODE, mirrorMode.value)
            }
            vSaveLocal.setOnClickListener {
                showInputDialog(this@SettingActivity) {
                    folderName = it
                }
            }
        }


    }

    private fun setupData() {}


    companion object {
        const val ZOOM_MODE = "zoom_mode"
        const val MIRROR_MODE = "mirror_mode"
        const val SAVE_LOCAL = "save_local"
    }

}