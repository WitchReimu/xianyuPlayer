//
// Created by Administrator on 2024/11/28.
//

#include "decodeStream.h"
#define TAG "decodeStream"


decodeStream::decodeStream(const char *path)
{
	strcpy(this->path, path);
	initStream();
}
decodeStream::~decodeStream()
{

	avcodec_free_context(&audioDecodeContext);
	avformat_close_input(&formatContext);
	avformat_free_context(formatContext);
}
void decodeStream::initStream()
{
	formatContext = avformat_alloc_context();
	int ret = avformat_open_input(&formatContext, path, nullptr, nullptr);

	if (ret < 0)
	{
		ALOGE("[%s] open input error %d", __FUNCTION__, ret);
		return;
	}
	ret = avformat_find_stream_info(formatContext, nullptr);

	if (ret < 0)
	{
		ALOGE("[%s] find stream info error %d", __FUNCTION__, ret);
	}
	streamIndex = av_find_best_stream(formatContext, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);

	if (streamIndex < 0)
	{
		ALOGE("[%s] find target type failed %d", __FUNCTION__, streamIndex);
	}
	AVStream *audioStream = formatContext->streams[streamIndex];
	audioDecode = avcodec_find_decoder(audioStream->codecpar->codec_id);
	audioDecodeContext = avcodec_alloc_context3(audioDecode);
	avcodec_parameters_to_context(audioDecodeContext, audioStream->codecpar);
	ret = avcodec_open2(audioDecodeContext, audioDecode, nullptr);

	if (ret < 0)
	{
		ALOGE("[%s] audio decode open error %d", __FUNCTION__, ret);
		return;
	}

}
void decodeStream::decodeFile()
{
	if (decodeThread == nullptr)
	{
		decodeThread = new std::thread(doDecode, this);
	}
}

void decodeStream::doDecode(decodeStream *instance)
{
	AVPacket *pPacket = av_packet_alloc();
	AVFrame *pFrame = av_frame_alloc();
	while (instance->decodeState == Running)
	{
		int ret = av_read_frame(instance->formatContext, pPacket);

		if (ret < 0)
		{
			instance->decodeState = Stop;
			break;
		}

		if (pPacket->stream_index == instance->streamIndex && pPacket->size > 0)
		{
			ret = avcodec_send_packet(instance->audioDecodeContext, pPacket);

			if (ret != 0)
			{
				ALOGW("[%s] send packet failed , failed code %d failed information %s",
				      __FUNCTION__,
				      ret,
				      av_err2str(ret));
				continue;
			}
			ret = avcodec_receive_frame(instance->audioDecodeContext, pFrame);

			if (ret < 0)
			{
				av_packet_unref(pPacket);
				ALOGW("[%s] receive frame failed ,failed information %s",
				      __FUNCTION__,
				      av_err2str(ret));
				continue;
			}

			if (instance->initSwrContext())
			{
				ALOGE("[%s] init swr context error ", __FUNCTION__);
				return;
			}
			int buffer_length = av_samples_get_buffer_size(nullptr,
			                                               pFrame->ch_layout.nb_channels,
			                                               pFrame->nb_samples,
			                                               instance->audioDecodeContext
				                                               ->sample_fmt,
			                                               1);
			audioFrameQueue frameQueue = instance->queue;
			audioFrameQueue::audioFrame_t
				&produceFrame = frameQueue.frameQueue[frameQueue.produceIndex];

			if (produceFrame.buffer == nullptr || produceFrame.bufferLength < buffer_length)
			{
				uint8_t *bufferData = static_cast<uint8_t *>(av_malloc(buffer_length));
				int covert_length = instance->covertData(bufferData, pFrame);

				if (covert_length < 0)
				{
					ALOGE("[%s] audio convert error , information --> %s",
					      __FUNCTION__,
					      av_err2str(covert_length));
					continue;
				}

				while (!frameQueue.isFull())
				{
					std::unique_lock<std::mutex> lock(instance->decodeMutex);
					instance->decodeCon.wait(lock);
					lock.unlock();
				}
				struct audioFrameQueue::audioFrame_t
					frame = {bufferData, covert_length, buffer_length};
				frameQueue.resetAudioFrame(frameQueue.produceIndex, frame);
			}
			else
			{
				int covert_length = instance->covertData(produceFrame.buffer, pFrame);
				if (covert_length < 0)
				{
					ALOGE("[%s] audio convert error , information --> %s",
					      __FUNCTION__,
					      av_err2str(covert_length));
					continue;
				}

				while (!frameQueue.isFull())
				{
					std::unique_lock<std::mutex> lock(instance->decodeMutex);
					instance->decodeCon.wait(lock);
					lock.unlock();
				}
				frameQueue.resetDataLength(frameQueue.produceIndex, covert_length);
			}
		}

	}

	av_frame_free(&pFrame);
	av_packet_free(&pPacket);
}

int decodeStream::getDecodeFileSampleRate()
{
	return audioDecodeContext->sample_rate;
}

int decodeStream::getDecodeFileChannelCount()
{
	return audioDecodeContext->ch_layout.nb_channels;
}
bool decodeStream::initSwrContext()
{
	//如果swr为空就进行初始化
	if (swrContext == nullptr)
	{
		int ret = swr_init(swrContext);

		if (ret < 0)
		{
			ALOGE("[%s] swr init error ", __FUNCTION__);
			return false;
		}
		AVSampleFormat targetFmt;

		switch (audioDecodeContext->sample_fmt)
		{
		case AV_SAMPLE_FMT_U8P:
			targetFmt = AV_SAMPLE_FMT_U8;
			break;
		case AV_SAMPLE_FMT_S16P:
			targetFmt = AV_SAMPLE_FMT_S16;
			break;
		case AV_SAMPLE_FMT_S32P:
			targetFmt = AV_SAMPLE_FMT_S32;
			break;
		case AV_SAMPLE_FMT_FLTP:
			targetFmt = AV_SAMPLE_FMT_FLT;
			break;
		case AV_SAMPLE_FMT_DBLP:
			targetFmt = AV_SAMPLE_FMT_DBL;
			break;
		case AV_SAMPLE_FMT_S64P:
			targetFmt = AV_SAMPLE_FMT_S64;
			break;
		default:
			targetFmt = audioDecodeContext->sample_fmt;
			break;
		}
		ret = swr_alloc_set_opts2(&swrContext,
		                          (const AVChannelLayout *)&audioDecodeContext->ch_layout,
		                          targetFmt,
		                          audioDecodeContext->sample_rate,
		                          (const AVChannelLayout *)&audioDecodeContext->ch_layout,
		                          audioDecodeContext->sample_fmt,
		                          audioDecodeContext->sample_rate,
		                          0,
		                          nullptr);

		if (ret < 0)
		{
			ALOGE("[%s] set option error, error information %s", __FUNCTION__, av_err2str(ret));
			return false;
		}
	}
	return true;
}
int decodeStream::covertData(uint8_t *bufferData, AVFrame *frame_ptr)
{
	int covert_length = swr_convert(swrContext,
	                                &bufferData,
	                                frame_ptr->nb_samples,
	                                (const uint8_t **)(&frame_ptr->data[0]),
	                                frame_ptr->nb_samples);
	return covert_length;
}
