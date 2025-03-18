//
// Created by Administrator on 2025/3/9.
//

#ifndef XIANYUPLAYER_APP_SRC_MAIN_CPP_FFMPEGMODE_VIDEOENCODESTREAM_H
#define XIANYUPLAYER_APP_SRC_MAIN_CPP_FFMPEGMODE_VIDEOENCODESTREAM_H

extern "C"
{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/opt.h"
#include "libswscale/swscale.h"
}

class VideoEncodeStream
{
  public:
	void transformTest();
	
};

#endif //XIANYUPLAYER_APP_SRC_MAIN_CPP_FFMPEGMODE_VIDEOENCODESTREAM_H
