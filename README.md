# Soft Pause
LibGDX로 만든 2D 플랫포머 게임입니다.
**Soft Pause**는 플레이어를 포함한 세상의 '수직 움직임'과 톱니만 멈추고, 좌우 이동은 그대로 할 수 있는 시간 정지 능력입니다.
공중에 멈춰 서서 걸어가거나, 점프패드의 힘을 저장했다가 원하는 위치에서 풀어 퍼즐을 풀어 나가세요.

- 총 6개 스테이지 (Stage 3부터 Soft Pause 해금)
- 주요 요소: Soft Pause / 점프패드 + 운동량 저장 / 움직이는 톱니 정지

## 조작법
| 키 | 동작 |
|---|---|
| ← → | 좌우 이동 |
| SPACE | 점프 |
| SHIFT | Soft Pause 켜기 / 끄기 (Stage 3부터) |
| ESC | 일시정지 / 재개 |
| ENTER | 게임 클리어 후 처음부터 다시 시작 |

## 실행 방법
- **jar**: Java 8 이상 설치 후 `java -jar Soft_Pause-1.0.0.jar`
- **Windows zip**: 압축 해제 후 `Soft_Pause.exe` 실행 (Java 설치 불필요)
- 실행 파일은 [Releases](../../releases)에서 받을 수 있습니다.

## 시연 영상
- YouTube: https://youtu.be/1a8a40XVMwI?si=ykNoIp1Sd87TResJ

## Credits
- Sprites: Kenney "Scribble Platformer" (CC0) – kenney.nl
  (player, jump pad and signs edited/created based on Kenney assets)
- Font: Gaegu by JIKJI SOFT (SIL Open Font License 1.1)
- Signs : board drawn with AI assistance, text added by author
- BGM: 「New Train」, 「Silhouette」by MFP【Marron Fields Production】 (DOVA-SYNDROME)
- SFX: Kenney "Digital Audio" & "Interface Sounds" (CC0) - kenney.nl

(커밋 작성자 sandeulbaram은 jsumin-jbnu의 개인 GitHub 계정입니다.)

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
