package org.descinerds.spacejockeytourguide

import android.app.Application
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus

class App : Application() {
    companion object {
        lateinit var bus: EventBus
        lateinit var db: FirebaseDatabase
    }

    override fun onCreate() {
        super.onCreate()
        bus = EventBus.getDefault()
        db = FirebaseDatabase.getInstance()
    }
}
