package com.example.xianyuplayer.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
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
        binding.recycleCurrentPlayList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recycleCurrentPlayList.adapter = playListAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.playerListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                playListAdapter.addListData(it)
            }
        }

        Log.i(TAG, "onViewCreated: --> ${binding.recycleCurrentPlayList.bottom}")
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}