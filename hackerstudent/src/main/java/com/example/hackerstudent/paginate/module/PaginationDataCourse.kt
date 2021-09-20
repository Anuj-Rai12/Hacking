package com.example.hackerstudent.paginate.module

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.hackerstudent.utils.GetConstStringObj
import com.example.hackerstudent.utils.Module
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

class PaginationDataCourse(
    private val fireStore: FirebaseFirestore,
    private val courseName: String
) :
    PagingSource<QuerySnapshot, Module>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Module>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Module> {
        return try {
            val query = fireStore.collection(GetConstStringObj.Create_course).document(courseName)
                .collection(GetConstStringObj.Create_Module)

            val currentPage = params.key ?: query.get().await()

            val lastDocumented = currentPage.documents.last()

            val nextPage = query.startAfter(lastDocumented).get().await()

            val courseData: MutableList<Module> = mutableListOf()

            currentPage.forEach {
                val op = it.toObject(Module::class.java)
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