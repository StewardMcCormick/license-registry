# Code Review — License Registry

**Проект:** `license-registry-service`
**Тип:** Java Servlet / JSP / JDBC / PostgreSQL (MVC), Gradle
**Дата ревью:** 2026-11-14
**Ревизия:** рабочее дерево, HEAD `232b6de` (+ незакоммиченные изменения)
**Объём:** весь `src/`, `web.xml`, `build.gradle.kts`, JSP, тесты, документация (`README.md`, `docs/`)
**Режим:** read-only, изменения в кодовую базу не вносились

---

## 1. Резюме

Проект имеет **здоровую слоистую архитектуру** (web → service → repository → model), интерфейсы репозиториев, простой DI через `ServiceRegistry`, фильтры для сквозных задач и понятную доменную модель. Код читается, конвенции в целом выдержаны, есть unit-тесты сервисов.

Однако найдены **функциональные баги, из-за которых часть заявленного функционала не работает**, а также **уязвимости безопасности**, противоречащие собственной документации. Ниже — сводка по приоритетам.

| Уровень | Кол-во | Примеры |
|---|---|---|
| 🔴 Критично | 3 | нерабочие POST-маршруты организации; IDOR на чтение лицензий; хранение пароля в несолёном SHA-256 |
| 🟠 Высоко | 4 | не enforced инвариант «≥1 ADMIN»; нетранзакционное создание организации; гонки check-then-act; суперадмин не может править лицензии |
| 🟡 Средне | 12 | устаревший кэш ролей в сессии; нет пула соединений; N+1; нет ограничений длины полей; утечка деталей исключений; нет security-заголовков и т.д. |
| 🔵 Стиль/низко | ~10 | импорты, логирование, мёртвый код, неэкранированный EL, перепутанные колонки в JSP |
| 📄 Документация | 5 | README описывает несуществующий функционал (BCrypt, AuditLog, static/, AJAX), типы PK расходятся с DDL |

---

## 2. Качество написания и code style

### 2.1 Положительное

- Единообразное именование классов/пакетов, аккуратные исключения (`ValidationException`, `NotFoundException`, `RepositoryException`).
- Репозитории реализуют интерфейсы, сервисы принимают зависимости через конструктор — легко тестируются.
- Последовательная работа с JDBC через `PreparedStatement` (параметризация везде — SQL-инъекций не найдено).
- Магические числа вынесены в константы (`EXPIRING_THRESHOLD_DAYS`, `MIN_PASSWORD_LENGTH`, `MAX_JOIN_CODE_ATTEMPTS`, `CODE_LENGTH`).
- JSP используют JSTL и `<c:out>` для пользовательских данных (в большинстве мест).

### 2.2 Замечания

| # | Место | Проблема |
|---|---|---|
| S1 | `web/controller/LicenseController.java:13-16` | Нарушен порядок импортов (`util.ParamUtil; util.PathUtil; util.CurrentUser; util.SessionUtil;` — не по алфавиту). Аналогично в других файлах — порядок непоследователен. |
| S2 | `web/OrganizationServlet.java:131`, `web/controller/LicenseController.java:36,87,167,204` | Длинные однострочные сигнатуры методов (>120 символов), что ухудшает читаемость. |
| S3 | `config/DatabaseConfig.java:49,72,79,85,91,148,155` | `System.out.println` вместо логгера — не управляется уровнем логирования, пишется в stdout контейнера. |
| S4 | `listener/AppContextListener.java:27` | `e.printStackTrace()` вместо логгера. |
| S5 | `repository/util/RowMapper.java:13,26-28` | Параметр `prefix` фактически не используется (везде передаётся `""`); метод `getRowNameWithPrefix` — мёртвая гибкость. |
| S6 | `web/OrganizationServlet.java:159-165` | Название `handleUserRoleRequest` не согласовано с `handleOrganizationAction`; логика `if/else` вместо `switch` при наличии уже разобранного action. |
| S7 | Комментарии | Смешение русского и английского, оставленные `// TODO: rewrite on AJAX` (`OrganizationServlet` в истории, `git show HEAD`). |
| S8 | `config/AppConfig.java` | Класс содержит одну константу; уместнее была бы константа в доменном/сервисном слое либо конфигурационный класс с несколькими параметрами. |
| S9 | JSP, напр. `org/settings.jsp:21,31,38`, `licenses/list.jsp:23,29` | Значения `${member.userId}`, `${org.id}`, `${type.id}` выводятся без `<c:out>`. Сейчас это UUID (безопасно), но паттерн небезопасен при появлении произвольных данных. |

