package com.example.camerax

import android.R.id.message
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.camerax.databinding.ActivityMainBinding
import com.example.camerax.utils.SaveDataCamera
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filters
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isRecording = false

    private var currentFilter = 0
    private val allFilters = Filters.values()

    private lateinit var filterAdapter: FilterAdapter


    val saveDataCamera = lazy { SaveDataCamera() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObserver()
        setupUI()       // <- init filterAdapter ở đây trước
        setupListener() // <- sau đó mới gán cho RecyclerView
        setupData()
    }

    private fun setupUI() {
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        binding.camera.setLifecycleOwner(this)
        binding.camera.addCameraListener(Listener())

        filterAdapter = FilterAdapter(allFilters) { selectedFilter ->
            changeCurrentFilter(selectedFilter)
        }
    }

    private fun setupData() {}
    private fun setupListener() {
        binding.apply {
            // đổi camera
            ivChangeCamera.setOnClickListener {
                val current = camera.facing
                camera.facing = if (current == Facing.BACK) {
                    Facing.FRONT
                } else {
                    Facing.BACK
                }
            }

            // Chụp ảnh hoặc quay video
            ivTakePhoto.setOnClickListener {
                if (camera.mode == Mode.PICTURE) {
                    // Chụp ảnh
                    camera.takePicture()
                } else {
                    if (!isRecording) {
                        // Bắt đầu quay
                        val videoFile = File(cacheDir, "video_${System.currentTimeMillis()}.mp4")
                        camera.takeVideo(videoFile)
                        isRecording = true
                        ivTakePhoto.setImageResource(R.drawable.ic_video_stop) // đổi icon sang nút Stop
                    } else {
                        // Dừng quay
                        camera.stopVideo()
                        isRecording = false
                        ivTakePhoto.setImageResource(R.drawable.ic_video_recording) // đổi icon lại
                    }
                }
            }

            // đổi mode
            ivChangeMode.setOnClickListener {
                val current = camera.mode
                camera.mode = if (current == Mode.PICTURE) {
                    ivTakePhoto.setImageResource(R.drawable.ic_video_recording)
                    ivChangeMode.setImageResource(R.drawable.ic_take_photo)
                    Mode.VIDEO
                } else {
                    ivTakePhoto.setImageResource(R.drawable.ic_take_photo)
                    ivChangeMode.setImageResource(R.drawable.ic_video_recording)
                    Mode.PICTURE
                }
            }

            icBack.setOnClickListener {
                grPhotoPreview.visibility = View.GONE
            }

            rcvFilter.apply {
                layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = filterAdapter
            }
        }
    }

    private fun changeCurrentFilter(currentFilter : Int) {
        binding.apply {
            if (camera.preview != Preview.GL_SURFACE) return
            val filter = allFilters[currentFilter]

            // Normal behavior:
            camera.filter = filter.newInstance()
        }


    }


    private fun setupObserver() {}

    private inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {

        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            exception.printStackTrace()
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            lifecycleScope.launch {
                val savePhotoSuccess = saveDataCamera.value.savePhoto(this@MainActivity, result)

                if (savePhotoSuccess) {
                    // Ảnh đã lưu → dùng chính bitmap từ savePhoto trả về (hoặc tự decode 1 lần)
                    result.toBitmap { bitmap ->
                        if (bitmap != null) {
                            binding.ivPhotoPreview.setImageBitmap(bitmap)
                            binding.grPhotoPreview.visibility = View.VISIBLE
                        }
                    }
                    Toast.makeText(this@MainActivity, "Photo saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Save photo failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            lifecycleScope.launch {
                val saveVideoSuccess = saveDataCamera.value.saveVideo(this@MainActivity, result)
                Toast.makeText(
                    this@MainActivity,
                    if (saveVideoSuccess) "Video saved!" else "Save video failed!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()

        }

        override fun onExposureCorrectionChanged(
            newValue: Float,
            bounds: FloatArray,
            fingers: Array<PointF>?
        ) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers)
        }

        override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
            super.onZoomChanged(newValue, bounds, fingers)
        }
    }


}


