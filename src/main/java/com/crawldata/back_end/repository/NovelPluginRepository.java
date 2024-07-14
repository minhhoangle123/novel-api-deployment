package com.crawldata.back_end.repository;

import com.crawldata.back_end.model.novel_plugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelPluginRepository  extends JpaRepository<novel_plugin, String> {
}