### 2.3 Мёртвый код / гигиена репозитория

- `git ls-files` показывает отслеживаемые, но удалённые файлы: `src/main/java/web/OrganizationsServlet.java` (удалён в рабочем дереве), `src/main/java/web/CreateOrganizationServlet.java` (удаление застейджено). Это следы незавершённого рефакторинга — стоит зафиксировать удаление.
- Пустая директория `src/main/webapp/WEB-INF/views/onboarding/` (не используется).
- `src/main/resources/` пуст, хотя `build.gradle.kts:34-36` настраивает `processResources`.
- Директории `build/` и `.gradle/` присутствуют в рабочем дереве (в `.gitignore`, не отслеживаются — ок).

---

## 3. Архитектура

### 3.1 Сильные стороны

- **Чёткое разделение слоёв**: `web` (сервлеты/фильтры/`LicenseController`), `service` (бизнес-логика), `repository` (JDBC), `model` (сущности/enum), `config`, `util`, `exception`. Соответствует MVC и декларируемому SOLID.
- **Сквозные задачи через фильтры**: кодировка, CSRF, аутентификация, членство в организации, роли, логирование ошибок — порядок в `web.xml` корректен (Encoding до Csrf, Auth до role-фильтров).
- **DI без фреймворка** через `ServiceRegistry`, хранимый в `ServletContext`; сервлеты — синглтоны, `LicenseController` stateless.
- **Транзакции** инкапсулированы в `DatabaseConfig.inTransaction`; репозитории имеют перегрузки `(Connection, ...)` — транзакция охватывает создание лицензии, типов и тегов.
- **Фоновый воркер** пересчёта статусов в `AppContextListener` (`ScheduledExecutorService`), корректно останавливается в `contextDestroyed`.

### 3.2 Архитектурные замечания

| # | Место | Замечание |
|---|---|---|
| A1 | `config/DatabaseConfig.java:287-296` | Нет **пула соединений** (DataSource/JNDI): каждое обращение к БД открывает новое `DriverManager.getConnection`. Под нагрузкой это дорого и ограничивает масштабирование. |
| A2 | `service/*`, интерфейсы репозиториев | Перегрузки `(Connection, ...)` протекают JDBC в сервисный слой. Приемлемый компромисс, но связывает бизнес-логику с реализацией доступа к данным. |
| A3 | `util/CurrentUser.java:17,50-52` | Дублирование состояния ролей (кэш `memberRoleMap`) в сессии — источник рассинхронизации (см. B9). |
| A4 | `service/StatsService.java:55-91` | N+1: для каждой организации отдельные запросы участников и лицензий; `getGlobalStats` выгружает **все** лицензии/пользователей, чтобы посчитать размеры. Для админки терпимо, но не масштабируется. |
| A5 | `service/LicenseService.java:126-140` | `getTypes`/`getTags` делают по одному `findById` на каждый id (N+1) вместо пакетного запроса. |
| A6 | `service/LicenseService.java:142-152` | `recalculateStatuses` — не транзакционный, обновляет лицензии по одной; при сбое возможен частично пересчитанный статус. |
| A7 | `web/OrganizationServlet.java` + `web/controller/LicenseController.java` | Смешение: сервлет организации также маршрутизирует лицензии. Для учебного проекта допустимо, но растёт связность. |
| A8 | `service/AuthService.java:43-51` | Ошибка аутентификации бросает `ValidationException` (семантически это аутентификация, а не валидация) → клиент получает `400`, а не `401`. |
| A9 | `web/LoginServlet.java:36-44`, `web/RegisterServlet.java:37-38` | Дублирование сборки `CurrentUser`; загрузка членств есть только при логине — при регистрации/после изменения ролей карта ролей может быть неполной. |
| A10 | `service/OrganizationService.java` | Нет операции удаления участника и нет транзакций для составных операций (см. B5, B6). |
| A11 | Всё приложение | Нет слоя логирования (только `java.util.logging` + `System.out`), нет конфигурации через `web.xml`/JNDI, нет версионирования API. |

