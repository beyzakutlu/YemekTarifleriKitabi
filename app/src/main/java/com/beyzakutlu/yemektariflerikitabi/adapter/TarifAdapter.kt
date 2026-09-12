package com.beyzakutlu.yemektariflerikitabi.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.beyzakutlu.yemektariflerikitabi.databinding.RecyclerRowBinding
import com.beyzakutlu.yemektariflerikitabi.model.Tarif
import com.beyzakutlu.yemektariflerikitabi.view.ListeFragmentDirections

class TarifAdapter (var tarifListesi: List<Tarif>) : RecyclerView.Adapter<TarifAdapter.TarifHolder>() {
    private var tamListe: List<Tarif> = tarifListesi

    //Adapter bizden bir holder istedi aşağıda hemen holder yazdık.
    class TarifHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){

    }

    // Burada kaç tane recycler view row olucak onu yazıyoruz
    override fun getItemCount(): Int {
        return tarifListesi.size
    }

    //tarif holder oluşturuyoruz
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TarifHolder {
        val recyclerRowBinding= RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TarifHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: TarifHolder,
        position: Int
    ) {
        holder.binding.recyclerViewTextView.text=tarifListesi[position].isim

        val gorselByte = tarifListesi[position].gorsel
        holder.binding.rowImageView.setImageBitmap(BitmapFactory.decodeByteArray(gorselByte, 0, gorselByte.size))

        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "eski", id = tarifListesi[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }

    fun filter(query: String) {
        tarifListesi = if (query.trim().isEmpty()) {
            tamListe
        } else {
            tamListe.filter { tarif ->
                tarif.isim.lowercase().contains(query.trim().lowercase())
            }
        }
        notifyDataSetChanged()
    }




}