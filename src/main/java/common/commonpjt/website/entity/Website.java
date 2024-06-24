package common.commonpjt.website.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Table(name = "TB_WEBSITE")
public class Website {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String url;
    @NonNull
    private String title;
    @NonNull
    private String description;

    @ManyToMany(mappedBy = "websites")
    private Set<SearchHistory> searchHistories = new HashSet<>();

    // Getters and setters
}