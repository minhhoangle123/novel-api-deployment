package com.crawldata.back_end.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a novel entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Novel {
     /**
     * The unique identifier of the novel.
     */
    String novelId;
    /**
     * The name of the novel.
     */
    String name;
    /**
     * The URL or path to the image associated with the novel.
     */
    String image;
    /**
     * A brief description of the novel.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String description;
    /**
     * The author of the novel.
     */
    Author author;
    /**
     * The novel's first chapter
     * **/
    String firstChapter;

    public String toString()
    {
        return novelId + "\n" + name + "\n" + image + "\n" + description + "\n" + author + "\n" + firstChapter;
    }

    public Novel noveId(String novelId)
    {
        setNovelId(novelId);
        return this;
    }
    public Novel name(String name)
    {
        setName(name);
        return this;
    }
    public Novel image(String image)
    {
        setImage(image);
        return this;
    }
    public Novel description(String description)
    {
        setDescription(description);
        return this;
    }
    public Novel author(Author author)
    {
        setAuthor(author);
        return this;
    }
    public Novel firstChapter(String firstChapter)
    {
        setFirstChapter(firstChapter);
        return this;
    }
}
