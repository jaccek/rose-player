package com.github.jaccek.roseplayer.repository.cursor

import android.content.ContentResolver
import com.github.jaccek.roseplayer.repository.Specification
import io.reactivex.Maybe
import io.reactivex.Single

interface CursorSpecification<TYPE> : Specification<TYPE> {

    fun query(contentResolver: ContentResolver): Maybe<List<TYPE>>
}
