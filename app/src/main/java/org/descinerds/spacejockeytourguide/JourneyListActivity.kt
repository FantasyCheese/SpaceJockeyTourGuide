package org.descinerds.spacejockeytourguide

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_journey_list.*
import kotlinx.android.synthetic.main.fragment_journey_list.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.act

class JourneyListActivity : AppCompatActivity() {
    val path = "journey"

    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey_list)
        setSupportActionBar(toolbar)

        listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                container.adapter = JourneyPagerAdapter(supportFragmentManager, snapshot.childrenCount.toInt())
            }
        }
        App.db.getReference(path).addValueEventListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.db.getReference(path).removeEventListener(listener?:return)
    }

    class PlaceholderFragment : Fragment() {

        private lateinit var path: String

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View
                = inflater!!.inflate(R.layout.fragment_journey_list, container, false)

        private var listener: ValueEventListener? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            path = arguments.getString(Keys.PATH)

            listener = object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onDataChange(data: DataSnapshot) {
                    val journey = data.getValue(Journey::class.java)
                    text_journey_name.text = journey.name
                    text_journey_name.onClick {
                        act.startActivity<JourneyDetailActivity>(Keys.PATH to path)
                    }
                    Glide.with(act).load(journey.image).centerCrop().into(background)
                }
            }
            App.db.getReference(path).addValueEventListener(listener)
        }

        override fun onDestroyView() {
            super.onDestroyView()
            App.db.getReference(path).removeEventListener(listener?:return)
        }

        companion object {
            fun newInstance(path: String) = PlaceholderFragment().apply {
                arguments = bundleOf(Keys.PATH to path)
            }
        }
    }

    inner class JourneyPagerAdapter(fm: FragmentManager, val journeyCount: Int) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int)
                = PlaceholderFragment.newInstance("$path/$position")

        override fun getCount() = journeyCount
    }
}
