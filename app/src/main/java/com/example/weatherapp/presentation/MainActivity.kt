package com.example.weatherapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.weatherapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.example.weatherapp.presentation.theme.WeatherAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationUtil: LocationUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("Permission", "No Permission Pls Check")
        }
        locationUtil = LocationUtil()
        locationUtil.createLocationRequest(this, fusedLocationClient)

        setContent {
            WearApp(locationUtil)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun WearApp(locationUtil: LocationUtil) {

        val listState = rememberScalingLazyListState()

        WeatherAppTheme {
            val locationPermissionsState = rememberMultiplePermissionsState(
                listOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )

            val contentModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            val iconModifier = Modifier
                .size(24.dp)
                .wrapContentSize(align = Alignment.Center)

            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 32.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.Bottom,
                state = listState,
//            autoCentering = true
            ) {
                item { Spacer(modifier = Modifier.size(20.dp)) }

                if (locationPermissionsState.allPermissionsGranted) {
                    if (!locationUtil.dataLoaded.value)

                        item {
                            TextWidget(
                                contentModifier,
                                "Location obtained"
                            )
                        }
                    else {
                        item {
                            CardWidget(
                                modifier = contentModifier,
                                title = locationUtil.data.value.name,
                                weatherDescription = locationUtil.data.value.weatherDescription,
                                time = locationUtil.data.value.time,
                                temperature = locationUtil.data.value.temp,
                                wind = locationUtil.data.value.wind,
                                visibility = locationUtil.data.value.visibility,
                                clouds = locationUtil.data.value.clouds
                            )
                        }
                    }
                } else {
                    val allPermissionsRevoked =
                        locationPermissionsState.permissions.size ==
                                locationPermissionsState.revokedPermissions.size


                    val textToShow = if (!allPermissionsRevoked) {
                        "Location obtained. "
                    } else if (locationPermissionsState.shouldShowRationale) {
                        "Getting your exact location is important for this app. " +
                                "Please grant us fine location."
                    } else {
                        "This feature requires location permission."
                    }



                    item { TextWidget(contentModifier, textToShow) }
                    item {
                        ButtonWidget(contentModifier, iconModifier) {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    }
                }

            }
        }
    }

    @Composable
    fun ButtonWidget(
        modifier: Modifier = Modifier,
        iconModifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            // Button
            Button(
                modifier = Modifier.size(ButtonDefaults.LargeButtonSize),
                onClick = { onClick() },
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "triggers phone action",
                    modifier = iconModifier
                )
            }
        }
    }

    @Composable
    fun TextWidget(modifier: Modifier = Modifier, text: String) {
        Text(
            modifier = modifier,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = text
        )
    }

    @Composable
    fun CardWidget(
        modifier: Modifier = Modifier,
        title: String,
        weatherDescription: String,
        time: String,
        temperature: Double,
        wind: Double,
        visibility: Int,
        clouds: Int,
    ) {
        AppCard(
            modifier = modifier,
            appName = { Text("Weather Details", color = Color.White) },
            time = { Text(time, color = if (temperature < 26) Color.White else Color.Red) },
            title = { Text(title, color = Color.Cyan) },
            onClick = {}
        ) {

            val icon = if (temperature < 26.0) R.mipmap.cold else R.mipmap.hot
            Row(horizontalArrangement = Arrangement.Center) {
                Image(
                    modifier = Modifier.height(20.dp),
                    painter = painterResource(id = icon),
                    contentDescription = "",
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(weatherDescription)
            }
            Row(horizontalArrangement = Arrangement.Center) {

                Image(
                    modifier = Modifier.height(20.dp),
                    painter = painterResource(id = R.mipmap.air),
                    contentDescription = "",
                )

                Spacer(modifier = Modifier.size(5.dp))
                Text("$wind m/s")

            }
            Row(horizontalArrangement = Arrangement.Center) {

                Image(
                    modifier = Modifier.height(20.dp),
                    painter = painterResource(id = R.mipmap.visibility),
                    contentDescription = "",
                )

                Spacer(modifier = Modifier.size(5.dp))
                Text("$visibility m")

            }
            Row(horizontalArrangement = Arrangement.Center) {

                Image(
                    modifier = Modifier.height(20.dp),
                    painter = painterResource(id = R.mipmap.cloud),
                    contentDescription = "",
                )

                Spacer(modifier = Modifier.size(5.dp))
                Text("$clouds %")

            }
        }
    }
}