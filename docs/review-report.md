# Code Review — License Registry

**Проект:** `license-registry-service`
**Тип:** Java 24 Servlet / JSP / JDBC / PostgreSQL 18 (MVC), Gradle
**Дата ревью:** 2026-11-14
**Ревизия:** рабочее дерево, HEAD `7e9c3ea` («Fix web layer and JSP»)
**Объём:** весь `src/`, `src/main/webapp/WEB-INF/web.xml`, `build.gradle.kts`, JSP, тесты, документация (`README.md`, `docs/`)
**Режим:** read-only. Изменения в кодовую базу не вносились; создан только этот отчёт.
**Проверка сборки:** `./gradlew --offline test` → `BUILD SUCCESSFUL`, тесты сервисов проходят.

---

## 1. Резюме

Проект демонстрирует **здоровую слоистую архитектуру** (web → service → repository → model), репозитории за интерфейсами, простой DI через `ServiceRegistry`, фильтры для сквозных задач (кодировка, CSRF, аутентификация, членство, роли, логирование) и понятную доменную модель. Код читается, конвенции в целом выдержаны, есть unit-тесты сервисов, SQL везде параметризован (`PreparedStatement`) — SQL-инъекций не найдено, а пользовательские данные в JSP в основном экранируются через `<c:out>`.

Большинство ранее известных критических дефектов в текущей ревизии **устранено**: маршрутизация `POST`-действий организации работает, проверка принадлежности лицензии организации (IDOR на чтение) добавлена, создание организации обёрнуто в транзакцию, инвариант «≥ 1 ADMIN» проверяется, конфликты `UNIQUE` транслируются в `ValidationException`, ограничения длины полей добавлены, детали исключений больше не утекают на страницу ошибки, перепутанные колонки в `settings.jsp` и безусловные кнопки статуса исправлены.

Тем не менее остаются значимые проблемы — прежде всего в **безопасности** (несолёный SHA-256 для паролей, отсутствие пула соединений, отсутствие security-заголовков и защиты от перебора), **производительности** (N+1 в админке и при загрузке типов/тегов) и **согласованности модели доступа** (глобальный справочник `LicenseType` редактируется из контекста организации; суперадмин вопреки документации может менять лицензии). Ниже — сводка по приоритетам.

| Уровень | Кол-во | Примеры |
|---|---|---|
| 🔴 Критично | 1 | несолёный SHA-256 для хранения паролей |
| 🟠 Высоко | 6 | нет пула соединений; N+1 в админке и при загрузке связей; глобальный `LicenseType` пишется из организации; суперадмин меняет лицензии; 500 при пустом пароле; неполные/небезопасные дефолты сессии |
| 🟡 Средне | 9 | сессия создаётся на каждый запрос; нет security-заголовков и флагов cookie; неизвестные URL отдают главную; гонка join-кода; нет `session-config`; конвейер фильтров по явному списку; `updated_at` перетирается воркером; `401` без error-page |
| 🔵 Стиль / низко | ~12 | мёртвый код и неиспользуемые методы; разнобой импортов; дублирование SQLState-констант; неоднородные репозитории; смешение `Servlet`/`Controller`; `AppConfig` из одной константы |
| 📄 Документация | 5 | README: `BIGSERIAL` вместо `UUID`; `AuditLog`/история изменений; JS+AJAX и каталоги `static/`, `WEB-INF/tags/`; Gradle 9.8.2 против wrapper 9.2.0 |

---

## 2. Методика

- Прочитаны все исходники `src/main/java` (32 класса), все JSP (15), `web.xml`, `build.gradle.kts`, `settings.gradle.kts`, оба теста, `README.md`, `docs/API.md`, `docs/domain-ddl.sql`.
- Выполнен поиск использования по коду для выявления мёртвых методов.
- Проверены инварианты, граничные случаи и потенциальные NPE.
- Отдельно проверено поведение `String.format("%02x", byte)` (корректно формирует два hex-символа для отрицательных байт — ложного бага здесь нет).
- Сборка и тесты запущены локально (offline) — проходят.

---

## 3. Качество написания и code style

### 3.1 Положительное

