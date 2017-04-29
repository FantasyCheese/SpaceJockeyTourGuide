package org.descinerds.spacejockeytourguide

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_journey_detail.*
import org.jetbrains.anko.act
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity

class JourneyDetailActivity : AppCompatActivity() {

    private lateinit var path: String

    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey_detail)

        path = intent.getStringExtra(Keys.PATH)

        listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(data: DataSnapshot) {
                val journey = data.getValue(Journey::class.java)
                updateUI(journey)
            }
        }
        App.db.getReference(path).addValueEventListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.db.getReference(path).removeEventListener(listener?:return)
    }

    private fun updateUI(journey: Journey) {
        list_image.layoutManager = LinearLayoutManager(this)
        list_image.adapter = ImageAdapter("$path/tour", journey.tour)

        navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    list_image.adapter = ImageAdapter("$path/tour", journey.tour)
                }
                R.id.navigation_dashboard -> {
                    list_image.adapter = ImageAdapter("$path/adventure", journey.adventure)
                }
                R.id.navigation_notifications -> {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    val elements = mutableListOf<Element>()
                    elements.addAll(journey.tour)
                    elements.addAll(journey.adventure)
                    val userElements = elements.filter { it.recordings?.containsKey(userId)?:false }

                    list_image.adapter = ImageAdapter("$path/achievement", userElements)
                }
                else -> return@OnNavigationItemSelectedListener false
            }
            true
        })
    }

    inner class ImageAdapter(val path:String, val elements: List<Element>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val element = elements[position]
            val vh = holder as ViewHolder

            Glide.with(holder.itemView.context)
                    .load(element.image_url).into(vh.imageView)
            holder.itemView.onClick {
                act.startActivity<RecorderActivity>(Keys.IMAGE to element.image_url, Keys.PATH to "$path/$position")
            }

            vh.textDescription.text = when {
                path.contains("tour") -> "Click to provide your description."
                path.contains("adventure") -> "Click to provide environmental sounds."
                path.contains("achievement") -> {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
                    val likeCount = element.recordings[userId]?.like
                    "$likeCount people recommend this."
                }
                else -> {
                    vh.textDescription.visibility = View.GONE
                    null
                }
            }
        }

        override fun getItemCount() = elements.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.journey_element, parent, false))

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView = (itemView as ViewGroup).find<ImageView>(R.id.image_view)
            val textDescription = (itemView as ViewGroup).find<TextView>(R.id.text_description)
        }
    }
}
