//
// Created by Administrator on 2024/12/1.
//

#ifndef OBOEPLAYER_H
#define OBOEPLAYER_H

#include <oboe/Oboe.h>
#include <memory>
#include "../LogUtils.h"
#include "decodeStream.h"

class oboePlayer: public oboe::AudioStreamDataCallback, oboe::AudioStreamErrorCallback
{
public:
	void initStream(decodeStream *stream);
	bool startPlay();
	~oboePlayer();
private:
	oboe::AudioStreamBuilder audioBuilder;
	decodeStream *decoderStream = nullptr;
	oboe::AudioStream *oboeAudioStream = nullptr;
	oboe::AudioFormat outputFormat= oboe::AudioFormat::Invalid;
	int dataOffset = 0;

	int renderAudioData(void *audioData, int32_t numFrames);
	virtual oboe::DataCallbackResult
	onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);
	virtual void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error);
	template<typename T>
	/**
	 *
	 * @tparam T
	 * @param audioData 待填充的缓冲区数据
	 * @param frame 数据帧
	 * @param byteCount 待填充缓冲区的长度
	 * @return
	 */
	int fillData(T audioData, audioFrameQueue::audioFrame_t &frame, int byteCount);
};


#endif //OBOEPLAYER_H
