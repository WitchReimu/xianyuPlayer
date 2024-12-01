//
// Created by Administrator on 2024/12/1.
//

#ifndef OBOEPLAYER_H
#define OBOEPLAYER_H

#include <oboe/Oboe.h>
#include "../LogUtils.h"
#include "decodeStream.h"

class oboePlayer: public oboe::AudioStreamDataCallback, oboe::AudioStreamErrorCallback
{
public:
	void initStream(decodeStream *stream);
	~oboePlayer();
private:
	oboe::AudioStreamBuilder audioBuilder;
	decodeStream *decoderStream = nullptr;
	oboe::AudioStream *oboeAudioStream = nullptr;

	virtual oboe::DataCallbackResult
	onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

	virtual void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error);
};


#endif //OBOEPLAYER_H
