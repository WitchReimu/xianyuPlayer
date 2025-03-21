package com.example.xianyuplayer.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.Constant
import com.example.xianyuplayer.adapter.helper.ItemTouchHelperCallback
import com.example.xianyuplayer.PlayerApplication
import com.example.xianyuplayer.R
import com.example.xianyuplayer.adapter.PlayListAdapter
import com.example.xianyuplayer.databinding.BottomFragmentPlayListBinding
import com.example.xianyuplayer.vm.PlayListBottomVMFactory
import com.example.xianyuplayer.vm.PlayListBottomViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlayListBottomFragment() : BottomSheetDialogFragment() {

    private lateinit var binding: BottomFragmentPlayListBinding
    private lateinit var playListAdapter: PlayListAdapter
    private lateinit var viewModel: PlayListBottomViewModel
    private val TAG = "PlayListBottomFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.playListBottomSheetDialogStyle)
        viewModel = ViewModelProvider(
            viewModelStore,
            PlayListBottomVMFactory(PlayerApplication.getInstance().repository)
        )[TAG, PlayListBottomViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomFragmentPlayListBinding.inflate(inflater, container, false)
        playListAdapter = PlayListAdapter()

        binding.recycleCurrentPlayList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = playListAdapter
        }
        val helperCallback = ItemTouchHelperCallback(playListAdapter)
        val itemTouchHelper = ItemTouchHelper(helperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recycleCurrentPlayList)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.playList.observe(viewLifecycleOwner) {
            if (it != null) {
                playListAdapter.addListData(it)
            }
        }
        //动态设置底部弹出框高度
        val layoutParams = binding.root.layoutParams
        layoutParams.height = (Constant.displayHeightExcludeSystem * 0.6).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}