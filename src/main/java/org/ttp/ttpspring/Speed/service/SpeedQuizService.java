package org.ttp.ttpspring.Speed.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ttp.ttpspring.Speed.Entity.SpeedQuiz;
import org.ttp.ttpspring.Speed.repository.SpeedQuizRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SpeedQuizService {

    @Autowired
    SpeedQuizRepository speedQuizRepository;

    public List<SpeedQuiz> getRandomQuizzes(int count) {
        return speedQuizRepository.findRandomQuizzes(count);
    }

}