- Единообразное именование классов/пакетов; аккуратная иерархия исключений (`ValidationException`, `NotFoundException`, `RepositoryException`).
- Репозитории реализуют интерфейсы, сервисы получают зависимости через конструктор — легко стабировать в тестах (что и делается).
- Все обращения к БД — через `PreparedStatement` с параметрами; динамический SQL в `search` строится только из фиксированных фрагментов и плейсхолдеров.
- Магические числа вынесены в константы (`EXPIRING_THRESHOLD_DAYS`, `MIN_PASSWORD_LENGTH`, `MAX_JOIN_CODE_ATTEMPTS`, `CODE_LENGTH`, `ADMIN_ONLY_SECTIONS`).
- JSP используют JSTL и `<c:out>` для пользовательских данных; `csrfToken` подставляется во все формы `POST`.
- Показательная деталь: `web.xml` задаёт `page-encoding UTF-8`, единый error-page, а `EncodingFilter` вынесен в начало цепочки.

### 3.2 Замечания по стилю

| # | Место | Проблема |
|---|---|---|
| S1 | По всему коду | Непоследовательный порядок импортов: в одних файлах сначала `java.*`, в других — свои пакеты (`exception.*`, `model.*`) вперемешку. Нет единого формата (например, `google-java-format`/`spotless`). |
| S2 | `web/controller/LicenseController.java` (напр. 36–38, 47–48, 64–65, 173–175), `web/OrganizationServlet.java` (напр. 39–40, 137–138) | Длинные однострочные сигнатуры (> 120 символов) ухудшают читаемость. |
| S3 | `util/PathUtil.segments` (`substring(1).split("/")`) | Неочевидная обработка `"/"` → `[""]`; полагается на `isOrgRoot`. Комментария/теста нет. |
| S4 | `repository/util/RowMapper` | Мапит только `User`; все остальные репозитории держат приватный `mapRow` вручную. Единая стратегия маппинга отсутствует. |
| S5 | `service/*` | Константы `"23505"`/`"23503"` (SQLState) продублированы в `AuthService`, `OrganizationService`, `LicenseTypeService`. Стоит вынести в одно место. |
| S6 | `config/AppConfig` | Класс из одной константы `EXPIRING_THRESHOLD_DAYS`; уместнее константа в сервисном слое или общий конфиг. |
| S7 | `web/util/View.java` (после `LOGGER`), `service/AdminService.java` (строки 20–21) | Двойные пустые строки. Мелочь. |
| S8 | `model/*` | Классы-сущности без `equals`/`hashCode`/`toString`. Для простых DTO это допустимо, но сравнение сущностей в тестах затруднено. |
| S9 | JSP (`list.jsp`, `settings.jsp`, `org-list.jsp`) | Значения `${type.id}`, `${tag.id}`, `${org.id}`, `${member.userId}` вставляются в разметку без `<c:out>` прямо в атрибуты. Сейчас это `UUID` (безопасно), но паттерн «сырой EL в разметке» потенциально опасен при появлении произвольных данных. |

---

## 4. Архитектура

### 4.1 Сильные стороны

- **Чёткое разделение слоёв**: `web` (сервлеты/фильтры/`LicenseController`), `service`, `repository`, `model`, `config`, `util`, `exception`. MVC соблюдён.
- **Сквозные задачи через фильтры** в корректном порядке (`web.xml`): `ErrorLogging` → `Encoding` → `Csrf` → `Auth` → `OrgMembership` → `OrgRole` → `Superadmin`. Кодировка применяется до чтения параметров; аутентификация — до проверок ролей.
- **DI без фреймворка** через `ServiceRegistry` в `ServletContext`; сервлеты-синглтоны, `LicenseController` stateless — потокобезопасны.
- **Транзакции** инкапсулированы в `DatabaseConfig.inTransaction`; перегрузки репозиториев `(Connection, ...)` позволяют атомарно создавать лицензию вместе с типами и тегами.
- **Фоновый воркер** пересчёта статусов (`AppContextListener`, `ScheduledExecutorService`) корректно останавливается в `contextDestroyed`.
- **Разграничение прав** реализовано и на уровне фильтров, и на уровне контроллера (`canManage`, проверка `orgId` лицензии).

### 4.2 Архитектурные замечания

