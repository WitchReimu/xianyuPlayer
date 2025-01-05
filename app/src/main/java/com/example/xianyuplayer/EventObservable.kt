package com.example.xianyuplayer

object EventObservable {

    private val observers = ArrayList<EventObserver>()

    fun notifyAll(payload: Any) {
        if (observers.isNotEmpty()) {

            for (observer in observers) {
                observer.onEvent(payload)
            }
        }
    }

    fun addObserver(observer: EventObserver) {
        observers.add(observer)
    }
}