import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.LinearProbingHashST;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdAudio;


/**
 * Created by andywang on 9/17/16.
 */
public class AutoCensor {
    public static void main(String[] args){
        final int SAMP_FREQ = 44100;


        LinearProbingHashST<String, Queue<double[]>> st = new LinearProbingHashST<String, Queue<double[]>>();

//   LinearProbingHashST<String, Integer> badWords = new LinearProbingHashST<String, Integer>();
        JSONParser parser = new JSONParser();
        double[] audio = StdAudio.read("audio.wav");
        In in = new In("sample.txt");
//      In in2 = new In("badWords.txt");
//      String[] bad = in2.readAllStrings();
//      for (int i = 0; i < bad.length; i++) {
//       badWords.put(bad[i], 0);
//      }


        String s = in.readAll();
        try{
            JSONObject all = (JSONObject)parser.parse(s);
            JSONArray resultsArr = (JSONArray)parser.parse(all.get("results").toString());
            JSONObject alt = (JSONObject)parser.parse(resultsArr.get(0).toString());
            JSONArray altArr = (JSONArray)parser.parse(alt.get("alternatives").toString());
            JSONObject time = (JSONObject)parser.parse(altArr.get(0).toString());
            JSONArray timeArr = (JSONArray)parser.parse(time.get("timestamps").toString());
            for (int i = 0; i < timeArr.size(); i++) {
                JSONArray e = (JSONArray)parser.parse(timeArr.get(i).toString());
                Queue<double[]> q;
                if (st.contains(e.get(0).toString())) {
                    // take Queue out, add to it, put Queue back in
                    q = st.get(e.get(0).toString());
                }
                else {
                    q = new Queue<double[]>();
                }
                double[] t = {Double.parseDouble(e.get(1).toString()), Double.parseDouble(e.get(2).toString())};
                q.enqueue(t);
                st.put(e.get(0).toString(), q);
            }

        }catch(ParseException pe){
            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
        }

        for (String x : st.keys()) {
            if (x.equals("****"))  {
                for (double[] frame : st.get(x)) { // for each timeframe corresponding to this word
                    for (int i = (int)(frame[0]*SAMP_FREQ); i < frame[1]*SAMP_FREQ; i++) {
                        audio[i] = 0.0; // bleep out from st.get(x)[0] to st.get(x)[1];
                    }
                }
            }
//       for (String y : badWords.keys()) {
//           if (x.equalsIgnoreCase(y)) System.out.println(x); // bleep out from st.get(x)[0] to st.get(x)[1]; // if (x.equals("****")
//       }
        }
        StdAudio.save("out.wav", audio);
    }
}

