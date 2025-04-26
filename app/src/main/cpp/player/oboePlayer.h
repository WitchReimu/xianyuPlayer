//
// Created by Administrator on 2024/12/1.
//

#ifndef OBOEPLAYER_H
#define OBOEPLAYER_H

#include <oboe/Oboe.h>
#include <memory>
#include "CommonUtils.h"
#include "sonic.h"
#include "decodeStream.h"
#include <jni.h>

class oboePlayer : public oboe::AudioStreamDataCallback, oboe::AudioStreamErrorCallback
{
  public:
	int playCircleType = playListCircle_enum::listCircle;
	static double pts;
	void initStream(decodeStream *stream, JNIEnv *env);
	void openStream();
	bool startPlay();
	bool pausePlay();
	bool closePlay();
	void setSonicSpeed(float speed);
	void setSonicRate(float rate);
	int getPlayerStatus();
	int playStatusChange(oboe::StreamState state);
	void setPlayCircleType(const char *type);
	~oboePlayer();

  private:
	decodeStream *decoderStream = nullptr;
	oboe::AudioStream *oboeAudioStream = nullptr;
	oboe::AudioFormat outputFormat = oboe::AudioFormat::Invalid;
//	sonicStream sonicStreamInstance = {};
	float *readBuffer = nullptr;
	int readBufferLength = 0;
	float sonicSpeed = 1.0f;
	float sonicRate = 1.0f;
	int dataOffset = 0;
	int playerState = 0;
	JavaVM *vm = nullptr;

	int renderAudioData(void *audioData, int32_t numFrames);
	virtual oboe::DataCallbackResult
	onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);
	virtual void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error);

	/**
	 *
	 * @tparam T
	 * @param audioData 待填充的缓冲区数据
	 * @param frame 数据帧
	 * @param byteCount 待填充缓冲区的长度
	 * @return
	 */
	template<typename T>
	int fillData(T audioData, audioFrameQueue::audioFrame_t &frame, int byteCount);
	void sonicFillData(float *audioData,
					   audioFrameQueue::audioFrame_t &frame,
					   int frameSamplesCount);
};

#endif //OBOEPLAYER_H
