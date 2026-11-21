
package cn.edu.sicnu.cs.hln.eighthwork

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.android.volley.Request
import com.android.volley.DefaultRetryPolicy
import com.android.volley.VolleyError
import com.android.volley.TimeoutError
import com.android.volley.NoConnectionError
import com.android.volley.NetworkError
import com.android.volley.ServerError
import com.android.volley.AuthFailureError
import com.android.volley.ParseError

import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import org.json.JSONObject

class WeatherDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WeatherError"
        private const val BASE_URL =
            "http://t.weather.itboy.net/api/weather/city/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_detail)

        // 1. 初始化 Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 2. 初始化控件
        val tvCity = findViewById<TextView>(R.id.tvCity)
        val tvWeather = findViewById<TextView>(R.id.tvWeather)
        val tvTemp = findViewById<TextView>(R.id.tvTemp)
        val tvDetail = findViewById<TextView>(R.id.tvDetail)
        val ivWeatherIcon =
            findViewById<ImageView>(R.id.ivWeatherIcon)

        // 3. 获取主页面传递的数据
        val cityCode = intent.getStringExtra("city_code") ?: ""
        val cityName = intent.getStringExtra("city_name") ?: ""

        tvCity.text = cityName

        // 4. 检查城市代码
        if (cityCode.isBlank()) {
            tvWeather.text = "城市代码为空，无法获取天气"
            tvTemp.text = ""
            Log.e(TAG, "接收到的城市代码为空")
            return
        }

        Log.d(TAG, "当前城市：$cityName")
        Log.d(TAG, "当前城市代码：$cityCode")

        // 5. 显示加载状态
        tvWeather.text = "正在获取天气信息..."
        tvTemp.text = ""

        // 6. 创建 Volley 请求队列
        val queue = Volley.newRequestQueue(applicationContext)

        // 注意：这里必须是普通 URL 字符串
        val url = BASE_URL + cityCode

        Log.d(TAG, "请求地址：$url")

        // 7. 创建网络请求
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,

            // 请求成功回调
            { response ->

                Log.d(TAG, "服务器返回：$response")

                try {
                    // 解析 JSON
                    val jsonObject = JSONObject(response)

                    // 检查服务器状态
                    val status = jsonObject.optString("status")

                    if (status.isNotEmpty() && status != "200") {
                        val message = jsonObject.optString("message")

                        Log.e(
                            TAG,
                            "接口返回异常：status=$status, message=$message"
                        )

                        tvWeather.text = "天气接口返回异常：$message"
                        tvTemp.text = ""
                        return@StringRequest
                    }

                    // 获取 data 对象
                    val dataObj = jsonObject.optJSONObject("data")

                    if (dataObj == null) {
                        Log.e(TAG, "JSON 中不存在 data 对象")
                        tvWeather.text = "天气数据格式异常"
                        return@StringRequest
                    }

                    // 获取当前温度
                    val wendu = dataObj.optString("wendu", "")

                    // 天气类型在 data.forecast[0].type（注意不在 data 顶层）
                    val forecastArray = dataObj.optJSONArray("forecast")
                    val todayForecast =
                        if (forecastArray != null && forecastArray.length() > 0)
                            forecastArray.optJSONObject(0)
                        else null

                    val type = todayForecast?.optString("type", "") ?: ""
                    val high = todayForecast?.optString("high", "") ?: ""
                    val low = todayForecast?.optString("low", "") ?: ""
                    val shidu = dataObj.optString("shidu", "")
                    val quality = dataObj.optString("quality", "")

                    if (wendu.isBlank() || type.isBlank()) {
                        Log.e(TAG, "天气或温度字段为空")
                        tvWeather.text = "天气数据不完整"
                        tvTemp.text = ""
                        tvDetail.text = ""
                        return@StringRequest
                    }

                    // 更新界面
                    tvTemp.text = "温度：${wendu}℃"
                    tvWeather.text = "天气：$type"

                    // 拼接详细信息
                    val detail = StringBuilder()
                    if (high.isNotBlank() || low.isNotBlank()) {
                        detail.append("今日：$low ~ $high")
                    }
                    if (shidu.isNotBlank()) {
                        if (detail.isNotEmpty()) detail.append("\n")
                        detail.append("湿度：$shidu")
                    }
                    if (quality.isNotBlank()) {
                        if (detail.isNotEmpty()) detail.append("\n")
                        detail.append("空气质量：$quality")
                    }
                    tvDetail.text = detail.toString()

                    // 更新天气图标
                    updateIcon(type, ivWeatherIcon)

                    Log.d(
                        TAG,
                        "天气获取成功：城市=$cityName，天气=$type，温度=$wendu"
                    )

                } catch (e: Exception) {

                    Log.e(TAG, "JSON 数据解析失败", e)

                    tvWeather.text = "数据解析错误"
                    tvTemp.text = ""
                }
            },

            // 请求失败回调
            { error ->

                // 获取详细错误信息
                val errorMessage = getVolleyErrorMessage(error)

                Log.e(TAG, "网络请求失败：$errorMessage", error)

                val networkResponse = error.networkResponse

                if (networkResponse != null) {

                    val statusCode = networkResponse.statusCode

                    val responseBody = try {
                        String(
                            networkResponse.data,
                            Charsets.UTF_8
                        )
                    } catch (e: Exception) {
                        "无法读取错误响应"
                    }

                    Log.e(
                        TAG,
                        "HTTP状态码：$statusCode，服务器响应：$responseBody"
                    )
                }

                tvWeather.text = errorMessage
                tvTemp.text = ""
            }
        )

        // 8. 设置网络请求超时时间
        stringRequest.retryPolicy = DefaultRetryPolicy(
            15000, // 超时时间：15秒
            1,     // 最大重试次数
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // 9. 加入请求队列
        queue.add(stringRequest)
    }

    // 获取 Volley 错误的详细信息
    private fun getVolleyErrorMessage(error: VolleyError): String {

        return when (error) {

            is TimeoutError ->
                "网络请求超时，请检查网络"

            is NoConnectionError ->
                "无法连接网络，请检查网络连接"

            is AuthFailureError ->
                "服务器身份验证失败"

            is ServerError ->
                "服务器错误，请稍后重试"

            is NetworkError ->
                "网络连接异常"

            is ParseError ->
                "服务器返回的数据解析失败"

            else ->
                "网络请求失败：${error.message ?: error.toString()}"
        }
    }

    // 根据天气类型更新图标
    private fun updateIcon(
        weatherType: String,
        imageView: ImageView
    ) {

        when {

            weatherType.contains("晴") ->
                imageView.setImageResource(R.drawable.sun)

            weatherType.contains("多云") ->
                imageView.setImageResource(R.drawable.mcloud)

            weatherType.contains("阴") ||
                    weatherType.contains("云") ->
                imageView.setImageResource(R.drawable.cloud)

            weatherType.contains("雨") ->
                imageView.setImageResource(R.drawable.rain)

            weatherType.contains("雪") ->
                imageView.setImageResource(R.drawable.snow)

            weatherType.contains("雷") ->
                imageView.setImageResource(R.drawable.thunder)

            else ->
                imageView.setImageResource(R.drawable.cloud)
        }
    }
}
