package com.project.DuAnTotNghiep.dto.Account;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignBranchRequest {
    private Long accountId;
    private Long branchId;
}
