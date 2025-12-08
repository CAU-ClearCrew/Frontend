package com.example.front.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


//연결할 HTTP 추가
//private const val BASE_URL = "https://10.210.61.15:8080/api"
private const val BASE_URL = "https://localhost:8080/api"


//retrofit 빌더 객체 추가
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())  //string응답용
    .addConverterFactory(GsonConverterFactory.create())     //JSON 응답용
    .baseUrl(BASE_URL)  //기본 url 추가
    .build()    //객체 빌드!


//retrofit이 HTTP를 요청해서 웹 서버와 통신하는 방법을 정의함.
//내가 백엔드 서버에 주는 것.
interface ApiService{

}


object Api{ //싱글톤 객체 : 전역에서 접근가능. (보통은 권장되지 않지만, api할 땐 사용함) (한 객체만을 허용하는 객체임)
    val retrofitService : ApiService by lazy {  //지연 초기화 : 최초 사용 시 ㄱㅊ은 초기화를 위함..
        retrofit.create(ApiService::class.java)
    }
}

