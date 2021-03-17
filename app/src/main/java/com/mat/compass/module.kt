package com.mat.compass

import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { LocationManager(androidContext())}
    single { AzimuthProvider(androidContext()) }
    single { Repository(get(), get()) }

    viewModel { CompassViewModel(get()) }
}