| # | Место | Замечание |
|---|---|---|
| A1 | `config/DatabaseConfig.java:290-299` | **Нет пула соединений.** Каждое обращение к БД открывает новый `DriverManager.getConnection`; на один запрос страницы их может быть несколько (фильтры + контроллер + сервис). Дорого под нагрузкой, ограничивает масштабирование. Нужны HikariCP / JNDI `DataSource`. |
| A2 | Интерфейсы репозиториев | Неоднородность: часть методов принимает `Connection`, часть — нет, часть имеет обе перегрузки. Сервисный слой вынужден знать про JDBC-соединение → протечка абстракции. |
| A3 | `service/LicenseService.java:128-142` | `getTypes`/`getTags` — N+1: по одному `findById` на каждую связь. Нужен пакетный `findAllByIds`/`IN (...)`. |
| A4 | `service/AdminService.java:42-60, 100-113` | `getGlobalStats` выгружает **все** строки `users`/`organizations`/`licenses`, чтобы посчитать размеры; `getOrganizationSummaries` делает по 2 запроса (участники + лицензии) на каждую организацию. N+1 + лишний трафик; просится `COUNT(*)` и агрегирующий запрос. |
| A5 | `service/LicenseService.java:144-154` | `recalculateStatuses` — не транзакционный, обновляет лицензии по одной (каждый `updateStatus` открывает соединение). При сбое — частично пересчитанные данные. Логично делать одним `UPDATE ... CASE`. |
| A6 | `web/OrganizationServlet.java` + `web/controller/LicenseController.java` | Сервлет организации и маршрутизирует свои действия, и делегирует лицензии в отдельный «контроллер». Для учебного проекта приемлемо, но растёт связность; стоит явно назвать `OrganizationServlet` фронт-контроллером. |
| A7 | `filter/OrgMembershipFilter`, `filter/OrgRoleFilter`, `LicenseController.exposeOrgContext/resolveOrgRole` | Один и тот же факт (роль пользователя в организации) запрашивается из БД до трёх раз за запрос. Можно кэшировать на время запроса. |
| A8 | `service/AuthService.java:56-64` | Ошибка аутентификации бросает `ValidationException` (семантика валидации, а не `401`), и `LoginServlet` возвращает `200` с формой. Для HTML-приложения допустимо, но не соответствует REST-семантике и `docs/API.md`. |
| A9 | `web/OrganizationServlet.java:91-99` | Два запроса (`getByUserId` + `getMembershipsByUserId`) ради одних и тех же членств; роли можно собрать из одного результата. |
| A10 | `web.xml` | Защищаемые пути перечислены вручную (`/`, `/onboarding`, `/org`, `/org/*`, `/admin`, `/admin/*`). Легко забыть новый защищённый маршрут. Надёжнее `/*` + белый список публичных путей. |
| A11 | Всё приложение | Нет конфигурации через `web.xml`/JNDI (кроме `.env`), нет слоя мониторинга/метрик, нет версионирования. Для семестрового проекта — норма. |

---

## 5. Потенциальные ошибки и баги

### 🔴 B1. Пароли хранятся в несолёном SHA-256

**Файлы:** `util/PasswordHasher.java:9,14-30`

```java
private static final String ALGORITHM = "SHA-256";
...
return hash(rawPassword).equals(storedHash);
```

Одиночный `MessageDigest` SHA-256 без соли и без растяжения (work factor). Это допускает радужные таблицы, одинаковые хеши для одинаковых паролей и быстрый offline-брутфорс. `README.md:76` честно документирует SHA-256, но сам выбор алгоритма для хранения паролей недопустим.

**Рекомендация:** перейти на bcrypt/argon2/scrypt (например `org.mindrot:jbcrypt`, `de.mkammerer:argon2-jvm`) или на PBKDF2 (`SecretKeyFactory`, соль + много итераций). Сравнение выполнять через `MessageDigest.isEqual` (константное время).

---

### 🟠 B2. NPE / HTTP 500 при отсутствии параметра `password` на `POST /login`

**Файлы:** `service/AuthService.java:56-64`, `util/PasswordHasher.java:14-17,28-30`, `web/LoginServlet.java:27-39`

```java
public User authenticate(String email, String rawPassword) {
    ...
    if (!PasswordHasher.verify(rawPassword, user.getPasswordHash())) { ... }
```

Если `email` существующего пользователя передан, а `password` отсутствует (`request.getParameter("password") == null`), управление доходит до `PasswordHasher.verify(null, hash)` → `hash(null)` → `rawPassword.getBytes(...)` → **`NullPointerException`**. HTML-атрибут `required` на форме обходится прямым POST. Итог — `500` вместо аккуратной ошибки. `register` от такого защищён (проверка `rawPassword == null`), а `authenticate` — нет.

