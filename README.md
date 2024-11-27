# Информациска Безбедност - Лаб 2

## Вовед
Оваа апликација е направена за да се прикаже и имплементира 2FA. Се користи Spring MVC и PostgreSql.
2FA се користи и за регистрација и за најава, со помош на креираната [AuthToken](#токен) класа/ентитет.

## Регистрација
![image](https://github.com/user-attachments/assets/85da595e-a053-40f0-bd8b-8223785dc222)
Корисникот ги внесува потребните информации: username, email и password. Потоа со клик на копчето Register внесените податоци се праќаат до контролерот одговорен за регистрација.  
Процесирањето се извршува со: `userService.register(username, email, password);`. Прво, се извршуваат проверки за:
- _дали веќе постои корисник со внесеното корисничко име_
- _дали веќе постои корисник со внесениот email_

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

## Хеширање

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

        return hashed;
    }
```

Во овој метод се прават следните операции.
1. __Се генерира сол__
2. __Солта се конкатенира на пасвордот и се добива низа од бајти сол + пасворд__
3. __Добиените бајти се хешираат одреден број на итерации и се добива хеширан пасворд.__
4. __На хешираниот пасворд се лепи одново солта, одделена со "$", за подоцна валидација.__
	- Пример за формат на хеширан пасворд: `rVmnJ7kvbmj5uMlTwp012dcbN8HkAVXtK9g0QffZ6Jg=$3CU/AoRj3/djFulmTQKIdg==`

Од кога ќе се извршат овие операции, се добива хеширан пасворд и се поставува како пасворд на новокреираниот корисник. Потоа се зачувува корисникот.

> [!NOTE]
> По регистрација на корисникот, тој се зачувува во база со полето `verified` поставено на `false`, со цел да не може корисник кој не е потврден со 2FA, да може да се најави.

## Праќање емаил

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
Во оваа функција се генерира [Токен](#токен), и се креира линк со параметар `token`. Овој линк се доставува преку маил и потребно е корисникот да го кликне линкот за да го верифицира токенот. 
Откако успешно се верифицира валидноста на токенот, корисникот се пренасочува до страната за најава.

## Најава
![image](https://github.com/user-attachments/assets/fe6c61bc-0a91-47e5-abf0-c2605d36cefe)

Корисникот се најавува со корисничко име или маил, и пасворд. Податоците за најава ги обработува: 

```Java
@Override
    public User login(String usernameOrEmail, String password) throws NoSuchAlgorithmException {
        User usr;

        Optional<User> usrByUsername = userRepository.findUserByUsername(usernameOrEmail);
        Optional<User> usrByEmail = userRepository.findUserByEmail(usernameOrEmail);

        if(usrByUsername.isPresent()){
            usr = usrByUsername.get();
        } else if (usrByEmail.isPresent()) {
            usr = usrByEmail.get();
        } else {
            throw new UserNotFoundException("Invalid credentials");
        }
        
        PsEncode passwordEncoder = new PsEncode(15);

        if(!passwordEncoder.matches(password,usr.getPassword())){
            throw new IncorrectPasswordException("Incorrect password");
        }

        if(!usr.isAccountVerified()){
            throw new UserNotVerifiedException("This account has not been activated.\nPlease click on the link sent to your email to activate it");
        }

        emailService.sendOTPMail(usr);

        return usr;

    }
```

Како и за регистрација, се прават иницијални проверки и повторно се користи класата `PsEncode` за овој пат да се спореди внесениот пасворд со зачуваниот пасворд во базата.
```Java
public boolean matches(String inputPassword, String dbPassword) throws NoSuchAlgorithmException {

        String[] passwordSplit = dbPassword.split("\\$");
        String hashedPassword = passwordSplit[0];
        String salt = passwordSplit[passwordSplit.length - 1];

        byte[] inputPasswordBytes = inputPassword.getBytes(StandardCharsets.UTF_8);
        byte[] saltBytes =  Base64.getDecoder().decode(salt);


        byte[] combined = combine(inputPasswordBytes, saltBytes);
        byte[] hashedInputBytes = hash(combined,iterations);

        String encoded = Base64.getEncoder().encodeToString(hashedInputBytes);

        return encoded.equals(hashedPassword);

    }
```

Принципот е сличен како претходно: 
1. __Се одделуваат хешот и солта од пасвордот во база.__
2. __Се комбинираат нововнесениот пасворд и солта__
3. __Се хешираат__
4. __Добиениот хеш се енкодира како стринг и се споредува со хешот на пасвордот од база__

Ако хешовите се совпаѓаат, тогаш корисникот го внел точниот пасворд.  
Следно, се генерира OTP(__One Time Password__) [код](#токен) и се праќа на корисникот преку емаил.

![image](https://github.com/user-attachments/assets/306dcaba-b2ab-45f3-be95-ea0ada0ff222)

Корисникот го внесува кодот и успешно се најавува на страницата.


## Токен

2FA се обезбедува со помош на класата/ентитетот `Token` заедно со `TokenService`.

```Java
@Entity
@Table(name = "auth_token")
@Getter @Setter @ToString
public class AuthToken {
    @Id
    @SequenceGenerator(name = "auth_token_id_seq",sequenceName = "auth_token_id_seq",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "auth_token_id_seq")
    private int id;
    @Column(name = "token_value")
    private String tokenValue;
    @Column(name = "expires")
    private LocalDateTime expiresAt;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id",nullable = false)
    private User user;
    @Column(name = "used")
    private boolean isUsed;
    private String type;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

Пред да се прати маил за верификација до корисникот, се генерира ваков токен и се зачувува во базата на податоци. Овој токен се користи за 2FA при регистрација и најава.Има два типа на токен што соодветно означуваат каков тип на код/вредност содржи токенот - **"otp"** и **"link"**. Главните карактеристики на токенот се неговата вредност и неговото времетраење. Сите проверки односно валидација на токени се извршуваат со `TokenService`.  

При клик на линкот за верификација на креирана сметка се користи:
```Java
  @Override @Transactional
    public void verify(String tokenValue) {
        Optional<AuthToken> tokenOptional = tokenRepository.findByTokenValue(tokenValue);
        LocalDateTime now = LocalDateTime.now();

        if(tokenOptional.isEmpty()) {
            throw new TokenNotFoundException("Token " + tokenValue + " not found");
        }

        AuthToken token =  tokenOptional.get();

        if(now.isAfter(token.getExpiresAt())){
            throw new TokenExpiredException("Token expired");
        };

        User user = token.getUser();

        token.setUsed(true);
        user.setAccountVerified(true);

        tokenRepository.save(token);
        userRepository.save(user);
    }
```

При внес на ОТП кодот за најава се користи:

```Java
@Override @Transactional
    public void verifyOTP(String[] otp) {
        if (otp.length != 6) {
            throw new InvalidOTPLength("Please fill out all fields");
        }

        String otpConcat = String.join("", otp);
        System.out.println("concat " + otpConcat);

        Optional<AuthToken> authTokenOptional = tokenRepository.findByTokenValue(otpConcat);
        if (authTokenOptional.isEmpty()) {
            throw new TokenNotFoundException("Token does not match");
        }

        AuthToken authToken = authTokenOptional.get();

        if (authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token expired");
        }

        authToken.setUsed(true);

        tokenRepository.save(authToken);

    }
```

Принципот на овие методи е ист, така што проверуваат:
- _Дали во базата постои токен со таква вредност_
- _Ако постои, дали рокот на токенот е валиден_

Ако поминат овие проверки, тогаш токенот е валиден и корисникот успешно е автентициран.  
Разликата е типот на вредноста што ја проверуваат. За најава - 6 цифрен OTP код, за регистрација - код од 36 карактери (uuid).

-------

#### Посолување
Целта на посолувањето е два корисници со ист пасворд да имаат различна хеш вредност зачувана во базата.
Ова е пример со два корисници кои имаат ист пасворд: "toska".

![image](https://github.com/user-attachments/assets/0c687e1b-ba9f-44a6-abe1-21460452e3ee)

#### Детали за праќање маил

За пракање маилови се користи `Spring Mail` преку SMTP сервер на gmail.

```
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_NAME}
spring.mail.password=${MAIL_PASS}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```



