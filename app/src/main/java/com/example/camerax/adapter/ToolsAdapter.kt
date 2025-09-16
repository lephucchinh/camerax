package com.example.camerax.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.camerax.databinding.LayoutClockBinding
import com.example.camerax.databinding.LayoutCropBinding
import com.example.camerax.databinding.LayoutFlashBinding
import com.example.camerax.databinding.LayoutGridBinding
import com.example.camerax.databinding.LayoutMenuBinding
import com.example.camerax.databinding.LayoutPhotographyBinding
import com.example.camerax.databinding.LayoutResolutionBinding
import com.otaliastudios.cameraview.R

class ToolsAdapter(
    private val onItemClick: (TypeItems) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<TypeItems>()

    private var selectedFlash: FlashType = FlashType.OFF
    private var selectedCrop: CropType = CropType.CROP_1_1
    private var selectedClock: ClockType = ClockType.OFF
    private var selectedMenu: MenuType = MenuType.GRID

    private var selectedGrid: GridType = GridType.NONE

    private var selectedFocus: FocusType = FocusType.AUTO

    private var selectedResolution: ResolutionType = ResolutionType.TYPE_720P

    private var selectedPhotography: PhotographyType = PhotographyType.OFF


    companion object {
        private const val FLASH_ITEM = 0
        private const val CROP_ITEM = 1
        private const val CLOCK_ITEM = 2
        private const val MENU_ITEM = 3
        private const val GRID_ITEM = 4

        private const val RESOLUTION_ITEM = 5

        private const val PHOTOGRAPHY_ITEM = 6
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is TypeItems.FlashItem -> FLASH_ITEM
        is TypeItems.CropItem -> CROP_ITEM
        is TypeItems.ClockItem -> CLOCK_ITEM
        is TypeItems.MenuItem -> MENU_ITEM
        is TypeItems.GridItem -> GRID_ITEM
        is TypeItems.PhotographyItem -> PHOTOGRAPHY_ITEM
        is TypeItems.ResolutionItem -> RESOLUTION_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            FLASH_ITEM -> FlashViewHolder(LayoutFlashBinding.inflate(inflater, parent, false))
            CROP_ITEM -> CropViewHolder(LayoutCropBinding.inflate(inflater, parent, false))
            CLOCK_ITEM -> ClockViewHolder(LayoutClockBinding.inflate(inflater, parent, false))
            MENU_ITEM -> MenuViewHolder(LayoutMenuBinding.inflate(inflater, parent, false))
            GRID_ITEM -> GridViewHolder(LayoutGridBinding.inflate(inflater, parent, false))
            RESOLUTION_ITEM -> ResolutionViewHolder(
                LayoutResolutionBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            PHOTOGRAPHY_ITEM -> PhotographyViewHolder(
                LayoutPhotographyBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TypeItems.FlashItem -> (holder as FlashViewHolder).bind(
                item,
                item.type == selectedFlash
            ) {
                selectedFlash = item.type
                notifyDataSetChanged()
                onItemClick(item)
            }

            is TypeItems.CropItem -> (holder as CropViewHolder).bind(
                item,
                item.type == selectedCrop
            ) {
                selectedCrop = item.type
                notifyDataSetChanged()
                onItemClick(item)
            }

            is TypeItems.ClockItem -> (holder as ClockViewHolder).bind(
                item,
                item.type == selectedClock
            ) {
                selectedClock = item.type
                notifyDataSetChanged()
                onItemClick(item)
            }

            is TypeItems.MenuItem -> (holder as MenuViewHolder).bind(
                item,
                item.type == selectedMenu
            ) {
                selectedMenu = item.type
                notifyDataSetChanged()
                onItemClick(item)
            }

            is TypeItems.GridItem -> (holder as GridViewHolder).bind(
                item,
                item.type == selectedGrid
            ) {
                selectedGrid = item.type
                notifyDataSetChanged()
                onItemClick(item)
            }

            is TypeItems.PhotographyItem -> (holder as PhotographyViewHolder).bind(
                item,
                item.type == selectedPhotography
            ) {
                selectedPhotography = item.type
                notifyDataSetChanged()
                onItemClick(item)
            }

            is TypeItems.ResolutionItem -> (holder as ResolutionViewHolder).bind(
                item,
                item.type == selectedResolution
            ) {
                selectedResolution = item.type
                notifyDataSetChanged()
                onItemClick(item)
            }
        }
    }

    // --- ViewHolders với highlight ---
    class FlashViewHolder(private val binding: LayoutFlashBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TypeItems.FlashItem, isSelected: Boolean, onClick: () -> Unit) {
            binding.ivFlash.setImageResource(item.image)
            binding.root.alpha = if (isSelected) 1f else 0.5f
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) com.example.camerax.R.color.blue else com.example.camerax.R.color.black
                )
            )
            binding.root.setOnClickListener { onClick() }
        }
    }

    class CropViewHolder(private val binding: LayoutCropBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TypeItems.CropItem, isSelected: Boolean, onClick: () -> Unit) {
            binding.tvCrop.text = item.title
            binding.root.alpha = if (isSelected) 1f else 0.5f
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) com.example.camerax.R.color.blue else com.example.camerax.R.color.black
                )
            )
            binding.root.setOnClickListener { onClick() }
        }
    }

    class ClockViewHolder(private val binding: LayoutClockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TypeItems.ClockItem, isSelected: Boolean, onClick: () -> Unit) {
            binding.tvClock.text = item.title
            binding.root.alpha = if (isSelected) 1f else 0.5f
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) com.example.camerax.R.color.blue else com.example.camerax.R.color.black
                )
            )
            binding.root.setOnClickListener { onClick() }
        }
    }

    class MenuViewHolder(private val binding: LayoutMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TypeItems.MenuItem, isSelected: Boolean, onClick: () -> Unit) {
            binding.tvMenu.text = item.title
            binding.tvMenu.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(binding.root.context, item.image),
                null, null, null
            )
//            binding.root.alpha = if (isSelected) 1f else 0.5f
            binding.root.setOnClickListener { onClick() }
        }
    }

    class GridViewHolder(private val binding: LayoutGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TypeItems.GridItem, isSelected: Boolean, onClick: () -> Unit) {
            binding.tvGrid.text = item.title
            binding.root.alpha = if (isSelected) 1f else 0.5f
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) com.example.camerax.R.color.blue else com.example.camerax.R.color.black
                )
            )
            binding.root.setOnClickListener { onClick() }
        }
    }

    class ResolutionViewHolder(private val binding: LayoutResolutionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TypeItems.ResolutionItem, isSelected: Boolean, onClick: () -> Unit) {
            binding.tvResolution.text = item.title
            binding.root.alpha = if (isSelected) 1f else 0.5f
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) com.example.camerax.R.color.blue else com.example.camerax.R.color.black
                )
            )
            binding.root.setOnClickListener { onClick() }
        }
    }


    class PhotographyViewHolder(private val binding: LayoutPhotographyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TypeItems.PhotographyItem, isSelected: Boolean, onClick: () -> Unit) {
            binding.tvPhotography.text = item.title
            binding.root.alpha = if (isSelected) 1f else 0.5f
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) com.example.camerax.R.color.blue else com.example.camerax.R.color.black
                )
            )
            binding.root.setOnClickListener { onClick() }
        }
    }


    fun setDefaultItems(newItems: List<TypeItems>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getDefaultItems(): TypeItems? {
        return items.getOrNull(0)
    }

    fun clearData() {
        items.clear()
    }
}
