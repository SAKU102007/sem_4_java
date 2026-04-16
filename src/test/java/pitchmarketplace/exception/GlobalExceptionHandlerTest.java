package pitchmarketplace.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Positive;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pitchmarketplace.dto.ApiErrorResponse;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private Validator validator;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldHandleNotFound() {
        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Pitch not found. id=1"),
                request("/api/v1/pitches/1")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("Pitch not found. id=1");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/pitches/1");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void shouldHandleMethodArgumentValidation() throws Exception {
        Method method = ValidationTarget.class.getDeclaredMethod("acceptBody", ValidationBody.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new ValidationBody(), "validationBody");
        bindingResult.addError(new FieldError("validationBody", "name", "name is required"));
        bindingResult.addError(new ObjectError("validationBody", "body is inconsistent"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(exception, request("/api/v1/body"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().details())
                .containsExactly("name: name is required", "validationBody: body is inconsistent");
    }

    @Test
    void shouldHandleBindValidation() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new ValidationBody(), "searchRequest");
        bindingResult.addError(new FieldError("searchRequest", "page", "page must be zero or positive"));
        bindingResult.addError(new ObjectError("searchRequest", "range is invalid"));

        ResponseEntity<ApiErrorResponse> response = handler.handleBindingValidation(
                new BindException(bindingResult),
                request("/api/v1/bookings/search")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().details())
                .containsExactly("page: page must be zero or positive", "searchRequest: range is invalid");
    }

    @Test
    void shouldHandleMethodValidationResults() throws Exception {
        Method method = ValidationTarget.class.getDeclaredMethod("validate", String.class, String.class);
        MethodParameter indexedParameter = new MethodParameter(method, 0);
        indexedParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
        MethodParameter keyedParameter = new MethodParameter(method, 1);

        ParameterValidationResult indexedResult = new ParameterValidationResult(
                indexedParameter,
                null,
                List.of(new DefaultMessageSourceResolvable(new String[]{"code"}, null, "must be positive")),
                null,
                2,
                null
        );
        ParameterValidationResult keyedResult = new ParameterValidationResult(
                keyedParameter,
                null,
                List.of(new DefaultMessageSourceResolvable(new String[]{"code"}, null, "must not be blank")),
                null,
                null,
                "id"
        );

        MethodValidationResult validationResult = MethodValidationResult.create(
                new ValidationTarget(),
                method,
                List.of(indexedResult, keyedResult)
        );
        HandlerMethodValidationException exception = new HandlerMethodValidationException(validationResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleMethodValidation(
                exception,
                request("/api/v1/validate")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().details())
                .containsExactly("value[2]: must be positive", "validate[id]: must not be blank");
    }

    @Test
    void shouldFallbackToExecutableNameWhenParameterNameIsBlank() throws Exception {
        Method method = ValidationTarget.class.getDeclaredMethod("validate", String.class, String.class);
        MethodParameter blankNameParameter = new MethodParameter(method, 0) {
            @Override
            public String getParameterName() {
                return "   ";
            }
        };

        ParameterValidationResult blankNameResult = new ParameterValidationResult(
                blankNameParameter,
                null,
                List.of(new DefaultMessageSourceResolvable(new String[]{"code"}, null, "must not be null")),
                null,
                null,
                null
        );

        MethodValidationResult validationResult = MethodValidationResult.create(
                new ValidationTarget(),
                method,
                List.of(blankNameResult)
        );
        HandlerMethodValidationException exception = new HandlerMethodValidationException(validationResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleMethodValidation(
                exception,
                request("/api/v1/validate")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().details()).containsExactly("validate: must not be null");
    }

    @Test
    void shouldHandleConstraintViolations() {
        Set<ConstraintViolation<ConstraintBean>> violations = validator.validate(new ConstraintBean(0L));
        ConstraintViolationException exception = new ConstraintViolationException(Set.copyOf(violations));

        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(
                exception,
                request("/api/v1/constraints")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().details()).hasSize(1);
        assertThat(response.getBody().details().get(0)).contains("id");
        assertThat(response.getBody().details().get(0)).contains("must be greater than 0");
    }

    @Test
    void shouldHandleBadRequestExceptions() {
        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(
                new IllegalArgumentException("Bad input"),
                request("/api/v1/bad")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Bad input");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void shouldHandleUnexpectedExceptions() {
        ResponseEntity<ApiErrorResponse> response = handler.handleGeneric(
                new Exception("Boom"),
                request("/api/v1/fail")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Unexpected server error");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/fail");
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }

    private static final class ValidationTarget {

        @SuppressWarnings("unused")
        private void acceptBody(ValidationBody body) {
        }

        @SuppressWarnings("unused")
        private void validate(String value, String anotherValue) {
        }
    }

    private static final class ValidationBody {
    }

    private static final class ConstraintBean {

        @Positive
        private final Long id;

        private ConstraintBean(Long id) {
            this.id = id;
        }
    }
}
