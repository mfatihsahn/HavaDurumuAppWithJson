package com.example.havadurumujson

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(),AdapterView.OnItemSelectedListener {
    var location:SimpleLocation?=null
    var tvSehir: TextView? = null
    var latitude: String? = null
    var longitude: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sehirler, R.layout.spinner_tek_satir)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnSehirler.setTitle("Şehir Seçin")
        spnSehirler.setPositiveButton("SEÇ")
        spnSehirler.adapter = spinnerAdapter
        spnSehirler.setOnItemSelectedListener(this)

       spnSehirler.setSelection(1)


verileriGetir("Ankara")

    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {



        if (position == 0) {

            location = SimpleLocation(this)

            if(!location!!.hasLocationEnabled()){

                spnSehirler.setSelection(1)
                Toast.makeText(this, "GPS Aç ki yerini bulalım", Toast.LENGTH_LONG).show()
                SimpleLocation.openSettings(this)
            }else{

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),60)
                }else{

                    location = SimpleLocation(this)
                    latitude = String.format("%.6f", location?.latitude)
                    longitude = String.format("%.6f", location?.longitude)
                    Log.e("LAT", "" + latitude)
                    Log.e("LONG", "" + longitude)

                    oankiSehriGetir(latitude, longitude)
                }


            }

        } else {
            var secilenSehir = parent?.getItemAtPosition(position).toString()
            tvSehir = view as TextView
            verileriGetir(secilenSehir)
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == 60){

            if(grantResults.size > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                location = SimpleLocation(this)
                latitude = String.format("%.6f", location?.latitude)
                longitude = String.format("%.6f", location?.longitude)
                Log.e("LAT", "" + latitude)
                Log.e("LONG", "" + longitude)

                oankiSehriGetir(latitude, longitude)

            }else {
                spnSehirler.setSelection(1)
                Toast.makeText(this, "İzin vereydin de konumunu bulaydık ", Toast.LENGTH_LONG).show()

            }


        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun oankiSehriGetir(lat: String?, longt: String?){
        var cityname:String?=null
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lat="+ lat +"&lon="+ longt +"&appid=5ff5f6121eced2f3ad373070cbbb2040&lang=tr&units=metric"
        val HavaDurumuObjeRequest2 = JsonObjectRequest(
            Request.Method.GET,url, null, object : Response.Listener<JSONObject> {

                override fun onResponse(response: JSONObject?) {
                    var main = response?.getJSONObject("main")

                    var sicaklik = main?.getInt("temp")
                    tvSicaklik.text=sicaklik.toString()


                    cityname=response?.getString("name")
                    tvSehir?.setText(cityname)
                    var weather = response?.getJSONArray("weather")
                    var info =weather?.getJSONObject(0)?.getString("description")
                    tvAciklama.text=info
                    var icon =weather?.getJSONObject(0)?.getString("description")


                    if (icon?.last() == 'd') {
                        rootLayout.background = getDrawable(R.drawable.bg)
                        tvAciklama.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                        tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                        spnSehirler.background.setColorFilter(resources.getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
                        tvTarih.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                        tvDerece.setTextColor(resources.getColor(R.color.colorPrimaryDark))

                    } else {
                        rootLayout.background = getDrawable(R.drawable.gece)
                        tvAciklama.setTextColor(resources.getColor(R.color.colorPrimary))
                        tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimary))
                        spnSehirler.background.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
                        tvTarih.setTextColor(resources.getColor(R.color.colorPrimary))
                        tvDerece.setTextColor(resources.getColor(R.color.colorPrimary))

                    }


                    var resimDosyaAdi = resources.getIdentifier("icon_" + icon?.sonKarakteriSil(), "drawable", packageName) //R.drawable.icon_50n
                    imgHavaDurumu.setImageResource(resimDosyaAdi)
                    tvTarih.text = tarihYazdir()
                    //Log.e("fatih", sicaklik+" -- "+cityname+"---" +info+"--"+icon)



                    //Toast.makeText(this@MainActivity, response.toString(), Toast.LENGTH_LONG).show()
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {

                }

            })

        MySingleton.getInstance(this)?.addToRequestQueue(HavaDurumuObjeRequest2)


    }


    override fun onResume() {
        super.onResume()
        location?.beginUpdates()
    }
    override fun onPause() {
        super.onPause()
        location?.endUpdates()
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

 fun verileriGetir(sehir:String){

        val url =
            "https://api.openweathermap.org/data/2.5/weather?q="+ sehir +"&appid=5ff5f6121eced2f3ad373070cbbb2040&lang=tr&units=metric"
        val HavaDurumuObjeRequest = JsonObjectRequest(
            Request.Method.GET,url, null, object : Response.Listener<JSONObject> {

                override fun onResponse(response: JSONObject?) {
                    var main = response?.getJSONObject("main")

                    var sicaklik = main?.getInt("temp")
                    tvSicaklik.text=sicaklik.toString()


                    var cityname=response?.getString("name")

                    var weather = response?.getJSONArray("weather")
                    var info =weather?.getJSONObject(0)?.getString("description")
                    tvAciklama.text=info
                    var icon =weather?.getJSONObject(0)?.getString("description")


                    if (icon?.last() == 'd') {
                        rootLayout.background = getDrawable(R.drawable.bg)
                        tvAciklama.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                        tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                        spnSehirler.background.setColorFilter(resources.getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
                        tvTarih.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                        tvDerece.setTextColor(resources.getColor(R.color.colorPrimaryDark))

                    } else {
                        rootLayout.background = getDrawable(R.drawable.gece)
                        tvAciklama.setTextColor(resources.getColor(R.color.colorPrimary))
                        tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimary))
                        spnSehirler.background.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
                        tvTarih.setTextColor(resources.getColor(R.color.colorPrimary))
                        tvDerece.setTextColor(resources.getColor(R.color.colorPrimary))

                    }


                    var resimDosyaAdi = resources.getIdentifier("icon_" + icon?.sonKarakteriSil(), "drawable", packageName) //R.drawable.icon_50n
                    imgHavaDurumu.setImageResource(resimDosyaAdi)
                    tvTarih.text = tarihYazdir()
                    //Log.e("fatih", sicaklik+" -- "+cityname+"---" +info+"--"+icon)



                    //Toast.makeText(this@MainActivity, response.toString(), Toast.LENGTH_LONG).show()
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {

                }

            })

        MySingleton.getInstance(this)?.addToRequestQueue(HavaDurumuObjeRequest)
    }
    fun tarihYazdir(): String {

        var takvim = Calendar.getInstance().time
        var formatlayici = SimpleDateFormat("EEE, MMM yyyy", Locale("tr"))
        var tarih = formatlayici.format(takvim)

        return tarih


    }
}
private fun String.sonKarakteriSil(): String {
    //50n ifadeyi 50 olarak geriye yollar
    return  this.substring(0,this.length-1)


}