**Рекомендация:** добавить проверку `if (rawPassword == null || rawPassword.isEmpty()) throw new ValidationException("Неверный email или пароль")`.

---

### 🟠 B3. Нет пула соединений (см. A1)

**Файл:** `config/DatabaseConfig.java:259-261,290-299`

`getConnection()` каждый раз открывает физическое соединение через `DriverManager`. При этом фильтры и контроллер за один запрос могут открывать 3–5 соединений. Под нагрузкой это узкое место и исчерпание соединений БД.

**Рекомендация:** подключить HikariCP (или `DataSource` из JNDI), выдавать соединения из пула.

---

### 🟠 B4. Глобальный справочник `LicenseType` редактируется из контекста организации

**Файлы:** `filter/OrgRoleFilter.java:23`, `web/controller/LicenseController.java:157-171` (`createType`), `service/LicenseTypeService.java:25-39`

`LicenseType` — **глобальный** справочник (в DDL нет `org_id`, `docs/domain-ddl.sql:70-80`). Но форма создания типа размещена на странице `/org/{orgId}/licenses/types`, а `OrgRoleFilter` считает admin-only только `reissue-code`/`promote`/`lower`. Значит **любой MANAGER любой организации** может создавать глобальные типы, видимые всем тенантам. Это межтенантная запись в общие данные и размывание изоляции.

**Рекомендация:** либо сделать `LicenseType` частью организации (`org_id`), либо ограничить управление типами ролью `ADMIN`/суперадмином в отдельном разделе и явно задокументировать глобальность справочника.

---

### 🟠 B5. Суперадмин может изменять и удалять лицензии, хотя объявлен «наблюдателем»

**Файлы:** `web/controller/LicenseController.java:240-248` (`canManage`), `186`; `filter/OrgMembershipFilter.java:33-36`

```java
private boolean canManage(CurrentUser user, UUID orgId) {
    if (user.isSuperadmin()) return true;   // ← полный CRUD по любой организации
    ...
}
```

`OrgMembershipFilter` пропускает суперадмина в любую организацию, а `canManage` разрешает ему `edit/cancel/restore/delete`. При этом `README.md:251,256` описывает суперадмина как «наблюдателя над всем» и выносит управление лицензиями в зону админов организаций/менеджеров. Поведение противоречит документации.

**Рекомендация:** определиться — либо явно разрешить суперадмину редактирование (и поправить README), либо оставить только чтение (вернуть `false` для суперадмина в изменяющих операциях).

---

### 🟠 B6. Дефолты сессии и отсутствие защиты от перебора

**Файлы:** `src/main/webapp/WEB-INF/web.xml` (нет `<session-config>`), `util/SessionUtil.java`

- Не заданы `HttpOnly`, `Secure`, `SameSite` для cookie сессии — зависят от контейнера.
- Нет `<session-config>` (таймаут по умолчанию), нет смены/ротации токена сессии, кроме `changeSessionId()` при логине.
- Нет **ограничения попыток входа** (brute-force / lockout / задержка / капча).
- Нет security-заголовков (`Content-Security-Policy`, `X-Frame-Options`, `X-Content-Type-Options`, HSTS) и требования HTTPS.

**Рекомендация:** задать cookie-флаги и `session-config`, добавить простой rate-limit по IP/email, выставить security-заголовки.

---

### 🟡 B7. Сессия создаётся на каждый запрос (включая анонимные)

**Файл:** `filter/CsrfFilter.java:22`

`request.getSession(true)` вызывается безусловно для всех путей `/*`, ещё до аутентификации. Каждый бот/сканер получает полноценную сессию, что даёт бесплатный рост потребления памяти. Токен при этом не ротируется и не меняется после логина.

**Рекомендация:** создавать сессию лениво (только когда нужен токен) или не создавать для очевидно анонимных/статических путей; ротировать CSRF-токен при смене аутентификации.

---

### 🟡 B8. Неизвестные URL отдают главную страницу вместо 404

**Файл:** `web/HomeServlet.java:14` (`@WebServlet("/")`)

