package com.androiddeveloper3005.mychat.model

//data class Users(var name : String , var image : String , var status : String)

class Users{
    var name : String? = null
    var image : String?= null
    var status : String?= null
    var uid : String?= null
    constructor(){

    }
    constructor( name : String? , status : String? , image : String? ,uid : String?){
        this.name = name
        this.status = status
        this.image = image
        this.uid = uid

    }



}