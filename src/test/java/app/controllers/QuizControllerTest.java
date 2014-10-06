package app.controllers;

import app.Application;
import app.domain.Quiz;
import app.repositories.QuizRepository;
import com.google.gson.Gson;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = Application.class)
public class QuizControllerTest {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testEmptyQuizList() throws Exception {
        this.mockMvc.perform(get("/quiz"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @DirtiesContext
    public void testAddingQuiz() throws Exception {
        String jsonQuiz = "{\"title\":\"testquiz1\",\"items\":\"["
                + "{}]\"}";

        this.mockMvc.perform(post("/quiz").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));

        List<Quiz> quizes = quizRepository.findAll();
        for (int i = 0; i < quizes.size(); i++) {
            if (quizes.get(i).getTitle().equals("testquiz1")) {
                return;
            }
        }

        throw new AssertionError();
    }

    @Test
    @DirtiesContext
    public void testCorrectOpenQuestionsAdded() throws Exception {
        String jsonQuiz = "{\"title\":\"testquiz2\",\"items\":\"["
                + "{\\\"question\\\":\\\"testquestion1\\\",\\\"item_type\\\":\\\"open_question\\\",\\\"$$hashKey\\\":\\\"003\\\"},"
                + "{\\\"question\\\":\\\"testquestion2\\\",\\\"item_type\\\":\\\"open_question\\\",\\\"$$hashKey\\\":\\\"006\\\"}"
                + "]\"}";

        this.mockMvc.perform(post("/quiz").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));

        List<Quiz> quizes = quizRepository.findAll();
        JSONArray items = new JSONArray(quizes.get(quizes.size() - 1).getItems());

        assertEquals("testquestion1", items.getJSONObject(0).getString("question"));
        assertEquals("open_question", items.getJSONObject(0).getString("item_type"));
        assertEquals("testquestion2", items.getJSONObject(1).getString("question"));
        assertEquals("open_question", items.getJSONObject(1).getString("item_type"));
    }

    @Test
    @DirtiesContext
    public void testCorrectNumberOfOpenQuestionsAdded() throws Exception {
        String jsonQuiz = "{\"title\":\"testquiz3\",\"items\":\"["
                + "{\\\"question\\\":\\\"testquestion1\\\",\\\"item_type\\\":\\\"open_question\\\"},"
                + "{\\\"question\\\":\\\"testquestion2\\\",\\\"item_type\\\":\\\"open_question\\\"},"
                + "{\\\"question\\\":\\\"testquestion3\\\",\\\"item_type\\\":\\\"open_question\\\"}"
                + "]\"}";

        this.mockMvc.perform(post("/quiz").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));

        List<Quiz> quizes = quizRepository.findAll();
        JSONArray items = new JSONArray(quizes.get(quizes.size() - 1).getItems());

        assertEquals(3, items.length());
    }
    @Test
    @DirtiesContext
    public void testIsOpenSetCorrectly() throws Exception {
        String jsonQuiz = "{\"title\":\"testquiz1\",\"isOpen\":\"true\",\"items\":\"["
                + "{}]\"}";
        this.mockMvc.perform(post("/quiz").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));
        
