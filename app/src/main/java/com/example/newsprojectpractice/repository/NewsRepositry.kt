package com.example.newsprojectpractice.repository

import com.example.newsprojectpractice.api.RetrofitInstance
import com.example.newsprojectpractice.db.ArticleDatabase
import com.example.newsprojectpractice.models.Article
import retrofit2.http.Query
import java.util.Locale.IsoCountryCode

class NewsRepositry (val db:ArticleDatabase){
    suspend fun getHeadlines(countryCode: String , pageNumber: Int)=
        RetrofitInstance.api.getHeadlines(countryCode,pageNumber)
    suspend fun searchNews(searchQuery: String, pageNumber: Int)=
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)
    suspend fun upsert(article: Article)=db.getArticleDoa().upsert(article)

    fun getFavouriteNews()=db.getArticleDoa().getAllArticles()
    suspend fun deleteArticle(article: Article)=db.getArticleDoa().deleteArticle(article)
}