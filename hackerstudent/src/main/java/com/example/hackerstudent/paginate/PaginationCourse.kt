package com.example.hackerstudent.paginate

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.hackerstudent.utils.FireBaseCourseTitle
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException


class PaginationCourse(private val query: Query) :
    PagingSource<QuerySnapshot, FireBaseCourseTitle>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, FireBaseCourseTitle>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, FireBaseCourseTitle> {
        return try {
            val currentPage = params.key ?: query.get().await()
            val lastDocumented = currentPage.documents.last()
            val nextPage = query.startAfter(lastDocumented).get().await()
            val courseData: MutableList<FireBaseCourseTitle> = mutableListOf()
            currentPage.forEach {
                val op = it.toObject(FireBaseCourseTitle::class.java)
                courseData.add(op)
            }
            LoadResult.Page(
                data = courseData,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

}