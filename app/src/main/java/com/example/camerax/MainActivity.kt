package com.example.camerax

import android.graphics.PointF
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.addCallback

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.camerax.adapter.ClockType
import com.example.camerax.adapter.CropType
import com.example.camerax.adapter.FilterAdapter
import com.example.camerax.adapter.FlashType
import com.example.camerax.adapter.FocusType
import com.example.camerax.adapter.GridType
import com.example.camerax.adapter.MenuType
import com.example.camerax.adapter.ToolsAdapter
import com.example.camerax.adapter.TypeItems
import com.example.camerax.adapter.getClockItems
import com.example.camerax.adapter.getCropItems
import com.example.camerax.adapter.getFlashItems
import com.example.camerax.adapter.getGridItems
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
import com.otaliastudios.cameraview.controls.Grid
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.filter.NoFilter
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import com.otaliastudios.cameraview.markers.AutoFocusMarker
import com.otaliastudios.cameraview.markers.DefaultAutoFocusMarker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isRecording = false

    private var currentFilter = 0
    private val allFilters = Filters.values()

    private lateinit var filterAdapter: FilterAdapter

    private lateinit var toolsAdapter: ToolsAdapter

    private var captureDelay: Long = 0L


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
        binding.camera.setAutoFocusMarker(
            DefaultAutoFocusMarker()
        )
        binding.camera.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS)
        binding.camera.autoFocusResetDelay = 2000

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


                }

                is TypeItems.ClockItem -> {
                    when (item.type) {
                        ClockType.OFF -> {
                            captureDelay = 0

                        }

                        ClockType.TIME_3S -> {
                            captureDelay = 3000L

                        }

                        ClockType.TIME_5S -> {
                            captureDelay = 5000L

                        }

                        ClockType.TIME_9S -> {
                            captureDelay = 9000L
                        }
                    }
                }

                is TypeItems.MenuItem -> {
                    when (item.type) {
                        MenuType.GRID -> {
                            binding.ivMenu.setImageResource(R.drawable.ic_grid)
                            toolsAdapter.setDefaultItems(getGridItems())

                        }

                        MenuType.EXPOSURE -> {
                            binding.ivMenu.setImageResource(R.drawable.ic_exposure)


                        }
                    }
                }


                is TypeItems.GridItem -> {
                    when (item.type) {
                        GridType.NONE -> {
                            binding.camera.grid = Grid.OFF
                        }

                        GridType.GIRD_3X3 -> {
                            binding.camera.grid = Grid.DRAW_3X3
                        }

                        GridType.CROSS -> {
                            binding.camera.grid = Grid.DRAW_CROSS

                        }

                        GridType.DIAGONAL -> {
                            binding.camera.grid = Grid.DRAW_DIAGONAL

                        }

//                        GridType.GR_1 -> {
//                            binding.camera.grid = Grid.DRAW_FIBONACCI
//
//                        }
//
//                        GridType.GR_2 -> {
//
//                        }
//
//                        GridType.GR_3 -> {
//
//                        }
//
//                        GridType.GR_4 -> {
//
//                        }

                        GridType.GRID_PHI_3X3 -> {
                            binding.camera.grid = Grid.DRAW_PHI
                        }

                        GridType.GRID_4X4 -> {
                            binding.camera.grid = Grid.DRAW_4X4

                        }

//                        GridType.TRIANGLE_1 -> {
//                            binding.camera.grid = Grid.DRAW_FIBONACCI
//                        }
//
//                        GridType.TRIANGLE_2 -> {
//
//                        }
                    }
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
                ivMenu.isSelected = false
                ivMenu.setImageResource(R.drawable.ic_menu)
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
                    if (captureDelay > 0) {
                        lifecycleScope.launch {
                            binding.overlay.visibility = View.VISIBLE
                            val seconds = (captureDelay / 1000).toInt()
                            for (i in seconds downTo 1) {
                                binding.tvCountdown.text = "$i s"
                                binding.tvCountdown.visibility = View.VISIBLE
                                delay(1000)
                            }
                            binding.tvCountdown.visibility = View.GONE
                            if (camera.filter !is NoFilter) {
                                camera.takePictureSnapshot()
                            } else {
                                camera.takePicture()
                            }
                            binding.overlay.visibility = View.GONE
                            captureDelay = 0L
                        }
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

            ivMenu.setOnClickListener {
                visibleRcvTools(true)
                toolsAdapter.setDefaultItems(getMenuItems())
                ivMenu.isSelected = true
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
            super.onCameraOpened(options)

            val min = options.exposureCorrectionMinValue
            val max = options.exposureCorrectionMaxValue

            // Mapping: 0..100 -> min..max
            binding.seekExposure.max = 100
            binding.seekExposure.progress = 50 // 0 EV ở giữa

            val minExposure = -3f
            val maxExposure = 3f
            val steps = 60  // tức là 0.1f mỗi nấc

            binding.seekExposure.max = steps
            binding.seekExposure.progress = steps / 2   // về 0.0 ở giữa

            binding.seekExposure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val exposureValue = minExposure + (progress.toFloat() / steps) * (maxExposure - minExposure)
                    // hiển thị khi đang kéo
                    binding.tvCountdown.text = String.format("%.1f", exposureValue)
                    binding.tvCountdown.visibility = View.VISIBLE

                    // áp dụng cho CameraView
                    binding.camera.exposureCorrection = exposureValue
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // hiện text khi bắt đầu kéo
                    binding.tvCountdown.visibility = View.VISIBLE
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // ẩn hoặc clear text khi dừng kéo
                    binding.tvCountdown.visibility = View.GONE
                    // hoặc: binding.tvCountdown.text = ""
                }
            })


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


