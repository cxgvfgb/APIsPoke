package com.example.pokeapiandroid

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar cada una de las 5 tarjetas con su respectiva API
        setupPokemonCard(R.id.cardMewtwo, "Mewtwo", autoLoad = true) { RetrofitClient.apiService.getMewtwo() }
        setupPokemonCard(R.id.cardPikachu, "Pikachu", autoLoad = true) { RetrofitClient.apiService.getPikachu() }
        setupPokemonCard(R.id.cardBulbasaur, "Bulbasaur", autoLoad = true) { RetrofitClient.apiService.getBulbasaur() }
        setupPokemonCard(R.id.cardCharmander, "Charmander", autoLoad = true) { RetrofitClient.apiService.getCharmander() }
        setupPokemonCard(R.id.cardSquirtle, "Squirtle", autoLoad = true) { RetrofitClient.apiService.getSquirtle() }
    }

    private fun setupPokemonCard(cardViewId: Int, defaultTitle: String, autoLoad: Boolean, fetchApi: suspend () -> PokemonResponse) {
        val cardView = findViewById<View>(cardViewId)
        val textTitle = cardView.findViewById<TextView>(R.id.textTitle)
        val imageViewPokemon = cardView.findViewById<ImageView>(R.id.imageViewPokemon)
        val btnFetch = cardView.findViewById<Button>(R.id.btnFetch)
        val progressBar = cardView.findViewById<ProgressBar>(R.id.progressBar)

        textTitle.text = defaultTitle
        btnFetch.text = "Cargar $defaultTitle"

        val loadData = { isManual: Boolean ->
            progressBar.visibility = View.VISIBLE
            btnFetch.isEnabled = false

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        fetchApi()
                    }
                    progressBar.visibility = View.GONE
                    btnFetch.isEnabled = true

                    if (isManual) {
                        showPokemonDetailDialog(response, imageViewPokemon.drawable)
                    } else {
                        response.sprites.frontDefault?.let { imageUrl ->
                            Glide.with(this@MainActivity).load(imageUrl).into(imageViewPokemon)
                        }
                        val capitalizedName = response.name.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                        }
                        cardView.findViewById<TextView>(R.id.textName).text = "Nombre: $capitalizedName"
                    }

                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    btnFetch.isEnabled = true
                    Log.e("POKE_DEBUG", "Error al consumir la API POKEAPI para $defaultTitle", e)
                    Toast.makeText(this@MainActivity, "Error al cargar $defaultTitle: ${e.localizedMessage}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        btnFetch.setOnClickListener {
            loadData(true)
        }

        if (autoLoad) {
            loadData(false)
        }
    }

    private fun showPokemonDetailDialog(pokemon: PokemonResponse, currentDrawable: Drawable?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pokemon_detail)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val dialogImagePokemon = dialog.findViewById<ImageView>(R.id.dialogImagePokemon)
        val layoutPokeball = dialog.findViewById<View>(R.id.layoutPokeballContainer)
        val imgPbTop = dialog.findViewById<View>(R.id.imgPbTop)
        val imgPbBottom = dialog.findViewById<View>(R.id.imgPbBottom)
        val imgPbCenter = dialog.findViewById<View>(R.id.imgPbCenter)
        val viewLight = dialog.findViewById<View>(R.id.viewCaptureLight)

        val dialogTextName = dialog.findViewById<TextView>(R.id.dialogTextName)
        val dialogTextHeight = dialog.findViewById<TextView>(R.id.dialogTextHeight)
        val dialogTextWeight = dialog.findViewById<TextView>(R.id.dialogTextWeight)
        val dialogTextTypes = dialog.findViewById<TextView>(R.id.dialogTextTypes)
        val dialogBtnClose = dialog.findViewById<Button>(R.id.dialogBtnClose)

        if (currentDrawable != null) {
            dialogImagePokemon.setImageDrawable(currentDrawable)
        }

        val capitalizedName = pokemon.name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        dialogTextName.text = "Nombre: $capitalizedName"
        dialogTextHeight.text = "Altura: ${pokemon.height}"
        dialogTextWeight.text = "Peso: ${pokemon.weight}"
        dialogTextTypes.text = "Tipo(s): ${pokemon.types.joinToString(", ") { it.type.name }}"

        dialogBtnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()

        // Iniciar Animación de Tragar
        animateSwallowCapture(dialogImagePokemon, layoutPokeball, imgPbTop, imgPbBottom, imgPbCenter, viewLight) {
            pokemon.sprites.frontDefault?.let { imageUrl ->
                Glide.with(this).load(imageUrl).into(dialogImagePokemon)
            }
        }
    }

    private fun animateSwallowCapture(
        pokemonView: ImageView, 
        pbContainer: View, 
        pbTop: View, 
        pbBottom: View, 
        pbCenter: View,
        viewLight: View,
        updateImage: () -> Unit
    ) {
        pbContainer.visibility = View.VISIBLE
        pbContainer.alpha = 0f
        pbContainer.translationY = 200f

        // 1. Lanzamiento de la Pokebola
        val launch = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(pbContainer, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(pbContainer, "translationY", 200f, 0f)
            )
            duration = 500
            interpolator = DecelerateInterpolator()
        }

        // 2. Apertura de la Pokebola y Rayo
        val open = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(pbTop, "translationY", 0f, -40f),
                ObjectAnimator.ofFloat(pbBottom, "translationY", 0f, 40f),
                ObjectAnimator.ofFloat(pbCenter, "scaleX", 1f, 0f),
                ObjectAnimator.ofFloat(pbCenter, "scaleY", 1f, 0f),
                ObjectAnimator.ofFloat(viewLight, "alpha", 0f, 1f).apply { 
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            viewLight.visibility = View.VISIBLE
                        }
                    })
                }
            )
            duration = 300
        }

        // 3. EFECTO TRAGAR: El Pokémon se encoge hacia el centro de la pokebola y se torna rojo
        val swallow = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(pokemonView, "scaleX", 1f, 0f),
                ObjectAnimator.ofFloat(pokemonView, "scaleY", 1f, 0f),
                ObjectAnimator.ofFloat(pokemonView, "translationY", 0f, 100f), // Se mueve hacia la pokebola
                ObjectAnimator.ofFloat(pokemonView, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(viewLight, "scaleX", 1f, 0f),
                ObjectAnimator.ofFloat(viewLight, "scaleY", 1f, 0f)
            )
            duration = 600
            interpolator = AccelerateInterpolator()
        }

        // 4. Cerrar Pokebola
        val close = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(pbTop, "translationY", -40f, 0f),
                ObjectAnimator.ofFloat(pbBottom, "translationY", 40f, 0f),
                ObjectAnimator.ofFloat(pbCenter, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(pbCenter, "scaleY", 0f, 1f)
            )
            duration = 200
        }

        // 5. Agitación de captura
        val shake = ObjectAnimator.ofFloat(pbContainer, "rotation", 0f, -20f, 20f, -15f, 15f, 0f).setDuration(800)

        // 6. Revelación
        val reveal = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(pbContainer, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(pokemonView, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(pokemonView, "scaleY", 0f, 1f),
                ObjectAnimator.ofFloat(pokemonView, "translationY", 100f, 0f),
                ObjectAnimator.ofFloat(pokemonView, "alpha", 0f, 1f)
            )
            duration = 500
            startDelay = 200
        }

        shake.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                viewLight.visibility = View.GONE
                updateImage()
            }
        })

        val finalSequence = AnimatorSet()
        finalSequence.playSequentially(launch, open, swallow, close, shake, reveal)
        finalSequence.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                pbContainer.visibility = View.INVISIBLE
            }
        })
        finalSequence.start()
    }
}