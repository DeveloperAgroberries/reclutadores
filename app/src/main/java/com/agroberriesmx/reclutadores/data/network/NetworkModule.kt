package com.agroberriesmx.reclutadores.data.network

import android.content.Context
import com.agroberriesmx.reclutadores.BuildConfig.BASE_URL
import com.agroberriesmx.reclutadores.data.CredentialsRepositoryImpl
import com.agroberriesmx.reclutadores.data.RepositoryImpl
import com.agroberriesmx.reclutadores.data.core.interceptor.AuthInterceptor
import com.agroberriesmx.reclutadores.data.local.DatabaseHelper
import com.agroberriesmx.reclutadores.domain.CredentialsRepository
import com.agroberriesmx.reclutadores.domain.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient
            .Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideReclutadoresApiService(retrofit: Retrofit):ReclutadoresApiService {
        return retrofit.create(ReclutadoresApiService::class.java)
    }
    
    // 2. MODIFICA esta función agregando el parámetro dbHelper
    @Provides
    @Singleton
    fun provideReclutadoresRepository(
        reclutadoresApiService: ReclutadoresApiService,
        dbHelper: DatabaseHelper // 👈 Hilt lo sacará de la función de arriba
    ): Repository {
        // 👈 Ahora pasamos ambos al constructor
        return RepositoryImpl(reclutadoresApiService, dbHelper)
    }

    // ⭐⭐⭐ ¡AÑADE ESTA FUNCIÓN PARA RESOLVER EL ERROR DE MissingBinding! ⭐⭐⭐
    @Provides
    @Singleton
    fun provideCredentialsRepository(
        // Hilt proveerá CredentialsRepositoryImpl, y este método lo devuelve como la interfaz.
        credentialsRepositoryImpl: CredentialsRepositoryImpl
    ): CredentialsRepository {
        return credentialsRepositoryImpl
    }

}