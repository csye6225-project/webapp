package com.example.demo.repository1;

import com.example.demo.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageRepo1 extends JpaRepository<Image, String> {
    @Query("select i from Image i where i.user_id=:uid")
    Image findImageByUser_id(@Param("uid") String uid);
}
