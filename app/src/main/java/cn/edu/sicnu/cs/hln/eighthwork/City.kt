package cn.edu.sicnu.cs.hln.eighthwork // 注意这里换成你自己的包名

data class City(
    val id: Int,
    val pid: Int,
    val city_code: String,
    val city_name: String,
    val post_code: String?,
    val area_code: String?,
    val ctime: String?
)