        assertTrue(quizRepository.findOne(1L).getIsOpen());
    }

    @Test
    @DirtiesContext
    public void testIsOpenReturnedCorrectly() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setIsOpen(false);
        quiz.setTitle("testquiz");
        quiz.setItems("[{}]");
        quizRepository.save(quiz);
        
        MvcResult mvcResult = this.mockMvc.perform(get("/quiz/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        Gson gson = new Gson();
        Quiz returnedQuiz = gson.fromJson(mvcResult.getResponse().getContentAsString(), Quiz.class);
        
        assertFalse(returnedQuiz.getIsOpen());
    }
    
    @Test
    @DirtiesContext
    public void testCorrectPlaceholderAnswerAdded() throws Exception {
        String jsonQuiz = "{\"title\":\"testquiz1\",\"items\":\"["
                + "{}]\"}";
        this.mockMvc.perform(post("/quiz").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));
        
        String jsonAnswer = "{\"answer\": \"vastaus\"}";
        this.mockMvc.perform(post("/quiz/1/placeholder").content(jsonAnswer).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
        
        JSONObject json = new JSONObject(quizRepository.findOne(1L).getPlaceholderAnswers().get(0).getAnswerData());
        String answer = json.getString("answer");
        assertTrue(answer.equals("vastaus"));
    }

    @Test
    @DirtiesContext
    public void testMultiplePlaceholderAnswersAdded() throws Exception {
        String jsonQuiz = "{\"title\":\"testquiz1\",\"items\":\"["
                + "{}]\"}";
        this.mockMvc.perform(post("/quiz").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));

        String jsonAnswer = "{\"answer\": \"vastaus\"}";
        this.mockMvc.perform(post("/quiz/1/placeholder").content(jsonAnswer).contentType(MediaType.APPLICATION_JSON));
        jsonAnswer = "{\"answer\": \"vastaus\"}";
        this.mockMvc.perform(post("/quiz/1/placeholder").content(jsonAnswer).contentType(MediaType.APPLICATION_JSON));
        jsonAnswer = "{\"answer\": \"vastaus\"}";
        this.mockMvc.perform(post("/quiz/1/placeholder").content(jsonAnswer).contentType(MediaType.APPLICATION_JSON));

        assertEquals(3, quizRepository.findOne(1L).getPlaceholderAnswers().size());
    }

    @Test
    @DirtiesContext
    public void testAddingItemsToExistingQuiz() throws Exception {
        String jsonQuiz = "{\"title\":\"testquiz1\",\"items\":\"["
                + "{}]\"}";

        this.mockMvc.perform(post("/quiz").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));

        String editedQuiz = "{\"title\":\"testquiz2\",\"items\":\"["
                + "{\\\"question\\\":\\\"testquestion1\\\",\\\"item_type\\\":\\\"open_question\\\",\\\"$$hashKey\\\":\\\"003\\\"},"
                + "{\\\"question\\\":\\\"testquestion2\\\",\\\"item_type\\\":\\\"open_question\\\",\\\"$$hashKey\\\":\\\"006\\\"}"
                + "]\"}";
        this.mockMvc.perform(post("/quiz/1").content(editedQuiz).contentType(MediaType.APPLICATION_JSON));

        List<Quiz> quizes = quizRepository.findAll();
        JSONArray items = new JSONArray(quizes.get(quizes.size() - 1).getItems());

        assertEquals(2, items.length());
    }

    @Test
    @DirtiesContext
    public void testRemovingItemsFromExistingQuiz() throws Exception {
        String jsonQuiz = "{\"title\":\"testquiz2\",\"items\":\"["
                + "{\\\"question\\\":\\\"testquestion1\\\",\\\"item_type\\\":\\\"open_question\\\",\\\"$$hashKey\\\":\\\"003\\\"},"
                + "{\\\"question\\\":\\\"testquestion2\\\",\\\"item_type\\\":\\\"open_question\\\",\\\"$$hashKey\\\":\\\"006\\\"}"
                + "]\"}";

        this.mockMvc.perform(post("/quiz").content(jsonQuiz).contentType(MediaType.APPLICATION_JSON));

        String editedQuiz = "{\"title\":\"testquiz2\",\"items\":\"["
                + "{\\\"question\\\":\\\"testquestion1\\\",\\\"item_type\\\":\\\"open_question\\\",\\\"$$hashKey\\\":\\\"003\\\"},"
                + "]\"}";

        this.mockMvc.perform(post("/quiz/1").content(editedQuiz).contentType(MediaType.APPLICATION_JSON));

        List<Quiz> quizes = quizRepository.findAll();
        JSONArray items = new JSONArray(quizes.get(quizes.size() - 1).getItems());

        assertEquals(1, items.length());
    }
}