---

## 4. Потенциальные ошибки и баги

### 🔴 B1. POST-маршруты организации недостижимы (404)

**Файл:** `web/OrganizationServlet.java:110-114` (и `handleOrganizationAction:131-157`)

```java
switch (PathUtil.segment(segments, 1)) {
    case "licenses" -> licenseController.doPost(req, resp, segments, orgId);
    case null       -> handleOrganizationAction(req, resp, segments, orgId);
    default         -> renderError(req, resp, SC_NOT_FOUND, "Страница не найдена");
}
```

`handleOrganizationAction` вызывается только при `segment(segments,1) == null`, но внутри он снова читает тот же сегмент:

```java
String action = PathUtil.segment(segments, 1); // всегда null
switch (action == null ? "" : action) { ... default -> 404 }
```

Итог: запросы `POST /org/{orgId}/reissue-code`, `POST /org/{orgId}/promote`, `POST /org/{orgId}/lower` попадают в `default` внешнего `switch` и возвращают **404**. Кнопки «Перевыпустить код», «Сделать админом», «Сделать менеджером» (`org/settings.jsp`) **не работают**. Это подтверждается и `docs/API.md:58-60`, где маршруты заявлены как рабочие. Баг присутствует и в HEAD.

**Рекомендация:** маршрутизировать не-`licenses` сегменты в `handleOrganizationAction`, например `case null, "reissue-code", "promote", "lower" -> handleOrganizationAction(...)` либо вынести действия в `default`-ветку с валидацией.

### 🔴 B2. IDOR: чтение чужой лицензии по orgId (cross-tenant)

**Файл:** `web/controller/LicenseController.java:87-119` (`handleLicenseInfoRequest`)

При `GET /org/{orgId}/licenses/{licenseId}` контроллер делает `getById(licenseId)` и **не проверяет** `license.getOrgId().equals(orgId)`. `OrgMembershipFilter` проверяет членство только в `{orgId}` из пути. Значит участник организации A, подставив id лицензии организации B, увидит карточку и форму редактирования чужой лицензии (утечка title, дат, заметок, типов, тегов).

`docs/API.md:94` обещает «несовпадение с orgId → 404», но для GET это не выполняется. POST-операции (`edit/cancel/restore/delete`) проверку **имеют** (`LicenseController.java:179`), поэтому дыра — на чтение.

**Рекомендация:** в `handleLicenseInfoRequest` (и в `showLicense`) проверять принадлежность: `if (!orgId.equals(license.getOrgId())) renderError(404)`.

### 🔴 B3. Пароли хранятся в несолёном SHA-256

**Файл:** `util/PasswordHasher.java:9,14-30`

Используется одиночный `MessageDigest` SHA-256 без соли и без растяжения (work factor). Это позволяет радужные таблицы и быстрый offline-брутфорс. `README.md:76` при этом декларирует **BCrypt**. Дополнительно `verify` сравнивает через `String.equals` (не константное время).

