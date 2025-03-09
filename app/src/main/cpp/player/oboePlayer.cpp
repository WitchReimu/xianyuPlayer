//
// Created by Administrator on 2024/12/1.
//

#include "oboePlayer.h"
#include <aaudio/AAudio.h>
#define TAG "oboePlayer"
#define MilliSecondBase 1000000
double oboePlayer::pts = 0;

oboePlayer::~oboePlayer()
{

}

void oboePlayer::initStream(decodeStream *stream, JNIEnv *env)
{
  decoderStream = stream;
  openStream();
  env->GetJavaVM(&vm);
}

oboe::DataCallbackResult
oboePlayer::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames)
{
  int state = renderAudioData(audioData, numFrames);

  if (state == decodeStream::decodeState_enum::Stop)
  {
	if (playCircleType == listCircle)
	{
	  decoderStream->requestNextAudioFile();
	} else if (playCircleType == singleCircle)
	{
	  decoderStream->requestRestartAudioFile();
	}
	return oboe::DataCallbackResult::Stop;
  }
  return oboe::DataCallbackResult::Continue;
}

void oboePlayer::onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error)
{
  //todo:失败后的回调
  AudioStreamErrorCallback::onErrorAfterClose(oboeStream, error);
}

int oboePlayer::renderAudioData(void *audioData, int32_t numFrames)
{
  int samplesCount = oboeAudioStream->getChannelCount() * numFrames;
  int byteCount = 0;
  int16_t *int16Data = nullptr;
  int32_t *int32Data = nullptr;
  float *floatData = nullptr;

  if (decoderStream->queue.isEmpty())
  {
	int state = decoderStream->getDecodeState();
	return state;
  }
  audioFrameQueue::audioFrame_t &frame = decoderStream->queue.frameQueue[decoderStream->queue.consumeIndex];
  pts = frame.pts;

  switch (outputFormat)
  {
	case oboe::AudioFormat::I16:
	  int16Data = static_cast<int16_t *>(audioData);
	  byteCount = samplesCount * sizeof(int16_t);
	  fillData(int16Data, frame, byteCount);
	  break;
	case oboe::AudioFormat::I24:
	case oboe::AudioFormat::I32:
	  int32Data = static_cast<int32_t *>(audioData);
	  byteCount = samplesCount * sizeof(int32_t);
	  fillData(int32Data, frame, byteCount);
	  break;
	case oboe::AudioFormat::Float:
	  floatData = static_cast<float *>(audioData);
	  byteCount = samplesCount * sizeof(float);
//	  fillData(floatData, frame, byteCount);
	  sonicFillData(floatData, frame, samplesCount);
	  break;
  }

  return decodeStream::decodeState_enum::Running;
}

bool oboePlayer::startPlay()
{
  if (oboeAudioStream != nullptr)
  {
	oboe::Result result = oboeAudioStream->requestStart();

	if (result != oboe::Result::OK)
	{
	  ALOGE("Error starting playback stream. Error: %s", oboe::convertToText(result));
	  oboeAudioStream->close();
	}
	playStatusChange(oboeAudioStream->getState());
	return result == oboe::Result::OK;
  }
  return false;
}

bool oboePlayer::pausePlay()
{
  if (oboeAudioStream != nullptr)
  {
	//500毫秒
	oboe::Result pauseResult = oboeAudioStream->pause(500 * MilliSecondBase);
	playStatusChange(oboeAudioStream->getState());
	if (oboe::Result::OK != pauseResult)
	{
	  ALOGE("[%s] error name %s", __FUNCTION__, oboe::convertToText(pauseResult));
	  return false;
	}
	oboe::Result flushResult = oboeAudioStream->requestFlush();
	playStatusChange(oboeAudioStream->getState());
	if (flushResult != oboe::Result::OK)
	{
	  ALOGE("[%s] error name %s", __FUNCTION__, oboe::convertToText(flushResult));
	  return false;
	}
	return true;
  }
  return false;
}

