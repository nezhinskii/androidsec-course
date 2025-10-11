package com.google.firebase.example.makeitso.data.injection

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.firebase.example.makeitso.R

@Module
@InstallIn(SingletonComponent::class)
object FirebaseHiltModule {
    @Provides fun auth(): FirebaseAuth = Firebase.auth

    @Provides fun firestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    fun provideWebClientId(@ApplicationContext context: Context): String =
        context.getString(R.string.default_web_client_id)
}