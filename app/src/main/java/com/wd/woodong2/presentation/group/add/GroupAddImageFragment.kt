package com.wd.woodong2.presentation.group.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.wd.woodong2.R
import com.wd.woodong2.databinding.GroupAddImageFragmentBinding

class GroupAddImageFragment: Fragment() {
    companion object {
        fun newInstance() = GroupAddImageFragment()
    }

    private var _binding: GroupAddImageFragmentBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: GroupAddSharedViewModel by activityViewModels {
        GroupAddSharedViewModelFactory(requireContext())
    }

    private lateinit var currentItem: String

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    when(currentItem) {
                        "imgMainImage" -> {
                            binding.imgMainImage.load(uri)
                            binding.imgMainImageInit.isVisible = false
                        }
                        "imgBackgroundImage" -> {
                            binding.imgBackgroundImage.load(uri)
                            binding.imgBackgroundImageInit.isVisible = false
                        }
                    }
                    sharedViewModel.setImage(currentItem, uri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GroupAddImageFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        imgMainImage.setOnClickListener {
            currentItem = "imgMainImage"
            galleryLauncher.launch(
                Intent(Intent.ACTION_PICK).setDataAndType(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*"
                )
            )
        }

        imgBackgroundImage.setOnClickListener {
            currentItem = "imgBackgroundImage"
            galleryLauncher.launch(
                Intent(Intent.ACTION_PICK).setDataAndType(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*"
                )
            )
        }

        btnAddGroup.setOnClickListener {
            btnAddGroup.isClickable = false //같은 모임 중복 생성 방지
            sharedViewModel.setGroupAddItem()
        }
    }

    private fun initViewModel() = with(sharedViewModel) {
        isLoadingState.observe(viewLifecycleOwner) { isLoadingState ->
            binding.progressBar.isVisible = isLoadingState
            if(isLoadingState) {
                Toast.makeText(requireContext(), R.string.group_add_toast_create_group_loading, Toast.LENGTH_SHORT).show()
            }
        }

        isCreateSuccess.observe(viewLifecycleOwner) { isCreateSuccess ->
            if(isCreateSuccess) {
                Toast.makeText(requireContext(), R.string.group_add_toast_create_group, Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), R.string.group_add_toast_no_info, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}