package org.descinerds.spacejockeytourguide

import android.app.Application
import org.greenrobot.eventbus.EventBus

class App : Application() {
    companion object {
        lateinit var bus: EventBus
        val journeys = arrayOf("2017: A SPACE JOCKEY", "Star Trek: Born In Darkness", "Star Wars: Revenge of the NASA")
    }

    override fun onCreate() {
        super.onCreate()
        bus = EventBus.getDefault()
    }
}
