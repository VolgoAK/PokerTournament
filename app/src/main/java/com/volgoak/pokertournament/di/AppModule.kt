package com.volgoak.pokertournament.di

import com.volgoak.pokertournament.service.ServiceStateRepository
import org.koin.dsl.module

val appModule = module {
    single { ServiceStateRepository() }
}