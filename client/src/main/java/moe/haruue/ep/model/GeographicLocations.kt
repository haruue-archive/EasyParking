package moe.haruue.ep.model

import com.amap.api.location.AMapLocation
import com.amap.api.maps.model.LatLng
import moe.haruue.ep.common.model.GeographicLocation

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
fun GeographicLocation.toLatLng() = LatLng(latitude, longitude)

fun LatLng.toGeographicLocation() = GeographicLocation(latitude, longitude)

fun AMapLocation.toLatLng() = LatLng(latitude, longitude)