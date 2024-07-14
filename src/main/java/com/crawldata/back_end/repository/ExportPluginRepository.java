package com.crawldata.back_end.repository;

import com.crawldata.back_end.model.export_plugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportPluginRepository extends JpaRepository<export_plugin, String> {
}
