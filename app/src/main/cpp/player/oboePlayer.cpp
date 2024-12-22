//
// Created by Administrator on 2024/12/1.
//

#include "oboePlayer.h"
#define TAG "oboePlayer"

oboePlayer::~oboePlayer()
{

}

void oboePlayer::initStream(decodeStream *stream)
{

	decoderStream = stream;
	audioBuilder.setDirection(oboe::Direction::Output);
	audioBuilder.setAudioApi(oboe::AudioApi::AAudio);
	audioBuilder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
	audioBuilder.setSampleRate(stream->getDecodeFileSampleRate());
	audioBuilder.setChannelCount(stream->getDecodeFileChannelCount());

	switch (stream->getDecodeFileFormat())
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

oboe::DataCallbackResult
oboePlayer::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames)
{
	int state = renderAudioData(audioData, numFrames);

	if (state == decodeStream::decodeState_enmu::Running)
		return oboe::DataCallbackResult::Continue;
	else
		return oboe::DataCallbackResult::Stop;
}

void oboePlayer::onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error)
{
	AudioStreamErrorCallback::onErrorAfterClose(oboeStream, error);
}

int oboePlayer::renderAudioData(void *audioData, int32_t numFrames)
{
	int samplesCount = decoderStream->getDecodeFileChannelCount() * numFrames;
	int byteCount = 0;
	int16_t *int16Data = nullptr;
	int32_t *int32Data = nullptr;
	float *floatData = nullptr;

	if (decoderStream->queue.isEmpty())
		return decoderStream->getDecodeState();
	audioFrameQueue::audioFrame_t
		&frame = decoderStream->queue.frameQueue[decoderStream->queue.consumeIndex];

	switch (outputFormat)
	{
	case oboe::AudioFormat::I16:
		int16Data = static_cast<int16_t *>(audioData);
		byteCount = samplesCount * sizeof(int16_t);
		fillData(int16Data, frame, byteCount);
		break;
	case oboe::AudioFormat::I32:
		int32Data = static_cast<int32_t *>(audioData);
		byteCount = samplesCount * sizeof(int32_t);
		fillData(int32Data, frame, byteCount);
		break;
	case oboe::AudioFormat::Float:
		floatData = static_cast<float *>(audioData);
		byteCount = samplesCount * sizeof(float);
		fillData(floatData, frame, byteCount);
		break;
	}

	return decodeStream::decodeState_enmu::Running;
}

bool oboePlayer::startPlay()
{
	if (oboeAudioStream != nullptr)
	{
		oboe::Result result = oboeAudioStream->requestStart();
		return result == oboe::Result::OK;
	}
	return false;
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
	}
	else
	{
		//待填充缓冲区的长度大于数据帧内的数据长度，将数据帧剩余的数据复制到待填充缓冲区内
		int leftDataLength = (frame.dataLength - dataOffset);
		memcpy(audioData, frame.buffer + dataOffset, leftDataLength);
		decoderStream->queue.consumeIndex =
			(decoderStream->queue.consumeIndex + 1) % decoderStream->queue.length;
		decoderStream->notifyCond();
		dataOffset = 0;

		if (decoderStream->queue.isEmpty())
			return 1;
		//取出缓冲区内的下一帧数据，将待填充区内的其他部分填充音频数据
		audioFrameQueue::audioFrame_t
			&nextFrame = decoderStream->queue.frameQueue[decoderStream->queue.consumeIndex];

		size_t bytePerSample = sizeof(audioData[0]);
		memcpy(audioData + (leftDataLength / bytePerSample),
		       nextFrame.buffer,
		       byteCount - leftDataLength);
		dataOffset = dataOffset + byteCount - leftDataLength;
	}
	return 0;
}
