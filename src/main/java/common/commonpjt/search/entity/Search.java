package common.commonpjt.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String link;
    private String snippet;

    private Search(Builder builder) {
        this.title = builder.title;
        this.link = builder.link;
        this.snippet = builder.snippet;
    }

    public static class Builder {
        private final String title;
        private final String link;
        private String snippet;

        public Builder(String title, String link) {
            this.title = title;
            this.link = link;
        }

        public Builder snippet(String snippet) {
            this.snippet = snippet;
            return this;
        }

        public Search build() {
            return new Search(this);
        }
    }
}
