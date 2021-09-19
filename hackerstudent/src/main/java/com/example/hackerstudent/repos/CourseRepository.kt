package com.example.hackerstudent.repos

import androidx.fragment.app.FragmentActivity
import com.example.hackerstudent.api.RestApi
import com.example.hackerstudent.utils.GetConstStringObj
import com.example.hackerstudent.utils.MySealed
import com.example.hackerstudent.utils.UploadFireBaseData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.razorpay.Checkout
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class CourseRepository @Inject constructor(
    private val restApi: RestApi,
    private val fireStore: FirebaseFirestore
) {
//    private val authInstance by lazy {
//        FirebaseAuth.getInstance()
//    }
//    private val currentUser by lazy {
//        authInstance.currentUser
//    }

    fun getTodayQuote() = flow {
        emit(MySealed.Loading("Getting Today Quote"))
        val data = try {
            val response = restApi.getInfo()
            MySealed.Success(response)
        } catch (e: Exception) {
            MySealed.Error(e, null)
        } catch (e: HttpException) {
            MySealed.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)

    fun getCourseOnlyThree(query: Query) = flow {
        emit(MySealed.Loading("Loading Featured Course"))
        val data = try {
            val info = query.get().await()
            val courseData: MutableList<UploadFireBaseData> = mutableListOf()
            info.forEach {
                courseData.add(it.toObject(UploadFireBaseData::class.java))
            }
            MySealed.Success(courseData)
        } catch (e: Exception) {
            MySealed.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)

    fun showPaymentOption(checkout: Checkout, activity: FragmentActivity, jsonObject: JSONObject) =
        flow {
            emit(MySealed.Loading("loading Payment Option"))
            val data = try {
                checkout.open(activity, jsonObject)
                MySealed.Success(null)
            } catch (e: Exception) {
                MySealed.Error(e, null)
            }
            emit(data)
        }.flowOn(IO)

    fun getPaidCourse(dataItem: String) = flow {
        emit(MySealed.Loading("Loading Course"))
        val data = try {
            val info =
                fireStore.collection(GetConstStringObj.Create_course).document(dataItem).get()
                    .await()
            /*val courseData = if (info.exists())
                info.toObject(UploadFireBaseData::class.java)
            else
                null*/
            MySealed.Success(info.toObject(UploadFireBaseData::class.java))
        } catch (e: Exception) {
            MySealed.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)
}