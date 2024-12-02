package com.example.newsprojectpractice.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.newsprojectpractice.R
import com.example.newsprojectpractice.databinding.ActivityNewsBinding
import com.example.newsprojectpractice.db.ArticleDatabase
import com.example.newsprojectpractice.models.Article
import com.example.newsprojectpractice.repository.NewsRepositry

class NewsActivity:AppCompatActivity() {
    lateinit var newsViewModel: NewsViewModel
    lateinit var binding : ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsRepositry =NewsRepositry(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application,newsRepositry)
        newsViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        val navHostFragment =supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController=navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

    }
}