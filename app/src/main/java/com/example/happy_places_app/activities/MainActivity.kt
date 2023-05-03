package com.example.happy_places_app.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happy_places_app.adapters.HappyPlacesAdapter
import com.example.happy_places_app.database.DatabaseHandler
import com.example.happy_places_app.databinding.ActivityMainBinding
import com.example.happy_places_app.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)

        }
        getHappyPlacesListFromLocalDB()
    }

    private fun setupHappyPlacesRecyclerView(
        happyPlaceList: ArrayList<HappyPlaceModel>){
        binding!!.rvHappyPlacesList.layoutManager =
            LinearLayoutManager(this)
        binding!!.rvHappyPlacesList.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this,happyPlaceList)
        binding!!.rvHappyPlacesList.adapter = placesAdapter
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlaceList()

        if (getHappyPlaceList.size > 0){
               binding!!.rvHappyPlacesList.visibility = View.VISIBLE
               binding!!.tvNoRecordsAvailable.visibility = View.GONE
               setupHappyPlacesRecyclerView(getHappyPlaceList)
        }else{
            binding!!.rvHappyPlacesList.visibility = View.GONE
            binding!!.tvNoRecordsAvailable.visibility = View.VISIBLE
        }

    }
}