import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechTimestamp;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy Wang and Jesse Chou on 9/17/16.
 * Credit Kevin Wayne/Robert Sedgewick
 * Future directions: stereo, realtime, applied to music (with background noise), other formats, bleep
 *
 * wav_file stereo
 * % cuss.wav true
 */
public class AutoCensor {

    private static final int SAMPLE_RATE = 44100;
    private static final String BASE_PATH = System.getProperty("user.dir");
    private static final String CLEAN_AUDIO_SUFFIX = "(clean)";
    private static final String usr = "cba714e0-1997-4641-9d21-78e9e14203f5";
    private static final String pwd = "JOKpzP8VO6YX";

    public static String getFullPathToAudioFile(String fileName) {
        return BASE_PATH + "/audiofiles/" + fileName;
    }

    public static String getFullPathToOutputFile(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        String cleanFileName = fileName.substring(0, dotIndex).concat(CLEAN_AUDIO_SUFFIX).concat(fileName.substring(dotIndex)) ;
        return BASE_PATH + "/cleanfiles/" + cleanFileName;
    }

    public static void main(String[] args) throws Exception {

        // set up words to censor
        List<String> wordList = new ArrayList<String>(); // bad words list
        wordList.add("shit");
        wordList.add("dick");

        String wavFileName = args[0];

        if(wavFileName == null) {
            throw new Exception("no file supplied.");
        }

        String audStr = getFullPathToAudioFile(wavFileName);
        String outputStr = getFullPathToOutputFile(wavFileName);

        File aud = new File(audStr);
        double[] audio = StdAudio.read(audStr);

        // if stereo
        if (args[1].equals("true")) {
            double[] newAudio = new double[audio.length / 2];
            for (int n = 0; n < audio.length / 2; n++) {
                newAudio[n] = audio[n * 2];
            }
            audio = newAudio;
        }

        SpeechToText speechToText = new SpeechToText(usr,pwd);

        RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
                .timestamps(true)
                .profanityFilter(true)
                .continuous(true)
                .build();

        ServiceCall<SpeechResults> serviceCall = speechToText.recognize(aud, recognizeOptions);
        SpeechResults speechResults = serviceCall.execute();

        List<Transcript> transcripts = speechResults.getResults();

        for (Transcript t : transcripts) {
            List<SpeechTimestamp> timeStamps = t.getAlternatives()
                    .get(0)
                    .getTimestamps();

            for (SpeechTimestamp timeStamp: timeStamps) {
                if (timeStamp.getWord().equals("****") || wordList.contains(timeStamp.getWord().toLowerCase())) {
                    for (int i = (int)(timeStamp.getStartTime()*SAMPLE_RATE); i < (int)(timeStamp.getEndTime()*SAMPLE_RATE); i++) {
                        audio[i] = 0;
                    }
                }
            }
        }

        // output wav from double[]

        StdAudio.save(outputStr, audio);
    }
}