Маппинг `/` делает `HomeServlet` «сервлетом по умолчанию»: любой несуществующий путь (например `/foo`, `/favicon.ico`) попадает в него. Для авторизованного пользователя это отдаёт `home/index.jsp` со статусом `200`, а не `404`. Для анонимного — редирект на `/login`.

**Рекомендация:** явно возвращать `404` для нераспознанных путей.

---

### 🟡 B9. Гонка при генерации `join_code` → 500

**Файл:** `service/OrganizationService.java:129-137,33-48`

`generateUniqueJoinCode` проверяет `findByJoinCode`, затем код вставляется в транзакции. При конкурентной коллизии `UNIQUE (join_code)` выбросит `RepositoryException`, которая **не** перехватывается в `createOrganization` → `500`. Вероятность низкая (10 символов, алфавит 32), но обработка `23505` с ретраем устранила бы проблему надёжно.

**Рекомендация:** ловить `SQLState 23505` и повторять генерацию (как в `joinOrganization`), либо генерировать код и вставлять `ON CONFLICT`.

---

### 🟡 B10. `updated_at` лицензии перетирается фоновым воркером

**Файлы:** `repository/JdbcLicenseRepository.java:188-199` (`updateStatus`), `service/LicenseService.java:144-154`

`updateStatus` делает `SET status = ?, updated_at = now()`. Воркер статусов вызывает это ежедневно, поэтому `updated_at` перестаёт отражать «время последнего редактирования пользователем» и отражает «время последнего пересчёта». Семантически неверно для аудита.

**Рекомендация:** не трогать `updated_at` при техническом пересчёте статуса, либо завести отдельное поле `status_updated_at`.

---

### 🟡 B11. Нет error-page для `401`

**Файлы:** `src/main/webapp/WEB-INF/web.xml:98-117`, `filter/OrgMembershipFilter.java:29`, `filter/OrgRoleFilter.java:33`

Фильтры умеют отдавать `SC_UNAUTHORIZED`, но `error-page` для `401` не объявлен (есть только `400/403/404/500`). В этих редких ветках пользователь получит стандартную страницу контейнера, а не `error.jsp`. Также сами 401-ветки почти недостижимы: `AuthFilter` раньше редиректит неаутентифицированных на `/login`.

**Рекомендация:** добавить `401` в `error-page` либо убрать недостижимые ветки.

---

### 🟡 B12. Мелкие функциональные/UX-дефекты

- `web/OrganizationServlet.java:119` — при `NotFoundException` показывается `404`, но нет обработки прочих сбоев; ок.
- `web/RegisterServlet.java` — при ошибке валидации введённые значения не сохраняются в форме (пользователь вводит заново).
- `web/controller/LicenseController.java:45,128-130` — GET `.../licenses/{id}/{unknown}` → `404` (ок), но GET `.../licenses/{unknownId}` → `404` (ок). Логика корректна, но `parseStatus` молча превращает опечатку в `null` (нет фильтра) — это может ввести в заблуждение.
- `service/LicenseService.java:185-195` — лицензия, истекающая **сегодня**, получает `EXPIRING`, а не `EXPIRED`. Граничное значение не зафиксировано тестом/документацией.
- `views/admin/dashboard.jsp:39-48` — выдача/снятие суперадмина по «ручному» вводу `userId` без списка пользователей; нет защиты от снятия роли с самого себя (кроме «последнего суперадмина»).
- `views/licenses/list.jsp:13-19` — фильтр статуса отдаёт значения `ACTIVE/EXPIRING/...` «как есть», без человекочитаемых подписей.

---

## 6. Безопасность (сводно)

| Область | Состояние | Комментарий |
|---|---|---|
| SQL-инъекции | ✅ | Везде `PreparedStatement`; динамический SQL в `search` собирается только из констант и `?`. |
| XSS | ✅ (в основном) | Пользовательские данные оборачиваются в `<c:out>`; в атрибуты вставляются только UUID. См. S9. |
| CSRF | ✅ | Токен в сессии, проверка на всех `POST`, все формы содержат `csrfToken`. Замечания: токен не ротируется, сравнение не константное. |
| Пароли | ❌ | Несолёный SHA-256 (B1). |
| Session fixation | ✅ | `changeSessionId()` при логине. |
| Идентификация/изоляция | 🟡 | IDOR на чтение лицензии исправлен; но глобальный `LicenseType` пишется из организации (B4). |
| Права суперадмина | 🟡 | Может менять лицензии вопреки README (B5). |
| Секреты | ✅ | `.env` в `.gitignore`, не отслеживается (`git ls-files` показывает только `.env.example`), исключён из WAR/`processResources`. |
| Заголовки/cookie/HTTPS/перебор | ❌ | Не настроены (B6). |
| Детали ошибок | ✅ | `ErrorServlet` для `>=500` отдаёт общее сообщение, детали — только в лог. |
| Пароль в логах | ✅ | Не логируется. |

