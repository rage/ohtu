package app.controllers;

import app.Application;
import app.domain.Quiz;
import app.domain.QuizAnswer;
import app.repositories.QuizAnswerRepository;
import app.repositories.QuizRepository;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class QuizAnswerControllerTest {
    @Autowired
    private QuizAnswerRepository quizAnswerRepository;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    
    private Quiz quiz;
    
    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        
        TestHelper.addQuizWithOneQuestion(mockMvc, "testquiz1", "testquestion1", true);
        
        this.quiz = quizRepository.findAll().get(0);
    }
    
    @Test
    @DirtiesContext
    public void testAddAnswer() throws Exception {
        String jsonQuiz = "{\"user\": \"matti\","
                         + "\"answer\": \"vastaus\"}";
        
        this.mockMvc.perform(post("/quiz/1/answer").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));
        
        QuizAnswer quizAnswer = quizAnswerRepository.findOne(1L);
        
        Assert.assertEquals("matti", quizAnswer.getUser().getName());
        Assert.assertEquals("vastaus", quizAnswer.getAnswer());
    }
    
    @Test
    @DirtiesContext
    public void testPostAnswerReturnsTwoAnswers() throws Exception {
        String jsonQuiz = "{\"user\": \"eero\","
                         + "\"answer\": \"vastaus\"}";
        this.mockMvc.perform(post("/quiz/"+ quiz.getId() + "/answer")
                             .content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));
        
        jsonQuiz = "{\"user\": \"masa\","
                         + "\"answer\": \"vastaus\"}";
        this.mockMvc.perform(post("/quiz/"+ quiz.getId() + "/answer")
                             .content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));
        
        jsonQuiz = "{\"user\": \"kalevi\","
                         + "\"answer\": \"vastaus\"}";
        MvcResult mvcAnswer = this.mockMvc.perform(post("/quiz/"+ quiz.getId() + "/answer")
                             .content(jsonQuiz).contentType(MediaType.APPLICATION_JSON)).andReturn();
        
        JSONObject obj = new JSONObject(mvcAnswer.getResponse().getContentAsString());
   
        assertEquals(2, new JSONArray(obj.getString("answers")).length());
    }
    
    @Test
    @DirtiesContext
    public void testCorrectNumberOfAnswers() throws Exception {
        String jsonAnswer = "{\"user\": \"ulla\","
                         + "\"answer\": \"vastaus\"}";
        
        for (int i = 0; i < 4; i++) {
            this.mockMvc.perform(post("/quiz/1/answer").content(jsonAnswer).contentType(MediaType.APPLICATION_JSON));
        }
        
        assertEquals(4, quizAnswerRepository.count());
    }
    
    @Test
    @DirtiesContext
    public void testGetAnswer() throws Exception {
        TestHelper.addAnAnswer(mockMvc, "testikysymys", "testivastaus", "matti", 1L);
        
        MvcResult mvcAnswer = this.mockMvc.perform(get("/quiz/1/answer/1")
                            .contentType(MediaType.APPLICATION_JSON))
                            .andReturn();
        
        String response = mvcAnswer.getResponse().getContentAsString();
        
        assertTrue(response.contains("\"user\":\"matti\""));
        assertTrue(response.contains("\\\"question\\\":\\\"testikysymys\\\""));
        assertTrue(response.contains("\\\"value\\\":\\\"testivastaus\\\""));
    }
    
    @Test
    @DirtiesContext
    public void testDeleteAnswer() throws Exception {
        Long quizId = quiz.getId();
        Integer answer1Id = TestHelper.addAnAnswer(mockMvc, "testikysymys", "testivastaus1", "user1", quizId);
        Integer answer2Id = TestHelper.addAnAnswer(mockMvc, "testikysymys", "testivastaus2", "user2", quizId);
        
        this.mockMvc.perform(delete("/quiz/" + quizId + "/answer/" + answer1Id)).andExpect(status().isOk());
        
        this.mockMvc.perform(get("/quiz/" + quizId + "/answer/" + answer1Id)).andExpect(status().isNotFound());
        this.mockMvc.perform(get("/quiz/" + quizId + "/answer/" + answer2Id)).andExpect(status().isOk());
    }
    
    @Test
    @DirtiesContext
    public void testPreviousAnswerIdIsSetAfterSecondAnswer() throws Exception {
        Long quizId = quiz.getId();
        Integer answer1Id = TestHelper.addAnAnswer(mockMvc, "testikysymys", "testivastaus1", "user1", quizId);
        Integer answer2Id = TestHelper.addAnAnswer(mockMvc, "testikysymys", "testivastaus2", "user1", quizId);
        
        MvcResult mvcAnswer = this.mockMvc.perform(get("/quiz/" + quizId + "/answer/" + answer2Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        
        String response = mvcAnswer.getResponse().getContentAsString();
        
        assertEquals(answer1Id, TestHelper.getIntegerByKeyFromJson(response, "previousAnswerId"));
    }
    
    @Test
    @DirtiesContext
    public void testPreviousAnswerIdIsNullFirstAnswer() throws Exception {
        Long quizId = quiz.getId();
        Integer answer1Id = TestHelper.addAnAnswer(mockMvc, "testikysymys", "testivastaus2", "user1", quizId);
        
        MvcResult mvcAnswer = this.mockMvc.perform(get("/quiz/" + quizId + "/answer/" + answer1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        
        String response = mvcAnswer.getResponse().getContentAsString();
        
        assertNull(TestHelper.getStringByKeyFromJson(response, "previousAnswerId"));
    }
    
    @Test
    @DirtiesContext
    public void testAnswerDateIsSet() throws Exception {
        Long quizId = quiz.getId();
        Integer answer1Id = TestHelper.addAnAnswer(mockMvc, "testikysymys", "testivastaus2", "user1", quizId);
        
        MvcResult mvcAnswer = this.mockMvc.perform(get("/quiz/" + quizId + "/answer/" + answer1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        
        String response = mvcAnswer.getResponse().getContentAsString();
        
        Long answerTimestamp = TestHelper.getLongByKeyFromJson(response, "answerDate");
        assertTrue(Math.abs(answerTimestamp-new Date().getTime())<60*1000);
    }
}