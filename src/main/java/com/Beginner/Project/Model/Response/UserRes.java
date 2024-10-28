package com.Beginner.Project.Model.Response;

import com.Beginner.Project.Model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRes {
    private Long id;
    private String userName;
    private String roleName;

    public static UserRes convertToRes(User user) {
        return new UserRes(user.getId(), user.getUserName(), user.getRole().getName());
    }
}
