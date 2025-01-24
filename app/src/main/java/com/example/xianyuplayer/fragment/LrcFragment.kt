package com.example.xianyuplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.xianyuplayer.MusicNativeMethod
import com.example.xianyuplayer.database.parseList
import com.example.xianyuplayer.databinding.FragmentLrcBinding
import com.github.gzuliyujiang.wheelview.LrcBean
import com.github.gzuliyujiang.wheelview.annotation.ScrollState
import com.github.gzuliyujiang.wheelview.contract.OnWheelChangedListener
import com.github.gzuliyujiang.wheelview.widget.WheelView
import java.io.File

class LrcFragment : Fragment() {
    private val TAG = "LrcFragment"

    private lateinit var binding: FragmentLrcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLrcBinding.inflate(inflater, container, false)

        binding.wheelViewLrc.setOnWheelChangedListener(object : OnWheelChangedListener {
            override fun onWheelScrolled(view: WheelView?, offset: Int) {

            }

            override fun onWheelSelected(view: WheelView?, position: Int) {

            }

            override fun onWheelScrollStateChanged(view: WheelView?, state: Int) {

                if (state == ScrollState.IDLE) {
                    binding.wheelViewLrc.setIndicatorEnabled(false)
                    val item = binding.wheelViewLrc.data[binding.wheelViewLrc.currentPosition]
                    // TODO: 歌词跳转未完成 很重要
                    if (item is LrcBean) {
                        MusicNativeMethod.getInstance().seekPosition(item.time)
                    }
                }
            }

            override fun onWheelLoopFinished(view: WheelView?) {

            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val file = File("/sdcard/Download/test_lrc.lrc")
        try {
            val parseList = LrcBean().parseList(file)
            binding.wheelViewLrc.setData(parseList)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}