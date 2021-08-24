package com.amarchaud.amdraganddrop.screen.new_user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.amarchaud.amdraganddrop.R
import com.amarchaud.amdraganddrop.databinding.DialogNewUserBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NewUserDialog : DialogFragment() {

    private var _binding: DialogNewUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewUserDialogViewModel by viewModels()

    private val platforms by lazy { arrayOf("iOS", "Android", "Fullstack") }

    override fun getTheme(): Int {
        return R.style.CustomDialog
    }

    companion object {
        const val OUTPUT = "output"
        const val PERSON_ADDED = "person_added"

        fun newInstance(): NewUserDialog {
            return NewUserDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = DialogNewUserBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }



    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            chooseImage.setOnClickListener {
                getPickImageIntent()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            val adapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    platforms
                )
            platform.setAdapter(adapter)

            validateButton.setOnClickListener {

                // other method
                viewModel.addNewUser(
                    EntityOnePerson(
                        name = this.nameEditText.text.toString(),
                        position = this.positionEditText.text.toString(),
                        location = this.locationEditText.text.toString(),
                        platform = this.platform.text.toString(),
                        pic = viewModel.imageUri?.toString()
                    )
                )
            }
        }

        observeTextChanged()
        viewModel.status.observe(viewLifecycleOwner, ::handleStatus)
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data?.clipData != null) {
                    val mClipData = result.data?.clipData
                    for (i in 0 until mClipData!!.itemCount) {
                        val item = mClipData.getItemAt(i)
                        val uri: Uri = item.uri
                        binding.chooseImage.setImageURI(uri)
                        viewModel.imageUri = uri
                    }
                } else if (result.data != null) {
                    val uri = result.data?.data
                    binding.chooseImage.setImageURI(uri)
                    viewModel.imageUri = uri
                }
            }

        }

    private fun getPickImageIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startForResult.launch(intent)
    }

    private fun handleStatus(status: Status?) {
        status?.let {
            when (it) {
                is Status.StatusOk -> {


                    val result: Bundle = Bundle().apply {
                        putParcelable(PERSON_ADDED, it.onePerson)
                    }
                    setFragmentResult(OUTPUT, result)

                    // other method with viewModel
                    //newUserViewModel.createNewUser(it.onePerson)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun observeTextChanged() {
        binding.run {
            lifecycleScope.launch {
                validateButton.isEnabled = false
                validateButton.alpha = 0.5f

                nameEditText
                    .textChanges()
                    .debounce(150)
                    .combine(
                        nameEditText
                            .textChanges()
                            .debounce(150)
                    ) { str1, str2 ->
                        !str1.isNullOrEmpty() && !str2.isNullOrEmpty()
                    }.combine(locationEditText.textChanges().debounce(150)) { isOk, str ->
                        isOk && !str.isNullOrEmpty()
                    }.collect {
                        if (it) {
                            validateButton.isEnabled = true
                            validateButton.alpha = 1f
                        } else {
                            validateButton.isEnabled = false
                            validateButton.alpha = 0.5f
                        }
                    }
            }
        }
    }
}