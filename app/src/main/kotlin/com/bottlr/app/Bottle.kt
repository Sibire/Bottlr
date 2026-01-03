package com.bottlr.app

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bottle(
    var name: String,
    var distillery: String,
    var type: String,
    var abv: String,
    var age: String,
    var photoUri: Uri?,
    var notes: String,
    var region: String,
    var keywords: String,
    var rating: String,
    var bottleID: String? = null
) : Parcelable
