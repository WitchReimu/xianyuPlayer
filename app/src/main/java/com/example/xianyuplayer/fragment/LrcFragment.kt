package com.example.xianyuplayer.fragment

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.xianyuplayer.R
import com.example.xianyuplayer.adapter.LrcAdapter
import com.example.xianyuplayer.adapter.helper.TestSnapHelper
import com.example.xianyuplayer.database.LrcBean
import com.example.xianyuplayer.databinding.FragmentLrcBinding
import java.io.File
import kotlin.math.abs

class LrcFragment : Fragment() {
    private val TAG = "LrcFragment"

    private lateinit var binding: FragmentLrcBinding
    private lateinit var lrcAdapter: LrcAdapter
    private val testSnapHelper = TestSnapHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLrcBinding.inflate(inflater, container, false)
        lrcAdapter = LrcAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycleLrc.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recycleLrc.adapter = lrcAdapter
        val file = File("/sdcard/Download/test_lrc.lrc")
        testSnapHelper.attachToRecyclerView(binding.recycleLrc)

        try {
            val parseList = LrcBean.parseList(file)
            lrcAdapter.setLrcData(parseList)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }


        binding.recycleLrc.addOnScrollListener(object : OnScrollListener() {

            // TODO: 拖动歌词，显示歌词开始时间，并完成跳转。
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    val rect = Rect()
                    val lrcRect = Rect()
                    val childAt = binding.recycleLrc.getChildAt(4)

                    if (childAt != null) {
                        childAt.getGlobalVisibleRect(rect)
                        Log.i(
                            TAG,
                            "onViewCreated: --> left,${rect.left} top,${rect.top} right,${rect.right} bottom,${rect.bottom}"
                        )
                        val txtLrcTime = binding.txtLrcTime
                        txtLrcTime.getGlobalVisibleRect(lrcRect)
                        val intersects = Rect.intersects(rect, lrcRect)
                        val contains = rect.contains(lrcRect)
                        Log.i(TAG, "onViewCreated: --> intersects $intersects")
                        Log.i(TAG, "onViewCreated: --> contains $contains")
                    }
                    binding.linearLrcContainer.visibility = View.GONE
                } else if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    binding.linearLrcContainer.visibility = View.VISIBLE
                }
            }
        })
    }
}