---

## 7. Соответствие документации реализации

| # | Документ | Заявлено | Факт |
|---|---|---|---|
| D1 | `README.md:74-107` | PK `BIGSERIAL` | `docs/domain-ddl.sql` использует `UUID DEFAULT uuidv7()` (строки 10, 32, 48, 72, 87, 118, 138, 157). |
| D2 | `README.md:83,188,301` | сущность `AuditLog` и «журнал значимых изменений» | в коде и в DDL отсутствует — функции истории/аудита нет. |
| D3 | `README.md:23,289-293` | «JS + AJAX», каталоги `static/` (CSS/JS) и `WEB-INF/tags/` | файлов нет; `src/main/webapp/static` и `WEB-INF/tags` отсутствуют, `src/main/resources` пуст. |
| D4 | `README.md:24` | Gradle `9.8.2` | `gradle/wrapper/gradle-wrapper.properties` закрепляет `gradle-9.2.0`. |
| D5 | `README.md:251,256` | суперадмин — «наблюдатель»; управление лицензиями вне его зоны | `canManage` разрешает суперадмину полный CRUD лицензий (B5). |

Отмечу и корректные места: `README.md` и `docs/API.md` точно описывают домен, статусы, воркер, схему Post/Redirect/Get и роли; `docs/API.md` в целом согласован с текущей маршрутизацией (включая исправленные `reissue-code`/`promote`/`lower`).

---

## 8. Тестирование

- Покрытие минимально: только `service/AuthServiceTest` и `service/LicenseServiceTest` (unit с in-memory/stub-репозиториями). **Нет тестов** на `OrganizationService`, `LicenseTypeService`, `TagService`, `AdminService`, репозитории (SQL/DDL), фильтры (auth/CSRF/членство/роли), сервлеты/маршрутизацию и JSP.
- Нет интеграционных тестов с БД (Testcontainers/embedded PG), нет проверки `docs/domain-ddl.sql`.
- Нет CI (GitHub Actions и т.п.).
- `LicenseServiceTest:80-85` (`searchDelegatesToRepository`) использует `assertSame` на списке из стаба — проверка тавтологична и зависит от реализации заглушки, а не от контракта.
- Транзакционные пути (`create`/`update`) не покрыты: они упираются в реальное соединение `DatabaseConfig.inTransaction`.
- Сборка/тесты выполняются: `./gradlew --offline test` → `BUILD SUCCESSFUL`.

---

## 9. Приоритизированный план исправлений

**Сначала (критично/высоко):**
1. Заменить хеширование паролей на bcrypt/argon2/PBKDF2 (B1).
2. Добавить проверку пустого `password` в `authenticate` (B2).
3. Подключить пул соединений (B3).
4. Устранить N+1 в `getTypes`/`getTags` и в `AdminService` (A3, A4).
5. Решить вопрос владения `LicenseType` (глобальный/организационный) и прав на его создание (B4).
6. Согласовать права суперадмина с README (B5).
7. Настроить cookie-флаги сессии, `session-config`, security-заголовки и ограничение попыток входа (B6).

**Далее (средне):**
8. Ленивое создание сессии и ротация CSRF-токена (B7).
9. Явный `404` для неизвестных путей (B8).
10. Обработка гонки `join_code` через `23505` + ретрай (B9).
11. Не перетирать `updated_at` при техническом пересчёте (B10).
12. Сделать `recalculateStatuses` транзакционным/пакетным (A5); именовать поток воркера, не давать задаче молча умирать.
13. Добавить `401` в `error-page` (B11).
14. Убрать дублирование запросов ролей за один запрос (A7).

**Полировка:**
15. Удалить мёртвый код (см. §10), централизовать SQLState-константы (S5).
16. Ввести единый формат кода (spotless/checkstyle) (S1, S2).
17. Дописать тесты (сервисы, фильтры, маршруты, репозитории, DDL) и CI.
18. Привести README/API в соответствие с реализацией (D1–D5).

