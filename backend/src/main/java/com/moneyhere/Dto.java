package com.moneyhere;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public final class Dto {
 private Dto(){}
 static final String EMAIL_DOMAIN = "[^@]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+";
 public record Register(@NotBlank @Size(max=130) String name,
  @jakarta.validation.constraints.Email(regexp=EMAIL_DOMAIN) @NotBlank @Size(max=254) String email,
  @NotBlank @Size(min=8,max=72) String password,boolean consent){}
 public record AccountStatus(@NotNull Boolean enabled){}
 public record RoleChange(@NotNull AccountRole role){}
 public record AdminPassword(@NotBlank @Size(min=12,max=72) String password){}
 public record Login(@jakarta.validation.constraints.Email @NotBlank String email,@NotBlank String password){}
 public record Email(@jakarta.validation.constraints.Email @NotBlank String email){}
 public record Reset(@NotBlank String token,@Size(min=8) String password){}
 public record Profile(String name,String goal,String level,String learningFormat,String theme,Boolean notifications){}
 public record Password(@NotBlank String currentPassword,@Size(min=8) String newPassword){}
 public record GoalBody(@NotBlank String title,@NotNull @DecimalMin("0.01") BigDecimal targetAmount,@NotNull @DecimalMin("0.00") BigDecimal currentAmount,LocalDate deadline){}
 public record EntryBody(@NotBlank String title,@NotBlank String category,@Pattern(regexp="INCOME|EXPENSE") String kind,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotNull LocalDate entryDate){}
 public record Key(@NotBlank String key){}
}
