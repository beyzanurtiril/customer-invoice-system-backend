package com.pia.telekom.service;

import com.pia.telekom.dto.LoginRequest;
import com.pia.telekom.dto.LoginResponse;
import com.pia.telekom.entity.Administrator;
import com.pia.telekom.exception.InvalidCredentialsException;
import com.pia.telekom.repository.AdministratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
  Basit kimlik doğrulama servisi.

  - Parolalar DB'de BCrypt hash olarak durur; karşılaştırma encoder.matches ile
    yapılır (hash'ler asla string olarak kıyaslanmaz).
  - Başarılı girişte opak bir token (UUID) üretilir. Bu token şimdilik yalnızca
    frontend'in "giriş yapıldı" durumunu taşıması içindir; endpoint'ler henüz
    token zorunlu tutmaz. İleride gerçek yetkilendirme istenirse buradaki yapı
    bozulmadan Spring Security + JWT'ye geçilebilir (login sözleşmesi aynı kalır).
  - Tek rol vardır: administrator tablosundaki herkes ADMIN kabul edilir.
*/
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdministratorRepository administratorRepository;

    // BCryptPasswordEncoder durumsuzdur, alan olarak paylaşmak güvenlidir.
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Administrator admin = administratorRepository
                .findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = UUID.randomUUID().toString();

        return new LoginResponse(token, admin.getEmail(), admin.getFullName(), "ADMIN");
    }
}
