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
    data class CropItem(val title: String, val type: CropType) : TypeItems()

    @Parcelize
    data class ClockItem(val title: String, val type: ClockType) : TypeItems()

    @Parcelize
    data class MenuItem(@DrawableRes val image: Int, val title: String, val type: MenuType) :
        TypeItems()

    @Parcelize
    data class GridItem(val title: String, val type: GridType) :
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
    GRID, EXPOSURE
}

enum class GridType {
    NONE, GIRD_3X3, GRID_PHI_3X3, GRID_4X4, CROSS, /*GR_1, GR_2, GR_3, GR_4,*/ DIAGONAL,/* TRIANGLE_1, TRIANGLE_2*/
}

enum class FocusType {
    AUTO, MACRO,INFINITY,CONTINUOUS
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
        TypeItems.CropItem("1:1", CropType.CROP_1_1),
        TypeItems.CropItem("4:3", CropType.CROP_4_3),
        TypeItems.CropItem("16:9", CropType.CROP_16_9),
    )
}

fun getClockItems(): List<TypeItems> {
    return listOf(
        TypeItems.ClockItem("Off", ClockType.OFF),
        TypeItems.ClockItem("3s", ClockType.TIME_3S),
        TypeItems.ClockItem("5s", ClockType.TIME_5S),
        TypeItems.ClockItem("9s", ClockType.TIME_9S)
    )
}

fun getMenuItems(): List<TypeItems> {
    return listOf(
        TypeItems.MenuItem(com.example.camerax.R.drawable.ic_grid, "Grid", MenuType.GRID),
        TypeItems.MenuItem(
            com.example.camerax.R.drawable.ic_exposure,
            "Exposure",
            MenuType.EXPOSURE
        )
    )
}

fun getGridItems(): List<TypeItems> {
    return listOf(
        TypeItems.GridItem("None", GridType.NONE),
        TypeItems.GridItem("3x3", GridType.GIRD_3X3),
        TypeItems.GridItem("Phi 3x3", GridType.GRID_PHI_3X3),
        TypeItems.GridItem("4x4", GridType.GRID_4X4),
        TypeItems.GridItem("Cross", GridType.CROSS),
        TypeItems.GridItem("Diagonal", GridType.DIAGONAL),
    )
}

