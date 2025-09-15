package com.example.camerax.adapter

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class TypeItems : Parcelable {
    @Parcelize
    data class FlashItem(@DrawableRes val image: Int, val type: FlashType) :
        TypeItems()

    @Parcelize
    data class CropItem(val title: String, val type: String) : TypeItems()

    @Parcelize
    data class ClockItem(val title: String, val type: String) : TypeItems()

    @Parcelize
    data class MenuItem(@DrawableRes val image: Int, val title: String, val type: String) :
        TypeItems()
}

enum class FlashType {
    OFF, ON, AUTO
}

enum class CropType {
    CROP_1_1,
    CROP_4_3,
    CROP_16_9
}

enum class ClockType {
    OFF, TIME_3S, TIME_5S, TIME_9S
}

enum class MenuType {
    GRID, FOCUS, EXPOSURE
}

fun getFlashItems(): List<TypeItems> {
    return listOf(
        TypeItems.FlashItem(com.example.camerax.R.drawable.ic_flash_off, FlashType.OFF),
        TypeItems.FlashItem(com.example.camerax.R.drawable.ic_flash, FlashType.ON),
        TypeItems.FlashItem(com.example.camerax.R.drawable.ic_flash_auto, FlashType.AUTO),
    )
}

fun getCropItems(): List<TypeItems> {
    return listOf(
        TypeItems.CropItem("1:1", CropType.CROP_1_1.name),
        TypeItems.CropItem("4:3", CropType.CROP_4_3.name),
        TypeItems.CropItem("16:9", CropType.CROP_16_9.name),
    )
}

fun getClockItems(): List<TypeItems> {
    return listOf(
        TypeItems.ClockItem("Off", ClockType.OFF.name),
        TypeItems.ClockItem("3s", ClockType.TIME_3S.name),
        TypeItems.ClockItem("5s", ClockType.TIME_5S.name),
        TypeItems.ClockItem("9s", ClockType.TIME_9S.name)
    )
}

fun getMenuItems(): List<TypeItems> {
    return listOf(
        TypeItems.MenuItem(com.example.camerax.R.drawable.ic_grid, "Grid", MenuType.GRID.name),
        TypeItems.MenuItem(com.example.camerax.R.drawable.ic_focus, "Focus", MenuType.FOCUS.name),
        TypeItems.MenuItem(
            com.example.camerax.R.drawable.ic_exposure,
            "Exposure",
            MenuType.EXPOSURE.name
        )
    )
}