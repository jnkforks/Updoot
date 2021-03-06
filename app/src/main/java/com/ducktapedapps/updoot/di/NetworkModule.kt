package com.ducktapedapps.updoot.di

import com.ducktapedapps.updoot.BuildConfig
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.SearchAdapter
import com.ducktapedapps.updoot.utils.SubredditInfoAdapter
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import com.ducktapedapps.updoot.utils.moshiAdapters.*
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.noties.markwon.Markwon
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {
    @Singleton
    @Provides
    fun provideOkHttpClient(tokenInterceptor: TokenInterceptor): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
        okHttpClient.addNetworkInterceptor(tokenInterceptor)
        okHttpClient.addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
            //as per reddit api guidelines to include proper user agent
            val userAgent = "android:com.ducktapedapps.updoot:" + BuildConfig.VERSION_NAME + " (by /u/nothoneypot)"

            //For removing default legacy json character support
            val url = chain.request().url.newBuilder().addQueryParameter("raw_json", "1").build()

            val request = chain.request().newBuilder().url(url).addHeader("User-Agent", userAgent).build()
            chain.proceed(request)
        })
        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            okHttpClient.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClient.build()
    }

    @Reusable
    @Provides
    fun provideMoshi(markwon: Markwon): Moshi = Moshi
            .Builder()
            .add(SubmissionListingAdapter())
            .add(MoreCommentsListAdapter(markwon))
            .add(CommentListingAdapter(markwon))
            .add(SubredditListingAdapter())
            .add(LinkDataAdapter())
            .add(TrendingSubsJsonAdapter())
            .add(SearchAdapter())
            .add(SubredditInfoAdapter())
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
            .baseUrl(Constants.API_BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .client(client)
            .build()

}