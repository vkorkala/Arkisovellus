package com.example.bottomnavigation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Home : Fragment() {

    // Luodaan muuttujat
    private lateinit var tvSaa: TextView
    private lateinit var tvPaikkakunta: TextView
    private lateinit var imageViewWeather: ImageView
    // Käyttää sijaintipalveluja
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // HTTP-asiakas säädatan hakemiseen
    private val client = OkHttpClient()
    // API-avain OpenWeatherMap-palveluun
    private val openWeatherMapApiKey = "YOUR_API_KEY"

    // Oikeuksien pyytämiseen liittyvä tulosten käsittelijä
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Jos sijaintioikeudet myönnetty, haetaan sijainti- ja säädata
            fetchLocationAndWeatherData()
        } else {
            // Näytetään viesti, jos sijaintioikeuksia ei ole annettu
            Toast.makeText(context, "Sijaintioikeuksia ei myonnetty", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ladataan fragmentin asettelu
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Yhdistetään elementit
        tvSaa = view.findViewById(R.id.tvSaa)
        tvPaikkakunta = view.findViewById(R.id.tvPaikkakunta)
        imageViewWeather = view.findViewById(R.id.imageViewWeather)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Tarkistetaan, onko sijaintioikeudet myönnetty
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Jos ei ole, pyydetään oikeuksia
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Jos on, haetaan sijainti- ja säädata
            fetchLocationAndWeatherData()
        }

        return view
    }

    // Funktio, joka hakee sijaintitiedot ja säädatan
    private fun fetchLocationAndWeatherData() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Hakee viimeisimmän sijainnin
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                // Hakee säädatan sijainnin perusteella
                val latitude = it.latitude
                val longitude = it.longitude
                fetchWeatherData(latitude, longitude)
            } ?: run {
                // Näytetään viesti, jos sijaintia ei voitu hakea
                Toast.makeText(context, "Sijaintia ei voitu hakea", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Funktio, joka hakee säädatan sijainnin perusteella
    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$openWeatherMapApiKey&units=metric"

        val request = Request.Builder()
            .url(url)
            .build()

        // Lähetetään HTTP-pyyntö
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    // Näytetään viesti, jos säädatan haku epäonnistui
                    Toast.makeText(context, "Säädataa ei voitu hakea", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonData = responseBody.string()
                        val jsonObject = JSONObject(jsonData)
                        val main = jsonObject.getJSONObject("main")
                        val weatherArray = jsonObject.getJSONArray("weather")
                        val weather = weatherArray.getJSONObject(0)
                        val temperature = main.getDouble("temp")
                        val windSpeed = jsonObject.getJSONObject("wind").getDouble("speed")
                        val weatherIcon = weather.getString("icon")
                        val cityName = jsonObject.getString("name")

                        // Päivitetään käyttöliittymän tiedot
                        requireActivity().runOnUiThread {
                            tvSaa.text = "Lämpotila: $temperature °C\n Tuuli: $windSpeed m/s"
                            tvPaikkakunta.text = "$cityName"
                            // Hakee sääkuvan
                            fetchWeatherImage(weatherIcon)
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        // Näytetään viesti, jos säädatan haku epäonnistui
                        Toast.makeText(context, "Säädataa ei voitu hakea", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Hakee sääkuvan annetulla ikonilla
    private fun fetchWeatherImage(icon: String) {
        val url = "https://openweathermap.org/img/wn/$icon@2x.png"

        val request = Request.Builder()
            .url(url)
            .build()

        // Lähetetään HTTP-pyyntö kuvan hakemiseksi
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    // Näytetään viesti, jos kuvan haku epäonnistui
                    Toast.makeText(context, "Sääkuvaa ei voitu hakea", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Hakee kuvan
                    val inputStream = response.body?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    // Päivittää käyttöliittymän kuvan
                    requireActivity().runOnUiThread {
                        imageViewWeather.setImageBitmap(bitmap)
                    }
                }
            }
        })
    }
}