int oboePlayer::getPlayerStatus()
{
  if (oboeAudioStream != nullptr)
  {
	int state = static_cast<int>(oboeAudioStream->getState());
	return state;
  }
  return static_cast<int>(oboe::StreamState::Uninitialized);
}

int oboePlayer::playStatusChange(oboe::StreamState streamState)
{

  switch (streamState)
  {
	case oboe::StreamState::Uninitialized:
	  playerState = 0;
	  break;
	case oboe::StreamState::Unknown:
	  playerState = 1;
	  break;
	case oboe::StreamState::Open:
	  playerState = 2;
	  break;
	case oboe::StreamState::Starting:
	  playerState = 3;
	  break;
	case oboe::StreamState::Started:
	  playerState = 4;
	  break;
	case oboe::StreamState::Pausing:
	  playerState = 5;
	  break;
	case oboe::StreamState::Paused:
	  playerState = 6;
	  break;
	case oboe::StreamState::Flushing:
	  playerState = 7;
	  break;
	case oboe::StreamState::Flushed:
	  playerState = 8;
	  break;
	case oboe::StreamState::Stopping:
	  playerState = 9;
	  break;
	case oboe::StreamState::Stopped:
	  playerState = 10;
	  break;
	case oboe::StreamState::Closing:
	  playerState = 11;
	  break;
	case oboe::StreamState::Closed:
	  playerState = 12;
	  break;
	case oboe::StreamState::Disconnected:
	  playerState = 13;
	  break;
  }
  bool isAttach = false;
  JNIEnv *env = getJniEnv(vm, isAttach);

  if (env != nullptr)
  {
	jclass nativeMethodClass = env->FindClass("com/example/xianyuplayer/MusicNativeMethod");
	jmethodID callBackMethod = env->GetStaticMethodID(nativeMethodClass,
													  "notifyPlayStatusChangeCallback",
													  "(I)V");
	env->CallStaticVoidMethod(nativeMethodClass, callBackMethod, playerState);

	if (isAttach)
	  vm->DetachCurrentThread();
  }
  return playerState;
}

bool oboePlayer::closePlay()
{
  oboe::Result result = oboe::Result::OK;

  if (oboeAudioStream != nullptr)
  {
	result = oboeAudioStream->stop(500 * MilliSecondBase);
	dataOffset = 0;
	oboeAudioStream->close();
	playStatusChange(oboeAudioStream->getState());
	delete oboeAudioStream;
	oboeAudioStream = nullptr;
  }
  sonicDestroyStream(sonicStreamInstance);
  return result == oboe::Result::OK;
}

void oboePlayer::openStream()
{
  oboe::AudioStreamBuilder audioBuilder;
  readBuffer = static_cast<float *>(calloc(1, 1024));
  readBufferLength = 1024;
  int sampleRate = decoderStream->getDecodeFileSampleRate();
  int channelCount = decoderStream->getDecodeFileChannelCount();
  sonicStreamInstance = sonicCreateStream(sampleRate, channelCount);
  sonicSetSpeed(sonicStreamInstance, sonicSpeed);
  sonicSetRate(sonicStreamInstance, sonicRate);
  audioBuilder.setDirection(oboe::Direction::Output);
  audioBuilder.setAudioApi(oboe::AudioApi::AAudio);
  audioBuilder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
  audioBuilder.setSampleRate(sampleRate);
  audioBuilder.setChannelCount(channelCount);

  switch (decoderStream->getDecodeFileFormat())
  {
	case AV_SAMPLE_FMT_S16P:
	case AV_SAMPLE_FMT_S16:
	  outputFormat = oboe::AudioFormat::I16;
	  break;
	case AV_SAMPLE_FMT_S32P:
	case AV_SAMPLE_FMT_S32:
	  outputFormat = oboe::AudioFormat::I32;
	  break;
	case AV_SAMPLE_FMT_FLTP:
	case AV_SAMPLE_FMT_FLT:
	  outputFormat = oboe::AudioFormat::Float;
	  break;
  }
  audioBuilder.setFormat(outputFormat);
  audioBuilder.setDataCallback(this);
  audioBuilder.setErrorCallback(this);
  audioBuilder.openStream(&oboeAudioStream);
}

