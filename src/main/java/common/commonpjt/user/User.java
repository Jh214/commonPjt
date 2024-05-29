package common.commonpjt.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

@Entity
public class User {
    @Id
    @GeneratedValue()
}
