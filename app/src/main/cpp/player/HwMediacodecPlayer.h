//
// Created by toptech-6 on 2025/2/11.
//

#ifndef XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_HWMEDIACODECPLAYER_H
#define XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_HWMEDIACODECPLAYER_H

#include <media/NdkMediaExtractor.h>
#include <string>
#include "../CommonUtils.h"

extern "C"
{
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavcodec/bsf.h"
}

class HwMediacodecPlayer
{
  public:
	HwMediacodecPlayer(const char *location);
  private:
	char location[NAME_MAX] = {};
	ANativeWindow *nativeWindow = nullptr;
	AMediaExtractor *extractor = nullptr;
	AMediaCodec *hwVideoDecoder = nullptr;
	AVFormatContext *avFormatContext = nullptr;
	AVCodecContext *videoCodecContext = nullptr;
	const AVBitStreamFilter *videoBitStreamFilter = nullptr;
	AVBSFContext *videoBsfCtx = nullptr;
	int videoStreamIndex = -1;
	void initMediacodec();
	void openFFmpegcodec();
	void startMediacodec();
};

#endif //XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_HWMEDIACODECPLAYER_H
