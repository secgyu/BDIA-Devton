package com.aos.beacondistance.http

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object HttpRetrofitService {
    fun createHttpService(): HttpRequests {

        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = getUnsafeOkHttpClient()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept-Charset", "UTF-8")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val gson: Gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .client(httpClient)
            .baseUrl("https://")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create()
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @kotlin.jvm.Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @kotlin.jvm.Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(
                sslSocketFactory,
                trustAllCerts[0] as X509TrustManager
            )
            builder.hostnameVerifier { _, _ -> true }
            builder
        } catch (e: Exception) {
            OkHttpClient.Builder()
        }
    }
}

inline fun <reified T> Retrofit.create(): T = create(T::class.java)