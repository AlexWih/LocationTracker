package com.example.aleksei.locationtracker

import io.reactivex.Single
import io.reactivex.functions.BiFunction

/**
 * Created by aleksei on 01/02/2017.
 */

class foo {

    internal fun bar() {
        Single.zip(Single.just(12), Single.just(12), BiFunction<Int, Int, String> { integer, integer2 -> "null" })

    }

}
