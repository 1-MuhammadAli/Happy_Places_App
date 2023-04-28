package com.example.happy_places_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happy_places_app.databinding.ActivityAddHappyPlaceBinding

class AddHappyPlaceActivity : AppCompatActivity() {
    //private var toolBarAddPlace : Toolbar? = null
    private var binding : ActivityAddHappyPlaceBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        //toolBarAddPlace = findViewById(R.id.toolbar_add_place)
        setSupportActionBar(binding!!.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding!!.toolbarAddPlace.setNavigationOnClickListener{
            onBackPressed()
        }
    }
}