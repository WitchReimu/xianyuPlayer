//
// Created by Administrator on 2024/12/1.
//

#ifndef OBOEPLAYER_H
#define OBOEPLAYER_H

#include <oboe/Oboe.h>
#include <memory>
#include "../CommonUtils.h"
#include "decodeStream.h"
#include <jni.h>

class oboePlayer : public oboe::AudioStreamDataCallback, oboe::AudioStreamErrorCallback
{
  public:
	void initStream(decodeStream *stream, JNIEnv *env);
	void openStream();
	bool startPlay();
	bool pausePlay();
	bool closePlay();
	int getPlayerStatus();
	int playStatusChange(oboe::StreamState state);
	int playCircleType = playListCircle_enum::listCircle;
	~oboePlayer();

  private:
	decodeStream *decoderStream = nullptr;
	oboe::AudioStream *oboeAudioStream = nullptr;
	oboe::AudioFormat outputFormat = oboe::AudioFormat::Invalid;
	int dataOffset = 0;
	int playerState = 0;
	jobject activityObject = nullptr;
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
};

#endif //OBOEPLAYER_H
