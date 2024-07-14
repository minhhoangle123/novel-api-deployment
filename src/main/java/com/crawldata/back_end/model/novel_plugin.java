package com.crawldata.back_end.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "novel_plugin")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class novel_plugin {
    @Id
    private String id;

    @Lob
    @Column(name = "data", columnDefinition="LONGBLOB")
    private byte[] data;
}
