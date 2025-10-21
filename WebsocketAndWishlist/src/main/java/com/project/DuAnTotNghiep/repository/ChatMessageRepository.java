package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    @Query("SELECT m FROM ChatMessageEntity m ORDER BY m.createDate ASC")
    List<ChatMessageEntity> findAllOrderByCreateDateAsc();
}