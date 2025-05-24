package com.kostadin.sis.account;

import com.kostadin.sis.user.color.UserColorPalette;
import com.kostadin.sis.user.color.UserColorResponse;
import com.kostadin.sis.user.model.response.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sis/user/v1.0.0")
public class UserAccountController implements UserAccountOperations {
    private final UserAccountService userAccountService;

    @Override
    @GetMapping("/account")
    public ResponseEntity<UserAccount> getUserAccount(@RequestParam String email){
        var user = userAccountService.getUserAccount(email);

        if (Objects.equals(user.getColor(), "#ffffff")){
            return ResponseEntity.status(CREATED).body(user);
        }
        return ResponseEntity.ok(user);
    }

    @Override
    @GetMapping("/color")
    public UserColorResponse getRandomColor(@RequestParam String employeeNumber) {
        return userAccountService.getRandomColor(employeeNumber);
    }

    @Override
    @GetMapping("/color-palette/{count}")
    public UserColorPalette getColorPalette(@PathVariable("count") int count, @RequestParam String employeeNumber) {
        return userAccountService.getColorPalette(count, employeeNumber);
    }
}
