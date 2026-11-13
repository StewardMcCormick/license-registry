# API — HTTP-маршруты

Приложение server-rendered (MVC, JSP): JSON-API нет. Успешный **GET** отдаёт HTML-страницу
(`WEB-INF/views/...`), успешный **POST** — обычно `302 redirect` (схема Post/Redirect/Get).
Ошибки отдаются страницей `error.jsp` с соответствующим HTTP-статусом.

## Общие правила

- Базовый путь — `contextPath`. Ниже пути даны без него.
- Тело POST — `application/x-www-form-urlencoded`.
- **CSRF:** на каждый `POST` обязателен параметр `csrfToken`; иначе `403`.
- **Аутентификация** — сессия (`CurrentUser`). Нет сессии → редирект на `/login` (или `403`/`401` от фильтра).
- Роли: глобальная `GlobalRole` = `SUPERADMIN` | `USER`; в организации `MemberRole` = `ADMIN` | `MANAGER`.
- Путь разбирается по сегментам (`/org/{orgId}/licenses/{id}/{action}`): `null`-сегмент = действие по умолчанию.
- Невалидный UUID в пути/параметре трактуется как `null` → `404`.

## Фильтры (`web.xml`, порядок важен)

| Фильтр | Путь | Поведение |
|---|---|---|
| `ErrorLoggingFilter` | `/*` | логирует необработанные исключения |
| `EncodingFilter` | `/*` | UTF-8 для запроса/ответа |
| `CsrfFilter` | `/*` | кладёт токен в сессию; проверяет `csrfToken` на POST |
| `AuthFilter` | `/`, `/create-organization`, `/join-organization`, `/licenses/*`, `/org/*`, `/admin`, `/admin/*` | требует аутентификации, иначе → `/login` |
| `OrgMembershipFilter` | `/org/*` | требует, чтобы пользователь был участником организации из пути (`/org/{orgId}/...`); иначе `403` |
| `OrgRoleFilter` | `/org/*` | для страницы настроек `/org/{id}` и действий `reissue-code`/`promote`/`lower` требует роль `ADMIN` **именно в этой организации** (роль в другой организации не подходит); иначе `403`. Раздел `licenses` пропускается (доступен и `MANAGER`) |
| `SuperadminFilter` | `/admin`, `/admin/*` | только `SUPERADMIN`, иначе `403` |

`error-page`: `Exception` и коды `400/403/404/500` → `/error`.

## Аутентификация

| Метод | Путь | Параметры | Результат |
|---|---|---|---|
| GET | `/login` | — | форма `auth/login.jsp` |
| POST | `/login` | `email`, `password` | ок → `302 /`; ошибка → форма с `error` |
| GET | `/register` | — | форма `auth/register.jsp` |
| POST | `/register` | `email`, `password`, `lastName`, `firstName`, `middleName` | ок → вход и `302 /`; ошибка → форма с `error` |
| POST | `/logout` | — | инвалидирует сессию, `302 /login` |

Валидация: email по шаблону, пароль ≥ 8 символов, `lastName`/`firstName` обязательны.

## Главная

| Метод | Путь | Результат |
|---|---|---|
| GET | `/` | без сессии → `302 /login`; иначе `home/index.jsp` (`organizations`, `rolesByOrgId`) |

## Организации

| Метод | Путь | Параметры | Доступ | Результат |
|---|---|---|---|---|
| POST | `/create-organization` | `name` | авторизован | ок → `302 /org/{id}/licenses`; ошибка → `home/index.jsp` + `error` |
| POST | `/join-organization` | `joinCode` | авторизован | ок → `302 /org/{id}/licenses`; ошибка → `home/index.jsp` + `error` |
| GET | `/org/{orgId}` | — | `ADMIN` | `org/settings.jsp` (`organization`, `members`); нет → `404` |
| POST | `/org/{orgId}/reissue-code` | — | `ADMIN` | перевыпуск join-кода → `302 /org/{orgId}` |
| POST | `/org/{orgId}/promote` | `userId` | `ADMIN` | повысить до `ADMIN` → `302 /org/{orgId}` |
| POST | `/org/{orgId}/lower` | `userId` | `ADMIN` | понизить до `MANAGER` → `302 /org/{orgId}` |

