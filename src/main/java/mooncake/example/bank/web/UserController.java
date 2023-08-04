package mooncake.example.bank.web;

import lombok.RequiredArgsConstructor;
import mooncake.example.bank.dto.ResponseDto;
import mooncake.example.bank.dto.request.UserReqDto;
import mooncake.example.bank.dto.response.UserRespDto;
import mooncake.example.bank.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    // 회원 가입은 인증되면 안된다 /s 를 빼고 /api/join 으로
    /*
     Validation 사용하기 - @Valid, BindingResult 를 통해 결과 받기
     */
    @PostMapping("/join")
    public ResponseEntity<ResponseDto<?>> join(@RequestBody @Valid UserReqDto.UserJoinReqDto requestDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {

            // 모든 에러를 확인해보자
            Map<String, String> errMap = new HashMap<>();

            for (FieldError error : bindingResult.getFieldErrors()) {
                errMap.put(error.getField(), error.getDefaultMessage());
            }

            return new ResponseEntity<>(new ResponseDto<>(-1, "유효성 검사 실패 ", errMap), HttpStatus.OK);
        }

        UserRespDto.UserJoinRespDto responseDto = userService.save(requestDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "회원가입성공", responseDto), HttpStatus.CREATED); //201 - 뭔가 만들어짐

    }

}