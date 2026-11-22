package com.moneyhere;
import jakarta.validation.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class DtoTest {
 private final Validator validator=Validation.buildDefaultValidatorFactory().getValidator();
 @Test void senhaCurtaRejeitada(){assertFalse(validator.validate(new Dto.Register("Maria","maria@exemplo.com","123",true)).isEmpty());}
 @Test void cadastroValido(){assertTrue(validator.validate(new Dto.Register("Maria","maria@exemplo.com","uma-senha-longa",true)).isEmpty());}
}
