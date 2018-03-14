package com.microjet.airqi2

import android.os.AsyncTask
import android.util.Log
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import io.fabric.sdk.android.services.settings.IconRequest.build
import okhttp3.Request
import io.fabric.sdk.android.services.settings.IconRequest.build
import okhttp3.HttpUrl






/**
 * Created by B00175 on 2018/3/13.
 */
class DownloadTask : AsyncTask<Void, Void, String>() {

    val client = OkHttpClient()
    val urlBuilder = HttpUrl.parse("http://api.mjairql.com/api/v1/getUserData")!!.newBuilder()
    .addQueryParameter("mac_address", "C8:C6:88:16:91:38")
    .addQueryParameter("start_time", "0")
    .addQueryParameter("end_time", "1520941868267")
    val url = urlBuilder.build().toString()
    val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("authorization",
                    "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6Ijk4NTE5MTRjNTZmN2UyMjc1MzY5YTRhMWFlMDVlODM3OGI1NDAxZDU1ZjMxMWQ5NjgyOWE0OWFiN2MxN2FlN2I1ZjlhMzYwODc3OWMzZTdjIn0.eyJhdWQiOiIxIiwianRpIjoiOTg1MTkxNGM1NmY3ZTIyNzUzNjlhNGExYWUwNWU4Mzc4YjU0MDFkNTVmMzExZDk2ODI5YTQ5YWI3YzE3YWU3YjVmOWEzNjA4Nzc5YzNlN2MiLCJpYXQiOjE1MjA5MzU3OTQsIm5iZiI6MTUyMDkzNTc5NCwiZXhwIjoxNTUyNDcxNzk0LCJzdWIiOiIyNTkiLCJzY29wZXMiOltdfQ.WV7Jr_zmKT7G6Dwgpibcjxzq5Le0eCwSuUh76pShB73Ue1zHZbCIOIyIGRGzPOomlh__jyikzGwuDsWJorVZRj_U6NzwHn_A-lmwcsfI2sx_4uOeP3QhFjy_6putK5waGbn4fDFqeu0QQOtXS2N8Ji7t9nRFmCiOElvP1Mnrj9lu146OIkl6SUR7eSKurBfWg29v7SMDkfJfkjEq9N14N1uC-KNt9p8Jr67Ly7Ajr065b2pXx4YYUX946uo_z_22EAoYj_ChAWMZO8wHEkCBAXrpWbwqf-qCHwKaHJD3plom22TFoJebFfRaoC2cZl6anActhBoIrnIiy-cjWlUc1F25JvbwIHHbvBx7rsBoI8CKu66M_84ESxg8wlY3OzwBBAM8FNqnOPTPHLrbBw_8f2iAiqgh0dbv4pObpEYU6TZKNqYH7SxcmOuBRccK70LmcMn7zy2lOWAbI6V29G-G5Lw2P_CyoDTwoEVzoLfB_TCpSAkedI5bg4FAU7aY_TGH56o51G_wsi8Ad4vQfOkq8KwZR5Zg9RJv98_qnK-lQ_V7mjdu7AoaLEz6d37fIG5qmYKWt9uhqcebN8MM5XG7S7vxjp1885gOZMbMeMXg_Dob3pTkzK-GM_qPcGA8kIDQmbUCJnJKc7e73ZMbMjBG9MAsWIG58jsDNPuDw6FfGy0")
            .build()

    override fun doInBackground(vararg params: Void?): String? {
        try {
            val response = client.newCall(request).execute()
            return if (!response.isSuccessful) {
                null
            } else response.body()?.string()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        // ...
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null) {
            Log.d("Download",result.toString())
        }

    }
}