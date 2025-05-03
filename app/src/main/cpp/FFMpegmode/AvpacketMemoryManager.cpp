//
// Created by Administrator on 2025/5/1.
//

#include "AvPacketMemoryManager.h"
#define TAG "AvPacketMemoryManager"

AvPacketMemoryManager::AvPacketMemoryManager()
{

}

AvPacketMemoryManager::~AvPacketMemoryManager()
{
  releaseAllPacketBuffer();
}

bool AvPacketMemoryManager::copyPacket(AVPacket *srcPacket)
{
  AVPacket *dstPacket = av_packet_clone(srcPacket);

  if (dstPacket == nullptr)
  {
	return false;
  }
  packetSize += dstPacket->size;

  packetDataArray.push(dstPacket);
  return true;
}

AVPacket *AvPacketMemoryManager::getPacketData()
{
  if (packetDataArray.size() <= 0)
  {
	return nullptr;
  }
  //todo: 有时为空，原因不知 待修复
  AVPacket *packet = packetDataArray.front();
  if (packet == nullptr)
  {
	ALOGE("[%s] memory packet is null", __FUNCTION__);
  }

  packetDataArray.pop();
  packetSize -= packet->size;
  return packet;
}
bool AvPacketMemoryManager::isFull()
{
  if (packetSize >= PACKET_SIZE_MAX_10MB)
	return true;
  else
	return false;
}
bool AvPacketMemoryManager::isEmpty()
{
  if (packetSize <= 0)
	return true;
  else
	return false;
}
int AvPacketMemoryManager::getPacketSize()
{
  return packetSize;
}

void AvPacketMemoryManager::releaseAllPacketBuffer()
{
  while (!packetDataArray.empty())
  {
	AVPacket *packet = packetDataArray.front();
	packetDataArray.pop();
	av_packet_unref(packet);
	av_packet_free(&packet);
  }
  packetSize = 0;
}
