//
// Created by Administrator on 2025/5/1.
//

#ifndef XIANYUPLAYER_APP_SRC_MAIN_CPP_FFMPEGMODE_AVPACKETMEMORYMANAGER_H
#define XIANYUPLAYER_APP_SRC_MAIN_CPP_FFMPEGMODE_AVPACKETMEMORYMANAGER_H

#define PACKET_SIZE_MAX_10MB 10485760

#include <forward_list>
#include <queue>
#include "CommonUtils.h"

extern "C"
{
#include "libavformat/avformat.h"
}

class AvPacketMemoryManager
{
  public:
	bool copyPacket(AVPacket *srcPacket);
	AVPacket *getPacketData();
	bool isFull();
	bool isEmpty();
	int getPacketSize();
	const int packetCacheStartFlag = PACKET_SIZE_MAX_10MB / 4;
	AvPacketMemoryManager();
	~AvPacketMemoryManager();
  private:
	std::queue<AVPacket *> packetDataArray;
	int packetSize = 0;
};

#endif //XIANYUPLAYER_APP_SRC_MAIN_CPP_FFMPEGMODE_AVPACKETMEMORYMANAGER_H
