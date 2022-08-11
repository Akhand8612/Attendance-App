package com.eiraj.intel.drone.Utils.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class PlaySound {

    private Context context;
    private Uri alertToneUri;
    private Ringtone ringtone;

    public PlaySound(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        alertToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        if (alertToneUri == null) {
            alertToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        ringtone = RingtoneManager.getRingtone(context, alertToneUri);
        ringtone.setLooping(true);
        ringtone.setVolume(1.0f);

        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSound() {
        ringtone.play();
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopSound() {
        ringtone.stop();
    }

    public boolean isAlarmPlaying() {
        return ringtone.isPlaying();
    }
}
