//
// Created by Administrator on 2024/11/28.
//

#include "decodeStream.h"
#define TAG "decodeStream"


decodeStream::decodeStream(const char *path)
{
	strcpy(this->path, path);
//	strcpy(this->path, "/sdcard/Download/Tifa_Morning_Cowgirl_4K.mp4");
	initStream();
}

decodeStream::~decodeStream()
{
	swr_free(&swrContext);
	avcodec_free_context(&audioDecodeContext);
	avformat_close_input(&formatContext);
	avformat_free_context(formatContext);
}

//初始化解码流与数据缓冲区
void decodeStream::initStream()
{
	queue = audioFrameQueue(3);
	decodeState = Initing;
	formatContext = avformat_alloc_context();
	int ret = avformat_open_input(&formatContext,
	                              path,
	                              nullptr,
	                              nullptr);

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
	ret = avcodec_open2(audioDecodeContext, audioDecode, NULL);

	if (ret < 0)
	{
		ALOGE("[%s] audio decode open error %d", __FUNCTION__, ret);
		return;
	}
	decodeState = Prepared;
}

void decodeStream::decodeFile()
{
	if (decodeThread == nullptr)
	{
		decodeThread = new std::thread(doDecode, this);
	}
}

//开始解码
void decodeStream::doDecode(decodeStream *instance)
{
	AVPacket *pPacket = av_packet_alloc();
	AVFrame *pFrame = av_frame_alloc();

	while (instance->decodeState == Running || instance->decodeState == Prepared)
	{
		int ret = av_read_frame(instance->formatContext, pPacket);

		if (ret == AVERROR_EOF)
		{
			ALOGW("[%s] 解码完成", __FUNCTION__);
			instance->decodeState = Stop;
			break;
		}
		else if (ret < 0)
		{
			ALOGE("[%s] 解码异常结束. 错误原因 %s", __FUNCTION__, av_err2str(ret));
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

			if (ret == AVERROR(EAGAIN))
			{
				av_packet_unref(pPacket);
				ALOGW("[%s] receive frame failed ,failed information %s",
				      __FUNCTION__,
				      av_err2str(ret));
				continue;
			}
			else if (ret < 0)
			{
				ALOGE("[%s] 接受音频帧失败, 错误原因 %s", __FUNCTION__, av_err2str(ret));
				break;
			}

			if (!instance->initSwrContext())
			{
				ALOGE("[%s] init swr context error", __FUNCTION__);
				break;
			}
			int nb_channels = pFrame->ch_layout.nb_channels;
			int buffer_length = av_samples_get_buffer_size(nullptr,
			                                               nb_channels,
			                                               pFrame->nb_samples,
			                                               instance->targetFmt,
			                                               1);
			audioFrameQueue &frameQueue = instance->queue;
			audioFrameQueue::audioFrame_t
				&produceFrame = frameQueue.frameQueue[frameQueue.produceIndex];

			int bytePerSample = av_get_bytes_per_sample(instance->targetFmt);

			//判断当前队列缓冲区长度是否大于音频帧的缓冲长度,小于->创建一个新的缓冲区 对新的缓冲区填充音频数据，将新的缓冲区加入队列中，将在下次使用。队列内的旧缓冲区进行释放，大于->对队列缓冲区内的内容进行覆盖。
			if (produceFrame.buffer == nullptr || produceFrame.bufferLength < buffer_length)
			{
				uint8_t *bufferData = static_cast<uint8_t *>(av_malloc(buffer_length));
				int covert_length = instance->covertData(bufferData, pFrame, buffer_length);

				if (covert_length < 0)
				{
					ALOGE("[%s] audio convert error , information --> %s",
					      __FUNCTION__,
					      av_err2str(covert_length));
					break;
				}

				while (frameQueue.isFull())
				{
					ALOGW("[%s] 队列缓冲区已满", __FUNCTION__);
					std::unique_lock<std::mutex> lock(instance->decodeMutex);
					instance->decodeCon.wait(lock);
					lock.unlock();
				}
				struct audioFrameQueue::audioFrame_t
					frame =
					{bufferData, covert_length * nb_channels * bytePerSample, buffer_length};
				//如果缓冲区队列内的buffer长度小于解码缓冲区内buffer的长度需要重新设置缓冲区队列的缓冲帧
				frameQueue.resetAudioFrame(frameQueue.produceIndex, frame);
			}
			else
			{
				memset(produceFrame.buffer, 0, produceFrame.bufferLength);
				int covert_length =
					instance->covertData(produceFrame.buffer, pFrame, buffer_length);

				if (covert_length < 0)
				{
					ALOGE("[%s] audio convert error , information --> %s",
					      __FUNCTION__,
					      av_err2str(covert_length));
					break;
				}

				while (frameQueue.isFull())
				{
					ALOGW("[%s] 队列缓冲区已满", __FUNCTION__);
					std::unique_lock<std::mutex> lock(instance->decodeMutex);
					instance->decodeCon.wait(lock);
					lock.unlock();
				}
				frameQueue.resetDataLength(frameQueue.produceIndex,
				                           covert_length * nb_channels * bytePerSample);
			}

			av_packet_unref(pPacket);
		}

	}

	av_frame_free(&pFrame);
	av_packet_free(&pPacket);
	ALOGI("[%s] 解码线程结束", __FUNCTION__);
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
		swrContext = swr_alloc();

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
		int ret = swr_alloc_set_opts2(&swrContext,
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
		ret = swr_init(swrContext);

		if (ret < 0)
		{
			ALOGE("[%s] swr init failed. failed information %s", __FUNCTION__, av_err2str(ret));
			return false;
		}
	}
	return true;
}

int decodeStream::covertData(uint8_t *bufferData, AVFrame *frame_ptr, int bufferLength)
{
	int covert_length = swr_convert(swrContext,
	                                &bufferData,
	                                bufferLength / frame_ptr->ch_layout.nb_channels,
	                                (const uint8_t **)(frame_ptr->data),
	                                frame_ptr->nb_samples);
	return covert_length;
}

int decodeStream::getDecodeFileFormat()
{
	return audioDecodeContext->sample_fmt;
}

void decodeStream::notifyCond()
{
	decodeCon.notify_one();
}

int decodeStream::getDecodeState()
{
	return decodeState;
}
