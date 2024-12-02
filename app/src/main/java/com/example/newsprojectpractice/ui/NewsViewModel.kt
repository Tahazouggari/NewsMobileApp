package com.example.newsprojectpractice.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsprojectpractice.models.Article
import com.example.newsprojectpractice.models.NewsResponse
import com.example.newsprojectpractice.repository.NewsRepositry
import com.example.newsprojectpractice.util.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response
import java.util.Locale.IsoCountryCode

class NewsViewModel(app:Application , val newsRepositry: NewsRepositry): AndroidViewModel(app){
    val headlines : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage =1
    var headlinesResponse:NewsResponse?=null


    val searchNews : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage =1
    var searchNewsResponse : NewsResponse?= null
    var newSearchQuery: String?=  null
    var oldSaerchQuery: String?=  null
    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String)=viewModelScope.launch {
        headlinesInterne(countryCode)
    }

    fun searchNews(searchQuery: String)=viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }


    private fun handleHeadlinesresponse(response: Response<NewsResponse>):Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse->
                headlinesPage++
                if(headlinesResponse == null){
                    headlinesResponse =resultResponse
                }else{
                    val oldArticles =headlinesResponse?.articles
                    val newArticles =resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlinesResponse?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    private fun handleSearchNewsResponse(response: Response<NewsResponse>):Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchNewsResponse == null || newSearchQuery != oldSaerchQuery) {
                    searchNewsPage = 1
                    oldSaerchQuery = newSearchQuery
                    searchNewsResponse = resultResponse
                } else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    fun addToFavourites(article: Article)=viewModelScope.launch {
        newsRepositry.upsert(article)
    }

    fun getFavouriteNews()=newsRepositry.getFavouriteNews()

    fun deleteArticle(article: Article)=viewModelScope.launch {
        newsRepositry.deleteArticle(article)
    }

    fun internetConnection(context: Context):Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                    else->false
                }
            }?:false
        }

    }


    private suspend fun headlinesInterne(countryCode: String){
        headlines.postValue((Resource.Loading()))
        try{
            if(internetConnection((this.getApplication()))){
                val response=newsRepositry.getHeadlines(countryCode , headlinesPage)
                headlines.postValue(handleHeadlinesresponse(response))
            }else{
                headlines.postValue(Resource.Error("No internet connection"))
            }
        }catch(t: Throwable) {
            when (t) {
                    is IOException->headlines.postValue(Resource.Error("Unable to connect"))
                    else ->headlines.postValue((Resource.Error("No Signal")))
            }
        }
    }
    private suspend fun searchNewsInternet(searchQuery: String){
        newSearchQuery= searchQuery
        searchNews.postValue(Resource.Loading())
        try{
            if(internetConnection((this.getApplication()))){
                val response=newsRepositry.searchNews(searchQuery , searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        }catch(t: Throwable) {
            when (t) {
                is IOException->searchNews.postValue(Resource.Error("Unable to connect"))
                else ->searchNews.postValue((Resource.Error("No Signal")))
            }
        }

    }

}