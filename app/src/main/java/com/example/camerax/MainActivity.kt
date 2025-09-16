package com.example.camerax

import android.graphics.PointF
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.addCallback

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isInvisible
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
import com.example.camerax.adapter.PhotographyType
import com.example.camerax.adapter.ResolutionType
import com.example.camerax.adapter.ToolsAdapter
import com.example.camerax.adapter.TypeItems
import com.example.camerax.adapter.getClockItems
import com.example.camerax.adapter.getCropItems
import com.example.camerax.adapter.getFlashItems
import com.example.camerax.adapter.getGridItems
import com.example.camerax.adapter.getMenuItems
import com.example.camerax.adapter.getPhotographyItems
import com.example.camerax.adapter.getResolutionItems
import com.example.camerax.databinding.ActivityMainBinding
import com.example.camerax.utils.SaveDataCamera
import com.example.camerax.utils.cropToRatio
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
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import kotlinx.coroutines.Job
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

    private var currentCropType: CropType = CropType.CROP_1_1

    private var recordJob: Job? = null



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
                    when (item.type) {
                        CropType.CROP_1_1 -> {
                            binding.camera.apply {
                                currentCropType = CropType.CROP_1_1
                                setPictureSize(
                                    SizeSelectors.aspectRatio(
                                        AspectRatio.of(1, 1),
                                        0.1f
                                    )
                                )
                                val params = layoutParams as ConstraintLayout.LayoutParams
                                params.dimensionRatio = "W,1:1"
                                layoutParams = params
                            }
                        }

                        CropType.CROP_4_3 -> {
                            binding.camera.apply {
                                currentCropType = CropType.CROP_4_3
                                setPictureSize(
                                    SizeSelectors.aspectRatio(
                                        AspectRatio.of(4, 3),
                                        0.1f
                                    )
                                )
                                val params = layoutParams as ConstraintLayout.LayoutParams
                                params.dimensionRatio = "W,4:3"
                                layoutParams = params
                            }
                        }

                        CropType.CROP_16_9 -> {
                            binding.camera.apply {
                                currentCropType = CropType.CROP_16_9
                                setPictureSize(
                                    SizeSelectors.aspectRatio(
                                        AspectRatio.of(16, 9),
                                        0.1f
                                    )
                                )
                                val params = layoutParams as ConstraintLayout.LayoutParams
                                params.dimensionRatio = "W,16:9"
                                layoutParams = params
                            }
                        }
                    }

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

                            binding.seekExposure.isVisible = true

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

                is TypeItems.PhotographyItem -> {
                    when (item.type) {
                        PhotographyType.OFF -> {

                        }

                        PhotographyType.ON -> {
                        }

                    }
                }

                is TypeItems.ResolutionItem -> {
                    val selector = when (item.type) {
                        ResolutionType.TYPE_720P -> {
                            SizeSelectors.and(
                                SizeSelectors.aspectRatio(AspectRatio.of(16, 9), 0f),  // chuẩn 16:9
                                SizeSelectors.biggest(),                               // lấy size lớn nhất có cùng tỉ lệ
                                SizeSelectors.maxWidth(1280)                           // không vượt quá 720p
                            )
                        }

                        ResolutionType.TYPE_1080P -> {
                            SizeSelectors.and(
                                SizeSelectors.aspectRatio(AspectRatio.of(16, 9), 0f),
                                SizeSelectors.biggest(),
                                SizeSelectors.maxWidth(1920)
                            )
                        }

                        ResolutionType.TYPE_4K -> {
                            SizeSelectors.and(
                                SizeSelectors.aspectRatio(AspectRatio.of(16, 9), 0f),
                                SizeSelectors.biggest(),
                                SizeSelectors.maxWidth(3840)
                            )
                        }
                    }

                    binding.camera.setVideoSize(selector)
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
                ivResolution.isSelected = false
                ivPhotography.isSelected = false
                seekExposure.isVisible = false
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
                    } else {
                        // ✅ Chụp ngay lập tức
                        if (camera.filter !is NoFilter) {
                            camera.takePictureSnapshot()
                        } else {
                            camera.takePicture()
                        }
                    }

                } else {
                    if (!isRecording) {
                        val videoFile =
                            File(cacheDir, "video_${System.currentTimeMillis()}.mp4")
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
                    checkShowToolsInModeVideo(true)
                    Mode.VIDEO
                } else {
                    ivTakePhoto.setImageResource(R.drawable.ic_take_photo)
                    ivChangeMode.setImageResource(R.drawable.ic_video_recording)
                    checkShowToolsInModeVideo(false)
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

            ivResolution.setOnClickListener {
                visibleRcvTools(true)
                toolsAdapter.setDefaultItems(getResolutionItems())
                ivResolution.isSelected = true
            }

            ivPhotography.setOnClickListener {
                visibleRcvTools(true)
                toolsAdapter.setDefaultItems(getPhotographyItems())
                ivPhotography.isSelected = true
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


    private fun onSetupSeekbar() {
//        val min = options.exposureCorrectionMinValue
//        val max = options.exposureCorrectionMaxValue

        // Mapping: 0..100 -> min..max
        binding.seekExposure.max = 100
        binding.seekExposure.progress = 50 // 0 EV ở giữa

        val minExposure = -3f
        val maxExposure = 3f
        val steps = 60

        binding.seekExposure.max = steps
        binding.seekExposure.progress = steps / 2   // về 0.0 ở giữa

        binding.seekExposure.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                val exposureValue =
                    minExposure + (progress.toFloat() / steps) * (maxExposure - minExposure)
                // hiển thị khi đang kéo
                binding.tvCountdown.text = String.format("%.1f", exposureValue)
                if (fromUser) {
                    binding.tvCountdown.visibility = View.VISIBLE
                }

                // áp dụng cho CameraView
                binding.camera.exposureCorrection = exposureValue
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // hiện text khi bắt đầu kéo
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // ẩn hoặc clear text khi dừng kéo
                binding.tvCountdown.visibility = View.GONE
                // hoặc: binding.tvCountdown.text = ""
            }
        })
    }

    private fun setupObserver() {}

    private fun checkShowToolsInModeVideo(isVideoMode: Boolean) {
        binding.apply {
            ivFilter.isVisible = isVideoMode.not()
            ivClock.isVisible = isVideoMode.not()
            ivCrop.isVisible = isVideoMode.not()

            ivResolution.isVisible = isVideoMode
            ivPhotography.isVisible = isVideoMode
        }
    }


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
            onSetupSeekbar()


        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            exception.printStackTrace()
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            result.toBitmap { bitmap ->
                if (bitmap != null) {
                    // Crop theo tỷ lệ đã chọn
                    val cropped = when (currentCropType) {
                        CropType.CROP_1_1 -> bitmap.cropToRatio(1, 1)
                        CropType.CROP_4_3 -> bitmap.cropToRatio(3, 4)
                        CropType.CROP_16_9 -> bitmap.cropToRatio(9, 16)
                        else -> bitmap
                    }

                    // Hiển thị preview
                    binding.ivPhotoPreview.setImageBitmap(cropped)
                    binding.grPhotoPreview.visibility = View.VISIBLE

                    // Lưu ảnh crop
                    lifecycleScope.launch {
                        val saved = saveDataCamera.value.savePhoto(this@MainActivity, cropped)
                        Toast.makeText(
                            this@MainActivity,
                            if (saved) "Photo saved!" else "Save failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Bitmap null!", Toast.LENGTH_SHORT).show()
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
            binding.apply {
                ivTakePhoto.setImageResource(R.drawable.ic_video_stop)
                ivGallery.isInvisible = true
                ivChangeMode.isInvisible = true
                ivChangeCamera.isInvisible = true
                ivSetting.isInvisible = true

                tvCountdown.visibility = View.VISIBLE  // TextView hiển thị thời gian
                tvCountdown.text = "00:00"
            }
            isRecording = true

            // Bắt đầu đếm
            recordJob = lifecycleScope.launch {
                var seconds = 0
                while (isRecording) {
                    val minutes = seconds / 60
                    val secs = seconds % 60
                    binding.tvCountdown.text = String.format("%02d:%02d", minutes, secs)
                    delay(1000)
                    seconds++
                }
            }
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            binding.apply {
                ivTakePhoto.setImageResource(R.drawable.ic_video_recording)
                ivGallery.isInvisible = false
                ivChangeMode.isInvisible = false
                ivChangeCamera.isInvisible = false
                ivSetting.isInvisible = false

                tvCountdown.visibility = View.GONE
            }
            isRecording = false

            // Dừng job countdown
            recordJob?.cancel()
        }


        override fun onExposureCorrectionChanged(
            newValue: Float,
            bounds: FloatArray,
            fingers: Array<PointF>?
        ) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers)
        }

        override fun onZoomChanged(
            newValue: Float,
            bounds: FloatArray,
            fingers: Array<PointF>?
        ) {
            super.onZoomChanged(newValue, bounds, fingers)
        }
    }


}


