package com.crawldata.back_end.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
/**
 * Represents an author entity.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Author {
     /**
     * The unique identifier of the author.
     */
    String authorId;
     /**
     * The name of the author.
     */
    String name;

    public Author authorId(String authorId)
    {
        setAuthorId(authorId);
        return this;
    }

    public Author name(String name)
    {
        setName(name);
        return this;
    }
}
