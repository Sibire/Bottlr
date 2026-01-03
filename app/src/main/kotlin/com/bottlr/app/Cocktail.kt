package com.bottlr.app

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cocktail(
    var name: String,
    var base: String,
    var mixer: String,
    var juice: String,
    var liqueur: String,
    var garnish: String,
    var extra: String,
    var photoUri: Uri?,
    var notes: String,
    var keywords: String,
    var rating: String,
    var cocktailID: String? = null
) : Parcelable
