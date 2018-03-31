package com.microjet.airqi2.URL

/**
 * Created by B00055 on 2018/3/28.
 */
object MjAQIUrl{
    val getUserData:String="http://api.mjairql.com/api/v1/getUserData"
    val postUserData:String="https://mjairql.com/api/v1/upUserData"
    val postRegister:String="https://mjairql.com/api/v1/register"
    val postLogin:String="https://mjairql.com/api/v1/login"
    val postWeather:String="https://mjairql.com/api/v1/upWeather"
    val getWeather:String="https://mjairql.com/api/v1/getWeather"//uuid=xxxaass&mac_address=qqq"
    val postForgetPassword:String="https://mjairql.com/api/v1/forgotPassword"
    val postEditUserData:String="https://mjairql.com/api/v1/editUserData"
    val postFWversion:String="https://mjairql.com/api/v1/fwCheck"//deviceType=0000&version=201803150004
}