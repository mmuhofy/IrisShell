package com.iris.irisshell.data.di

import com.iris.irisshell.data.settings.FirstLaunchRepositoryImpl
import com.iris.irisshell.data.settings.TerminalFontSizeRepositoryImpl
import com.iris.irisshell.data.terminal.BootstrapObserver
import com.iris.irisshell.data.terminal.TriggerBootstrap
import com.iris.irisshell.domain.terminal.ObserveBootstrapUseCase
import com.iris.irisshell.domain.terminal.ObserveFirstLaunchUseCase
import com.iris.irisshell.domain.terminal.SetTerminalFontSizeUseCase
import com.iris.irisshell.domain.terminal.TriggerBootstrapUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt bindings that wire `:data` implementations into the domain interfaces
 * consumed by `:ui`.
 *
 * Per AGENT.md §119-121 the data layer implements interfaces declared in
 * `:domain`. This module is the seam.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindObserveBootstrap(
        impl: BootstrapObserver,
    ): ObserveBootstrapUseCase

    @Binds
    @Singleton
    abstract fun bindTriggerBootstrap(
        impl: TriggerBootstrap,
    ): TriggerBootstrapUseCase

    @Binds
    @Singleton
    abstract fun bindObserveFirstLaunch(
        impl: FirstLaunchRepositoryImpl,
    ): ObserveFirstLaunchUseCase

    @Binds
    @Singleton
    abstract fun bindSetTerminalFontSize(
        impl: TerminalFontSizeRepositoryImpl,
    ): SetTerminalFontSizeUseCase
}
