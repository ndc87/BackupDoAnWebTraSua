package com.project.DuAnTotNghiep.dto.Branch;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BranchDTO {
    private Long id;
    private String branchCode;
    private String branchName;
    private String address;
    private String phone;
    private String email;
    private boolean isActive;
}
