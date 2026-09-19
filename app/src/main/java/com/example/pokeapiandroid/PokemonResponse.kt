package com.example.pokeapiandroid

import com.google.gson.annotations.SerializedName

data class PokemonResponse(
    @SerializedName("name") val name: String,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int,
    @SerializedName("sprites") val sprites: SpriteResponse,
    @SerializedName("types") val types: List<TypeSlotResponse>,
)

data class SpriteResponse(
    @SerializedName("front_default") val frontDefault: String?
)

data class TypeSlotResponse(
    @SerializedName("slot") val slot: Int,
    @SerializedName("type") val type: TypeResponse
)

data class TypeResponse(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)
