package stagger;

import listener.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.su.ling.stagger.FormatException;
import se.su.ling.stagger.TagNameException;
import stagger.TagFile;

/**
 *
 * @author filippia
 */
public class AnalyzeText {

    Communication c;
    TagFile taggedInput;

    public AnalyzeText(Communication c, TagFile taggedInput) {
        this.taggedInput = taggedInput;
        this.c = c;
        try {
            ExchangeMassages();
        } catch (IOException | ClassNotFoundException | FormatException | TagNameException ex) {
            Logger.getLogger(AnalyzeText.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void ExchangeMassages() throws IOException, ClassNotFoundException, FormatException, TagNameException {
        String taggedSentense, received_text, cleaned_received_text;
        while (true) {
            //String received_text = c.ReceiveMessage().replaceAll("[^\\u0000-\\uFFFF]", "").replaceAll("@", "").replaceAll("#", "").replaceAll("_", "").replaceAll("~", "");
            received_text = c.ReceiveMessage();
            cleaned_received_text = received_text.replaceAll("[^\\x00-\\x7F]", "");
            
            if (received_text.startsWith("EOF") || received_text.startsWith("ERROR")) {
                break;
            } else {
                //If more than 50% of the text consists of invalid characters, then the whole text is considered invalid
                if (cleaned_received_text.length() < received_text.length()/2)
                    taggedSentense = "";
                else
                    taggedSentense = taggedInput.getTaggedText(received_text);
                //System.out.println("tagged: " + taggedSentense);
                if ("ERROR".equals(c.SendMessage(taggedSentense + "\n"))) {
                    break;
                }
            }
        }
    }
}
