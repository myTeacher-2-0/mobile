package com.crw.myteacher.utils

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.RecaptchaAction

class MyTeacherApplication: Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
    }


    fun executeRecaptcha(action: RecaptchaAction): Task<String> =
        Recaptcha.fetchTaskClient(this@MyTeacherApplication, SITE_KEY)
            .onSuccessTask { client ->
                Log.d(TAG, "Recaptcha client initialized successfully")
                client.executeTask(action)
            }


    companion object {
        private const val TAG = "MyTeacherApplication"
        private const val SITE_KEY = "6LcL6RAtAAAAAIf2PbZa7_pKj9ETt6j7hBMTmhY7"

        lateinit var instance: MyTeacherApplication
            private set
    }
}