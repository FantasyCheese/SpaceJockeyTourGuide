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
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_journey_detail.*
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick

class JourneyDetailActivity : AppCompatActivity() {

    val tourImages = arrayOf(
            "https://cdn.pixabay.com/photo/2015/07/06/13/58/arlberg-pass-833326_960_720.jpg",
            "https://cdn.pixabay.com/photo/2015/02/18/11/50/mountain-landscape-640617_960_720.jpg",
            "http://allswalls.com/?module=images&act=downloadResize&file=landscape-photos-wallpaper-4.jpg&x=3840x2400&y="
    )
    val adventureImages = arrayOf(
            "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcTz_fr_rlO2IuN7t53sQrXWlu9xA16HlV0K5yLNeA0tJwI1se7E",
            "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcQfQcj21cFDZ1QESOieaJaJH3yJoSHyT2ECJ1zYUOM4hHE__CcmRA",
            "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcTOzAAIuzU5TRZW6JsSo_5Yd6Ndp2yf-4oQqNw53ivfZTcEq_KMtg"
    )

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                list_image.adapter = ImageAdapter(tourImages)
            }
            R.id.navigation_dashboard -> {
                list_image.adapter = ImageAdapter(adventureImages)
            }
            R.id.navigation_notifications -> {

            }
            else -> return@OnNavigationItemSelectedListener false
        }
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey_detail)

        list_image.layoutManager = LinearLayoutManager(this)
        list_image.adapter = ImageAdapter(tourImages)

        val navigation = findViewById(R.id.navigation) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    class ImageAdapter(val images: Array<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            Glide.with(holder.itemView.context)
                    .load(images[position]).into((holder as ViewHolder).imageView)
            holder.itemView.onClick {

            }
        }

        override fun getItemCount() = images.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.journey_element, parent, false))

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView = (itemView as ViewGroup).find<ImageView>(R.id.image_view)
        }
    }
}
