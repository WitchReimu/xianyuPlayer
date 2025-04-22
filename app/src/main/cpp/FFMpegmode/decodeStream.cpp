//
// Created by Administrator on 2024/11/28.
//

#include "decodeStream.h"
#define TAG "decodeStream"

decodeStream::decodeStream(const char *path, JNIEnv *env, jobject *nativeBridgeClass)
{
  strcpy(this->path, path);
//	strcpy(this->path, "/sdcard/Download/Tifa_Morning_Cowgirl_4K.mp4");
  nativeBridge = env->NewGlobalRef(*nativeBridgeClass);
  env->GetJavaVM(&vm);
  initStream();
}

decodeStream::~decodeStream()
{
  bool isAttach = false;
  JNIEnv *env = getJniEnv(vm, isAttach);

  if (env != nullptr && nativeBridge != nullptr)
  {
	env->DeleteGlobalRef(nativeBridge);

	if (isAttach)
	{
	  vm->DetachCurrentThread();
	}
  }
  swr_free(&swrContext);
  avcodec_free_context(&audioDecodeContext);
  avformat_close_input(&formatContext);
  avformat_free_context(formatContext);
}

//初始化解码流与数据缓冲区
void decodeStream::initStream()
{
  decodeState = Initing;
  formatContext = avformat_alloc_context();
  openStream();
}

void decodeStream::decodeFile()
{
  if (decodeThread == nullptr)
  {
	decodeState = Running;
	decodeThread = new std::thread(doDecode, this);
  }
}

//开始解码
void decodeStream::doDecode(decodeStream *instance)
{
  AVPacket *pPacket = av_packet_alloc();
  AVFrame *pFrame = av_frame_alloc();
  struct timeval curTimeStamp{};
  bool isAttach = false;
  JNIEnv *env = getJniEnv(instance->vm, isAttach);
  jclass bridgeClass = nullptr;
  jmethodID methodId = nullptr;

  if (env != nullptr)
  {
	bridgeClass = env->GetObjectClass(instance->nativeBridge);
	methodId = env->GetStaticMethodID(bridgeClass, "notifyDtsChange", "(D)V");
  }

  while (instance->decodeState == Running || instance->decodeState == Prepared)
  {

	if (instance->seekPosition >= 0 && instance->seekPosition < instance->getAudioDuration())
	{
	  std::unique_lock<std::mutex> lock(instance->decodeMutex);
	  int ret = avformat_seek_file(instance->formatContext,
								   -1,
								   INT64_MIN,
								   instance->seekPosition,
								   INT64_MAX,
								   0);
	  if (ret >= 0)
	  {
		avcodec_flush_buffers(instance->audioDecodeContext);
	  }
	  instance->seekPosition = -1;
	}
	int ret = av_read_frame(instance->formatContext, pPacket);

	if (ret == AVERROR_EOF)
	{
	  ALOGW("[%s] 解码完成", __FUNCTION__);
	  break;
	} else if (ret < 0)
	{
	  ALOGE("[%s] 解码异常结束. 错误原因 %s", __FUNCTION__, av_err2str(ret));
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
		av_frame_unref(pFrame);
		av_packet_unref(pPacket);
		ALOGW("[%s] receive frame failed ,failed information %s",
			  __FUNCTION__,
			  av_err2str(ret));
		continue;
	  } else if (ret < 0)
	  {
		ALOGE("[%s] 接受音频帧失败, 错误原因 %s", __FUNCTION__, av_err2str(ret));
		break;
	  }

	  if (pFrame->pkt_dts != AV_NOPTS_VALUE)
	  {
		gettimeofday(&curTimeStamp, nullptr);
		long msec = curTimeStamp.tv_sec * 1000 + curTimeStamp.tv_usec / 1000;
		long intervalTimeS = msec - instance->lastTimeStamp;
		if (intervalTimeS > 100)
		{
		  instance->lastTimeStamp = msec;
		  double dts = pFrame->pkt_dts * av_q2d(instance->audioStreamTimeBase);

		  if (methodId != nullptr)
			env->CallStaticVoidMethod(bridgeClass, methodId, dts);
		}
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
	  audioFrameQueue::audioFrame_t &produceFrame = frameQueue.frameQueue[frameQueue.produceIndex];
	  int bytePerSample = av_get_bytes_per_sample(instance->targetFmt);
	  double pts = pFrame->pts * av_q2d(instance->audioStreamTimeBase);
	  //判断当前队列缓冲区长度是否大于音频帧的缓冲长度,小于->创建一个新的缓冲区 对新的缓冲区填充音频数据，将新的缓冲区加入队列中，将在下次使用;队列内的旧缓冲区进行释放. 大于->对队列缓冲区内的内容进行覆盖。
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

		while (frameQueue.isFull() && instance->decodeState == Running)
		{
		  std::unique_lock<std::mutex> lock(instance->decodeMutex);
		  instance->decodeCon.wait(lock);
		  lock.unlock();
		}
		struct audioFrameQueue::audioFrame_t frame = {bufferData,
													  covert_length * nb_channels * bytePerSample,
													  buffer_length,
													  pts};
		//如果缓冲区队列内的buffer长度小于解码缓冲区内buffer的长度需要重新设置缓冲区队列的缓冲帧
		frameQueue.resetAudioFrame(frameQueue.produceIndex, frame);
	  } else
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

		while (frameQueue.isFull() && instance->decodeState == Running)
		{
		  std::unique_lock<std::mutex> lock(instance->decodeMutex);
		  instance->decodeCon.wait(lock);
		  lock.unlock();
		}
		produceFrame.pts = pts;
		frameQueue.resetDataLength(frameQueue.produceIndex,
								   covert_length * nb_channels * bytePerSample);
	  }
	  av_packet_unref(pPacket);
	}
  }

  if (isAttach)
	instance->vm->DetachCurrentThread();

  av_frame_free(&pFrame);
  av_packet_free(&pPacket);
  instance->decodeState = Stop;
  ALOGI("[%s] 音频解码线程结束", __FUNCTION__);
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
		ALOGW("[%s] oboe undefine sample format %d",
			  __FUNCTION__,
			  audioDecodeContext->sample_fmt);
		targetFmt = AV_SAMPLE_FMT_FLT;
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

