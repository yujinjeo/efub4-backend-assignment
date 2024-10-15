package efub.assignment.community.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import efub.assignment.community.CommunityApplication;
import efub.assignment.community.account.AccountRepository;
import efub.assignment.community.account.dto.AccountUpdateRequestDto;
import efub.assignment.community.account.dto.SignUpRequestDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/account/data.sql")
@ActiveProfiles("test")
@ContextConfiguration(classes = CommunityApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
class AccountControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AccountRepository accountRepository;

    @BeforeEach
    public void mockMvcSetUp(){
        this.mockMvc=MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @Test
    @DisplayName("✅ createAccount : 회원가입 성공")
    void signUp_success() throws Exception{

        final String url="/accounts";

        // given
        final String email = "test@gmail.com";
        final String password = "yujin00lice!";
        final String nickname= "flalice";
        final String university = "test university";
        final String studentId = "20084";
        SignUpRequestDto requestDto = createSignUpRequestDto(email, password, nickname, university, studentId);

        // when
        final String requestBody = objectMapper.writeValueAsString(requestDto);

        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nickname").value(nickname));

    }

    @Test
    @DisplayName("🚨 createAccount : 동일한 email이 존재할 경우 회원가입 실패")
    void signUp_fail() throws Exception{

        final String url="/accounts";

        // given
        final String email = "yujinalice00@gmail.com";
        final String password = "yujin00lice!";
        final String nickname= "falice";
        final String university = "sample university";
        final String studentId = "20620811";
        SignUpRequestDto requestDto = createSignUpRequestDto(email, password, nickname, university, studentId);


        // when
        final String requestBody = objectMapper.writeValueAsString(requestDto);

        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));


        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nickname").value(nickname));

    }

    @Test
    @DisplayName("✅ getAccount : id를 통한 회원 정보 조회 성공")
    void getAccount_success() throws Exception {

        // given
        String url = "/accounts/{account_id}";
        Long accountId=1L;

        final String expectedEmail = "yujinalice00@gmail.com";
        final String expectedNickname = "floweralice";

        // when
        ResultActions resultActions = mockMvc.perform(get(url,accountId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(expectedEmail))
                .andExpect(jsonPath("$.nickname").value(expectedNickname));


    }

    @Test
    @DisplayName("🚨 getAccount : 존재하지 않는 id를 통한 회원 정보 조회 실패")
    void getAccount_fail() throws Exception {

        // given
        String url = "/accounts/{account_id}";
        Long accountId=3L;

        final String expectedEmail = "test@gmail.com";
        final String expectedNickname = "floweralice";

        // when
        ResultActions resultActions = mockMvc.perform(get(url,accountId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(expectedEmail))
                .andExpect(jsonPath("$.nickname").value(expectedNickname));


    }

    @Test
    @DisplayName("✅ updateAccount : 사용자 정보 수정 성공")
    void update_success() throws Exception {

        // given
        String url = "/accounts/profile/{account_id}";
        Long accountId= 1L;

        final String updateEmail = "test0022@gmail.com";
        final String updatePassword = "password00!!";
        final String updateNickname = "samplenickname";
        AccountUpdateRequestDto requestDto = createUpdateRequestDto(updateEmail, updatePassword, updateNickname);

        // when
        String requestBody = objectMapper.writeValueAsString(requestDto);
        ResultActions resultActions = mockMvc.perform(patch(url,accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(updateEmail))
                .andExpect(jsonPath("$.nickname").value(updateNickname));

    }

    @Test
    @DisplayName("🚨 updateAccount : 동일한 닉네임을 가진 사용자 정보 수정 실패")
    void update_fail() throws Exception {

        // given
        String url = "/accounts/profile/{account_id}";
        Long accountId= 1L;

        final String updateEmail = "test@gmail.com";
        final String updatePassword = "password00!!";
        final String updateNickname = "floweralice";
        AccountUpdateRequestDto requestDto = createUpdateRequestDto(updateEmail, updatePassword, updateNickname);

        // when
        String requestBody = objectMapper.writeValueAsString(requestDto);
        ResultActions resultActions = mockMvc.perform(patch(url,accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(jsonPath("$.email").value(updateEmail))
                .andExpect(jsonPath("$.nickname").value(updateNickname));

    }

    @Test
    @DisplayName("✅ accountDelete : 사용자 회원 탈퇴 성공")
    void withdraw_success() throws Exception{

        // given
        String url = "/accounts/{account_id}";
        Long accountId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(delete(url,accountId));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("삭제가 완료되었습니다."));

    }

    @Test
    @DisplayName("🚨 accountDelete : id가 존재하지 않는 사용자 회원 탈퇴 실패")
    void withdraw_fail() throws Exception{

        // given
        String url = "/accounts/{account_id}";
        Long accountId = 100L;

        // when
        ResultActions resultActions = mockMvc.perform(delete(url,accountId));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string("삭제가 완료되었습니다."));

    }

    private SignUpRequestDto createSignUpRequestDto(String email, String password, String nickname, String university,String studentId){
        return SignUpRequestDto.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .university(university)
                .studentId(studentId)
                .build();
    }

    private AccountUpdateRequestDto createUpdateRequestDto(String email, String password, String nickname){
        return AccountUpdateRequestDto.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();

    }
}