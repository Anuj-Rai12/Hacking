package com.uptodd.uptoddapp.ui.expertCounselling
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.database.expertCounselling.UpComingSessionModel
import com.uptodd.uptoddapp.databinding.FragmentExpertCounsellingBinding
import com.uptodd.uptoddapp.databinding.FragmentUpcomingSessionsBinding
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ShowInfoDialog
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "ActivitySampleFragment"

class UpComingSessionFragment : Fragment(), UpcomingSessionInterface {


    private lateinit var binding: FragmentUpcomingSessionsBinding

    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
    }

    private val uptoddDatabase: UptoddDatabase by lazy {
        UptoddDatabase.getInstance(requireContext())
    }

    private var expertCounsellingList = mutableListOf<ExpertCounselling>()

    private val ioScope = CoroutineScope(Dispatchers.Main)

    private val adapter = UpcomingSessionAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpcomingSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       fetchDataFromApi()
    }

    private fun fetchDataFromApi() {
        val uid = AllUtil.getUserId()
        AndroidNetworking.get("https://www.uptodd.com/api/appusers/upcomingSessionDetails/$uid")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return

                    Log.i(TAG, "${response.get("data")}")

                    /* the get method doesn't support returning
                       nullable types so in order to handle nullable
                       objects try block is used to detect nullable
                       object.
                    */

                    try {
                        val data = (response.get("data") as JSONObject).get("upcominSessions") as JSONArray
                        if (data.length() <= 0) {
                            showNoData()
                            hideRecyclerView()
                        } else {
                            parseData(AllUtil.getExpertCounselling((response.get("data") as JSONObject)
                                .get("upcominSessions").toString()))
                            hideNodata()
                        }

                        if ((response.get("data") as JSONObject).
                            getInt("sessionBookingAllowed") == 1)
                        {
                            if(data.length()<=0) {
                                binding.noDataContainer.text="Expert Session not booked yet"
                            }
                            if((response.get("data") as JSONObject).
                                getString("bookingLink").toString().isNullOrEmpty() ||
                                (response.get("data") as JSONObject).
                                getString("bookingLink")=="null")
                            {
                                Log.d(TAG,"empty")
                                fetchBookingLink((response.get("data") as JSONObject).
                                getInt("sessionBookingId"))
                            }
                            else
                            {
                                binding.bookingButton.visibility=View.VISIBLE;
                                val link=(response
                                    .get("data") as JSONObject).getString("bookingLink")
                                binding.bookingButton.setOnClickListener {
                                    if(!TextUtils.isEmpty(link) && link.startsWith("http")) {
                                        var intent = Intent(
                                            Intent.ACTION_VIEW, Uri.parse(
                                                link
                                            )
                                        )
                                        startActivity(intent)
                                    }
                                }
                            }

                        }
                        else
                        {
                            if(data.length()<=0) {
                                binding.noDataContainer.text="Expert Session booking window is not opened yet"
                            }
                            binding.bookingButton.visibility=View.GONE;
                        }
                        binding.tncText.setOnClickListener {
                            TermsAndConditions.show((response.get("data") as JSONObject).getString("tnc")
                                ,fragmentManager!!)
                        }

                    } catch (e: Exception) {
                        Log.i(TAG, "${e.message}")
                        showNoData()
                        hideRecyclerView()
                        return
                    } finally {
                        binding.expConeRefresh.isRefreshing = false
                    }
                }


                override fun onError(anError: ANError?) {
                    Log.e(TAG, "${anError?.message}")
                    binding.expConeRefresh.isRefreshing = false
                }

            })
    }

    private fun parseData(data: ArrayList<ExpertCounselling>) {

        expertCounsellingList.clear()
        expertCounsellingList.addAll(data)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter.list = expertCounsellingList
        binding.expConRecycler.adapter = adapter
        showRecyclerView()
    }

    fun fetchBookingLink(sessionId:Int)
    {

        val uid = AllUtil.getUserId()
        AndroidNetworking.get("https://www.uptodd.com/api/appusers/getBookingLink/$uid?sessionBookingId=$sessionId")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return

                    Log.i(TAG, "${response.get("data")}")

                    /* the get method doesn't support returning
                       nullable types so in order to handle nullable
                       objects try block is used to detect nullable
                       object.
                    */

                    try {
                        binding.bookingButton.visibility=View.VISIBLE
                        val data = (response.get("data") as String)
                        binding.bookingButton.setOnClickListener {
                            if(!TextUtils.isEmpty(data) && data.startsWith("http")) {
                                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
                                startActivity(intent)
                            }
                            else
                                binding.bookingButton.visibility=View.GONE
                        }


                    } catch (e: Exception) {
                        Log.i(TAG, e.message.toString())
                    } finally {
                        binding.expConeRefresh.isRefreshing = false
                    }
                }


                override fun onError(anError: ANError?) {
                    Log.e(TAG, "${anError?.message}")
                    binding.expConeRefresh.isRefreshing = false
                }

            })


    }


    private fun hideNodata() {
        binding.noDataContainer.isVisible = false
    }

    private fun showNoData() {
        binding.noDataContainer.isVisible = true
    }

    private fun showRecyclerView() {
        binding.expConRecycler.isVisible = true
    }

    private fun hideRecyclerView() {
        binding.expConRecycler.isVisible = false
    }


    override fun onClick(exp_con: ExpertCounselling) {
        findNavController().navigate(HomeExpertCounsellingDirections.
        actionHomeExpertCounsellingFragmentToExpertSuggestionsFragment(exp_con))
    }

}