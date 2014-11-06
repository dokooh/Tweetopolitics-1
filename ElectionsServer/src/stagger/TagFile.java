/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stagger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import se.su.ling.stagger.EnglishTokenizer;
import se.su.ling.stagger.FormatException;
import se.su.ling.stagger.LatinTokenizer;
import se.su.ling.stagger.SwedishTokenizer;
import se.su.ling.stagger.TagNameException;
import se.su.ling.stagger.TaggedToken;
import se.su.ling.stagger.Tagger;
import se.su.ling.stagger.Token;
import se.su.ling.stagger.Tokenizer;

public class TagFile {
    Tagger tagger;
    String lang = null;
    int MAX_LENGTH =1000000; 
    
    public TagFile() throws FileNotFoundException, IOException, ClassNotFoundException {
        String modelFile = null;
        modelFile = "swedish.bin";
        ObjectInputStream modelReader = new ObjectInputStream(new FileInputStream(modelFile));
        System.err.println("Loading Stagger model ...");
        tagger = (Tagger) modelReader.readObject();
        lang = tagger.getTaggedData().getLanguage();
        modelReader.close();
        System.err.println("Model loaded and it is ready for use...");
    }

    private static Tokenizer getTokenizer(Reader reader, String lang) {
        Tokenizer tokenizer;
        if (lang.equals("sv")) {
            tokenizer = new SwedishTokenizer(reader);
        } else if (lang.equals("en")) {
            tokenizer = new EnglishTokenizer(reader);
        } else if (lang.equals("any")) {
            tokenizer = new LatinTokenizer(reader);
        } else {
            throw new IllegalArgumentException();
        }
        return tokenizer;
    }

    public String getTaggedText(String textForTagging) throws IOException, ClassNotFoundException, FormatException, TagNameException {
        String outputSentense="";
        String outputEntities="";
        String taggedSentense = "";
        String[] validList = {"IN","NN", "JJ", "PC","PM","RO","VB","UO"};
        
        boolean hasNE = true;
        boolean extendLexicon = true;
        boolean preserve = false;
        boolean plainOutput = false;
        
        TaggedToken[][] inputSents = null;

        tagger.setExtendLexicon(extendLexicon);
        if (!hasNE) {
            tagger.setHasNE(false);
        }

        /*Read the sentense*/
        Reader text_reader = new StringReader(textForTagging);
        Tokenizer tokenizer = getTokenizer(text_reader, lang);
        ArrayList<Token> sentence;
        int sentIdx = 0;
        long base = 0;
        while ((sentence = tokenizer.readSentence()) != null) {
            TaggedToken[] sent = new TaggedToken[sentence.size()];
            if (tokenizer.sentID != null) {
                sentIdx = 0;
            }
            for (int j = 0; j < sentence.size(); j++) {
                Token tok = sentence.get(j);
                String id;
                id = sentIdx + ":" + tok.offset;
                sent[j] = new TaggedToken(tok, id);
            }
            TaggedToken[] taggedSent = tagger.tagSentence(sent, true, false);
            for (TaggedToken mytoken : taggedSent) {
                if (mytoken.lf.length() < 3)
                    continue;
                if (Arrays.asList(validList).contains(tagger.getTaggedData().getPosTagSet().getTagName(mytoken.posTag).split("\\|", 2)[0]) ) {
                    outputSentense += mytoken.lf;
                    outputSentense += " ";
                    if (outputSentense.length() > MAX_LENGTH) {
                        outputSentense="";
                        break;
                    }
                }
                if (mytoken.neTypeTag != -1) {
                    outputEntities += "~"+mytoken.lf+","+tagger.getTaggedData().getNETypeTagSet().getTagName(mytoken.neTypeTag);
                    if (outputEntities.length() > MAX_LENGTH) {
                        outputEntities="";
                        break;
                    }
                }
            } 
            sentIdx++;
        }
        taggedSentense = outputSentense+outputEntities;
        tokenizer.yyclose();
        return taggedSentense;
    }
}
