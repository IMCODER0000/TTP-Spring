package org.ttp.ttpspring.Speed.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ttp.ttpspring.Speed.Entity.SpeedQuiz;

import java.util.List;

@Repository
public interface SpeedQuizRepository extends JpaRepository<SpeedQuiz, Long> {


    @Query(value = "SELECT * FROM speed_quiz ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<SpeedQuiz> findRandomQuizzes(@Param("count") int count);

}
