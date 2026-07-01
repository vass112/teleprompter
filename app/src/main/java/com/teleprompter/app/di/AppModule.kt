package com.teleprompter.app.di

import android.content.Context
import com.teleprompter.app.data.db.AppDatabase
import com.teleprompter.app.data.db.ScriptDao
import com.teleprompter.app.data.repository.ScriptRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideScriptDao(database: AppDatabase): ScriptDao {
        return database.scriptDao()
    }

    @Provides
    @Singleton
    fun provideScriptRepository(scriptDao: ScriptDao): ScriptRepository {
        return ScriptRepository(scriptDao)
    }
}
