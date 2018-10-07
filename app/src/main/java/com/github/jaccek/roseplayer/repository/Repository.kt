package com.github.jaccek.roseplayer.repository

import io.reactivex.Maybe

interface Repository<TYPE> {

    fun add(item: TYPE)

    fun update(item: TYPE)

    fun remove(item: TYPE)

    fun query(spec: Specification<TYPE>): Maybe<List<TYPE>>
}
