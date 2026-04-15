package com.vocallock.core.di

import androidx.room.Room
import com.vocallock.core.permission.PermissionManager
import com.vocallock.data.database.VocalLockDatabase
import com.vocallock.data.datastore.globalSettingsDataStore
import com.vocallock.data.provider.AppIconProvider
import com.vocallock.data.repository.NudgeRepositoryImpl
import com.vocallock.domain.repository.NudgeRepository
import com.vocallock.domain.usecase.ShouldShowNudgeUseCase
import com.vocallock.domain.usecase.SnoozeNudgeUseCase
import com.vocallock.domain.usecase.TrackInheritedUsageUseCase
import com.vocallock.domain.usecase.audio.VerifyVoicePhraseUseCase
import com.vocallock.feature.detail.DetailViewModel
import com.vocallock.feature.group.GroupContentsViewModel
import com.vocallock.feature.home.HomeViewModel
import com.vocallock.feature.selector.AppSelectorViewModel
import com.vocallock.feature.settings.SettingsViewModel
import com.vocallock.feature.splash.SplashViewModel
import com.vocallock.service.NudgeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // ── Database & DAOs ──────────────────────────────────────────
    single {
        Room.databaseBuilder(
            androidContext(),
            VocalLockDatabase::class.java,
            "vocal_lock_db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { get<VocalLockDatabase>().appDao() }
    single { get<VocalLockDatabase>().groupDao() }

    // ── Providers & Managers ─────────────────────────────────────
    single { AppIconProvider(androidContext()) }
    single { PermissionManager(androidContext()) }

    // ── DataStore & Repositories ─────────────────────────────────
    single { androidContext().globalSettingsDataStore }
    single<NudgeRepository> { NudgeRepositoryImpl(androidContext()) }

    // ── UseCases (Business Logic) ────────────────────────────────
    factory { VerifyVoicePhraseUseCase() }
    factory { SnoozeNudgeUseCase(repo = get()) }

    // Added for NUDGES:
    factory { TrackInheritedUsageUseCase(repo = get()) }
    factory { ShouldShowNudgeUseCase(repo = get()) }

    // ── Services ─────────────────────────────────────────────────
    single { NudgeManager(androidContext()) }

    // ── ViewModels ───────────────────────────────────────────────
    viewModel { HomeViewModel(appDao = get(), groupDao = get(), globalSettings = get()) }
    viewModel { DetailViewModel(savedStateHandle = get(), appDao = get(), groupDao = get()) }
    viewModel {
        AppSelectorViewModel(
            savedStateHandle = get(),
            appDao = get(),
            iconProvider = get()
        )
    }
    viewModel { SettingsViewModel(permMgr = get(), globalSettings = get()) }
    viewModel { SplashViewModel(ctx = androidContext()) }
    viewModel { GroupContentsViewModel(savedStateHandle = get(), groupDao = get(), appDao = get()) }
}