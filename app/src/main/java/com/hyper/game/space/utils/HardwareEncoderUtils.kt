package com.hyper.game.space.utils

import android.media.MediaCodecInfo
import android.media.MediaCodecList

data class VideoProfile(val width: Int, val height: Int, val fps: Int)

object HardwareEncoderUtils {
    fun getBestSupportedProfile(): VideoProfile {
        // Find best hardware accelerated AVC/HEVC encoder
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (info in codecList.codecInfos) {
            if (!info.isEncoder) continue
            if (!info.isHardwareAccelerated) continue
            
            val types = info.supportedTypes
            if (types.contains("video/avc") || types.contains("video/hevc")) {
                val mimeType = if (types.contains("video/hevc")) "video/hevc" else "video/avc"
                val caps = info.getCapabilitiesForType(mimeType)
                val videoCaps = caps?.videoCapabilities
                
                // Check common profiles in descending order
                val targets = listOf(
                    VideoProfile(3840, 2160, 60), // 4K 60
                    VideoProfile(2560, 1440, 60), // 2K 60
                    VideoProfile(1920, 1080, 90), // FHD 90
                    VideoProfile(1920, 1080, 60), // FHD 60
                    VideoProfile(1280, 720, 60)   // HD 60
                )
                
                for (target in targets) {
                    if (videoCaps?.isSizeSupported(target.width, target.height) == true) {
                        // Approximation: check if framerate is achievable at that size
                        val supportedFps = videoCaps.getSupportedFrameRatesFor(target.width, target.height)
                        if (supportedFps != null && supportedFps.upper >= target.fps) {
                            return target
                        }
                    }
                }
            }
        }
        return VideoProfile(1920, 1080, 60) // Fallback Default
    }

    fun getProfileFromString(resolutionStr: String, fps: Int): VideoProfile {
        return when (resolutionStr) {
            "4K UHD" -> VideoProfile(3840, 2160, fps)
            "2K QHD" -> VideoProfile(2560, 1440, fps)
            "1080p FHD" -> VideoProfile(1920, 1080, fps)
            "720p HD" -> VideoProfile(1280, 720, fps)
            else -> VideoProfile(1920, 1080, fps)
        }
    }
}
