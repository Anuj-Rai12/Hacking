package com.uptodd.uptoddapp.ui.account.account

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.account.Account
import com.uptodd.uptoddapp.databinding.DialogChangeLanguageBinding
import com.uptodd.uptoddapp.databinding.DialogExtendSubscriptionBinding
import com.uptodd.uptoddapp.databinding.DialogSwitchNannyBinding
import com.uptodd.uptoddapp.databinding.FragmentAccountBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.workManager.cancelUptoddWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.Locale


class AccountFragment : Fragment() {

    private val PICK_IMAGE: Int = 1
    private lateinit var binding: FragmentAccountBinding
    private lateinit var viewModel: AccountViewModel

    private var editMode = false

    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    var token: String? = null
    var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        preferences = requireActivity().getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences.edit()

        ChangeLanguage(requireContext()).setLanguage()

        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_account, container, false)
        binding.lifecycleOwner = this


        if (preferences.contains("uid"))
            uid = preferences.getString("uid", "")
        if (preferences.contains("token"))
            token = preferences.getString("token", "")

        val factory = UptoddViewModelFactory.getInstance(requireActivity().application)

        viewModel = ViewModelProvider(this, factory).get(AccountViewModel::class.java)

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.title =
            getString(R.string.account)
        //(activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true)

        binding.textViewChangePassword.setOnClickListener { onClickChangePassword() }
        binding.buttonRanking.setOnClickListener { onClickRanking() }
        binding.buttonNannyMode.setOnClickListener { showNonParentDialog() }
        binding.imageButtonCapture.setOnClickListener { onClickCapture() }
        binding.imageButtonLogout.setOnClickListener { onClickLogout() }
        binding.textViewChangeLanguage.setOnClickListener { onClickChangeLanguage() }

        initObservers()

        return binding.root
    }

    private fun initObservers() {
        //Observer to load data
        viewModel.currentAccount.observe(viewLifecycleOwner, Observer {
            Log.d("div", "AccountFragment L85 ${viewModel.currentAccount.value}")
            if (it == null) {
                viewModel.isRepositoryEmpty.value = true
                showLoadingDialog()
            } else
                viewModel.isRepositoryEmpty.value = false
            if (it != null && it.isNannyMode) {
                viewModel.isNannyEnabled = true
                //binding.layoutNanny.visibility=View.VISIBLE
                //binding.buttonNannyMode.visibility=View.GONE
                binding.buttonNannyMode.text = getString(R.string.see_update_nanny_details)
            }
            setFields(it)
        })

        //observer for setting up nanny mode
        viewModel.isNannyUpdating.observe(viewLifecycleOwner, Observer {
            Log.d("div", "AccountFragment L112 ${viewModel.isNannyUpdated}")
            if (!it && viewModel.isNannyUpdated) {
                if (viewModel.isNannyUpdated) {
                    Log.d("div", "AccountFragment L115 ${viewModel.isNannyUpdated}")
                    UpToddDialogs(requireContext()).showDialog(R.drawable.gif_done,
                        getString(R.string.nanny_details_are_updated), getString(R.string.close),
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                dialog.dismiss()
                            }
                        })
                }
            }
        })

        //Observer for saving changes in account
        viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
            Log.d(
                "div",
                "${viewModel.isLoadingDialogVisible.value} ${viewModel.isDataLoadedToDatabase}"
            )
            if (!it && viewModel.isDataLoadedToDatabase) {
                UpToddDialogs(requireContext()).showDialog(R.drawable.gif_done,
                    getString(R.string.account_details_are_updated), getString(R.string.close),
                    object : UpToddDialogs.UpToddDialogListener {
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    })
            }
        })
    }

    private fun onClickLogout() {
        val dialogBinding = DataBindingUtil.inflate<DialogExtendSubscriptionBinding>(
            layoutInflater, R.layout.dialog_extend_subscription, null, false
        )
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.textView.text = getString(R.string.are_you_sure_logout)
        dialogBinding.buttonYes.setOnClickListener {
            editor.putBoolean("loggedIn", false)
            editor.remove("language")
            editor.commit()

            AndroidNetworking.get("https://uptodd.com/api/userlogout/{userId}")
                .addHeaders("Authorization", "Bearer $token")
                .addPathParameter("userId", viewModel.uid)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.i("debug", "$response")
                        editor.remove("LaunchTime")
                    }

                    override fun onError(anError: ANError?) {
                        Log.i("debug", "${anError?.message}")
                    }
                })


            WorkManager.getInstance(requireContext()).cancelUptoddWorker()

            AllUtil.unregisterToken()

            UptoddSharedPreferences.getInstance(requireContext()).clearAllPreferences()

            CoroutineScope(Dispatchers.IO).launch {
                UptoddDatabase.getInstance(requireContext()).clearAllTables()
            }

            startActivity(
                Intent(
                    activity,
                    LoginActivity::class.java
                )
            )
            activity?.finishAffinity()
        }
        dialogBinding.buttonNo.setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.show()

    }

    private fun onClickCapture() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_picture)),
            PICK_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            viewModel.imageUri = data.data
            viewModel.imagePath = viewModel.imageUri?.path
            Log.d("div", "AccountFragment L127 ${viewModel.imagePath}")
            viewModel.imageBitmap = MediaStore.Images.Media.getBitmap(
                activity?.contentResolver,
                viewModel.imageUri
            )

            binding.imageViewProfileImage.setImageURI(viewModel.imageUri)
        } else
            Toast.makeText(
                activity,
                getString(R.string.unable_to_select_image),
                Toast.LENGTH_LONG
            )
                .show()
    }

    private fun onClickRanking() {
        findNavController().navigate(AccountFragmentDirections.actionAccountFragment2ToRankingFragment())
    }

    private fun onClickChangeLanguage() {
        val dialogBinding = DataBindingUtil.inflate<DialogChangeLanguageBinding>(
            layoutInflater,
            R.layout.dialog_change_language,
            null,
            false
        )
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val currentLanguage = preferences!!.getString("language", "English")
        if (currentLanguage == "English")
            dialogBinding.radioButtonEnglish.isChecked = true
        else
            dialogBinding.radioButtonHindi.isChecked = true

        dialogBinding.buttonOk.setOnClickListener {
            val selectedId = dialogBinding.radioGroup.checkedRadioButtonId
            when (selectedId) {
                dialogBinding.radioButtonEnglish.id -> {
                    setLanguage("English", "en")
                }
                dialogBinding.radioButtonHindi.id -> {
                    setLanguage("Hindi", "hi")
                }
            }
            dialog.dismiss()
        }

        dialog.show()

        val window: Window? = dialog.window
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val r: Resources = requireContext().resources
        val px =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, r.displayMetrics)
                .toInt()

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(px, 0, px, 0)
        dialogBinding.layout.layoutParams = params

    }

    private fun setLanguage(language: String, languageCode: String) {
        editor.putString("language", language)
        editor.commit()
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        requireContext().resources.updateConfiguration(
            config,
            requireContext().resources.displayMetrics
        )

        val intent = Intent(requireActivity(), TodosListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        Toast.makeText(
            requireActivity(),
            getString(R.string.the_language_is_updated_and_the_changes_will_be_visible_in_few_hours),
            Toast.LENGTH_LONG
        ).show()
        requireActivity().finish()
    }

    private fun showNonParentDialog() {
        val dialogBinding = DataBindingUtil.inflate<DialogSwitchNannyBinding>(
            layoutInflater,
            R.layout.dialog_switch_nanny,
            null,
            false
        )
        var passwordEye = false

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnDismissListener {
            if (viewModel.isNannyEnabled) {
                //TODO Update UI.... show nanny details
            }
        }
        dialogBinding.imageButtonPasswordEye.setOnClickListener {
            passwordEye = !passwordEye
            if (passwordEye)
                dialogBinding.editTextPassword.transformationMethod = null
            else
                dialogBinding.editTextPassword.transformationMethod =
                    MyPasswordTransformationMethod()
        }
        dialogBinding.editTextPassword.transformationMethod = MyPasswordTransformationMethod()
        if (viewModel.currentAccount.value != null && viewModel.currentAccount.value!!.isNannyMode) {
            Log.d(
                "div",
                "AccountFragment L122 ${viewModel.currentAccount.value!!.nannyModeUserID} ${viewModel.currentAccount.value!!.nannyModePassword}"
            )
            dialogBinding.editTextUserID.setText(viewModel.currentAccount.value!!.nannyModeUserID)
            dialogBinding.editTextPassword.setText(viewModel.currentAccount.value!!.nannyModePassword)
        }
        dialogBinding.buttonSubmit.setOnClickListener {
            Log.d(
                "div",
                "AccountFragment L95 ${viewModel.currentAccount.value?.nannyModeUserID} ${viewModel.currentAccount.value?.nannyModePassword}"
            )
            if (dialogBinding.editTextUserID.text.isEmpty())
                dialogBinding.textViewError.text = getString(R.string.enter_userid)
            else if (dialogBinding.editTextPassword.text.length < 8)
                dialogBinding.textViewError.text = getString(R.string.short_password)
            else {
                if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                    viewModel.isNannyUpdating.value = true
                    viewModel.isNannyUpdated = false
                    showNannyUploadingDialog()
                    viewModel.setUpNannyMode(
                        dialogBinding.editTextUserID.text.toString(),
                        dialogBinding.editTextPassword.text.toString()
                    )
                    viewModel.isNannyUpdating.observe(viewLifecycleOwner, Observer {
                        Log.d("div", "AccountFragment L235 ${viewModel.isNannyUpdated}")
                        if (!it) {
                            if (viewModel.isNannyUpdated) {
                                Log.d("div", "AccountFragment L238 ${viewModel.isNannyUpdated}")
                                dialog.dismiss()
                            }
                        }
                    })
                } else {
                    //showInternetNotConnectedDialog()
                    dialogBinding.textViewError.text =
                        getString(R.string.no_internet_connection)
                }
            }
        }
        //dialog.setCancelable(false)
        dialog.show()

        val window: Window? = dialog.window
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun onClickChangePassword() {
        view?.findNavController()?.navigate(
            AccountFragmentDirections.actionAccountFragment2ToChangePasswordFragment(false)
        )
    }

    private fun setFields(account: Account?) {
        //set textFields
        if (account != null) {

            editor.putString("profileImageUrl", account.profileImageURL)
            editor.commit()
            bindImage(binding.imageViewProfileImage, account.profileImageURL)
            binding.textViewName.text =
                getString(R.string.welcome) + " " + if (account.name.isNullOrBlank()
                        .or(account.name == "null")
                ) "User"
                else
                    account.name
            binding.editTextName.setText(
                if (account.name.isNullOrBlank().or(account.name == "null")) ""
                else
                    account.name
            )
            binding.editTextEmail.setText(
                if (account.email.isNullOrBlank().or(account.email == "null")) ""
                else
                    account.email
            )
            binding.editTextPhone.setText(
                if (account.phone.isNullOrBlank().or(account.phone == "null")) ""
                else
                    account.phone
            )
            binding.editTextAddress.setText(
                if (account.address.isNullOrBlank().or(account.address == "null")) ""
                else
                    account.address
            )
            binding.editTextKidsName.setText(
                if (account.kidsName.isNullOrBlank().or(account.kidsName == "null")) ""
                else
                    account.kidsName
            )
            if (account.motherStage == "pre birth" || account.motherStage == "prenatal") {
                binding.layoutKidsInfo.visibility = View.GONE
            } else {
                val months = KidsPeriod(requireActivity()).getKidsAge()
                binding.editTextKidsAge.setText("$months")
                binding.editTextKidsGender.setText(
                    if (account.kidsGender.isNullOrEmpty().or(account.kidsGender == "null")) ""
                    else
                        account.kidsGender
                )
            }
            if (account.currentSubscribedPlan==0L)
            binding.editTextCurrentSubscribedPlan.setText("Master Program")
            else
                binding.editTextCurrentSubscribedPlan.setText("Premium Program")

            binding.editTextSubscriptionStartDate.setText(account.subscriptionStartDate)
            binding.editTextSubscriptionStartDate2.setText(UptoddSharedPreferences.getInstance(requireContext()).getSubEnd())
        }
    }

    private fun convertFields(editText: EditText, mode: Boolean) {
        editText.isEnabled = mode
        editText.isFocusable = mode
        editText.isFocusableInTouchMode = mode
    }

    fun bindImage(imgView: ImageView, imgUrl: String?) {
        /*imgUrl?.let {
            //val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            var url="https:uptodd.com/uploads/"
            if(preferences!!.contains("profileImageUrl"))
                url+=preferences!!.getString("profileImageUrl","")
            var stage="pre"
            if(preferences!!.contains("babyName") && preferences!!.getString("babyName","")!="null"
                && preferences!!.getString("babyName","")!="baby")
                stage="post"
            var res=R.drawable.ic_broken_image
            if(stage=="pre")
                res=R.drawable.pre_birth_profile
            else
                res=R.drawable.post_birth_profile
            Log.d("div","AccountFragment L298 ${"https:uptodd.com/uploads/$imgUrl"}")
            Glide.with(imgView.context)
                .load("https:uptodd.com/uploads/$imgUrl")
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(res)
                )
                .into(imgView)
        }*/
        if (imgUrl != null) {
            var preferences: SharedPreferences? = null
            preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
            var url = ""
            url = imgUrl
            var stage = "pre"
            if (preferences!!.contains("babyName") && preferences.getString(
                    "babyName",
                    ""
                ) != "null"
                && preferences.getString("babyName", "") != "baby"
            )
                stage = "post"
            var res = R.drawable.ic_broken_image
            if (stage == "pre")
                res = R.drawable.pre_birth_profile
            else
                res = R.drawable.post_birth_profile
            if (url == "null" || url == "") {
                imgView.setImageResource(res)
            } else {
                url = "https:uptodd.com/uploads/$url"


                var imageFile: File?
                val folder =
                    File(
                        requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            .toString() + "/UpTodd"
                    )
                var success = true
                if (!folder.exists()) {
                    success = folder.mkdirs()
                }
                if (success) {
                    //imageFile = File(imagePath!!)             //overwrite to capturedImage in CaptureImageFragment
                    val date = Date()
                    imageFile = File(
                        folder.absolutePath + File.separator + "Profile.jpg"
                    )
                    imageFile.createNewFile()

                    Glide.with(this).asBitmap().load(url)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?,
                            ) {
                                imgView.setImageBitmap(resource)
                                val ostream = ByteArrayOutputStream()

                                resource.compress(Bitmap.CompressFormat.JPEG, 100, ostream)
                                val fout = FileOutputStream(imageFile)
                                fout.write(ostream.toByteArray())
                                fout.close()
                                val values = ContentValues()
                                values.put(
                                    MediaStore.Images.Media.DATE_TAKEN,
                                    System.currentTimeMillis()
                                )
                                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                values.put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
                                activity?.contentResolver?.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    values
                                )
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                Log.d("div", "GenerateCardFragment L190 $placeholder")
                            }
                        })

                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.image_not_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                if (!imageFile.exists()) {

                    Glide.with(imgView.context)
                        .load(url)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.loading_animation)
                                .error(res)
                        )
                        .into(imgView)
                } else {
                    imgView.setImageBitmap(BitmapFactory.decodeFile(imageFile.absolutePath))
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_user_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_edit) {
            editMode = !editMode
            if (editMode) {
                item.setIcon(R.drawable.ic_baseline_save_24)

                binding.imageButtonCapture.visibility = View.VISIBLE
                //make textFields editable
                convertFields(binding.editTextKidsName, true)
                convertFields(binding.editTextPhone, true)
                convertFields(binding.editTextAddress, true)
                convertFields(binding.editTextEmail, true)
                //change background of non-editable fields
                binding.editTextName.setBackgroundResource(0)
                binding.editTextKidsAge.setBackgroundResource(0)
                binding.editTextKidsGender.setBackgroundResource(0)
                binding.editTextCurrentSubscribedPlan.setBackgroundResource(0)
                binding.editTextSubscriptionStartDate.setBackgroundResource(0)
            } else {
                save()
                viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
                    if (!it && viewModel.isDataLoadedToDatabase) {
                        item.setIcon(R.drawable.ic_baseline_edit_24)
                    }
                }
                )
                Log.d("save","clicked")
            }
            return false
        }
        else
            return super.onOptionsItemSelected(item)
    }

    private fun save() {
        if (binding.editTextKidsName.text.isEmpty())
            binding.textViewKidsError.text = getString(R.string.enter_valid_name)
        else if (binding.editTextPhone.text?.length!! < 10)
            binding.editTextPhone.error = getString(R.string.enter_valid_phone)
        else if (binding.editTextAddress.text.isNullOrBlank())
            binding.editTextAddress.error = getString(R.string.enter_valid_address)
        else if (!AllUtil.isEmailValid(binding.editTextEmail.text.toString()))
            binding.editTextEmail.error = getString(R.string.enter_valid_email)
        //set conditions for validity of textFields

        else {
            val account = Account()
            //add details to account
            account.kidsName = binding.editTextKidsName.text.toString()
            account.phone = binding.editTextPhone.text.toString()
            account.address = binding.editTextAddress.text.toString()
            account.email = binding.editTextEmail.text.toString()
            account.financeMailId =" "
            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                viewModel.isLoadingDialogVisible.value = true
                viewModel.isDataLoadedToDatabase = false
                showUploadingDialog()

                if (viewModel.imageBitmap != null)
                    viewModel.imageFile = saveFileToLocalCache(viewModel.imageBitmap)

                viewModel.saveDetails(account)
                Log.d("save","details")

                viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
                    if (!it) {
                        if (viewModel.isDataLoadedToDatabase) {
                            Log.d("div", "AccountFragment L331 ${viewModel.imageUri}")
                            if (viewModel.imageFile != null) {
                                binding.imageViewProfileImage.setImageURI(viewModel.imageUri)
                                saveImage(viewModel.imageBitmap!!)
                            }
                            viewModel.imageFile = null



                            binding.imageButtonCapture.visibility = View.INVISIBLE
                            //make textFields non-editable
                            convertFields(binding.editTextKidsName, false)
                            convertFields(binding.editTextPhone, false)
                            convertFields(binding.editTextAddress, false)
                            convertFields(binding.editTextFinancialMail, false)
                            convertFields(binding.editTextEmail, false)
                            //change background of non-editable fields
                            binding.editTextName.setBackgroundResource(R.drawable.round_edittext)
                            binding.editTextKidsAge.setBackgroundResource(R.drawable.round_edittext)
                            binding.editTextKidsGender.setBackgroundResource(R.drawable.round_edittext)
                            binding.editTextCurrentSubscribedPlan.setBackgroundResource(R.drawable.round_edittext)
                            binding.editTextSubscriptionStartDate.setBackgroundResource(R.drawable.round_edittext)
                            binding.textViewKidsError.text = ""
                        } else {
                            Toast.makeText(
                                activity,
                                getString(R.string.failed_to_load_to_database),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
            } else {
                //showInternetNotConnectedDialog()
                val snackbar = Snackbar.make(
                    binding.layout,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.retry)) {
                        save()
                    }
                snackbar.show()
            }

        }
    }

    private fun saveImage(finalBitmap: Bitmap) {
        try {
            var imageFile: File?
            //val state: String = Environment.getExternalStorageState()
            var folder: File?
            folder = File(
                requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    .toString() + "/UpTodd"
            )
            var success = true
            if (!folder.exists()) {
                success = folder.mkdirs()
            }
            if (success) {
                //imageFile = File(imagePath!!)             //overwrite to capturedImage in CaptureImageFragment
                imageFile = File(folder.absolutePath + File.separator + "Profile.jpg")
                imageFile.createNewFile()
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.image_not_saved),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }
            val ostream = ByteArrayOutputStream()

            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream)
            val fout = FileOutputStream(imageFile)
            fout.write(ostream.toByteArray())
            fout.close()
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
            activity?.contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            //activity?.contentResolver?.openOutputStream(Uri.fromFile(imageFile))
            //Toast.makeText(activity, "Saved", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.d("div", "GenerateCardFragment L408 $e")
        }
    }

    private fun saveFileToLocalCache(finalBitmap: Bitmap?): File? {
        try {
            var imageFile: File? = null
            val state: String = Environment.getExternalStorageState()
            var folder: File? = null
            if (android.os.Build.VERSION.SDK_INT >= 29)
                folder = File(
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .toString() + "/UpToddCards"
                )
            else
                folder =
                    File(Environment.getExternalStorageDirectory().toString() + "/UpToddCards")
            var success = true
            if (!folder.exists()) {
                success = folder.mkdirs()
            }
            if (success) {
                imageFile =
                    File(folder.absolutePath + File.separator + "Profile Image")             //overwrite to capturedImage in CaptureImageFragment
                imageFile.createNewFile()
            } else {
                Toast.makeText(activity, "Image Not saved", Toast.LENGTH_SHORT).show()
                return null
            }
            val ostream = ByteArrayOutputStream()

            finalBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, ostream)
            val fout = FileOutputStream(imageFile)
            fout.write(ostream.toByteArray())
            fout.close()
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
            activity?.contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            viewModel.isSavedToLocal = true
            return imageFile

        } catch (e: Exception) {
            Log.d("div", "GenerateCardFragment L358 $e")
            return null
        }
        /*val file = File(activity?.externalCacheDir, "profilePic.png")
        val fOut = FileOutputStream(file)
        finalBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.flush()
        fOut.close()
        //file.setReadable(true, false)
        viewModel.isSavedToLocal=true
        return file

    } catch (e: Exception) {
        Log.d("div", "AccountFragment L451 $e")
        return null
    }*/
    }

    private fun showInternetNotConnectedDialog() {
        /*val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            "Network not connected",
            "Back",
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })*/

    }

    private fun showUploadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isRepositoryEmpty.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    private fun showNannyUploadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isNannyUpdating.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())
    }

    private fun showConfirmEditDialog() {
        val dialogBinding = DataBindingUtil.inflate<DialogExtendSubscriptionBinding>(
            layoutInflater, R.layout.dialog_extend_subscription, null, false
        )
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.textView.text = getString(R.string.are_you_sure_to_leave_without_editing)
        dialogBinding.buttonYes.setOnClickListener {
            requireActivity().onBackPressed()
        }
        dialogBinding.buttonNo.setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.show()
    }

}