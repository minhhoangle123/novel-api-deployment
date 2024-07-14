package com.crawldata.back_end.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a chapter entity within a novel.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chapter {
    /**
     * The unique identifier of the novel to which the chapter belongs.
     */
    String novelId;
    /**
     * The name of the novel to which the chapter belongs.
     */
    String novelName;
    /**
     * The unique identifier of the chapter.
     */
    String chapterId;
    /**
     the novel's nextChapterId
     **/
    private String nextChapterId;
    /**
     the novel's preChapterId
     **/
    private String preChapterId;
    /**
     * The name of the chapter.
     */
    String name;
    /**
     * The author of the novel containing the chapter.
     */
    Author author;
    /**
     * The content of the chapter.
     */
    String content;

    public String toString()
    {
        return novelId + "\n" + novelName + "\n" + chapterId + "\n" + preChapterId + "\n" + name + "\n" + author + "\n" + content + "\n" + nextChapterId;
    }

    public Chapter novelId(String novelId)
    {
        setNovelId(novelId);
        return this;
    }
    public Chapter novelName(String novelName)
    {
        setNovelName(novelName);
        return this;
    }
    public Chapter chapterId(String chapterId)
    {
        setChapterId(chapterId);
        return this;
    }
    public Chapter nextChapterId(String nextChapterId)
    {
        setNextChapterId(nextChapterId);
        return this;
    }
    public Chapter preChapterId(String preChapterId)
    {
        setPreChapterId(preChapterId);
        return this;
    }
    public Chapter name(String name)
    {
        setName(name);
        return this;
    }
    public Chapter author(Author author)
    {
        setAuthor(author);
        return this;
    }
    public Chapter content(String content)
    {
        setContent(content);
        return this;
    }

}
