//
// Created by Administrator on 2025/1/2.
//

#include "AudioAlbumCover.h"

#define TAG "AudioAlbumCover"

AudioAlbumCover::AudioAlbumCover(const char *absolutePath, int length)
{
	strcpy(path, absolutePath);
}

AudioAlbumCover::~AudioAlbumCover()
{
	avformat_close_input(&formatContext);
	avformat_free_context(formatContext);
}
std::pair<int, char *> AudioAlbumCover::getAlbumCover()
{
	formatContext = avformat_alloc_context();
	int ret = avformat_open_input(&formatContext, path, nullptr, nullptr);

	std::pair<int, char *> data(0, nullptr);
	if (ret < 0)
	{
		ALOGE("[%s] error string -> %s", __FUNCTION__, av_err2str(ret));
		return data;
	}
	ret = avformat_find_stream_info(formatContext, nullptr);

	if (ret < 0)
	{
		ALOGE("[%s] error string -> %s", __FUNCTION__, av_err2str(ret));
		return data;
	}
	AVPacket *pPacket = av_packet_alloc();

	for (int i = 0; i < formatContext->nb_streams; ++i)
	{
		av_read_frame(formatContext, pPacket);
		if (formatContext->streams[pPacket->stream_index]->codecpar->codec_type
			== AVMEDIA_TYPE_VIDEO && formatContext->streams[pPacket->stream_index]->disposition
			== AV_DISPOSITION_ATTACHED_PIC)
		{

			if (pPacket->size < 0)
			{
				continue;
			}
			char *buffer = new char[pPacket->size];
			memcpy(buffer, pPacket->data, pPacket->size);
			data.first = pPacket->size;
			data.second = buffer;
			av_packet_unref(pPacket);
			break;
		}
	}
	av_packet_free(&pPacket);
	return data;
}

