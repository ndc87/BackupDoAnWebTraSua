package com.project.DuAnTotNghiep.entity;

import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "size")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Size implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Nationalized
    private String name;

    @Column(name = "delete_flag", nullable = false)
    private boolean deleteFlag = false;
}
