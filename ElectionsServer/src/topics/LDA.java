package topics;

import cc.mallet.classify.tui.Text2Vectors;
import cc.mallet.topics.tui.Vectors2Topics;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import listener.Communication;

public class LDA {

    private static final String STOP_WORDS = "stopwords_se.txt";
    private String ITERATIONS ;
    private String THREADS ;
    private String NUM_TOPICS ;
    private String NOM_WORDS_TO_ANALYZE;
    
    private String username;

    public LDA(Communication c) {
        String received_text = c.ReceiveMessage();
        this.username = received_text.split("-")[0];
        this.ITERATIONS = received_text.split("-")[1];
        this.THREADS = received_text.split("-")[2];
        this.NUM_TOPICS =received_text.split("-")[3];
        this.NOM_WORDS_TO_ANALYZE = received_text.split("-")[4];
        
        ExtractTopics();
    }
    
    public void ExtractTopics() {
        Text2Vectors t2v = new Text2Vectors();
        // form string array args and invoke main of Text2Vectors.
        String[] arguments = {"--input", "tweets/"+username+"/tagged_tweets/",
            "--output", "tweets/"+username+"/lda/topic-input.mallet",
            "--remove-stopwords", "true",
            "--stoplist-file", STOP_WORDS,
            "--token-regex", "[\\p{L}\\p{M}]+",
            "--keep-sequence" 
        };
        try {
            t2v.main(arguments);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LDA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LDA.class.getName()).log(Level.SEVERE, null, ex);
        }
        Vectors2Topics v2t = new Vectors2Topics();
        String[]  arguments2 = {"--num-iterations",ITERATIONS,
            "--num-top-words", NOM_WORDS_TO_ANALYZE,
            "--num-threads", THREADS,
            "--doc-topics-threshold", "0.26",
            "--input", "tweets/"+username+"/lda/topic-input.mallet",
            "--num-topics", NUM_TOPICS,
            "--output-state", "tweets/"+username+"/lda/output_state.gz",
            "--output-topic-keys", "tweets/"+username+"/lda/output_topic_keys",
            "--output-doc-topics", "tweets/"+username+"/lda/output_doc_topics.txt"
        };
        try {
            v2t.main(arguments2);
        } catch (IOException ex) {
            Logger.getLogger(LDA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