Создатель организации становится `ADMIN`, присоединившийся по коду — `MANAGER`.
`joinCode` нечувствителен к регистру; повторное вступление → `error`.

## Лицензии

Все пути — внутри организации; доступ только её участникам. Секция — `/org/{orgId}/licenses`.

| Метод | Путь | Параметры | Результат |
|---|---|---|---|
| GET | `/org/{orgId}/licenses` | `status`, `typeId`, `tagId`, `q` (все опциональны) | `licenses/list.jsp` (`licenses`, `types`, `tags`) |
| GET | `/org/{orgId}/licenses/new` | — | `licenses/form.jsp` (`types`) |
| POST | `/org/{orgId}/licenses/new` | см. поля ниже | ок → `302 /org/{orgId}/licenses/{id}`; ошибка → `form.jsp` + `error` |
| GET | `/org/{orgId}/licenses/{id}` | — | `licenses/detail.jsp` (`license`, `types`, `tags`) |
| GET | `/org/{orgId}/licenses/{id}/edit` | — | `licenses/form.jsp` (`license`, `types`, `selectedTypes`, `selectedTags`) |
| POST | `/org/{orgId}/licenses/{id}/edit` | см. поля ниже | ок → `302 .../{id}` |
| POST | `/org/{orgId}/licenses/{id}/cancel` | — | статус → `CANCELLED` → `302 .../{id}` |
| POST | `/org/{orgId}/licenses/{id}/restore` | — | снять `CANCELLED`, пересчитать статус → `302 .../{id}` |
| POST | `/org/{orgId}/licenses/{id}/delete` | — | удалить → `302 /org/{orgId}/licenses` |

**Поля лицензии (create/edit):**

| Поле | Формат | Обяз. | Примечание |
|---|---|---|---|
| `title` | строка | да | обрезается |
| `issuedAt` | ISO date `YYYY-MM-DD` | да | |
| `expiresAt` | ISO date | да | не раньше `issuedAt` |
| `notes` | строка | нет | пустое → `null` |
| `typeIds` | UUID (можно повторять) | нет | каждый тип должен существовать |
| `tags` | строка через запятую | нет | отсутствующие теги создаются автоматически |

Статус `ACTIVE/EXPIRING/EXPIRED` вычисляется по `expiresAt` (порог «истекает» — 30 дней);
`CANCELLED` ставится только вручную и не перетирается.
Несуществующий `{id}` → `404`; несовпадение с `orgId` → `404`.

## Типы лицензий

| Метод | Путь | Параметры | Результат |
|---|---|---|---|
| GET | `/licenses/types` | — | `licenses/types.jsp` (`types`) |
| POST | `/licenses/types` | `code`, `name` | ок → `302 /licenses/types`; ошибка → `types.jsp` + `error` |

`code` приводится к верхнему регистру и уникален; `name` обязателен.

## Админка

| Метод | Путь | Параметры | Доступ | Результат |
|---|---|---|---|---|
| GET | `/admin` | — | `SUPERADMIN` | `admin/dashboard.jsp` (`stats`, `organizations`) |
| POST | `/admin/grant-superadmin` | `userId` | `SUPERADMIN` | выдать `SUPERADMIN` → `302 /admin` |
| POST | `/admin/revoke-superadmin` | `userId` | `SUPERADMIN` | вернуть `USER` → `302 /admin` |

## Ошибки

| Метод | Путь | Назначение |
|---|---|---|
| GET/POST | `/error` | рендерит `error.jsp` по атрибутам контейнера (`status`, `message`) |

| Код | Когда |
|---|---|
| `401` / redirect `/login` | нет аутентификации |
| `403` | невалидный CSRF, доступ не по роли |
| `404` | не найдена организация/лицензия, невалидный UUID, неизвестный путь |
| `400` | ошибка валидации бизнес-правила |
| `405` | неподдерживаемый метод для маршрута |
| `500` | необработанная ошибка |
