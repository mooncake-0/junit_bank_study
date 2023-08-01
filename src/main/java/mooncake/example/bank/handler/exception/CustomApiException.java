package mooncake.example.bank.handler.exception;

public class CustomApiException extends RuntimeException{

    public CustomApiException(String errMsg) {
        super(errMsg);
    }
}
