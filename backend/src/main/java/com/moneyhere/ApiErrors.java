package com.moneyhere;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestControllerAdvice public class ApiErrors {
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Map<String,String>> responseStatus(ResponseStatusException e){
  String message=e.getReason();
  return ResponseEntity.status(e.getStatusCode())
   .body(Map.of("message",message==null?"Não foi possível concluir a solicitação.":message));
 }

 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException e){
  Map<String,String> fields=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(f->fields.put(f.getField(),f.getDefaultMessage()));
  return ResponseEntity.badRequest().body(Map.of("message","Confira os campos informados","fields",fields));
 }
}
