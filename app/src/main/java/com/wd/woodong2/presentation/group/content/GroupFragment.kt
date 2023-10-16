package com.wd.woodong2.presentation.group.content

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.firebase.database.FirebaseDatabase
import com.wd.woodong2.data.repository.GroupRepositoryImpl
import com.wd.woodong2.databinding.GroupFragmentBinding

class GroupFragment : Fragment() {
    companion object {
        fun newInstance() = GroupFragment()
    }
    private var _binding : GroupFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupViewModel by viewModels { GroupViewModelFactory() }

    private val groupListAdapter by lazy {
        GroupListAdapter(
            itemClickListener = { item ->
                // GroupDetailActivity 이동
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GroupFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        recyclerViewGroup.adapter = groupListAdapter
        viewModel.getGroupItem()
    }

    private fun initViewModel() = with(viewModel) {
        groupList.observe(viewLifecycleOwner) {
            groupListAdapter.submitList(it)
        }
        loadingState.observe(viewLifecycleOwner) { loadingState ->
            if(!loadingState) {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}