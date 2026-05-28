package com.bird.StarrySkyTeaHouse.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import com.bird.StarrySkyTeaHouse.R

class TeaMusic private constructor(context: Context) {

    companion object {
        @Volatile
        private var sInstance: TeaMusic? = null

        fun getInstance(context: Context): TeaMusic {
            return sInstance ?: synchronized(this) {
                sInstance ?: TeaMusic(context.applicationContext).also { sInstance = it }
            }
        }
    }

    private val mSoundPool: SoundPool
    private val mButtonTapSoundId: Int
    private val mAppContext = context.applicationContext
    private val mAudioManager = mAppContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseBackgroundForFocusLoss()
            AudioManager.AUDIOFOCUS_GAIN -> resumeBackgroundAfterFocusGain()
        }
    }
    private var mBgmPlayer: MediaPlayer? = null
    private var mAudioFocusRequest: AudioFocusRequest? = null
    private var mButtonTapLoaded = false
    private var mResumeAfterFocusGain = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mSoundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
        mButtonTapSoundId = mSoundPool.load(mAppContext, R.raw.button_tap, 1)
        mSoundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (sampleId == mButtonTapSoundId && status == 0) mButtonTapLoaded = true
        }
        rebuildBackgroundPlayer()
    }

    fun playBackground() {
        if (!requestAudioFocus()) return
        try {
            if (mBgmPlayer?.isPlaying == false) {
                mBgmPlayer?.start()
            }
        } catch (_: IllegalStateException) {
            rebuildBackgroundPlayer()
        }
    }

    fun stopBackground() {
        try {
            if (mBgmPlayer?.isPlaying == true) {
                mBgmPlayer?.pause()
            }
        } catch (_: IllegalStateException) {
            rebuildBackgroundPlayer()
        }
        mResumeAfterFocusGain = false
        abandonAudioFocus()
    }

    fun playButtonTap() {
        if (!mButtonTapLoaded) return
        mSoundPool.play(
            mButtonTapSoundId,
            TeaMusicContract.BUTTON_TAP_VOLUME,
            TeaMusicContract.BUTTON_TAP_VOLUME,
            1,
            0,
            1f
        )
    }

    @Suppress("DEPRECATION")
    private fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = mAudioFocusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                .build()
                .also { mAudioFocusRequest = it }
            mAudioManager.requestAudioFocus(request)
        } else {
            mAudioManager.requestAudioFocus(
                mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    @Suppress("DEPRECATION")
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioFocusRequest?.let(mAudioManager::abandonAudioFocusRequest)
        } else {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener)
        }
    }

    private fun pauseBackgroundForFocusLoss() {
        try {
            mResumeAfterFocusGain = mBgmPlayer?.isPlaying == true
            mBgmPlayer?.pause()
        } catch (_: IllegalStateException) {
            rebuildBackgroundPlayer()
        }
    }

    private fun resumeBackgroundAfterFocusGain() {
        if (!mResumeAfterFocusGain) return
        mResumeAfterFocusGain = false
        playBackground()
    }

    private fun rebuildBackgroundPlayer() {
        mBgmPlayer?.release()
        mBgmPlayer = MediaPlayer.create(mAppContext, R.raw.bgm)?.apply {
            isLooping = true
            setVolume(TeaMusicContract.BGM_VOLUME, TeaMusicContract.BGM_VOLUME)
        }
    }
}
