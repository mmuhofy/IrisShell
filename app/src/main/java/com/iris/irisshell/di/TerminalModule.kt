package com.iris.irisshell.di

import android.app.Application
import android.content.Context
import com.iris.irisshell.terminal.ProotRunner
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.TerminalSessionClientImpl
import com.iris.irisshell.terminal.TerminalViewClientImpl
import com.iris.irisshell.terminal.UbuntuBootstrap
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the terminal layer.
 *
 * Provides the singleton graph that Phase 1 Terminal Core functionality relies on:
 *  - `UbuntuBootstrap` (lifecycle for PRoot + Ubuntu rootfs)
 *  - `TerminalManager` (PTY session lifecycle, tab state)
 *
 * Per AGENT.md §125-128 the terminal layer is isolated — only `:app` (and `:agent`
 * later) interact with it. This module lives in `:app` because the inject graph
 * depends on `Application`.
 */
@Module
@InstallIn(SingletonComponent::class)
object TerminalModule {

    @Provides
    @Singleton
    fun provideUbuntuBootstrap(
        @ApplicationContext context: Context
    ): UbuntuBootstrap = UbuntuBootstrap(context)

    @Provides
    @Singleton
    fun provideTerminalManager(
        application: Application,
        ubuntuBootstrap: UbuntuBootstrap
    ): TerminalManager = TerminalManager(
        ubuntuBootstrap = ubuntuBootstrap,
        application = application
    )
}

