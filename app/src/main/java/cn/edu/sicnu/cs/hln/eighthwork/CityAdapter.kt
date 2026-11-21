package cn.edu.sicnu.cs.hln.eighthwork // 改包名

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CityAdapter(
    private val cityList: List<City>,
    private val onItemClick: (City) -> Unit // 点击事件回调
) : RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCityName: TextView = view.findViewById(R.id.tvCityName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = cityList[position]
        holder.tvCityName.text = city.city_name
        // 点击事件
        holder.itemView.setOnClickListener {
            onItemClick(city)
        }
    }

    override fun getItemCount() = cityList.size
}