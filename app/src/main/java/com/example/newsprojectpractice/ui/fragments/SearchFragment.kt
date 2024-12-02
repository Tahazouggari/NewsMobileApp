package com.example.newsprojectpractice.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsprojectpractice.R
import com.example.newsprojectpractice.adapters.NewsAdapter
import com.example.newsprojectpractice.databinding.FragmentHeadlinesBinding
import com.example.newsprojectpractice.databinding.FragmentSearchBinding
import com.example.newsprojectpractice.ui.NewsActivity
import com.example.newsprojectpractice.ui.NewsViewModel
import com.example.newsprojectpractice.util.Constants
import com.example.newsprojectpractice.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.newsprojectpractice.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Error

class SearchFragment : Fragment(R.layout.fragment_search) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var retryButton: Button
    lateinit var errorText: TextView
    lateinit var itemSearchError: CardView
    lateinit var binding: FragmentSearchBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        itemSearchError=view.findViewById((R.id.itemHeadlinesError))
        val inflater=requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view :View=inflater.inflate(R.layout.item_error,null)


        retryButton=view.findViewById(R.id.retryButton)
        errorText=view.findViewById(R.id.errorText)

        newsViewModel=(activity as NewsActivity).newsViewModel
        setupSearchRecycler()


        newsAdapter.setOnItemClickListener {
            val bundle=Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articleFragment,bundle)
        }
        var job: Job? = null
        binding.searchEdit.addTextChangedListener(){ editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        newsViewModel.searchNews(editable.toString())
                    }
                }
            }
        }
        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response->
            when(response){
                is Resource.Success<*>->{
                    hideProgressbar()
                    hideErrorMessage()
                    response.date?.let{
                            newsResponse -> newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages =newsResponse.totalResults / Constants.QUERY_PAGE_SIZE
                        isLastpage =newsViewModel.searchNewsPage ==totalPages
                        if(isLastpage){
                            binding.recyclerSearch.setPadding(0,0,0,0)
                        }
                    }


                }
                is Resource.Error<*>->{
                    hideProgressbar()
                    response.message?.let { message->
                        Toast.makeText(activity,"Sorry Error: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }

                }
                is Resource.Loading<*>->{
                    showProgressbar()
                }

            }
        })
        retryButton.setOnClickListener{
            if(binding.searchEdit.text.toString().isNotEmpty()){
                newsViewModel.searchNews(binding.searchEdit.text.toString())
            }else{
                hideErrorMessage()
            }
        }

    }


    var isError=false
    var isLoading= false
    var isLastpage = false
    var isScrolling = false


    private fun hideProgressbar(){
        binding.paginationProgressBar.visibility=View.VISIBLE
        isLoading=false
    }

    private fun showProgressbar(){
        binding.paginationProgressBar.visibility=View.VISIBLE
        isLoading=true
    }
    private fun hideErrorMessage(){
        itemSearchError.visibility=View.VISIBLE
        isError=false
    }


    private fun showErrorMessage(message: String){
        itemSearchError.visibility=View.VISIBLE
        errorText.text= message
        isError=true
    }


    val scrollListener=object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNorLoadingAndNotLastPage = !isLoading && !isLastpage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val iTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNorLoadingAndNotLastPage && isNotAtBeginning
            if (shouldPaginate) {
                newsViewModel.searchNews(binding.searchEdit.text.toString())
                isScrolling = false
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }
    private fun setupSearchRecycler(){
        newsAdapter=NewsAdapter()
        binding.recyclerSearch.apply {
            adapter = newsAdapter
            layoutManager=LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }

}