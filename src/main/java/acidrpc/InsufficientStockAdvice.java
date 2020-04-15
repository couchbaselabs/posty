package acidrpc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class InsufficientStockAdvice {

    @ResponseBody
    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.OK)
    String insufficientStockHandler(InsufficientStockException err) {

        //crank the error message into JSON object
        return String.format("{\"OrderError\":\"%s\"}", err.getMessage());
        //return err.getMessage();
        //render JSON as string
    }
}
