package com.madcookie.holostation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.madcookie.holostation.data.Channel
import com.madcookie.holostation.data.Repository
import com.madcookie.holostation.databinding.ItemAddChannelBinding
import com.madcookie.holostation.databinding.ItemChannelBinding
import java.util.*

interface ChannelListRvAdapterListener {
    fun onClickAddChannel()
}

class ChannelListRvAdapter(private val channelListRvAdapterListener: ChannelListRvAdapterListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ChannelViewHolder(val binding: ItemChannelBinding) : RecyclerView.ViewHolder(binding.root)
    class AddChannelViewHolder(val binding: ItemAddChannelBinding) : RecyclerView.ViewHolder(binding.root)

    var isItemClickBlock = false

    var currentList: MutableList<Channel> = mutableListOf()
        private set

    override fun getItemViewType(position: Int): Int {
        return if (position == currentList.size) {
            VH_ADD_CHANNEL
        } else {
            VH_CHANNEL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VH_ADD_CHANNEL) {
            AddChannelViewHolder(ItemAddChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ChannelViewHolder(ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AddChannelViewHolder -> {
                if (currentList.count() == Repository.channelList.count()) {
                    holder.binding.root.visibility = View.GONE
                } else {
                    holder.binding.root.visibility = View.VISIBLE
                    holder.binding.root.setOnClickListener {
                        if (!isItemClickBlock) {
                            channelListRvAdapterListener.onClickAddChannel()
                        }
                    }
                }

            }
            is ChannelViewHolder -> {
                val channel = currentList[position]
                holder.binding.also { binding ->
                    binding.channelName.text = channel.name
                    binding.iconLive.visibility = if (channel.isLive) View.VISIBLE else View.INVISIBLE
                    binding.circleImageView.setImageResource(channel.profileImage)
                    binding.description.text = "${channel.group.msg} / ${channel.gen.msg}"

                    binding.root.setOnClickListener { v ->
                        if (isItemClickBlock) {
                            return@setOnClickListener
                        }
                        if (channel.videoId.isBlank()) {
                            goToYoutubeChannel(v.context, channel.id)
                        } else if (channel.isLive || channel.videoId.isNotBlank()) {
                            goToYoutubeVideo(v.context, channel.videoId)
                        }
                    }

                }
            }
        }

    }

    override fun getItemCount(): Int {
        //채널 추가버튼 때문에 +1
        return currentList.size + 1
    }

    fun submitList(newChannelList: List<Channel>) {
        this.currentList = newChannelList.toMutableList()
        notifyDataSetChanged()
    }

    private fun goToYoutubeVideo(context: Context, videoId: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$videoId")
        )
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }

    private fun goToYoutubeChannel(context: Context, channelId: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://www.youtube.com/channel/$channelId"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/channel/$channelId")
        )
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }


    inner class ItemTouchCallback : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.START or ItemTouchHelper.END
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            Collections.swap(currentList, viewHolder.adapterPosition, target.adapterPosition)
            notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (currentList.count() == Repository.channelList.count()) {
                notifyItemChanged(currentList.count())
            }
            currentList.removeAt(viewHolder.adapterPosition)
            notifyItemRemoved(viewHolder.adapterPosition)
        }

        override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            if (viewHolder is AddChannelViewHolder) return 0
            return super.getSwipeDirs(recyclerView, viewHolder)
        }

        override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            if (viewHolder is AddChannelViewHolder) return 0
            return super.getDragDirs(recyclerView, viewHolder)
        }

    }

    companion object {
        private const val VH_CHANNEL = 0
        private const val VH_ADD_CHANNEL = 1
    }


}