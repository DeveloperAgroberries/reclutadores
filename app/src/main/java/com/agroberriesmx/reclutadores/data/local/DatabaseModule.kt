package com.agroberriesmx.reclutadores.data.local

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabaseHelper(@ApplicationContext context: Context): DatabaseHelper{
        return DatabaseHelper(context)
    }

    @Provides
    @Singleton
    fun provideCombustiblesLocalDBService(databaseHelper: DatabaseHelper): ReclutadoresLocalDBService {
        return ReclutadoresLocalDBServiceImpl(databaseHelper)
    }

    /*@Provides
    @Singleton
    fun provideRecordsRepository(localDBService: ReclutadoresLocalDBService): RecordsRepository {
        return RecordsRepositoryImpl(localDBService)
    }*/
}