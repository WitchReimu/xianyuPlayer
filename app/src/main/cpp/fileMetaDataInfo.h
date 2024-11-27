//
// Created by Administrator on 2024/11/22.
//

#ifndef FILEMETADATAINFO_H
#define FILEMETADATAINFO_H

#include "LogUtils.h"
#include <memory>
#include <string>
#include <vector>
extern "C"
{
#include "libavutil/avutil.h"
#include "libavformat/avformat.h"
};

class fileMetaDataInfo
{
public:
	fileMetaDataInfo(const char *filePath = "");
	~fileMetaDataInfo();
	void setFilePath(const char *filePath);
	std::vector<std::shared_ptr<const AVDictionaryEntry>> getMetaData();
private:
	AVFormatContext *avFormatContext = nullptr;
	bool isInit = false;
	std::vector<std::shared_ptr<const AVDictionaryEntry>> dictionEntrys;

	void initMetadataInfo(const char *filePath);
};


#endif //FILEMETADATAINFO_H
