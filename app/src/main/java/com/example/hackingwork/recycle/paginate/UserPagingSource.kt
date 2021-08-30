package com.example.hackingwork.recycle.paginate

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.hackingwork.utils.CreateUserAccount
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class UserPagingSource(private val query: Query) :PagingSource<QuerySnapshot,CreateUserAccount>(){
    override fun getRefreshKey(state: PagingState<QuerySnapshot, CreateUserAccount>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, CreateUserAccount> {
        return try {
            val currentPage = params.key ?: query.get().await()
            val lastDocumented = currentPage.documents.last()
            val nextPage = query.startAfter(lastDocumented).get().await()
            val users: MutableList<CreateUserAccount> = mutableListOf()
            currentPage.forEach {
                val op=it.toObject(CreateUserAccount::class.java)
                op.id=it.id
                users.add(op)
            }
            LoadResult.Page(
                data = users,
                prevKey =null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}