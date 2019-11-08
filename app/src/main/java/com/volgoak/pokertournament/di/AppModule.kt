package com.volgoak.pokertournament.di

import com.google.gson.GsonBuilder
import com.volgoak.pokertournament.data.DataProvider
import com.volgoak.pokertournament.data.TournamentRepository
import com.volgoak.pokertournament.screens.main.MainViewModel
import com.volgoak.pokertournament.screens.tournament.TournamentViewModel
import com.volgoak.pokertournament.service.ServiceStateRepository
import com.volgoak.pokertournament.utils.NotificationsCreator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ServiceStateRepository() }
    single { GsonBuilder().setPrettyPrinting().create() }
    single { DataProvider(get(), get()) }
    single { TournamentRepository() }
    single { NotificationsCreator()}
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get()) }
    viewModel { TournamentViewModel(get()) }
}