**Рекомендация:** перейти на bcrypt/argon2/scrypt (например, `org.mindrot:jbcrypt` или `de.mkammerer:argon2-jvm`), либо на PBKDF2 (`javax.crypto.SecretKeyFactory`) с солью; сравнение — `MessageDigest.isEqual`.

### 🟠 B4. Не enforced инвариант «в организации всегда ≥ 1 ADMIN»

**Файл:** `service/OrganizationService.java:59-75`

`lowerToManager` не проверяет, что после понижения останется хотя бы один `ADMIN`. Админ может понизить себя или всех админов, оставив организацию без управления (нарушение `README.md:218`). Также нет запрета на само-понижение.

**Рекомендация:** перед понижением считать админов и запрещать операцию, если останется 0.

### 🟠 B5. Создание организации нетранзакционно (возможны «сироты»)

**Файл:** `service/OrganizationService.java:29-37`

`organizationRepository.create(...)` и `memberRepository.create(...)` — две независимые операции. Если вторая упадёт, останется организация без администратора (нарушение инварианта). Обернуть в `DatabaseConfig.inTransaction`.

### 🟠 B6. Гонки check-then-act → 500 вместо валидации

- `AuthService.register:33-40` — проверка существования email, затем вставка. При гонке — нарушение `UNIQUE`, `RepositoryException` → **500** вместо «email уже занят».
- `OrganizationService.joinOrganization:43-48` — проверка членства, затем вставка (PK `(org_id,user_id)`).
- `LicenseTypeService.create:26-29` — проверка `findByCode`, затем вставка.
- `TagService.getOrCreate:20-30` — проверка, затем вставка (в рамках транзакции лицензии откатится, но вне её — 500).
- `OrganizationService.generateUniqueJoinCode:102-110` — проверка + вставка без ретрая на конфликт.

**Рекомендация:** обрабатывать `SQLState 23505` (unique violation) и транслировать в `ValidationException`, либо использовать `INSERT ... ON CONFLICT`.

### 🟠 B7. Суперадмин не может изменять лицензии, хотя фильтры его пропускают

**Файл:** `web/controller/LicenseController.java:179,216-218`

`belongsToCurrentOrg` смотрит в `currentUser.getMemberRoleMap()`; у суперадмина карты членства нет, поэтому `cancel/restore/delete/edit` для него вернут 404, тогда как `OrgMembershipFilter` и `OrgRoleFilter` суперадмина пропускают (`isSuperadmin()`). Поведение противоречиво: просмотр есть, изменение — нет. (Если это осознанно — «наблюдатель», как в `README.md:251`, стоит явно задокументировать и убрать мёртвые ветки.)

### 🟡 B8. Утечка внутренних деталей ошибки пользователю

**Файл:** `web/ErrorServlet.java:40-45`

В `error.jsp` выводится `exception.getClass().getSimpleName() + ": " + message` — имена классов и сообщения БД/стека доступны конечному пользователю. Оставить детали в логах, а пользователю — обобщённое сообщение.

### 🟡 B9. Устаревший кэш ролей в сессии

**Файл:** `util/CurrentUser.java:17,50-52`; использование — `web/controller/LicenseController.java:216-218`, `views/layout/org-navbar.jsp`

Карта ролей заполняется при логине и мутируется при создании/вступлении в организацию. После повышения/понижения другим админом карта в сессии остаётся старой до перелогина: навигация (`org-navbar.jsp`) и проверка `belongsToCurrentOrg` оперируют устаревшими данными. Фильтры при этом ходят в БД, что создаёт рассогласование между UI и реальными правами.

**Рекомендация:** не хранить производные права в сессии; брать роль из БД на каждый запрос (как делают фильтры) либо инвалидировать сессию при изменении роли.

### 🟡 B10. `ResultSet` не закрыт

**Файл:** `repository/JdbcOrganizationRepository.java:127`

`ResultSet rs = statement.executeQuery();` — без try-with-resources (в остальных методах закрывается). Утечка ресурса до закрытия `Connection`; лучше закрывать явно.

