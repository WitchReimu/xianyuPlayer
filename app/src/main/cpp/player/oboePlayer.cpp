//
// Created by Administrator on 2024/12/1.
//

#include "oboePlayer.h"

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
	audioBuilder.setDataCallback(this);
	audioBuilder.setErrorCallback(this);
	audioBuilder.openStream(&oboeAudioStream);
}
oboe::DataCallbackResult
oboePlayer::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames)
{

	return oboe::DataCallbackResult::Continue;
}
void oboePlayer::onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error)
{
	AudioStreamErrorCallback::onErrorAfterClose(oboeStream, error);
}
