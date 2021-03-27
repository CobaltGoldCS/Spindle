package com.cobaltware.webscraper.datahandling

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import javax.xml.xpath.XPath

// Parcelize data structures for carrying values between activities
@Parcelize
data class Book(val col_id : Int,
                val title: String,
                val url : String) : Parcelable
@Parcelize
data class Config(val col_id : Int,
                  val domain : String,
                  val mainXPath: String,
                  val prevXPath: String,
                  val nextXPath : String) : Parcelable
