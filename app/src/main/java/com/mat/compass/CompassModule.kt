package com.mat.compass

import com.mat.compass.data.AzimuthProvider
import com.mat.compass.data.CoordsDataStore
import com.mat.compass.data.LocationManager
import com.mat.compass.data.Repository
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val compassModule = module {
    single { LocationManager(androidContext()) }
    single { AzimuthProvider(androidContext()) }
    single { CoordsDataStore(androidContext()) }
    single { Repository(get(), get()) }

    viewModel { CompassViewModel(get(), get()) }
}