void oboePlayer::setPlayCircleType(const char *type)
{
  if (strcmp(type, "单曲循环") == 0)
  {
	playCircleType = singleCircle;
  } else if (strcmp(type, "列表循环") == 0)
  {
	playCircleType = listCircle;
  }
}

void oboePlayer::sonicFillData(float *audioData,
							   audioFrameQueue::audioFrame_t &frame,
							   int frameSamplesCount)
{

  int available = sonicSamplesAvailable(sonicStreamInstance) * oboeAudioStream->getChannelCount();

  if (available < frameSamplesCount)
  {
	float PDouble[frame.dataLength / sizeof(float)];
	memcpy(PDouble, frame.buffer, frame.dataLength);
	sonicWriteFloatToStream(sonicStreamInstance,
							PDouble,
							frame.dataLength /
							(sizeof(float) * oboeAudioStream->getChannelCount()));

	decoderStream->queue.consumeIndex = (decoderStream->queue.consumeIndex + 1) %
										decoderStream->queue.length;
	decoderStream->notifyCond();
  }
  available = sonicSamplesAvailable(sonicStreamInstance);

  if (available > 0)
  {
	int samplesCountLength = frameSamplesCount * sizeof(float);

	if (readBufferLength < samplesCountLength)
	{
	  readBufferLength = samplesCountLength;
	  realloc(readBuffer, readBufferLength);
	}
	memset(readBuffer, 0, readBufferLength);
	int readCount = sonicReadFloatFromStream(sonicStreamInstance,
											 readBuffer,
											 frameSamplesCount /
											 oboeAudioStream->getChannelCount());
	memcpy(audioData, readBuffer, samplesCountLength);
  }
}

void oboePlayer::setSonicSpeed(float speed)
{
  if (speed <= 0)
	return;
  sonicSpeed = speed;
}

void oboePlayer::setSonicRate(float rate)
{
  if (rate < 0)
	return;
  sonicRate = rate;
}

template<typename T>
int oboePlayer::fillData(T audioData, audioFrameQueue::audioFrame_t &frame, int byteCount)
{
  memset(audioData, 0, byteCount);

  if (frame.dataLength - dataOffset > byteCount)
  {
	//待填充缓冲区的长度小于数据帧内的数据长度，直接将数据帧的数据复制到待填充缓冲区内
	memcpy(audioData, frame.buffer + dataOffset, byteCount);
	dataOffset += byteCount;
  } else
  {
	//待填充缓冲区的长度大于数据帧内的数据长度，将数据帧剩余的数据复制到待填充缓冲区内
	int leftDataLength = (frame.dataLength - dataOffset);
	memcpy(audioData, frame.buffer + dataOffset, leftDataLength);
	//循环获得缓冲队列内的音频帧直到将填充区内的缓冲区填满，或者在填充区内未填满的情况下出现缓冲队列内的音频帧为空的情况。
	for (int i = 0; i < decoderStream->queue.length; ++i)
	{
	  decoderStream->queue.consumeIndex = (decoderStream->queue.consumeIndex + 1) %
										  decoderStream->queue.length;
	  dataOffset = 0;

	  if (decoderStream->queue.isEmpty())
	  {
		decoderStream->notifyCond();
		return 1;
	  }
	  audioFrameQueue::audioFrame_t &nextFrame = decoderStream->queue.frameQueue[decoderStream->queue.consumeIndex];

	  if (nextFrame.dataLength - dataOffset > byteCount - leftDataLength)
	  {
		size_t bytePerSample = sizeof(audioData[0]);
		memcpy(audioData + (leftDataLength / bytePerSample),
			   nextFrame.buffer,
			   byteCount - leftDataLength);
		dataOffset = dataOffset + byteCount - leftDataLength;
		break;
	  } else
	  {
		size_t bytePerSample = sizeof(audioData[0]);
		memcpy(audioData + (leftDataLength / bytePerSample),
			   nextFrame.buffer,
			   nextFrame.bufferLength);
	  }
	}
	decoderStream->notifyCond();
  }
  return 0;
}
