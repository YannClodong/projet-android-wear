package com.example.projetclw

import android.app.Activity
import android.os.Bundle
import com.example.projetclw.databinding.ActivityMain2Binding

class MainActivity2 : Activity() {

    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}