---

## 10. Мёртвый код и неиспользуемая поверхность (проверено grep-ом)

| Файл | Элемент | Статус |
|---|---|---|
| `repository/UserRepository.java:17` | `findByOrganizationId` | не вызывается в `main` |
| `repository/UserRepository.java:23` | `delete` | не вызывается |
| `service/LicenseService.java:120` | `getByOrgId` | не вызывается (используется `search`) |
| `service/LicenseTypeService.java:50,64` | `rename`, `delete` | нет эндпоинтов |
| `service/TagService.java:28,37,49` | `getById`, `rename`, `delete` | нет эндпоинтов (`getById` достижим только из мёртвых методов) |
| `repository/LicenseTypeLinkRepository.java:11,17` | `remove`, `findLicenseIdsByTypeId` | не вызываются |
| `repository/LicenseTagRepository.java:11,17` | `remove`, `findLicenseIdsByTagId` | не вызываются |
| `repository/OrganizationRepository.java:24` | `delete` | не вызывается |
| `repository/LicenseTypeRepository.java:22`, `repository/TagRepository.java:22,24` | `update`, `update`/`delete` | достижимы только из мёртвых методов сервисов |

Плюс: `src/main/resources/` пуст, хотя `build.gradle.kts` настраивает `processResources`; каталоги `static/`, `WEB-INF/tags/` отсутствуют. `build/` и `.gradle/` — в `.gitignore`, не отслеживаются.

---

## 11. Итоговая оценка

| Критерий | Оценка | Комментарий |
|---|---|---|
| Архитектура | Хорошо | Явные слои, интерфейсы, DI, корректный конвейер фильтров; минус — нет пула соединений, N+1, протечка `Connection` в сервисный слой |
| Качество кода / стиль | Удовлетворительно | Читаемо и в целом консистентно; есть мёртвый код, разнобой импортов, дублирование констант |
| Корректность / баги | Хорошо | Ранее критические дефекты устранены; остались 500 при пустом пароле, гонка `join_code`, перетирание `updated_at`, граничный статус «истекает сегодня», неизвестные URL → 200 |
| Безопасность | Требует доработки | Несолёный SHA-256, отсутствие пула/заголовков/флагов cookie/анти-брутфорса; глобальный `LicenseType`; права суперадмина против README |
| Тесты | Недостаточно | Только 2 unit-теста сервисов; нет web/интеграционных тестов и CI |
| Документация | Расходится с кодом | README: `BIGSERIAL`/`AuditLog`/`JS+AJAX`/`static`/`tags`/Gradle 9.8.2; расхождение по правам суперадмина |

Проект представляет собой **зрелую базовую архитектуру** сервлетного приложения с уже исправленными ранее критическими уязвимостями. Перед демонстрацией/эксплуатацией необходимо устранить пункты B1–B6 (шифрование паролей, пул соединений, N+1, модель доступа, настройки сессии) и привести документацию в соответствие с фактической реализацией.

---

### Приложение: карта ключевых файлов и зон ответственности

```
config/        AppConfig (одна константа), DatabaseConfig (нет пула; inTransaction),
               ServiceRegistry (ручной DI)
listener/      AppContextListener (воркер пересчёта статусов, раз в сутки)
filter/        ErrorLogging, Encoding, Csrf, Auth, OrgMembership, OrgRole, Superadmin
model/         User, Organization, OrganizationMember, License, LicenseType, Tag + enum-ы
repository/    интерфейсы + Jdbc*-реализации (часть методов с Connection), util/RowMapper
service/       Auth, Organization, License, LicenseType, Tag, Admin
web/           Home("/"), Login, Logout, Register, Onboarding, JoinOrganization, Organization,
               AdminDashboard, AdminAction, Error, BaseServlet, util/View,
               controller/LicenseController
util/          PasswordHasher (SHA-256), CsrfTokenUtil, JoinCodeGenerator, ParamUtil,
               PathUtil, SessionUtil, CurrentUser
webapp/WEB-INF/views/  JSP: auth/, home/, licenses/, org/, admin/, layout/, error.jsp
docs/          API.md, domain-ddl.sql, review-report.md (этот отчёт)
```
