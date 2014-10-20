package app.repositories;

import app.domain.Quiz;
import app.domain.QuizAnswer;
import app.domain.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    public List<QuizAnswer> findByQuizAndUserNot(Quiz quiz, User user, Pageable pageable);
    
    //show answers with least reviews. placeholders only if no other available
    @Query("select qa from QuizAnswer qa where quiz = :quiz and (user is null or user <> :user) order by placeholder asc, reviewCount asc")
    public List<QuizAnswer> findQuizzesToReview(@Param("quiz") Quiz quiz, @Param("user") User user, Pageable pageable);
    public List<QuizAnswer> findByQuizAndPlaceholderIsTrue(Quiz quiz);
    public List<QuizAnswer> findByQuiz(Quiz quiz);
    public List<QuizAnswer> findByQuiz(Quiz quiz, Pageable pageable);
    public List<QuizAnswer> findByQuizAndUser(Quiz q, User user);
    public List<QuizAnswer> findByUser(User user);
}
