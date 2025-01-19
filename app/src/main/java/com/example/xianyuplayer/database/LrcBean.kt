package com.example.xianyuplayer.database

import com.github.gzuliyujiang.wheelview.LrcBean
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern


fun LrcBean.parseList(lrcFile: File): ArrayList<LrcBean> {
    if (!lrcFile.exists() || !lrcFile.canRead()) {
        throw IllegalArgumentException("permission error or file not exist ")
    }
    val lrcList = ArrayList<LrcBean>()
    val bufferedReader =
        BufferedReader(InputStreamReader(FileInputStream(lrcFile), StandardCharsets.UTF_8))

    bufferedReader.use { reader ->
        var line = reader.readLine()

        while (line != null) {
            val lineLrc = parseLine(line)
            lrcList.addAll(lineLrc)
            line = reader.readLine()
        }
    }
    lrcList.sortBy {
        it.time
    }
    return lrcList
}

private fun LrcBean.parseLine(line: String): ArrayList<LrcBean> {
    val lrcBeans = ArrayList<LrcBean>()
    val regex = "\\[(\\d{2}):(\\d{2}).(\\d{0,3})]"
    val matches = Pattern.compile(regex)
    val matcher = matches.matcher(line)
    val text: String = line.replace(Regex(regex), "").trim()

    while (matcher.find()) {
        val minute = matcher.group(1)!!.toLong()
        val second = matcher.group(2)!!.toLong()
        val millisecond = matcher.group(3)!!.toLong()
        val time: Long = minute * 60L * 1000L + second * 1000L + millisecond
        lrcBeans.add(LrcBean(time, text))
    }
    return lrcBeans
}