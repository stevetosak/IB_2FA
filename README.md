# Информациска Безбедност - Лаб 2

## Вовед
Оваа апликација е направена за да се прикаже и имплементира 2FA. Се користи Spring MVC и PostgreSql.
2FA се користи и за регистрација и за најава, со помош на креираната AuthToken класа/ентитет.

## Регистрација
Корисникот ги внесува потребните информации: username, email и password. Потоа со клик на копчето Register внесените податоци се праќаат до контролерот одговорен за регистрација.  
Процесирањето се извршува со: `userService.register(username, email, password);`. Прво, се извршуваат проверки за:
- дали веќе постои корисник со внесеното корисничко име
- дали веќе постои корисник со внесениот email

```Java
@Transactional
    @Override
    public User register(String username, String email, String password) throws NoSuchAlgorithmException {
        userRepository.findUserByUsername(username).ifPresent(user -> {
            throw new UsernameExistsException("Username " + username + " already exists");
        });
        userRepository.findUserByEmail(email).ifPresent(user -> {
            throw new EmailExistsException("Email " + email + " already exists");
        });

        PsEncode passwordEncoder = new PsEncode(15);
        String passwordHash = passwordEncoder.encode(password,16);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordHash);

        userRepository.save(user);

        emailService.sendVerificationEmailRegister(user);

        return user;
    }
```
Во оваа функција се генерира [Токен](#токен)


### Хеширање

Следно, се предава работата на класата `PsEncode` која е задолжена за хеширање и посолување на пасвордот, односно методот `String encode(String password, int saltLength) `

```Java
 public String encode(String password, int saltLength) throws NoSuchAlgorithmException {
        SecureRandom sr = new SecureRandom();
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] salt = new byte[saltLength];
        sr.nextBytes(salt);

        byte[] combined = combine(passwordBytes, salt);
        combined = hash(combined,iterations);

        String hashed = String.format(
                "%s$%s",
                Base64.getEncoder().encodeToString(combined),
                Base64.getEncoder().encodeToString(salt));

        System.out.println("HASH: " + hashed);

        return hashed;
    }
```

Во овој метод се прават следните операции.
1. Се генерира сол
2. Солта се конкатенира на пасвордот и се добива низа од бајти сол + пасворд
3. Добиените бајти се хешираат одреден број на итерации и се добива хеширан пасворд.
4. На хешираниот пасворд се лепи одново солта, одделена со "$", за подоцна валидација.
	- Пример за формат на хеширан пасворд: `rVmnJ7kvbmj5uMlTwp012dcbN8HkAVXtK9g0QffZ6Jg=$3CU/AoRj3/djFulmTQKIdg==`

Од кога ќе се извршат овие операции, се добива хеширан пасворд и се поставува како пасворд на новокреираниот корисник. Потоа се зачувува корисникот.

> [!NOTE]
> По регистрација на корисникот, тој се зачувува во база со полето `verified` поставено на `false`, со цел да не може корисник кој не е потврден со 2FA, да може да се најави.

### Праќање емаил

Во овој момент е креирана корисничка сметка, па треба да се прати mail за верификација на корисничката сметка.
```Java
public void sendVerificationEmailRegister(User user) {

        AuthToken authToken = tokenService.createAuthToken(user,"link", LocalDateTime.now().plusMinutes(5));
        String link = verifyURL + "?token=" + authToken.getTokenValue();
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify account registration");
        message.setText("Click on this link to verify your account registration: " + link);
        mailSender.send(message);
    }
```

## Токен






Страната за регистрација:
![image](https://github.com/user-attachments/assets/85da595e-a053-40f0-bd8b-8223785dc222)



