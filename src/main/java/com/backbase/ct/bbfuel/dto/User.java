package com.backbase.ct.bbfuel.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String externalId;
    private String fullName;
    private String role;
    private List<String> productGroupNames;
}
