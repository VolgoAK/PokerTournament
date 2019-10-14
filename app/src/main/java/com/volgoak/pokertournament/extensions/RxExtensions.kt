package com.volgoak.pokertournament.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.SerialDisposable

infix fun Disposable.into(container: CompositeDisposable?) = container?.add(this)
infix fun Disposable.into(container: SerialDisposable?) = container?.set(this)