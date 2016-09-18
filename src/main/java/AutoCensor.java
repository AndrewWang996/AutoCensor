import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechTimestamp;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Andy Wang and Jesse Chou on 9/17/16.
 * Credit Kevin Wayne/Robert Sedgewick
 */
public class AutoCensor {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {

//        // create a new POST request
//        PostMethod method = new PostMethod();
//
//        // add Headers
//        method.addRequestHeader("Content-Type","audio/flac");
//        method.addRequestHeader("Transfer-Encoding","chunked");

        final int SAMPLE_RATE = 44100;

        String usr = "cba714e0-1997-4641-9d21-78e9e14203f5";
        String pwd = "JOKpzP8VO6YX";
        String audStr = "/Users/andywang/Downloads/cuss.wav";

        File aud = new File(audStr);

        double[] audio = StdAudio.read(audStr);

        SpeechToText speechToText = new SpeechToText(usr,pwd);

        RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
                .timestamps(true)
                .profanityFilter(true)
                .build();

        ServiceCall<SpeechResults> serviceCall = speechToText.recognize(aud, recognizeOptions);
        SpeechResults speechResults = serviceCall.execute();

        List<Transcript> transcripts = speechResults.getResults();
        List<SpeechTimestamp> timeStamps = transcripts.get(0)
                .getAlternatives()
                .get(0)
                .getTimestamps();

        for (SpeechTimestamp timeStamp: timeStamps) {
            if (timeStamp.getWord().equals("****")) {
                double start = timeStamp.getStartTime();
                double end = timeStamp.getEndTime();
                for (int i = (int)(start*SAMPLE_RATE); i < (int)(end*SAMPLE_RATE); i++) {
                    audio[i] = 0;
                }
            }
        }

        // output wav from double[]
        StdAudio.save("output.wav", audio);
    }
}

