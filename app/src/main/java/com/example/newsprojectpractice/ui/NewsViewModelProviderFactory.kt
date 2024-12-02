package com.example.newsprojectpractice.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.newsprojectpractice.repository.NewsRepositry

class NewsViewModelProviderFactory (val app:Application,val newsRepositry: NewsRepositry):ViewModelProvider.Factory{
    override fun <T:ViewModel> create(modelClass: Class<T>): T{
        return NewsViewModel(app,newsRepositry) as T
    }
}