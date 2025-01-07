//
// Created by Administrator on 2024/11/22.
//

#include "fileMetaDataInfo.h"
#include <limits>
#define TAG "fileMetaDataInfo"

fileMetaDataInfo::fileMetaDataInfo(const char *filePath)
{
  if (strcmp(filePath, "") == 0)
  {
	return;
  }
  avFormatContext = avformat_alloc_context();

  initMetadataInfo(filePath);
}

fileMetaDataInfo::~fileMetaDataInfo()
{
  avformat_close_input(&avFormatContext);
  avformat_free_context(avFormatContext);
}

std::vector<std::shared_ptr<const AVDictionaryEntry>> fileMetaDataInfo::getMetaData()
{
  if (isInit)
  {
	dictionEntrys.clear();
	const AVDictionaryEntry *dictionaryEntry = nullptr;
	dictionaryEntry = av_dict_iterate(avFormatContext->metadata, dictionaryEntry);
	std::shared_ptr<const AVDictionaryEntry> entryPtr(dictionaryEntry);

	while (dictionaryEntry != nullptr)
	{
	  dictionEntrys.push_back(entryPtr);
	  dictionaryEntry = av_dict_iterate(avFormatContext->metadata, dictionaryEntry);
	  entryPtr.reset(dictionaryEntry);
	}
  } else
  {
	ALOGE("[%s] get metaData failed not success init ", __FUNCTION__);
  }
  return dictionEntrys;
}

void fileMetaDataInfo::setFilePath(const char *filePath)
{
  if (isInit)
  {
	avformat_close_input(&avFormatContext);
  }

  initMetadataInfo(filePath);
}

void fileMetaDataInfo::initMetadataInfo(const char *filePath)
{
  char path[NAME_MAX] = {0};
  strcpy(path, filePath);
  int ret = avformat_open_input(&avFormatContext, path, nullptr, nullptr);

  if (ret != 0)
  {
	ALOGE("[%s] open input error ", __FUNCTION__);
	return;
  }
  ret = avformat_find_stream_info(avFormatContext, nullptr);

  if (ret < 0)
  {
	avformat_close_input(&avFormatContext);
	ALOGE("[%s] find stream info error ", __FUNCTION__);
	return;
  }
  isInit = true;
}

