package common.commonpjt.website;

import common.commonpjt.website.entity.Website;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebsiteRepository extends JpaRepository<Website, Long> {
}
