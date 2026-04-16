package com.example.notecompose.domain.util

sealed class OrderType {
    object Ascending: OrderType()
    object Descending: OrderType()
}
