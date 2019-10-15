package com.volgoak.pokertournament.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeSafe(owner: LifecycleOwner, block: (T) -> Unit) {
    this.observe(owner, Observer {
        it?.let { block(it) }
    })
}

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, block: (T) -> Unit) {
    this.observe(owner, Observer {
        it?.let {
            removeObservers(owner)
            block(it)
        }
    })
}