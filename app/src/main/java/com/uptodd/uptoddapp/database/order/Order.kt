package com.uptodd.uptoddapp.database.order

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName="order_table")
data class Order(
    @PrimaryKey(autoGenerate = true)
    var orderId:Long=0,

    @ColumnInfo(name = "product_name")
    var productname:String?=null,

    @ColumnInfo(name = "delivery_status")
    var deliveryStatus:Boolean=false,              //true=delivered  ;  false=not delivered

    @ColumnInfo(name = "delivery_date")
    var deliveryDate:String?=null,                       //ddmmyyyy format

    @ColumnInfo(name = "details_url")
    var detailsUrl:String?=null,

    @ColumnInfo(name="details")
    var details:String?=null

) : Serializable