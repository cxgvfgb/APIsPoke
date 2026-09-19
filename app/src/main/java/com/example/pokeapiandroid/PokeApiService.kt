package com.example.pokeapiandroid

import retrofit2.http.GET

interface PokeApiService {
    @GET("pokemon/mewtwo")
    suspend fun getMewtwo(): PokemonResponse

    @GET("pokemon/pikachu")
    suspend fun getPikachu(): PokemonResponse

    @GET("pokemon/bulbasaur")
    suspend fun getBulbasaur(): PokemonResponse

    @GET("pokemon/charmander")
    suspend fun getCharmander(): PokemonResponse

    @GET("pokemon/squirtle")
    suspend fun getSquirtle(): PokemonResponse

    companion object{
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}