package com.example.mp3

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater


class SoundAdapter(
    private val sounds: List<Sound>,
    private val Click: (Sound) -> Unit
) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>(){

    class SoundViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val imgSound: ImageView = view.findViewById(R.id.imgSound)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_sound_view,parent,false)
        return SoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val sound = sounds[position]
        holder.tvTitle.text = sound.title
        holder.tvDuration.text = sound.duration
        holder.imgSound.setImageResource(sound.coverResId)

        holder.itemView.setOnClickListener { Click(sound)}
    }

    override fun getItemCount() = sounds.size


}
