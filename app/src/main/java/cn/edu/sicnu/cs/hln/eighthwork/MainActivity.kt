
package cn.edu.sicnu.cs.hln.eighthwork

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val cityList = mutableListOf<City>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 添加浅灰色分割线
        val dividerItemDecoration = DividerItemDecoration(
            this,
            LinearLayoutManager.VERTICAL
        )

        val divider = resources.getDrawable(
            R.drawable.divider_shape,
            theme
        )

        dividerItemDecoration.setDrawable(divider)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // 开启子线程读取本地城市 JSON
        Thread {
            try {
                val jsonString = readFileFromRaw(R.raw.citycode)

                // 使用 Gson 解析 JSON
                val type = object : TypeToken<List<City>>() {}.type

                val allCities: List<City> =
                    Gson().fromJson(jsonString, type)

                // 过滤有效城市并排序
                val validCities = allCities
                    .filter { it.city_code.isNotBlank() }
                    .sortedBy { it.city_name }

                // 返回主线程更新界面
                runOnUiThread {

                    cityList.clear()
                    cityList.addAll(validCities)

                    recyclerView.adapter = CityAdapter(cityList) { selectedCity ->

                        if (selectedCity.city_code.isBlank()) {
                            Log.e("CityError", "城市代码为空")
                            return@CityAdapter
                        }

                        // 跳转天气详情页面
                        val intent = Intent(
                            this,
                            WeatherDetailActivity::class.java
                        )

                        intent.putExtra(
                            "city_code",
                            selectedCity.city_code
                        )

                        intent.putExtra(
                            "city_name",
                            selectedCity.city_name
                        )

                        startActivity(intent)
                    }
                }

            } catch (e: Exception) {

                Log.e("CityError", "城市列表加载失败", e)

                runOnUiThread {
                    android.widget.Toast.makeText(
                        this,
                        "城市数据加载失败，请检查 JSON 文件",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }

        }.start()
    }

    // 读取 res/raw 目录中的 JSON 文件
    private fun readFileFromRaw(rawName: Int): String {

        return BufferedReader(
            InputStreamReader(resources.openRawResource(rawName))
        ).use { reader ->
            reader.readText()
        }
    }
}
