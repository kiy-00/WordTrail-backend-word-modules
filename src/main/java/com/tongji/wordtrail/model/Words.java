package com.tongji.wordtrail.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.List;

@Document(collection = "words")
public class Words {
    @JsonSerialize(using = ToStringSerializer.class)
    @Id
    private ObjectId id; // 使用 ObjectId 类型

    private String word;
    private String language;
    private int difficulty;
    private List<String> synonyms;
    private List<String> antonyms;
    private List<String> tags;
    private List<Phonetic> phonetics;
    private List<PartOfSpeech> partOfSpeechList;

    // Getter 和 Setter
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public List<String> getSynonyms() { return synonyms; }
    public void setSynonyms(List<String> synonyms) { this.synonyms = synonyms; }

    public List<String> getAntonyms() { return antonyms; }
    public void setAntonyms(List<String> antonyms) { this.antonyms = antonyms; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<Phonetic> getPhonetics() { return phonetics; }
    public void setPhonetics(List<Phonetic> phonetics) { this.phonetics = phonetics; }

    public List<PartOfSpeech> getPartOfSpeechList() { return partOfSpeechList; }
    public void setPartOfSpeechList(List<PartOfSpeech> partOfSpeechList) { this.partOfSpeechList = partOfSpeechList; }

    // 嵌套类：Phonetic
    public static class Phonetic {
        private String ipa;
        private String audio;

        public String getIpa() { return ipa; }
        public void setIpa(String ipa) { this.ipa = ipa; }

        public String getAudio() { return audio; }
        public void setAudio(String audio) { this.audio = audio; }
    }

    // 嵌套类：PartOfSpeech
    public static class PartOfSpeech {
        private String type;
        private List<String> definitions;
        private List<Example> examples;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<String> getDefinitions() { return definitions; }
        public void setDefinitions(List<String> definitions) { this.definitions = definitions; }

        public List<Example> getExamples() { return examples; }
        public void setExamples(List<Example> examples) { this.examples = examples; }
    }

    // 嵌套类：Example
    public static class Example {
        private String sentence;
        private String translation;

        public String getSentence() { return sentence; }
        public void setSentence(String sentence) { this.sentence = sentence; }

        public String getTranslation() { return translation; }
        public void setTranslation(String translation) { this.translation = translation; }
    }
}
