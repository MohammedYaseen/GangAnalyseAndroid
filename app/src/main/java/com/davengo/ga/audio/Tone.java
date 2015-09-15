package com.davengo.ga.audio;

import android.media.AudioTrack;
import android.os.AsyncTask;

/**
 * Created by malkhameri on 11.08.2015.
 */
public class Tone implements Comparable<Tone> {
    public static int ms;
    public static Note note;
    private Integer priority;
    private boolean audioTrackInitialized = false;
    private byte []  audioData;
    private int length;
    private AudioTrack audioTrack;

    public Tone(Note note, int ms) {
        this.note = note;
        this.ms = ms;
        this.priority = 0;
    }

    public Tone(Note note, int ms, int priority) {
        this.note = note;
        this.ms = ms;
        this.priority = priority;
    }

    public enum Note {

        REST, A4, A4$, B4, C4, C4$, D4, D4$, E4, F4, F4$, G4, G4$, A5, A5$, B5, C5, C5$, D5, D5$, E5, F5, F5$, G5, G5$, A6, A6$, B6, C6, C6$, D6, D6$, E6, F6, F6$, G6, G6$, A7, A7$, B7, C7, C7$, D7, D7$, E7, F7, F7$, G7, G7$, A8, A8$, B8, C8, C8$, D8, D8$, E8, F8, F8$, G8, G8$;
        public static final int SAMPLE_RATE = 16 * 1024; // ~16KHz
        private byte[] sin = new byte[SAMPLE_RATE];

        Note() {
            int n = this.ordinal();
            if (n > 0) {
                double exp = ((double) n - 1) / 12d;
                double f = 440d * Math.pow(2d, exp);
                for (int i = 0; i < sin.length; i++) {
                    double period = SAMPLE_RATE / f;
                    double angle = 2.0 * Math.PI * i / period;
                    sin[i] = (byte) (Math.sin(angle) * 127f);
                }
            }
        }

        public byte[] data() {
            return sin;
        }
    }

    //		@Override
    public int compareTo(Tone t) {
        return this.priority.compareTo(t.priority);
    }

    public void play(AudioTrack audioTrack, Note note, int ms) {
        ms = Math.min(ms, 1000);
        this.audioTrack = audioTrack;
        length = Note.SAMPLE_RATE * ms / 1000;
        audioData = note.data();
        // align data, cause sample size is 16 Bit
        if (audioData != null && audioTrack != null) {
            new PlayToneTask().execute();
            //audioTrack.release();
            //audioTrack.play();
        }

        //audioTrack.write(note.data(), 0, ms);

        //audioTrack.flush();

/*        while (!toneQueue.isEmpty()) {
            try {
                Tone tone = toneQueue.take();
                audioTrack.write(tone.note.data(), 0, (length % 2 == 0 ? length : length + 1));
                //audioTrack.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        audioTrack.play();*/

    }
    private class PlayToneTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            audioTrack.write(audioData, 0, (length % 2 == 0 ? length : length + 1));
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
