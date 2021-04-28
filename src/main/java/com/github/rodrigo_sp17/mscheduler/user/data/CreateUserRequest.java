package com.github.rodrigo_sp17.mscheduler.user.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Data
public class CreateUserRequest extends RepresentationModel<CreateUserRequest> {

    private Long userId;

    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String confirmPassword;

    private String name;

    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String socialId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String registrationId;

}