void decodeStream::changeStream(const char *path)
{
  strcpy(this->path, path);
  decodeState = Stop;

  if (decodeThread != nullptr)
  {
	decodeCon.notify_one();
	decodeThread->join();
  }
  decodeThread = nullptr;
  avformat_close_input(&formatContext);
  avcodec_free_context(&audioDecodeContext);
  queue.reset();
  audioDecode = nullptr;
  streamIndex = -1;
  decodeState = Idle;
  openStream();
}

void decodeStream::openStream()
{
  int ret = avformat_open_input(&formatContext, this->path, nullptr, nullptr);

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
  audioStreamTimeBase = audioStream->time_base;
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

bool decodeStream::seekToPosition(long position)
{
  seekPosition = position * 1000;

  if (position > getAudioDuration())
	return false;
  return true;
}

int64_t decodeStream::getAudioDuration()
{
  return formatContext->duration;
}

void decodeStream::requestNextAudioFile()
{
  if (nativeBridge != nullptr)
  {
	bool attach = false;
	JNIEnv *env = getJniEnv(vm, attach);

	if (env != nullptr)
	{
	  jclass nativeClass = env->GetObjectClass(nativeBridge);
	  jmethodID javaMethod = env->GetStaticMethodID(nativeClass, "nextAudio", "()V");
	  if (javaMethod == nullptr)
	  {
		ALOGE("[%s] javaMethod is null ", __FUNCTION__);
		return;
	  }
	  env->CallStaticVoidMethod(nativeClass, javaMethod);

	  if (attach)
		vm->DetachCurrentThread();
	}
  }
}

void decodeStream::requestRestartAudioFile()
{
  if (nativeBridge != nullptr)
  {
	bool attach = false;
	JNIEnv *env = getJniEnv(vm, attach);

	if (env != nullptr)
	{
	  jclass nativeClass = env->GetObjectClass(nativeBridge);
	  jmethodID javaMethod = env->GetMethodID(nativeClass, "requestRestartAudioFile", "()V");
	  if (javaMethod == nullptr)
	  {
		ALOGE("[%s] javaMethod is null ", __FUNCTION__);
		return;
	  }
	  env->CallVoidMethod(nativeBridge, javaMethod);

	  if (attach)
		vm->DetachCurrentThread();
	}
  }
}
