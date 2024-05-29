package common.commonpjt.user.dto;

import common.commonpjt.user.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequest {
    private String userId;
    private String password;
    private String userName;
    private String userEmail;
    private String userPhone;

    public User toEntity() {
        return User.builder()
                .userId(userId)
                .password(password)
                .userName(userName)
                .userEmail(userEmail)
                .userPhone(userPhone)
                .build();
    }
}
