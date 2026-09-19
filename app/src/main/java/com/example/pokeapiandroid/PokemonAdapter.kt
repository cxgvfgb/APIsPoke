package com.example.pokeapiandroid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PokemonAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val pokemonList: List<Pair<String, suspend () -> PokemonResponse>>
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon_card, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val (defaultTitle, fetchApi) = pokemonList[position]
        holder.bind(defaultTitle, fetchApi)
    }

    override fun getItemCount(): Int = pokemonList.size

    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        private val imageViewPokemon: ImageView = itemView.findViewById(R.id.imageViewPokemon)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textHeight: TextView = itemView.findViewById(R.id.textHeight)
        private val textWeight: TextView = itemView.findViewById(R.id.textWeight)
        private val textTypes: TextView = itemView.findViewById(R.id.textTypes)
        private val btnFetch: Button = itemView.findViewById(R.id.btnFetch)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(defaultTitle: String, fetchApi: suspend () -> PokemonResponse) {
            textTitle.text = defaultTitle
            btnFetch.text = "Cargar $defaultTitle"

            val loadData = {
                progressBar.visibility = View.VISIBLE
                btnFetch.isEnabled = false

                lifecycleOwner.lifecycleScope.launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            fetchApi()
                        }
                        progressBar.visibility = View.GONE
                        btnFetch.isEnabled = true
                        val capitalizedName = response.name.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                        }
                        textName.text = "Nombre: $capitalizedName"
                        textHeight.text = "Altura: ${response.height}"
                        textWeight.text = "Peso: ${response.weight}"

                        val typeList = response.types.joinToString(", ") { it.type.name }
                        textTypes.text = "Tipo(s): $typeList"

                        response.sprites.frontDefault?.let { imageUrl ->
                            Glide.with(itemView.context)
                                .load(imageUrl)
                                .into(imageViewPokemon)
                        }

                    } catch (e: Exception) {
                        progressBar.visibility = View.GONE
                        btnFetch.isEnabled = true
                        Log.e("POKE_DEBUG", "Error al consumir la API POKEAPI para $defaultTitle", e)
                        Toast.makeText(itemView.context, "Error al cargar $defaultTitle: ${e.localizedMessage}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }

            btnFetch.setOnClickListener {
                loadData()
            }

            loadData()
        }
    }
}