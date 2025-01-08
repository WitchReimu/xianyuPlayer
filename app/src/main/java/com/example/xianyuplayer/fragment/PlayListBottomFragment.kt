package com.example.xianyuplayer.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.Constant
import com.example.xianyuplayer.ItemTouchHelperCallback
import com.example.xianyuplayer.R
import com.example.xianyuplayer.adapter.PlayListAdapter
import com.example.xianyuplayer.databinding.BottomFragmentPlayListBinding
import com.example.xianyuplayer.vm.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlayListBottomFragment(private val mainViewModel: MainViewModel) :
    BottomSheetDialogFragment() {

    private lateinit var binding: BottomFragmentPlayListBinding
    private lateinit var playListAdapter: PlayListAdapter
    private val TAG = "PlayListBottomFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.playListBottomSheetDialogStyle)
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
            layoutParams.height = (Constant.displayHeightExcludeSystem * 0.6).toInt()
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

        mainViewModel.playerListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                playListAdapter.addListData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}