### 🟡 B11. Нет ограничений длины полей

`AuthService.register` не проверяет длины (`email` ≤ 255, `last_name/first_name/middle_name` ≤ 100). `LicenseService.validateTitle` — без ограничения 255. `OrganizationService.createOrganization` — без 255. `TagService.normalize` — без 50. Слишком длинный ввод приводит к ошибке БД → 500 вместо аккуратной валидации.

### 🟡 B12. Отсутствие базовой безопасности на уровне транспорта/сессии

- Нет security-заголовков (`Content-Security-Policy`, `X-Frame-Options`, `X-Content-Type-Options`, HSTS).
- Не задаются флаги cookie сессии (`HttpOnly`, `Secure`, `SameSite`) — зависят от контейнера.
- Нет ограничения попыток входа (brute-force), нет задержки/капчи.
- Нет проверки, что приложение работает по HTTPS.

### 🟡 B13. Статус лицензии, истекающей «сегодня»

**Файл:** `service/LicenseService.java:183-193`

`expiresAt.isBefore(today)` → EXPIRED, иначе если `<= today+30` → EXPIRING. Лицензия, истекающая **сегодня**, получает `EXPIRING`, а не `EXPIRED`. Граничные значения стоит зафиксировать тестом и в документации.

### 🔵 B14. Мелкие функциональные/UX-дефекты

- `views/org/settings.jsp:18,22-23` — заголовки «Фамилия»/«Имя» перепутаны с ячейками: под «Фамилия» выводится `firstName`, под «Имя» — `lastName`.
- `views/licenses/detail.jsp:23-31` — кнопки «Отменить» и «Восстановить» показываются всегда, независимо от текущего статуса.
- `views/licenses/form.jsp:24` — при создании значение `<c:out value='${license.title}'/>` внутри `value="..."` с вложенными кавычками; работает, но хрупко.
- `LicenseController.doGet:39-44` — любой неизвестный третий сегмент (`.../{id}/что-угодно`) трактуется как просмотр карточки, а не 404.
- `AdminDashboardServlet`/`dashboard.jsp` — выдача/снятие суперадмина по «ручному» вводу `userId` без списка пользователей; нет защиты от снятия последнего суперадмина или самого себя.

---

## 5. Соответствие документации реализации

| # | Документ | Заявлено | Факт |
|---|---|---|---|
| D1 | `README.md:76` | пароль хранится как **BCrypt** | несолёный SHA-256 (`PasswordHasher.java`) |
| D2 | `README.md:83,188,301` | сущность `AuditLog` и «журнал значимых изменений» | в коде и в `docs/domain-ddl.sql` отсутствует |
| D3 | `README.md:23,289-293` | «JS + AJAX», каталоги `static/` (CSS/JS) и `WEB-INF/tags/` | файлов нет; `static/` и `tags/` отсутствуют |
| D4 | `README.md:74-107` | PK `BIGSERIAL` | `docs/domain-ddl.sql` использует `UUID DEFAULT uuidv7()` |
| D5 | `docs/API.md:58-60` | POST `reissue-code`/`promote`/`lower` работают | не работают (см. B1) |

Отдельно: `README.md:36,210,240` корректно описывает фоновый воркер — он реализован. `README.md:66` и `docs/API.md` в целом точно описывают домен и статусы.

---

## 6. Тестирование

- Покрытие минимально: только `service/AuthServiceTest` и `service/LicenseServiceTest` (unit, с in-memory/stub-репозиториями). **Нет тестов** на репозитории (SQL), фильтры (авторизация/CSRF/членство/роли), сервлеты/маршрутизацию, JSP.
- Нет интеграционных тестов с БД (Testcontainers/H2/embedded PG), нет проверки DDL.
- Нет CI-конфигурации (GitHub Actions и т.п.).
- `LicenseServiceTest:80-85` использует `assertSame` на списке из стаба — хрупкая проверка, зависит от реализации заглушки, а не от контракта.
- В `LicenseServiceTest` не покрыты транзакционные пути (`create`/`update`), т.к. они упираются в реальное соединение (`DatabaseConfig.inTransaction`).

