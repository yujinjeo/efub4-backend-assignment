package efub.assignment.community.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import efub.assignment.community.CommunityApplication;
import efub.assignment.community.account.AccountRepository;
import efub.assignment.community.board.BoardRepository;
import efub.assignment.community.post.PostRepository;
import efub.assignment.community.post.dto.PostRequestDto;
import efub.assignment.community.post.dto.PostUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/post/data.sql")
@ContextConfiguration(classes = CommunityApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
class PostControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected BoardRepository boardRepository;


    @BeforeEach
    void mockMvcSetUp() {
        this.mockMvc= MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @Test
    @DisplayName("✅ createPost : 게시글 등록 성공")
    void createNewPost_success() throws Exception {
        // given
        final String url = "/posts";
        final String boardId = "1";
        final String writerNickname="floweralice";
        final String title = "Welcome to the Board";
        final String content="This is the first post on this board.";
        PostRequestDto requestDto = createPostRequestDto(boardId, writerNickname, title, content);

        // when
        String requestBody = objectMapper.writeValueAsString(requestDto);
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));


        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content));


    }

    @Test
    @DisplayName("🚨 createPost : 존재하지 않는 board에 게시글 등록 실패")
    void createNewPost_fail() throws Exception {
        // given
        final String url = "/posts";
        final String boardId = "2";
        final String writerNickname="floweralice";
        final String title = "Welcome";
        final String content="This is the first this board.";
        PostRequestDto requestDto = createPostRequestDto(boardId, writerNickname, title, content);

        // when
        String requestBody = objectMapper.writeValueAsString(requestDto);
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));


        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content));


    }

    @Test
    @DisplayName("✅ get post : post id 게시글 조회 성공")
    void getOnePost_success() throws Exception{

        // given
        String url = "/posts/{post_id}";
        Long postId=1L;

        final String expectedTitle = "Welcome to the Board";
        final String expectedContent="This is the first post on this board.";

        // when
        ResultActions resultActions = mockMvc.perform(get(url,postId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(expectedTitle))
                .andExpect(jsonPath("$.content").value(expectedContent));


    }

    @Test
    @DisplayName("🚨 get post : 존재하지 않는 post id 게시글 조회 실패")
    void getOnePost_fail() throws Exception{

        // given
        String url = "/posts/{post_id}";
        Long postId=2L;

        final String expectedTitle = "Welcome to the Board";
        final String expectedContent="This is the first post on this board.";

        // when
        ResultActions resultActions = mockMvc.perform(get(url,postId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(expectedTitle))
                .andExpect(jsonPath("$.content").value(expectedContent));


    }

    @Test
    @DisplayName("✅ update post : 게시글 수정 성공")
    void updatePost_success() throws Exception {

        // given
        String url = "/posts/{post_id}";
        Long postId=1L;

        final String title = "환영합니다!";
        final String content= "공고 게시판에는 공고가 매주 1회 업로드됩니다.";

        PostUpdateDto requestDto = createPostUpdateDto(title, content);


        // when
        String requestBody = objectMapper.writeValueAsString(requestDto);
        ResultActions resultActions = mockMvc.perform(put(url,postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content));

    }

    @Test
    @DisplayName("🚨 update post : 존재하지 않는 게시글 수정 실패")
    void updatePost_fail() throws Exception {

        // given
        String url = "/posts/{post_id}";
        Long postId=2L;

        final String title = "환영합니다!";
        final String content= "공고 게시판에는 공고가 매주 1회 업로드됩니다.";

        PostUpdateDto requestDto = createPostUpdateDto(title, content);


        // when
        String requestBody = objectMapper.writeValueAsString(requestDto);
        ResultActions resultActions = mockMvc.perform(put(url,postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content));

    }

    @Test
    @DisplayName("✅ delete post : 게시글 삭제 성공")
    void deletePost_success() throws Exception{

        // given
        String url = "/posts/{post_id}";
        Long postId = 1L;

        Long accountId=1L;

        // when
        ResultActions resultActions = mockMvc.perform(delete(url,postId)
                .param("accountId", String.valueOf(accountId)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("글을 삭제하였습니다."));

    }

    @Test
    @DisplayName("🚨 delete post : 작성자가 아닌 사용자의 게시글 삭제 실패")
    void deletePost_fail() throws Exception{

        // given
        String url = "/posts/{post_id}";
        Long postId = 1L;

        Long accountId=2L;

        // when
        ResultActions resultActions = mockMvc.perform(delete(url,postId)
                .param("accountId", String.valueOf(accountId)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("글을 삭제하였습니다."));

    }


    private PostRequestDto createPostRequestDto(String boardId, String writerNickname, String title, String content){
        return new PostRequestDto(boardId,writerNickname,title,content,"false");
    }

    private PostUpdateDto createPostUpdateDto(String title, String content){
        return PostUpdateDto.builder()
                .title(title)
                .content(content)
                .build();
    }

}