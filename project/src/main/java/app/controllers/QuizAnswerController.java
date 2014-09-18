package app.controllers;

import app.domain.QuizAnswer;
import app.repositories.QuizAnswerRepository;
import app.repositories.QuizRepository;
import app.services.QuizService;
import java.util.List;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class QuizAnswerController {
    @Autowired
    private QuizAnswerRepository quizAnswerRepository;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private QuizService quizService;
    
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/quiz/{quizId}/answer", method = RequestMethod.POST, consumes = "application/json")
    public List<QuizAnswer> newAnswer(@PathVariable Long quizId, @Valid @RequestBody QuizAnswer quizAnswer) {
        List<QuizAnswer> answersToReview = quizService.sumbitAnswer(quizAnswer, quizId);
        return answersToReview;
    }
    
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/quiz/{quizId}/answer/{answerId}", method = RequestMethod.GET, produces = "application/json")
    public QuizAnswer getAnswer(@PathVariable Long answerId) {
        return quizAnswerRepository.findOne(answerId);
    }
    
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/quiz/{quizId}/answer", method = RequestMethod.GET, produces = "application/json")
    public List<QuizAnswer> getAnswers(@PathVariable Long quizId) {
        return quizAnswerRepository.findByQuiz(quizRepository.findOne(quizId));
    }
    
    @ResponseBody
    @RequestMapping(value = "/answer", method = RequestMethod.GET, produces = "application/json")
    public List<QuizAnswer> getAllAnswers(@PathVariable Long quizId) {
        return quizAnswerRepository.findAll();
    }
}