---

## 7. Приоритизированный план исправлений

**Сначала (критично):**
1. Починить маршрутизацию `OrganizationServlet.doPost` (B1) — без этого не работают настройки организации.
2. Добавить проверку `orgId == license.orgId` на чтение лицензии (B2).
3. Заменить хеширование пароля на bcrypt/argon2 (B3).

**Затем (высоко):**
4. Ввести проверку инварианта «≥1 ADMIN» и запрет само-понижения (B4).
5. Обернуть создание организации в транзакцию (B5).
6. Обрабатывать unique-violation как `ValidationException` (B6).
7. Определиться с правами суперадмина на изменение лицензий (B7).

**Далее (средне):**
8. Убрать/пересматривать кэш ролей в сессии (B9).
9. Ограничить длины входных полей (B11).
10. Скрыть детали исключений от пользователя (B8).
11. Добавить security-заголовки и флаги cookie, ограничение попыток входа (B12).
12. Подключить пул соединений (A1) и устранить N+1 в админке (A4, A5).
13. Дописать тесты (репозитории, фильтры, маршруты) и CI.

**Полировка:**
14. Логирование вместо `System.out`/`printStackTrace` (S3, S4).
15. Исправить перепутанные колонки в `settings.jsp` и видимость кнопок статуса (B14).
16. Привести README/API в соответствие с реализацией (D1–D5).
17. Зафиксировать удаление устаревших сервлетов, удалить пустые каталоги.

---

## 8. Итоговая оценка

| Критерий | Оценка | Комментарий |
|---|---|---|
| Архитектура | Хорошо | Явные слои, интерфейсы, DI, фильтры; минус — нет пула соединений и есть N+1 |
| Качество кода / стиль | Удовлетворительно | Читаемо и консистентно, но есть мёртвый код, разнобой в импортах, `System.out` |
| Корректность / баги | Требует доработки | Не работают POST-маршруты организации; IDOR; несколько гонок и не-enforced инвариантов |
| Безопасность | Требует доработки | Несолёный SHA-256, IDOR, утечка деталей ошибок, нет заголовков/анти-брутфорса |
| Тесты | Недостаточно | Только 2 unit-теста сервисов, нет интеграционных и web-тестов |
| Документация | Расходится с кодом | README описывает несуществующие BCrypt/AuditLog/static/AJAX и BIGSERIAL |

Проект демонстрирует правильную базовую архитектуру сервлетного приложения, но перед демонстрацией/эксплуатацией необходимо устранить пункты B1–B3 (критично) и B4–B7 (высоко), а также привести документацию в соответствие с фактической реализацией.

---

### Приложение: карта ключевых файлов

```
config/        AppConfig, DatabaseConfig (пул отсутствует, inTransaction), ServiceRegistry (DI)
listener/      AppContextListener (воркер пересчёта статусов)
filter/        ErrorLogging, Encoding, Csrf, Auth, OrgMembership, OrgRole, Superadmin
model/         User, Organization, OrganizationMember, License, LicenseType, Tag + enum-ы
repository/    интерфейсы + Jdbc*-реализации, util/RowMapper
service/       Auth, Organization, License, LicenseType, Tag, Stats
web/           Home, Login, Logout, Register, Onboarding, JoinOrganization, Organization,
               AdminDashboard, AdminAction, Error, BaseServlet, controller/LicenseController
util/          PasswordHasher, CsrfTokenUtil, JoinCodeGenerator, ParamUtil, PathUtil,
               SessionUtil, CurrentUser
webapp/WEB-INF/views/  JSP (auth, home, licenses, org, admin, layout, error)
docs/          API.md, domain-ddl.sql
```
