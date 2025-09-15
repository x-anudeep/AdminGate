package app.model;

import java.util.Arrays;

public class Article {
    private String title;
    private String[] authors; // Assuming authors are stored as an array of strings
    private String abstractText;
    private String[] keywords; // Array for keywords
    private String body;
    private String[] references; // Array for references

    public Article(String title, String[] authors, String abstractText, String[] keywords, String body, String[] references) {
        this.title = title;
        this.authors = authors;
        this.abstractText = abstractText;
        this.keywords = keywords;
        this.body = body;
        this.references = references;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String getBody() {
        return body;
    }

    public String[] getReferences() {
        return references;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setReferences(String[] references) {
        this.references = references;
    }

    // toString() method for easy display
    @Override
    public String toString() {
        return "Article{" +
                "title='" + title + '\'' +
                ", authors=" + Arrays.toString(authors) +
                ", abstractText='" + abstractText + '\'' +
                ", keywords=" + Arrays.toString(keywords) +
                ", body='" + body + '\'' +
                ", references=" + Arrays.toString(references) +
                '}';
    }
}
