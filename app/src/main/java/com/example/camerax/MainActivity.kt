package com.example.camerax

import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.camerax.adapter.CropType
import com.example.camerax.adapter.FilterAdapter
import com.example.camerax.adapter.FlashType
import com.example.camerax.adapter.ToolsAdapter
import com.example.camerax.adapter.TypeItems
import com.example.camerax.adapter.getClockItems
import com.example.camerax.adapter.getCropItems
import com.example.camerax.adapter.getFlashItems
import com.example.camerax.adapter.getMenuItems
import com.example.camerax.databinding.ActivityMainBinding
import com.example.camerax.utils.SaveDataCamera
import com.example.camerax.utils.setSafeClickListener
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.filter.NoFilter
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelector
import com.otaliastudios.cameraview.size.SizeSelectors
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isRecording = false

    private var currentFilter = 0
    private val allFilters = Filters.values()

    private lateinit var filterAdapter: FilterAdapter

    private lateinit var toolsAdapter: ToolsAdapter

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


        toolsAdapter = ToolsAdapter { item ->
            when (item) {
                is TypeItems.FlashItem -> {
                    val flashMode = when (item.type) {
                        FlashType.OFF -> {
                            binding.ivFlash.setImageResource(R.drawable.ic_flash_off)
                            Flash.OFF
                        }

                        FlashType.ON -> {
                            binding.ivFlash.setImageResource(R.drawable.ic_flash)
                            Flash.ON

                        }

                        FlashType.AUTO -> {
                            binding.ivFlash.setImageResource(R.drawable.ic_flash_auto)
                            Flash.AUTO

                        }
                    }
                    binding.camera.flash = flashMode

                }

                is TypeItems.CropItem -> {
//                    val ratio = when (item.type) {
//                        CropType.CROP_1_1 -> AspectRatio.of(1, 1)
//                        CropType.CROP_4_3 -> AspectRatio.of(4, 3)
//                        CropType.CROP_16_9 -> AspectRatio.of(16, 9)
//                    }
//                    val selector: SizeSelector = SizeSelectors.or(
//                        SizeSelectors.and(SizeSelectors.aspectRatio(ratio, 0f),   SizeSelectors.biggest()),
//                        SizeSelectors.and(SizeSelectors.aspectRatio(ratio, 0.02f), SizeSelectors.biggest()),
//                        SizeSelectors.biggest()
//                    )
//
//                    binding.camera.setPreviewStreamSize(selector)
//                    binding.camera.setPictureSize(selector)
//                    binding.camera.setVideoSize(selector)

                }

                is TypeItems.ClockItem -> {

                }

                is TypeItems.MenuItem -> {

                }

                is TypeItems.FocusItem -> {

                }

                is TypeItems.GridItem -> {

                }
            }
        }


        binding.rcvTools.apply {
            adapter = toolsAdapter
        }

        filterAdapter = FilterAdapter(allFilters) { selectedFilter ->
            changeCurrentFilter(selectedFilter)
        }

        changeColorIvFilter()
    }


    private fun setupData() {}
    private fun setupListener() {

        onBackPressedDispatcher.addCallback(this) {
            this@MainActivity.finish()
        }
        binding.apply {

            overlay.setOnClickListener {
                visibleRcvTools(false)
                ivFlash.isSelected = false
                ivCrop.isSelected = false
                ivClock.isSelected = false
                ivOptionMore.isSelected = false
                toolsAdapter.clearData()
            }
            // đổi camera
            ivChangeCamera.setSafeClickListener {
                val current = camera.facing
                stopRecordingIfNeeded()
                camera.facing = if (current == Facing.BACK) {
                    Facing.FRONT
                } else {
                    Facing.BACK
                }
            }

            // Chụp ảnh hoặc quay video
            ivTakePhoto.setSafeClickListener {
                if (camera.mode == Mode.PICTURE) {
                    // Nếu đang có filter (khác NONE) thì chụp snapshot để có filter
                    if (camera.filter !is NoFilter) {
                        camera.takePictureSnapshot()
                    } else {
                        camera.takePicture()
                    }
                } else {
                    if (!isRecording) {
                        val videoFile = File(cacheDir, "video_${System.currentTimeMillis()}.mp4")
                        if (camera.filter is NoFilter) {
                            camera.takeVideo(videoFile)
                        } else {
                            camera.takeVideoSnapshot(videoFile)
                        }
                    } else {
                        camera.stopVideo()
                    }
                }
            }


            // đổi mode
            ivChangeMode.setSafeClickListener {
                if (isRecording) return@setSafeClickListener
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

            ivBack.setSafeClickListener {
                grPhotoPreview.visibility = View.GONE
                stopRecordingIfNeeded()
            }

            rcvFilter.apply {
                layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = filterAdapter
            }

            ivFinish.setOnClickListener {
                this@MainActivity.onBackPressedDispatcher.onBackPressed()
            }

            ivFlash.setOnClickListener {
                visibleRcvTools(true)
                toolsAdapter.setDefaultItems(getFlashItems())
                ivFlash.isSelected = true
            }

            ivCrop.setOnClickListener {
                visibleRcvTools(true)
                toolsAdapter.setDefaultItems(getCropItems())
                ivCrop.isSelected = true

            }

            ivClock.setOnClickListener {

                visibleRcvTools(true)
                toolsAdapter.setDefaultItems(getClockItems())
                ivClock.isSelected = true

            }

            ivFilter.setOnClickListener {
                if (rcvFilter.isVisible) {
                    rcvFilter.isVisible = false
                } else {
                    rcvFilter.isVisible = true
                }
                changeColorIvFilter()
            }

            ivOptionMore.setOnClickListener {
                visibleRcvTools(true)
                toolsAdapter.setDefaultItems(getMenuItems())
                ivOptionMore.isSelected = true
            }
        }
    }

    private fun visibleRcvTools(isVisible: Boolean) {
        binding.apply {
            rcvTools.isVisible = isVisible
            overlay.isVisible = isVisible
        }
    }

    private fun changeColorIvFilter() {
        binding.ivFilter.setColorFilter(
            ContextCompat.getColor(
                this@MainActivity,
                if (binding.rcvFilter.isVisible) R.color.blue else R.color.white
            ),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun changeCurrentFilter(currentFilter: Int) {
        binding.apply {
            if (camera.preview != Preview.GL_SURFACE) return
            val filter = allFilters[currentFilter]

            // Normal behavior:
            camera.filter = filter.newInstance()
        }


    }


    private fun setupObserver() {}


    private fun stopRecordingIfNeeded() {
        if (isRecording) {
            binding.camera.stopVideo()
            binding.ivTakePhoto.setImageResource(R.drawable.ic_video_recording)
            isRecording = false
        }
    }

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
                    Toast.makeText(this@MainActivity, "Save photo failed!", Toast.LENGTH_SHORT)
                        .show()
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
            binding.ivTakePhoto.setImageResource(R.drawable.ic_video_stop)
            isRecording = true
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            binding.ivTakePhoto.setImageResource(R.drawable.ic_video_recording)
            isRecording = false
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


